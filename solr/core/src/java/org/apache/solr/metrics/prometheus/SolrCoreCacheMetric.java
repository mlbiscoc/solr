package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.Timer;

public class SolrCoreCacheMetric extends SolrCoreMetric {
  public static final String CORE_CACHE_SEARCHER_METRICS = "solr_metrics_core_cache";

  public SolrCoreCacheMetric(
      Metric dropwizardMetric, String coreName, String metricName, boolean cloudMode) {
    super(dropwizardMetric, coreName, metricName, cloudMode);
  }

  @Override
  public SolrCoreMetric parseLabels() {
    String[] parsedMetric = metricName.split("\\.");
    if (dropwizardMetric instanceof Gauge) {
      String cacheType = parsedMetric[2];
      labels.put("cacheType", cacheType);
    }
    return this;
  }

  @Override
  void toPrometheus(SolrPrometheusCoreRegistry solrPrometheusCoreRegistry) {
    if (dropwizardMetric instanceof Gauge) {
      solrPrometheusCoreRegistry.exportGauge(
          (Gauge<?>) dropwizardMetric, CORE_CACHE_SEARCHER_METRICS, labels);
    }
  }
}
