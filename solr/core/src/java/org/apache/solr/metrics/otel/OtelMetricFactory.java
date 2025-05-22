package org.apache.solr.metrics.otel;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import org.apache.solr.metrics.otel.instruments.OtelDoubleCounter;
import org.apache.solr.metrics.otel.instruments.OtelDoubleHistogram;
import org.apache.solr.metrics.otel.instruments.OtelDoubleUpDownCounter;
import org.apache.solr.metrics.otel.instruments.OtelLongCounter;
import org.apache.solr.metrics.otel.instruments.OtelLongHistogram;
import org.apache.solr.metrics.otel.instruments.OtelLongUpDownCounter;

public class OtelMetricFactory {
  public static OtelDoubleCounter createDoubleCounter(
      DoubleCounter counter, Attributes attributes) {
    return new OtelDoubleCounter(counter, attributes);
  }

  public static OtelLongCounter createLongCounter(LongCounter counter, Attributes attributes) {
    return new OtelLongCounter(counter, attributes);
  }

  public static OtelDoubleUpDownCounter createDoubleUpDownCounter(
      DoubleUpDownCounter counter, Attributes attributes) {
    return new OtelDoubleUpDownCounter(counter, attributes);
  }

  public static OtelLongUpDownCounter createLongUpDownCounter(
      LongUpDownCounter counter, Attributes attributes) {
    return new OtelLongUpDownCounter(counter, attributes);
  }

  public static OtelDoubleHistogram createDoubleHistogram(
      DoubleHistogram histogram, Attributes attributes) {
    return new OtelDoubleHistogram(histogram, attributes);
  }

  public static OtelLongHistogram createLongHistogram(
      LongHistogram histogram, Attributes attributes) {
    return new OtelLongHistogram(histogram, attributes);
  }
}
