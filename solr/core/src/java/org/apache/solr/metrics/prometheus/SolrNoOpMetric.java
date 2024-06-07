package org.apache.solr.metrics.prometheus;

import org.apache.solr.metrics.prometheus.exporters.SolrPrometheusExporter;

public class SolrNoOpMetric extends SolrMetric {
  public SolrNoOpMetric() {}

  @Override
  public SolrMetric parseLabels() {
    return this;
  }

  @Override
  public void toPrometheus(SolrPrometheusExporter exporter) {}
}
