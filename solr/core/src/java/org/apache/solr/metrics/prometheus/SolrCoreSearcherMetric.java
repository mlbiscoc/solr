package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;

import static org.apache.solr.metrics.prometheus.SolrCoreCacheMetric.CORE_CACHE_SEARCHER_METRICS;

public class SolrCoreSearcherMetric extends SolrCoreMetric {
    public static final String CORE_SEARCHER_METRICS = "solr_metrics_core_searcher";
    public static final String CORE_SEARCHER_CACHE_METRICS = "solr_metrics_core_searcher_cache";

    public SolrCoreSearcherMetric(Metric dropwizardMetric) {
        this.dropwizardMetric = dropwizardMetric;
    }

    @Override
    void toPrometheus(SolrPrometheusCoreRegistry solrPrometheusCoreRegistry, String metricName) {
        String coreName = solrPrometheusCoreRegistry.coreName;
        String[] splitString = metricName.split("\\.");
        if (dropwizardMetric instanceof Gauge) {
            if (metricName.endsWith("liveDocsCache")) {
                solrPrometheusCoreRegistry.exportGauge((Gauge<?>) dropwizardMetric, CORE_CACHE_SEARCHER_METRICS, coreName, splitString[2]);
            } else {
                solrPrometheusCoreRegistry.exportGauge((Gauge<?>) dropwizardMetric, CORE_SEARCHER_METRICS, coreName, splitString[2]);
            }
        }
    }
}
