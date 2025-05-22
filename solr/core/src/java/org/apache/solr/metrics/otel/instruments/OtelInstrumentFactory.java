package org.apache.solr.metrics.otel.instruments;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;

public class OtelInstrumentFactory {

  public static OtelLongCounter getOtelLongCounter(LongCounter counter, Attributes attributes) {
    return new OtelLongCounter(counter, attributes);
  }
}
