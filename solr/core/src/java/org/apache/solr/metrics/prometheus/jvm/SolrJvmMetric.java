package org.apache.solr.metrics.prometheus.jvm;

import com.codahale.metrics.Metric;
import org.apache.solr.metrics.prometheus.SolrMetric;

public abstract class SolrJvmMetric extends SolrMetric {
  public SolrJvmMetric(Metric dropwizardMetric, String metricName) {
    super(dropwizardMetric, metricName);
  }
}
