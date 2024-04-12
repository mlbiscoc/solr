package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Metric;

public class SolrCoreHighlighterMetric extends SolrCoreMetric {
    public static final String CORE_HIGHLIGHER_METRICS = "solr_metrics_core_highlighter_requests";

    public SolrCoreHighlighterMetric(Metric dropwizardMetric, String coreName, String metricName) {
        this.dropwizardMetric = dropwizardMetric;
        this.coreName = coreName;
        this.metricName = metricName;
    }

    @Override
    void toPrometheus(SolrPrometheusCoreRegistry solrPrometheusCoreRegistry) {
        String[] parsedMetric = metricName.split("\\.");
        if (dropwizardMetric instanceof Counter) {
            String type = parsedMetric[1];
            String item = parsedMetric[2];
            solrPrometheusCoreRegistry.exportCounter((Counter) dropwizardMetric, CORE_HIGHLIGHER_METRICS, coreName, type, item);
        }
    }
}
