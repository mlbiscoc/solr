package org.apache.solr.metrics.otel.instruments;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import org.apache.solr.metrics.otel.OtelDoubleMetric;

public class OtelDoubleHistogram implements OtelDoubleMetric {

  private final DoubleHistogram histogram;
  private final Attributes attributes;

  public OtelDoubleHistogram(DoubleHistogram histogram, Attributes attributes) {
    this.histogram = histogram;
    this.attributes = attributes;
  }

  @Override
  public void measure(Double value) {
    histogram.record(value, attributes);
  }
}
