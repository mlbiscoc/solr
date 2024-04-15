package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;

import java.util.Map;

import static org.apache.solr.metrics.prometheus.SolrCoreCacheMetric.CORE_CACHE_SEARCHER_METRICS;

public class SolrCoreSearcherMetric extends SolrCoreMetric {
    public static final String CORE_SEARCHER_METRICS = "solr_metrics_core_searcher";

    public SolrCoreSearcherMetric(Metric dropwizardMetric, String coreName, String metricName) {
        this.dropwizardMetric = dropwizardMetric;
        this.coreName = coreName;
        this.metricName = metricName;
    }

    @Override
    public SolrCoreMetric parseLabels() {
        String[] parsedMetric = metricName.split("\\.");
        if (dropwizardMetric instanceof Gauge) {
            String type = parsedMetric[2];
            labels = Map.of(
                    "core", coreName,
                    "type", type);
        }
        return this;
    }

    @Override
    void toPrometheus(SolrPrometheusCoreRegistry solrPrometheusCoreRegistry) {
        if (dropwizardMetric instanceof Gauge) {
            if (metricName.endsWith("liveDocsCache")) {
                solrPrometheusCoreRegistry.exportGauge((Gauge<?>) dropwizardMetric, CORE_CACHE_SEARCHER_METRICS, labels);
            } else {
                solrPrometheusCoreRegistry.exportGauge((Gauge<?>) dropwizardMetric, CORE_SEARCHER_METRICS, labels);
            }
        } else {
            System.out.println("This metric does not exist");
        }
    }
}
