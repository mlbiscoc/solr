package org.apache.solr.metrics.otel.instruments;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongHistogram;
import org.apache.solr.metrics.otel.OtelLongMetric;

public class OtelLongHistogram implements OtelLongMetric {

  protected final LongHistogram histogram;
  protected final Attributes attributes;

  public OtelLongHistogram(
      LongHistogram histogram, io.opentelemetry.api.common.Attributes attributes) {
    this.histogram = histogram;
    this.attributes = attributes;
  }

  @Override
  public void measure(Long value) {
    histogram.record(value, attributes);
  }
}
