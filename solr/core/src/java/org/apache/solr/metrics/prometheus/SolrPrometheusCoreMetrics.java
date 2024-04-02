package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Metric;
import com.codahale.metrics.Timer;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.Unit;

import java.util.Map;

public class SolrPrometheusCoreMetrics extends SolrPrometheusMetrics {
    public static final String CORE_REQUESTS_TOTAL = "solr_metrics_core_query_requests_total";
    public static final String CORE_REQUESTS_QUERY_MEAN_RATE = "solr_metrics_core_query_mean_rate";

    public SolrPrometheusCoreMetrics(PrometheusRegistry prometheusRegistry) {
        super(prometheusRegistry);
    }

    @Override
    public SolrPrometheusCoreMetrics registerDefaultMetrics() {
        registerCounter(CORE_REQUESTS_TOTAL, "Solr requests Total", "category", "handler", "collection");
        registerGauge(CORE_REQUESTS_QUERY_MEAN_RATE, "Solr requests Mean rate", "category", "handler", "collection");
        return this;
    }

    public void convertDropwizardMetric(String metricName, String coreName, Metric dropwizardMetric) {
        System.out.println(dropwizardMetric.toString());
        if (dropwizardMetric instanceof Timer) {
            String[] splitString = metricName.split("\\.");
            getMetricCounter(CORE_REQUESTS_TOTAL).labelValues(splitString[0], splitString[1], coreName).inc(((Timer) dropwizardMetric).getCount());
            getMetricGauge(CORE_REQUESTS_QUERY_MEAN_RATE).labelValues(splitString[0], splitString[1], coreName).set(((Timer) dropwizardMetric).getMeanRate());
        }
    }

}
