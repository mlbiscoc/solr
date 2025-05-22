package org.apache.solr.metrics.otel;

@FunctionalInterface
public interface OtelLongMetric {
  void measure(Long value);
}
