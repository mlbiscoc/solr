package org.apache.solr.metrics.otel.instruments;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongGauge;
import org.apache.solr.metrics.otel.OtelLongMetric;

public class OtelLongGauge implements OtelLongMetric {

  private final LongGauge gauge;
  private final Attributes attributes;

  public OtelLongGauge(LongGauge gauge, Attributes attributes) {
    this.gauge = gauge;
    this.attributes = attributes;
  }

  @Override
  public void measure(Long value) {
    gauge.set(value, attributes);
  }
}
