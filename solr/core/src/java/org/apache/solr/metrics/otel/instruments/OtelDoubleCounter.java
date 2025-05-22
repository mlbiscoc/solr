package org.apache.solr.metrics.otel.instruments;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleCounter;
import org.apache.solr.metrics.otel.OtelDoubleMetric;

public class OtelDoubleCounter implements OtelDoubleMetric {

  private final DoubleCounter counter;
  private final Attributes attributes;

  public OtelDoubleCounter(DoubleCounter counter, Attributes attributes) {
    this.counter = counter;
    this.attributes = attributes;
  }

  public void inc() {
    measure(1.0);
  }

  @Override
  public void measure(Double value) {
    counter.add(value, attributes);
  }
}
