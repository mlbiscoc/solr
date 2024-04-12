package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;

import static org.apache.solr.metrics.prometheus.SolrCoreCacheMetric.CORE_CACHE_SEARCHER_METRICS;

public class SolrCoreSearcherMetric extends SolrCoreMetric {
    public static final String CORE_SEARCHER_METRICS = "solr_metrics_core_searcher";

    public SolrCoreSearcherMetric(Metric dropwizardMetric, String coreName, String metricName) {
        this.dropwizardMetric = dropwizardMetric;
        this.coreName = coreName;
        this.metricName = metricName;
    }

    @Override
    void toPrometheus(SolrPrometheusCoreRegistry solrPrometheusCoreRegistry) {
        String[] parsedMetric = metricName.split("\\.");
        String type;
        if (dropwizardMetric instanceof Gauge) {
            type = parsedMetric[2];
            if (metricName.endsWith("liveDocsCache")) {
                solrPrometheusCoreRegistry.exportGauge((Gauge<?>) dropwizardMetric, CORE_CACHE_SEARCHER_METRICS, coreName, type);
            } else {
                solrPrometheusCoreRegistry.exportGauge((Gauge<?>) dropwizardMetric, CORE_SEARCHER_METRICS, coreName, type);
            }
        } else {
            System.out.println("This metric does not exist");
        }
    }
}
