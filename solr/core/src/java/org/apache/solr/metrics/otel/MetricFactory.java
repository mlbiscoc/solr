package org.apache.solr.metrics.otel;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import org.apache.solr.metrics.otel.instruments.BoundDoubleCounter;
import org.apache.solr.metrics.otel.instruments.BoundDoubleHistogram;
import org.apache.solr.metrics.otel.instruments.BoundDoubleUpDownCounter;
import org.apache.solr.metrics.otel.instruments.BoundLongCounter;
import org.apache.solr.metrics.otel.instruments.BoundLongHistogram;
import org.apache.solr.metrics.otel.instruments.BoundLongTimer;
import org.apache.solr.metrics.otel.instruments.BoundLongUpDownCounter;

public class MetricFactory {
  public static BoundDoubleCounter createDoubleCounter(
      DoubleCounter counter, Attributes attributes) {
    return new BoundDoubleCounter(counter, attributes);
  }

  public static BoundLongCounter createBoundLongCounter(
      LongCounter counter, Attributes attributes) {
    return new BoundLongCounter(counter, attributes);
  }

  public static BoundDoubleUpDownCounter createBoundDoubleUpDownCounter(
      DoubleUpDownCounter counter, Attributes attributes) {
    return new BoundDoubleUpDownCounter(counter, attributes);
  }

  public static BoundLongUpDownCounter createBoundLongUpDownCounter(
      LongUpDownCounter counter, Attributes attributes) {
    return new BoundLongUpDownCounter(counter, attributes);
  }

  public static BoundDoubleHistogram createBoundDoubleHistogram(
      DoubleHistogram histogram, Attributes attributes) {
    return new BoundDoubleHistogram(histogram, attributes);
  }

  public static BoundLongHistogram createBoundLongHistogram(
      LongHistogram histogram, Attributes attributes) {
    return new BoundLongHistogram(histogram, attributes);
  }

  public static BoundLongTimer createBoundLongTimerHistogram(
      LongHistogram histogram, Attributes attributes) {
    return new BoundLongTimer(histogram, attributes);
  }
}
