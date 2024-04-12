package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Metric;

public class SolrCoreHighlighterMetric extends SolrCoreMetric {
    public static final String CORE_HIGHLIGHER_METRICS = "solr_metrics_core_highlighter_requests";

    public SolrCoreHighlighterMetric(Metric dropwizardMetric) {
        this.dropwizardMetric = dropwizardMetric;
    }

    @Override
    void toPrometheus(SolrPrometheusCoreRegistry solrPrometheusCoreRegistry, String metricName) {
        String coreName = solrPrometheusCoreRegistry.coreName;
        String[] splitString = metricName.split("\\.");
        if (dropwizardMetric instanceof Counter) {
            solrPrometheusCoreRegistry.exportCounter((Counter) dropwizardMetric, CORE_HIGHLIGHER_METRICS, coreName, splitString[1], splitString[2]);
        }
    }
}
