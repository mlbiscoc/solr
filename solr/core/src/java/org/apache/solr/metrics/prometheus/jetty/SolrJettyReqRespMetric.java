package org.apache.solr.metrics.prometheus.jetty;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.Timer;
import org.apache.solr.metrics.prometheus.SolrMetric;
import org.apache.solr.metrics.prometheus.exporters.SolrPrometheusExporter;

/* Dropwizard metrics of name requests and responses */
public class SolrJettyReqRespMetric extends SolrJettyMetric {
  public static final String JETTY_RESPONSES_TOTAL = "solr_metrics_jetty_response";
  public static final String JETTY_REQUESTS_TOTAL = "solr_metrics_jetty_requests";

  public SolrJettyReqRespMetric(Metric dropwizardMetric, String metricName) {
    super(dropwizardMetric, metricName);
  }

  @Override
  public SolrMetric parseLabels() {
    String[] parsedMetric = metricName.split("\\.");
    String label = parsedMetric[parsedMetric.length - 1].split("-")[0];
    if (metricName.endsWith("xx-responses")) {
      labels.put("status", label);
    } else {
      labels.put("method", label);
    }
    return this;
  }

  @Override
  public void toPrometheus(SolrPrometheusExporter exporter) {
    if (metricName.endsWith("xx-responses")) {
      exporter.exportMeter(JETTY_RESPONSES_TOTAL, (Meter) dropwizardMetric, getLabels());
    } else if (metricName.endsWith("-requests")) {
      if (dropwizardMetric instanceof Counter) {
        exporter.exportCounter(JETTY_REQUESTS_TOTAL, (Counter) dropwizardMetric, getLabels());
      } else if (dropwizardMetric instanceof Timer) {
        exporter.exportTimerCount(JETTY_REQUESTS_TOTAL, (Timer) dropwizardMetric, getLabels());
      }
    }
  }
}
