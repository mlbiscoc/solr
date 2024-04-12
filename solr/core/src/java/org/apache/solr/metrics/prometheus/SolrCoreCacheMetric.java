package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;

public class SolrCoreCacheMetric extends SolrCoreMetric {
    public static final String CORE_CACHE_SEARCHER_METRICS = "solr_metrics_core_cache_gauge";

    public SolrCoreCacheMetric(Metric dropwizardMetric, String coreName, String metricName) {
        this.dropwizardMetric = dropwizardMetric;
        this.coreName = coreName;
        this.metricName = metricName;
    }
    @Override
    void toPrometheus(SolrPrometheusCoreRegistry solrPrometheusCoreRegistry)  {
        String[] parseMetric = metricName.split("\\.");

        if (dropwizardMetric instanceof Gauge) {
            String cacheType = parseMetric[2];
            solrPrometheusCoreRegistry.exportGauge((Gauge<?>) dropwizardMetric, CORE_CACHE_SEARCHER_METRICS, coreName, cacheType);
        } else {
            System.out.println("This Metric does not exist");
        }
    }
}
