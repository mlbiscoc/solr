package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Metric;
import org.apache.solr.metrics.prometheus.jetty.SolrJettyDispatchesMetric;
import org.apache.solr.metrics.prometheus.jetty.SolrJettyMetric;
import org.apache.solr.metrics.prometheus.jetty.SolrJettyNoOpMetric;
import org.apache.solr.metrics.prometheus.jetty.SolrJettyReqRespMetric;

public class SolrPrometheusJettyExporter extends SolrPrometheusExporter {
  @Override
  public void exportDropwizardMetric(Metric dropwizardMetric, String metricName) {
    SolrJettyMetric solrJettyMetric;
    if (metricName.endsWith("xx-responses") || metricName.endsWith("-requests")) {
      solrJettyMetric = new SolrJettyReqRespMetric(dropwizardMetric, metricName);
    } else if (metricName.endsWith(".dispatches")) {
      solrJettyMetric = new SolrJettyDispatchesMetric(dropwizardMetric, metricName);
    } else {
      solrJettyMetric = new SolrJettyNoOpMetric(dropwizardMetric, metricName);
    }
    solrJettyMetric.parseLabels().toPrometheus(this);
  }
}
