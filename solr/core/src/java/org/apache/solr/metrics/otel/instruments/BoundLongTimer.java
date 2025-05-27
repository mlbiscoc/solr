package org.apache.solr.metrics.otel.instruments;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongHistogram;
import java.util.concurrent.TimeUnit;

public class BoundLongTimer extends BoundLongHistogram {

  // TODO is this right with ThreadLocal?
  // Use ThreadLocal so start()/stop() pairs are thread-safe
  private final ThreadLocal<TimingContext> current = new ThreadLocal<>();

  public BoundLongTimer(LongHistogram histogram, Attributes attributes) {
    super(histogram, attributes);
  }

  public void start() {
    if (current.get() != null) {
      throw new IllegalStateException("Timer already started on this thread");
    }
    current.set(new TimingContext(histogram, attributes));
  }

  /** Must have called start() first. */
  public void stop() {
    TimingContext ctx = current.get();
    if (ctx == null) {
      throw new IllegalStateException("Must call start() before stop()");
    }
    try {
      ctx.close(); // record the metric
    } finally {
      current.remove();
    }
  }

  /** AutoCloseable API: use in try-with-resources. */
  public TimingContext time() {
    return new TimingContext(histogram, attributes);
  }

  public static class TimingContext implements AutoCloseable {
    private final LongHistogram hist;
    private final Attributes attrs;
    private final long startNanos;

    private TimingContext(LongHistogram hist, Attributes attrs) {
      this.hist = hist;
      this.attrs = attrs;
      this.startNanos = System.nanoTime();
    }

    /** record elapsed *milliseconds* when the try-block exits */
    @Override
    public void close() {
      long elapsedNanos = System.nanoTime() - startNanos;
      long elapsedMs = TimeUnit.NANOSECONDS.toMillis(elapsedNanos);
      hist.record(elapsedMs, attrs);
    }
  }
}
