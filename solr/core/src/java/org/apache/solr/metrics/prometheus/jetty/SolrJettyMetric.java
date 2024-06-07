package org.apache.solr.metrics.prometheus.jetty;

import com.codahale.metrics.Metric;
import org.apache.solr.metrics.prometheus.SolrMetric;

/** Base class is a wrapper to export a solr.jetty {@link com.codahale.metrics.Metric} */
public abstract class SolrJettyMetric extends SolrMetric {
  public SolrJettyMetric(Metric dropwizardMetric, String metricName) {
    super(dropwizardMetric, metricName);
  }
}
