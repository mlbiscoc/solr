package org.apache.solr.metrics.prometheus.jetty;

import com.codahale.metrics.Metric;
import com.codahale.metrics.Timer;
import org.apache.solr.metrics.prometheus.SolrMetric;
import org.apache.solr.metrics.prometheus.SolrPrometheusExporter;

public class SolrJettyDispatchesMetric extends SolrJettyMetric {
  public static final String JETTY_DISPATCHES_TOTAL = "solr_metrics_jetty_dispatches";

  public SolrJettyDispatchesMetric(Metric dropwizardMetric, String metricName) {
    super(dropwizardMetric, metricName);
  }

  @Override
  public SolrMetric parseLabels() {
    return this;
  }

  @Override
  public void toPrometheus(SolrPrometheusExporter exporter) {
    if (dropwizardMetric instanceof Timer) {
      exporter.exportTimerCount(JETTY_DISPATCHES_TOTAL, (Timer) dropwizardMetric, getLabels());
    }
  }
}
