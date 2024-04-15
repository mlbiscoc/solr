package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;

import java.util.HashMap;
import java.util.Map;

import static org.apache.solr.metrics.prometheus.SolrCoreCacheMetric.CORE_CACHE_SEARCHER_METRICS;

public class SolrCoreSearcherMetric extends SolrCoreMetric {
    public static final String CORE_SEARCHER_METRICS = "solr_metrics_core_searcher";

    public SolrCoreSearcherMetric(Metric dropwizardMetric, String coreName, String metricName, boolean cloudMode) {
        super(dropwizardMetric, coreName, metricName, cloudMode);
    }

    @Override
    public SolrCoreMetric parseLabels() {
        String[] parsedMetric = metricName.split("\\.");
        if (dropwizardMetric instanceof Gauge) {
            String type = parsedMetric[2];
            labels.put("type", type);
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
