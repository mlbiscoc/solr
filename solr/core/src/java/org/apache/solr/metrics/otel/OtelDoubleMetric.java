package org.apache.solr.metrics.otel;

@FunctionalInterface
public interface OtelDoubleMetric {
  void measure(Double value);
}
