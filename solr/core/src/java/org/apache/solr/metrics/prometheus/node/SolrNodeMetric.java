package org.apache.solr.metrics.prometheus.node;

import com.codahale.metrics.Metric;
import org.apache.solr.metrics.prometheus.SolrMetric;

/** Base class is a wrapper to export a solr.node {@link com.codahale.metrics.Metric} */
public abstract class SolrNodeMetric extends SolrMetric {
  public static final String NODE_THREAD_POOL = "solr_metrics_node_thread_pool";

  public SolrNodeMetric(Metric dropwizardMetric, String metricName) {
    super(dropwizardMetric, metricName);
  }
}
