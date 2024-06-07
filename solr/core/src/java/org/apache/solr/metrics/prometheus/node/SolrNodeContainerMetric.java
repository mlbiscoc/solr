package org.apache.solr.metrics.prometheus.node;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import org.apache.solr.metrics.prometheus.SolrMetric;
import org.apache.solr.metrics.prometheus.exporters.SolrPrometheusExporter;

/* Dropwizard metrics of name CONTAINER.* */
public class SolrNodeContainerMetric extends SolrNodeMetric {
  public static final String NODE_CORES = "solr_metrics_node_cores";
  public static final String NODE_CORE_FS_BYTES = "solr_metrics_node_core_root_fs_bytes";

  public SolrNodeContainerMetric(Metric dropwizardMetric, String metricName) {
    super(dropwizardMetric, metricName);
  }

  @Override
  public SolrMetric parseLabels() {
    String[] parsedMetric = metricName.split("\\.");
    labels.put("category", parsedMetric[0]);
    return this;
  }

  @Override
  public void toPrometheus(SolrPrometheusExporter exporter) {
    String[] parsedMetric = metricName.split("\\.");
    if (dropwizardMetric instanceof Gauge) {
      if (metricName.startsWith("CONTAINER.cores.")) {
        labels.put("item", parsedMetric[2]);
        exporter.exportGauge(NODE_CORES, (Gauge<?>) dropwizardMetric, getLabels());
      } else if (metricName.startsWith("CONTAINER.fs.coreRoot.")) {
        labels.put("item", parsedMetric[3]);
        exporter.exportGauge(NODE_CORE_FS_BYTES, (Gauge<?>) dropwizardMetric, getLabels());
      }
    }
  }
}
