package org.apache.solr.metrics.otel.instruments;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import org.apache.solr.metrics.otel.OtelLongMetric;

public class OtelLongCounter implements OtelLongMetric {

  private final LongCounter baseCounter;
  private final io.opentelemetry.api.common.Attributes attributes;

  public OtelLongCounter(LongCounter baseCounter, Attributes attributes) {
    this.baseCounter = baseCounter;
    this.attributes = attributes;
  }

  @Override
  public void measure(Long value) {
    baseCounter.add(value, attributes);
  }
}
