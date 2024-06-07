package org.apache.solr.metrics.prometheus.node;

import com.codahale.metrics.Metric;
import org.apache.solr.metrics.prometheus.SolrMetric;
import org.apache.solr.metrics.prometheus.SolrPrometheusExporter;

public class SolrNodeNoOpMetric extends SolrNodeMetric {

    public SolrNodeNoOpMetric(Metric dropwizardMetric, String metricName) {
        super(dropwizardMetric, metricName);
    }
    @Override
    public SolrMetric parseLabels() {
        return this;
    }

    @Override
    public void toPrometheus(SolrPrometheusExporter exporter) {

    }
}
