package org.apache.solr.metrics.otel.instruments;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import org.apache.solr.metrics.otel.OtelDoubleMetric;

public class OtelDoubleUpDownCounter implements OtelDoubleMetric {

  private final DoubleUpDownCounter upDownCounter;
  private final Attributes attributes;

  public OtelDoubleUpDownCounter(DoubleUpDownCounter upDownCounter, Attributes attributes) {
    this.upDownCounter = upDownCounter;
    this.attributes = attributes;
  }

  @Override
  public void measure(Double value) {
    upDownCounter.add(value, attributes);
  }
}
