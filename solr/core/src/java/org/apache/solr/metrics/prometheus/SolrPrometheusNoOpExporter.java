package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Metric;

public class SolrPrometheusNoOpExporter extends SolrPrometheusExporter {

  public SolrPrometheusNoOpExporter() {}

  @Override
  public void exportDropwizardMetric(Metric dropwizardMetric, String metricName) {}
}
