package org.apache.solr.metrics.otel;

import static java.util.stream.Collectors.toList;

import io.opentelemetry.exporter.prometheus.PrometheusMetricReader;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
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
        (includedNames.isEmpty()) ? collect() : collect(includedNames::contains);

    if (includedLabels.isEmpty()) {
      return snapshotsToFilter;
    }

    // Build a new MetricSnapshot based on label filters
    MetricSnapshots.Builder filteredSnapshots = MetricSnapshots.builder();
    snapshotsToFilter.forEach(
        metricSnapshot -> {
          switch (metricSnapshot) {
            case CounterSnapshot counter -> {
              List<CounterSnapshot.CounterDataPointSnapshot> filtered =
                  filterCounterDatapoint(counter, includedLabels);
              if (!filtered.isEmpty()) {
                filteredSnapshots.metricSnapshot(
                    new CounterSnapshot(counter.getMetadata(), filtered));
              }
            }
            case HistogramSnapshot histogram -> {
              List<HistogramSnapshot.HistogramDataPointSnapshot> filtered =
                  filterHistogramDatapoint(histogram, includedLabels);
              if (!filtered.isEmpty()) {
                filteredSnapshots.metricSnapshot(
                    new HistogramSnapshot(histogram.getMetadata(), filtered));
              }
            }
            case GaugeSnapshot gauge -> {
              List<GaugeSnapshot.GaugeDataPointSnapshot> filtered =
                  filterGaugeDatapoint(gauge, includedLabels);
              if (!filtered.isEmpty()) {
                filteredSnapshots.metricSnapshot(new GaugeSnapshot(gauge.getMetadata(), filtered));
              }
            }
            default -> throw new IllegalStateException(
                "Unknown type filtering prometheus metric labels: " + metricSnapshot.getClass());
          }
        });
    return filteredSnapshots.build();
  }

  private List<CounterSnapshot.CounterDataPointSnapshot> filterCounterDatapoint(
      CounterSnapshot cs, Map<String, Set<String>> includedLabels) {
    return cs.getDataPoints().stream()
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
  }

  private List<HistogramSnapshot.HistogramDataPointSnapshot> filterHistogramDatapoint(
      HistogramSnapshot hs, Map<String, Set<String>> includedLabels) {
    return hs.getDataPoints().stream()
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
  }

  private List<GaugeSnapshot.GaugeDataPointSnapshot> filterGaugeDatapoint(
      GaugeSnapshot gs, Map<String, Set<String>> includedLabels) {
    return gs.getDataPoints().stream()
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
  }

  @Override
  public MetricSnapshots collect() {
    return super.collect();
  }
}
