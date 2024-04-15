package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;

import java.util.Map;

public class SolrCoreIndexMetric extends SolrCoreMetric {
    public static final String CORE_INDEX_METRICS = "solr_metrics_index";

    public SolrCoreIndexMetric(Metric dropwizardMetric, String coreName, String metricName) {
        this.dropwizardMetric = dropwizardMetric;
        this.coreName = coreName;
        this.metricName = metricName;
    }

    @Override
    public SolrCoreMetric parseLabels() {
        if (dropwizardMetric instanceof Gauge) {
            String[] parsedMetric = metricName.split("\\.");
            String type = parsedMetric[1];
            labels = Map.of(
                    "core", coreName,
                    "type", type);
        }
        return this;
    }

    @Override
    void toPrometheus(SolrPrometheusCoreRegistry solrPrometheusCoreRegistry) {
        if (dropwizardMetric instanceof Gauge) {
            solrPrometheusCoreRegistry.exportGauge((Gauge<?>) dropwizardMetric, CORE_INDEX_METRICS, labels);
        }
    }
}
