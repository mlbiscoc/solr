package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Metric;
import io.prometheus.metrics.model.snapshots.Labels;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.solr.metrics.prometheus.exporters.SolrPrometheusExporter;

/**
 * Base class is a wrapper to categorize and export {@link com.codahale.metrics.Metric} to {@link
 * io.prometheus.metrics.model.snapshots.DataPointSnapshot} and register to a {@link
 * SolrPrometheusExporter}. {@link com.codahale.metrics.MetricRegistry} does not support tags unlike
 * prometheus. Metrics registered to the registry need to be parsed out from the metric name to be
 * exported to {@link io.prometheus.metrics.model.snapshots.DataPointSnapshot}
 */
public abstract class SolrMetric {
  public Metric dropwizardMetric;
  public String metricName;
  public Map<String, String> labels = new HashMap<>();

  public SolrMetric() {}

  public SolrMetric(Metric dropwizardMetric, String metricName) {
    this.dropwizardMetric = dropwizardMetric;
    this.metricName = metricName;
  }

  public abstract SolrMetric parseLabels();

  public abstract void toPrometheus(SolrPrometheusExporter exporter);

  public Labels getLabels() {
    return Labels.of(new ArrayList<>(labels.keySet()), new ArrayList<>(labels.values()));
  }
}
