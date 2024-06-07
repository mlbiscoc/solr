package org.apache.solr.metrics.prometheus.exporters;

import com.codahale.metrics.Metric;
import org.apache.solr.metrics.prometheus.SolrMetric;
import org.apache.solr.metrics.prometheus.SolrNoOpMetric;
import org.apache.solr.metrics.prometheus.jvm.SolrJvmBuffersMetric;
import org.apache.solr.metrics.prometheus.jvm.SolrJvmMemoryMetric;
import org.apache.solr.metrics.prometheus.jvm.SolrJvmOsMetric;

/**
 * This class maintains a {@link io.prometheus.metrics.model.snapshots.MetricSnapshot}s exported
 * from solr.jvm {@link com.codahale.metrics.MetricRegistry}
 */
public class SolrPrometheusJvmExporter extends SolrPrometheusExporter
    implements PrometheusJvmExporterInfo {
  public SolrPrometheusJvmExporter() {
    super();
  }

  @Override
  public void exportDropwizardMetric(Metric dropwizardMetric, String metricName) {
    SolrMetric solrJvmMetric = categorizeMetric(dropwizardMetric, metricName);
    solrJvmMetric.parseLabels().toPrometheus(this);
  }

  @Override
  public SolrMetric categorizeMetric(Metric dropwizardMetric, String metricName) {
    String metricCategory = metricName.split("\\.", 2)[0];
    switch (JvmCategory.valueOf(metricCategory)) {
      case gc:
      case memory:
        return new SolrJvmMemoryMetric(dropwizardMetric, metricName);
      case os:
      case threads:
        return new SolrJvmOsMetric(dropwizardMetric, metricName);
      case buffers:
        return new SolrJvmBuffersMetric(dropwizardMetric, metricName);
      default:
        return new SolrNoOpMetric();
    }
  }
}
