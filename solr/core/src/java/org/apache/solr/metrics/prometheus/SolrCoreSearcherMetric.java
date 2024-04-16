package org.apache.solr.metrics.prometheus;

import static org.apache.solr.metrics.prometheus.SolrCoreCacheMetric.CORE_CACHE_SEARCHER_METRICS;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.Timer;

public class SolrCoreSearcherMetric extends SolrCoreMetric {
  public static final String CORE_SEARCHER_METRICS = "solr_metrics_core_searcher_documents";
  public static final String CORE_SEARCHER_TIMES = "solr_metrics_core_average_searcher_warmup_time";

  public SolrCoreSearcherMetric(
      Metric dropwizardMetric, String coreName, String metricName, boolean cloudMode) {
    super(dropwizardMetric, coreName, metricName, cloudMode);
  }

  @Override
  public SolrCoreMetric parseLabels() {
    String[] parsedMetric = metricName.split("\\.");
    if (dropwizardMetric instanceof Gauge) {
      String type = parsedMetric[2];
      labels.put("type", type);
    }
    return this;
  }

  @Override
  void toPrometheus(SolrPrometheusCoreRegistry solrPrometheusCoreRegistry) {
    if (dropwizardMetric instanceof Gauge) {
      if (metricName.endsWith("liveDocsCache")) {
        solrPrometheusCoreRegistry.exportGauge(
            (Gauge<?>) dropwizardMetric, CORE_CACHE_SEARCHER_METRICS, labels);
      } else {
        solrPrometheusCoreRegistry.exportGauge(
            (Gauge<?>) dropwizardMetric, CORE_SEARCHER_METRICS, labels);
      }
    } else if (dropwizardMetric instanceof Timer) {
      solrPrometheusCoreRegistry.exportTimer((Timer) dropwizardMetric, CORE_SEARCHER_TIMES, labels);
    }
  }
}
