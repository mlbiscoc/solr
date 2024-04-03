package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.*;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.Quantiles;
import io.prometheus.metrics.model.snapshots.Unit;

import java.util.Map;

public class SolrPrometheusCoreMetrics extends SolrPrometheusMetrics {
    public static final String CORE_REQUESTS_TOTAL = "solr_metrics_core_requests_total";
    public static final String CORE_REQUESTS_TOTAL_TIME = "solr_metrics_core_requests_total_time";
    public static final String CORE_REQUESTS_QUERY_MEAN_RATE = "solr_metrics_core_mean_rate";
    public static final String CORE_REQUEST_TIMES_HIST = "solr_metrics_request_times_histogram";
    public static final String CORE_REQUEST_TIMES_SUM = "solr_metrics_request_times_summary";

    public SolrPrometheusCoreMetrics(PrometheusRegistry prometheusRegistry) {
        super(prometheusRegistry);
    }

    @Override
    public SolrPrometheusCoreMetrics registerDefaultMetrics() {
        registerCounter(CORE_REQUESTS_TOTAL, "Solr requests Total", "category", "handler", "collection", "type");
        registerCounter(CORE_REQUESTS_TOTAL_TIME, "Solr requests Total", "category", "handler", "collection", "type");
        registerGauge(CORE_REQUESTS_QUERY_MEAN_RATE, "Solr requests Mean rate", "category", "handler", "collection");
        registerHistogram(CORE_REQUEST_TIMES_HIST, "Solr Requests times", "category", "handler", "collection", "type");
        registerSummary(CORE_REQUEST_TIMES_SUM, "Solr Request Times Summary", "category", "handler", "collection", "type");
        return this;
    }

    public void convertDropwizardMetric(String metricName, String coreName, Metric dropwizardMetric) {
        System.out.println(dropwizardMetric.toString());
        String[] splitString = metricName.split("\\.");
        if (dropwizardMetric instanceof Meter) {
            getMetricCounter(CORE_REQUESTS_TOTAL).labelValues(splitString[0], splitString[1], coreName, splitString[2]).inc(((Meter) dropwizardMetric).getCount());
        } else if (dropwizardMetric instanceof Timer) {
            System.out.println("GET TIME");
//            getMetricHistogram(CORE_REQUEST_TIMES_HIST).labelValues(splitString[0], splitString[1], coreName, splitString[2]).observe(((Timer) dropwizardMetric).getOneMinuteRate());
        } else if (dropwizardMetric instanceof Counter) {
            if (metricName.endsWith("requests")) {
                getMetricCounter(CORE_REQUESTS_TOTAL).labelValues(splitString[0], splitString[1], coreName, splitString[2]).inc(((Counter) dropwizardMetric).getCount());
            } else if (metricName.endsWith("totalTime")) {
                getMetricCounter(CORE_REQUESTS_TOTAL_TIME).labelValues(splitString[0], splitString[1], coreName, splitString[2]).inc(((Counter) dropwizardMetric).getCount());
            }
        } else if (dropwizardMetric instanceof Gauge) {

        }
    }
}
