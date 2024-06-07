package org.apache.solr.metrics.prometheus.jvm;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import org.apache.solr.metrics.prometheus.exporters.SolrPrometheusExporter;

/* Dropwizard metrics of name buffers.* */
public class SolrJvmBuffersMetric extends SolrJvmMetric {
  public static final String JVM_BUFFERS = "solr_metrics_jvm_buffers";
  public static final String JVM_BUFFERS_BYTES = "solr_metrics_jvm_buffers_bytes";

  public SolrJvmBuffersMetric(Metric dropwizardMetric, String metricName) {
    super(dropwizardMetric, metricName);
  }

  @Override
  public SolrJvmMetric parseLabels() {
    String[] parsedMetric = metricName.split("\\.");
    labels.put("pool", parsedMetric[1]);
    labels.put("item", parsedMetric[2]);
    return this;
  }

  @Override
  public void toPrometheus(SolrPrometheusExporter exporter) {
    String[] parsedMetric = metricName.split("\\.");
    if (dropwizardMetric instanceof Gauge) {
      if (metricName.endsWith(".Count")) {
        exporter.exportGauge(JVM_BUFFERS, (Gauge<?>) dropwizardMetric, getLabels());
      } else if (metricName.endsWith(".MemoryUsed") || metricName.endsWith(".TotalCapacity")) {
        exporter.exportGauge(JVM_BUFFERS_BYTES, (Gauge<?>) dropwizardMetric, getLabels());
      }
    }
  }
}
