package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Metric;

import java.util.Map;

public class SolrCoreHighlighterMetric extends SolrCoreMetric {
    public static final String CORE_HIGHLIGHER_METRICS = "solr_metrics_core_highlighter_requests";

    public SolrCoreHighlighterMetric(Metric dropwizardMetric, String coreName, String metricName) {
        this.dropwizardMetric = dropwizardMetric;
        this.coreName = coreName;
        this.metricName = metricName;
    }

    @Override
    public SolrCoreMetric parseLabels() {
        String[] parsedMetric = metricName.split("\\.");
        String type = parsedMetric[1];
        String item = parsedMetric[2];
        labels = Map.of(
                "core", coreName,
                "type", type,
                "item", item);
        return this;
    }

    @Override
    void toPrometheus(SolrPrometheusCoreRegistry solrPrometheusCoreRegistry) {
        if (dropwizardMetric instanceof Counter) {
            solrPrometheusCoreRegistry.exportCounter((Counter) dropwizardMetric, CORE_HIGHLIGHER_METRICS, labels);
        }
    }
}
