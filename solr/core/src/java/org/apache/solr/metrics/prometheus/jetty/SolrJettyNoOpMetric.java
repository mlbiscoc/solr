package org.apache.solr.metrics.prometheus.jetty;

import com.codahale.metrics.Metric;
import org.apache.solr.metrics.prometheus.SolrMetric;
import org.apache.solr.metrics.prometheus.SolrPrometheusExporter;

public class SolrJettyNoOpMetric extends SolrJettyMetric {
  public SolrJettyNoOpMetric(Metric dropwizardMetric, String metricName) {
    super(dropwizardMetric, metricName);
  }

  @Override
  public SolrMetric parseLabels() {
    return this;
  }

  @Override
  public void toPrometheus(SolrPrometheusExporter exporter) {}
}
