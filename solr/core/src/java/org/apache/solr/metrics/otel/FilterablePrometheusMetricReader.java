package org.apache.solr.metrics.otel;

import static java.util.stream.Collectors.toList;

import io.opentelemetry.exporter.prometheus.PrometheusMetricReader;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class FilterablePrometheusMetricReader extends PrometheusMetricReader {

  public FilterablePrometheusMetricReader(
      boolean otelScopeEnabled, Predicate<String> allowedResourceAttributesFilter) {
    super(otelScopeEnabled, allowedResourceAttributesFilter);
  }

  public MetricSnapshots collect(
      Set<String> includedNames, Map<String, Set<String>> includedLabels) {

    MetricSnapshots snapshotsToFilter =
        (includedNames.isEmpty()) ? super.collect() : super.collect(includedNames::contains);

    if (includedLabels.isEmpty()) {
      return snapshotsToFilter;
    }

    MetricSnapshots.Builder filteredSnapshots = MetricSnapshots.builder();
    for (MetricSnapshot ms : snapshotsToFilter) {
      if (ms instanceof CounterSnapshot) {
        CounterSnapshot c = (CounterSnapshot) ms;
        List<CounterSnapshot.CounterDataPointSnapshot> kept =
            c.getDataPoints().stream()
                .filter(
                    dp ->
                        includedLabels.entrySet().stream()
                            .allMatch(
                                entry ->
                                    dp.getLabels().stream()
                                        .anyMatch(
                                            l ->
                                                l.getName().equals(entry.getKey())
                                                    && entry.getValue().contains(l.getValue()))))
                .collect(toList());
        if (!kept.isEmpty()) {
          filteredSnapshots.metricSnapshot(new CounterSnapshot(c.getMetadata(), kept));
        }
        continue;
      }
      if (ms instanceof HistogramSnapshot) {
        HistogramSnapshot h = (HistogramSnapshot) ms;
        List<HistogramSnapshot.HistogramDataPointSnapshot> kept =
            h.getDataPoints().stream()
                .filter(
                    dp ->
                        includedLabels.entrySet().stream()
                            .allMatch(
                                entry ->
                                    dp.getLabels().stream()
                                        .anyMatch(
                                            l ->
                                                l.getName().equals(entry.getKey())
                                                    && entry.getValue().contains(l.getValue()))))
                .collect(toList());
        if (!kept.isEmpty()) {
          filteredSnapshots.metricSnapshot(new HistogramSnapshot(h.getMetadata(), kept));
        }
        continue;
      }
      if (ms instanceof GaugeSnapshot) {
        GaugeSnapshot g = (GaugeSnapshot) ms;
        List<GaugeSnapshot.GaugeDataPointSnapshot> kept =
            g.getDataPoints().stream()
                .filter(
                    dp ->
                        includedLabels.entrySet().stream()
                            .allMatch(
                                entry ->
                                    dp.getLabels().stream()
                                        .anyMatch(
                                            l ->
                                                l.getName().equals(entry.getKey())
                                                    && entry.getValue().contains(l.getValue()))))
                .collect(toList());
        if (!kept.isEmpty()) {
          filteredSnapshots.metricSnapshot(new GaugeSnapshot(g.getMetadata(), kept));
        }
      }
    }
    return filteredSnapshots.build();
  }

  @Override
  public MetricSnapshots collect() {
    return super.collect();
  }
}
