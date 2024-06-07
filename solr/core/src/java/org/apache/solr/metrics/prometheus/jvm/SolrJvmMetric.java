package org.apache.solr.metrics.prometheus.jvm;

import com.codahale.metrics.Metric;
import org.apache.solr.metrics.prometheus.SolrMetric;

/** Base class is a wrapper to export a solr.jvm {@link com.codahale.metrics.Metric} */
public abstract class SolrJvmMetric extends SolrMetric {
  public SolrJvmMetric(Metric dropwizardMetric, String metricName) {
    super(dropwizardMetric, metricName);
  }
}
