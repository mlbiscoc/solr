/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.solr.cloud;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.common.cloud.ZkNodeProps;
import org.apache.solr.util.RTimer;

/**
 * Used to hold statistics about some SolrCloud operations.
 *
 * <p>This is experimental API and subject to change.
 */
public class Stats {
  static final int MAX_STORED_FAILURES = 10;

  final Map<String, Stat> stats = new ConcurrentHashMap<>();
  private volatile int queueLength;

  public Map<String, Stat> getStats() {
    return stats;
  }

  public int getSuccessCount(String operation) {
    Stat stat = stats.get(operation.toLowerCase(Locale.ROOT));
    return stat == null ? 0 : stat.success.get();
  }

  public int getErrorCount(String operation) {
    Stat stat = stats.get(operation.toLowerCase(Locale.ROOT));
    return stat == null ? 0 : stat.errors.get();
  }

  public void success(String operation) {
    String op = operation.toLowerCase(Locale.ROOT);
    Stat stat = stats.get(op);
    if (stat == null) {
      stat = new Stat();
      stats.put(op, stat);
    }
    stat.success.incrementAndGet();
  }

  public void error(String operation) {
    String op = operation.toLowerCase(Locale.ROOT);
    Stat stat = stats.get(op);
    if (stat == null) {
      stat = new Stat();
      stats.put(op, stat);
    }
    stat.errors.incrementAndGet();
  }

  public TimingContext time(String operation) {
    String op = operation.toLowerCase(Locale.ROOT);
    Stat stat = stats.get(op);
    if (stat == null) {
      stat = new Stat();
      stats.put(op, stat);
    }
    return new TimingContext(stat.requestTime);
  }

  public void storeFailureDetails(String operation, ZkNodeProps request, SolrResponse resp) {
    String op = operation.toLowerCase(Locale.ROOT);
    Stat stat = stats.get(op);
    if (stat == null) {
      stat = new Stat();
      stats.put(op, stat);
    }
    ArrayDeque<FailedOp> failedOps = stat.failureDetails;
    synchronized (failedOps) {
      if (failedOps.size() >= MAX_STORED_FAILURES) {
        failedOps.removeFirst();
      }
      failedOps.addLast(new FailedOp(request, resp));
    }
  }

  public List<FailedOp> getFailureDetails(String operation) {
    Stat stat = stats.get(operation.toLowerCase(Locale.ROOT));
    if (stat == null || stat.failureDetails.isEmpty()) return null;
    ArrayDeque<FailedOp> failedOps = stat.failureDetails;
    synchronized (failedOps) {
      ArrayList<FailedOp> ret = new ArrayList<>(failedOps);
      return ret;
    }
  }

  public int getQueueLength() {
    return queueLength;
  }

  public void setQueueLength(int queueLength) {
    this.queueLength = queueLength;
  }

  public void clear() {
    stats.clear();
  }

  /**
   * Accumulates timing statistics similar to Dropwizard Timer but using simple statistics. This
   * provides a bridge between the old Dropwizard Timer API and OpenTelemetry.
   *
   * <p>Note: This is a simplified implementation that provides basic statistics. For more advanced
   * features like percentiles and rates, consider using proper OpenTelemetry Histogram metrics in
   * the future.
   */
  public static class TimingStats {
    private final AtomicInteger count = new AtomicInteger();
    private volatile double sum = 0.0;
    private volatile double min = Double.MAX_VALUE;
    private volatile double max = Double.MIN_VALUE;
    private volatile long startTime = System.nanoTime();
    private final Object lock = new Object();

    public void update(double elapsed) {
      count.incrementAndGet();
      synchronized (lock) {
        sum += elapsed;
        if (elapsed < min) min = elapsed;
        if (elapsed > max) max = elapsed;
      }
    }

    public int getCount() {
      return count.get();
    }

    public double getMean() {
      int currentCount = count.get();
      return currentCount > 0 ? sum / currentCount : 0.0;
    }

    public double getMin() {
      return min == Double.MAX_VALUE ? 0.0 : min;
    }

    public double getMax() {
      return max == Double.MIN_VALUE ? 0.0 : max;
    }

    public double getSum() {
      return sum;
    }

    // Basic compatibility methods for Dropwizard Timer API
    // Note: These are simplified implementations

    /**
     * Returns a simplified mean rate calculation. This is not as sophisticated as Dropwizard's
     * exponentially-weighted moving average.
     */
    public double getMeanRate() {
      long elapsedNanos = System.nanoTime() - startTime;
      if (elapsedNanos == 0) return 0.0;
      return count.get() * 1_000_000_000.0 / elapsedNanos; // requests per second
    }

    /**
     * Placeholder for 5-minute rate - simplified implementation. In a production system, this
     * should use proper exponentially-weighted moving averages.
     */
    public double getFiveMinuteRate() {
      return getMeanRate(); // Simplified - just return mean rate
    }

    /**
     * Placeholder for 15-minute rate - simplified implementation. In a production system, this
     * should use proper exponentially-weighted moving averages.
     */
    public double getFifteenMinuteRate() {
      return getMeanRate(); // Simplified - just return mean rate
    }

    /** Simple snapshot-like access to statistics. Returns the mean as a basic "snapshot" metric. */
    public double getSnapshot() {
      return getMean();
    }
  }

  /**
   * A timing context that can be used to measure elapsed time for operations. Implements
   * AutoCloseable for use with try-with-resources pattern.
   *
   * <p>Records measurements to the associated TimingStats for accumulative statistics.
   */
  public static class TimingContext implements AutoCloseable {
    private final RTimer timer;
    private final TimingStats stats;

    public TimingContext(TimingStats stats) {
      this.timer = new RTimer(TimeUnit.MILLISECONDS);
      this.stats = stats;
    }

    /**
     * Stops the timer and returns the elapsed time in milliseconds. Also records the measurement to
     * the associated TimingStats.
     *
     * @return elapsed time in milliseconds
     */
    public double stop() {
      double elapsed = timer.stop();
      if (stats != null) {
        stats.update(elapsed);
      }
      return elapsed;
    }

    /** Stops the timer when used in try-with-resources. */
    @Override
    public void close() {
      stop();
    }
  }

  public static class Stat {
    public final AtomicInteger success;
    public final AtomicInteger errors;
    public final TimingStats requestTime;
    public final ArrayDeque<FailedOp> failureDetails;

    public Stat() {
      this.success = new AtomicInteger();
      this.errors = new AtomicInteger();
      this.requestTime = new TimingStats();
      this.failureDetails = new ArrayDeque<>();
    }
  }

  public static class FailedOp {
    public final ZkNodeProps req;
    public final SolrResponse resp;

    public FailedOp(ZkNodeProps req, SolrResponse resp) {
      this.req = req;
      this.resp = resp;
    }
  }
}
