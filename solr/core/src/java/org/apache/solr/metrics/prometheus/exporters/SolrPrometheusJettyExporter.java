package org.apache.solr.metrics.prometheus.exporters;

import com.codahale.metrics.Metric;
import org.apache.solr.metrics.prometheus.SolrMetric;
import org.apache.solr.metrics.prometheus.SolrNoOpMetric;
import org.apache.solr.metrics.prometheus.jetty.SolrJettyDispatchesMetric;
import org.apache.solr.metrics.prometheus.jetty.SolrJettyReqRespMetric;

/**
 * This class maintains a {@link io.prometheus.metrics.model.snapshots.MetricSnapshot}s exported
 * from solr.jetty {@link com.codahale.metrics.MetricRegistry}
 */
public class SolrPrometheusJettyExporter extends SolrPrometheusExporter {
  public SolrPrometheusJettyExporter() {
    super();
  }

  @Override
  public void exportDropwizardMetric(Metric dropwizardMetric, String metricName) {
    SolrMetric solrJettyMetric = categorizeMetric(dropwizardMetric, metricName);
    solrJettyMetric.parseLabels().toPrometheus(this);
  }

  @Override
  public SolrMetric categorizeMetric(Metric dropwizardMetric, String metricName) {
    if (metricName.endsWith("xx-responses") || metricName.endsWith("-requests")) {
      return new SolrJettyReqRespMetric(dropwizardMetric, metricName);
    } else if (metricName.endsWith(".dispatches")) {
      return new SolrJettyDispatchesMetric(dropwizardMetric, metricName);
    } else {
      return new SolrNoOpMetric();
    }
  }
}
