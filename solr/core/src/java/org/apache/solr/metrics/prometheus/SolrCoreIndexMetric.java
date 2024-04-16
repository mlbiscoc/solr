package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.Timer;

public class SolrCoreIndexMetric extends SolrCoreMetric {
  public static final String CORE_INDEX_METRICS = "solr_metrics_core_index_size_bytes";

  public SolrCoreIndexMetric(
      Metric dropwizardMetric, String coreName, String metricName, boolean cloudMode) {
    super(dropwizardMetric, coreName, metricName, cloudMode);
  }

  @Override
  public SolrCoreMetric parseLabels() {
    return this;
  }

  @Override
  void toPrometheus(SolrPrometheusCoreRegistry solrPrometheusCoreRegistry) {
    if (dropwizardMetric instanceof Gauge) {
      if (metricName.endsWith("sizeInBytes")) {
        solrPrometheusCoreRegistry.exportGauge(
                (Gauge<?>) dropwizardMetric, CORE_INDEX_METRICS, labels);
      }
    }
  }
}
