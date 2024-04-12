package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;

public class SolrCoreIndexMetric extends SolrCoreMetric {
    public static final String CORE_INDEX_METRICS = "solr_metrics_index";

    public SolrCoreIndexMetric(Metric dropwizardMetric, String coreName, String metricName) {
        this.dropwizardMetric = dropwizardMetric;
        this.coreName = coreName;
        this.metricName = metricName;
    }

    @Override
    void toPrometheus(SolrPrometheusCoreRegistry solrPrometheusCoreRegistry) {
        String[] parsedMetric = metricName.split("\\.");
        if (dropwizardMetric instanceof Gauge) {
            String type = parsedMetric[1];
            solrPrometheusCoreRegistry.exportGauge((Gauge<?>) dropwizardMetric, CORE_INDEX_METRICS, coreName, type);
        }
    }
}
