package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;

public class SolrCoreIndexMetric extends SolrCoreMetric {
    public static final String CORE_INDEX_METRICS = "solr_metrics_index";

    public SolrCoreIndexMetric(Metric dropwizardMetric) {
        this.dropwizardMetric = dropwizardMetric;
    }

    @Override
    void toPrometheus(SolrPrometheusCoreRegistry solrPrometheusCoreRegistry, String metricName) {
        String coreName = solrPrometheusCoreRegistry.coreName;
        String[] splitString = metricName.split("\\.");
        if (dropwizardMetric instanceof Gauge) {
            solrPrometheusCoreRegistry.exportGauge((Gauge<?>) dropwizardMetric, CORE_INDEX_METRICS, coreName, splitString[1]);
        }
    }
}
