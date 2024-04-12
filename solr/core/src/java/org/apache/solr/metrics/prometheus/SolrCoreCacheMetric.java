package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;

import java.util.HashMap;

public class SolrCoreCacheMetric extends SolrCoreMetric {
    public static final String CORE_CACHE_SEARCHER_METRICS = "solr_metrics_core_cache_gauge";

    public SolrCoreCacheMetric(Metric dropwizardMetric) {
        this.dropwizardMetric = dropwizardMetric;
    }
    @Override
    void toPrometheus(SolrPrometheusCoreRegistry solrPrometheusCoreRegistry, String metricName)  {
        String coreName = solrPrometheusCoreRegistry.coreName;
        String[] splitString = metricName.split("\\.");
        if (dropwizardMetric instanceof Gauge) {
            solrPrometheusCoreRegistry.exportGauge((Gauge<?>) dropwizardMetric, CORE_CACHE_SEARCHER_METRICS, coreName, splitString[2]);
        }
    }
}
