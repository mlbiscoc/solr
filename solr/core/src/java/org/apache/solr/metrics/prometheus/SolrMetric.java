package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Metric;
import io.prometheus.metrics.model.snapshots.Labels;
import org.apache.solr.metrics.prometheus.core.SolrCoreMetric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class SolrMetric {
    public Metric dropwizardMetric;
    public String metricName;
    public Map<String, String> labels = new HashMap<>();

    public SolrMetric(
            Metric dropwizardMetric, String metricName) {
        this.dropwizardMetric = dropwizardMetric;
        this.metricName = metricName;
    }
    public abstract SolrMetric parseLabels();

    public abstract void toPrometheus(SolrPrometheusExporter exporter);

    public Labels getLabels() {
        return Labels.of(new ArrayList<>(labels.keySet()), new ArrayList<>(labels.values()));
    }
}
