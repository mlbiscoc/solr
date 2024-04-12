package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;

public class SolrCoreSearcherMetric extends SolrCoreMetric {
    public static final String CORE_SEARCHER_METRICS = "solr_metrics_core_searcher";

    public SolrCoreSearcherMetric(Metric dropwizardMetric) {
        this.dropwizardMetric = dropwizardMetric;
    }

    @Override
    void toPrometheus(SolrPrometheusCoreRegistry solrPrometheusCoreRegistry, String metricName) {
        String coreName = solrPrometheusCoreRegistry.coreName;
        String[] splitString = metricName.split("\\.");
        if (dropwizardMetric instanceof Gauge) {
            solrPrometheusCoreRegistry.exportGauge((Gauge<?>) dropwizardMetric, CORE_SEARCHER_METRICS, coreName, splitString[2]);
        }
    }
}
