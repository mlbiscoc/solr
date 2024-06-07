package org.apache.solr.metrics.prometheus.jetty;

import com.codahale.metrics.Metric;
import org.apache.solr.metrics.prometheus.SolrMetric;

public abstract class SolrJettyMetric extends SolrMetric {
  public SolrJettyMetric(Metric dropwizardMetric, String metricName) {
    super(dropwizardMetric, metricName);
  }
}
