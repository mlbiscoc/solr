package org.apache.solr.metrics.otel.instruments;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import org.apache.solr.metrics.otel.OtelLongMetric;

public class OtelLongUpDownCounter implements OtelLongMetric {

  private final LongUpDownCounter upDownCounter;
  private final Attributes attributes;

  public OtelLongUpDownCounter(LongUpDownCounter upDownCounter, Attributes attributes) {
    this.upDownCounter = upDownCounter;
    this.attributes = attributes;
  }

  public void inc() {
    measure(1L);
  }

  public void dec() {
    measure(-1L);
  }

  @Override
  public void measure(Long value) {
    upDownCounter.add(value, attributes);
  }
}
