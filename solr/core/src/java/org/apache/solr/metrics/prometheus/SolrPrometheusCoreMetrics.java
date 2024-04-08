package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.*;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.Quantiles;
import io.prometheus.metrics.model.snapshots.Unit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SolrPrometheusCoreMetrics extends SolrPrometheusMetrics {
    public static final String CORE_REQUESTS_TOTAL = "solr_metrics_core_requests_total";
    public static final String CORE_REQUESTS_TOTAL_TIME = "solr_metrics_core_requests_total_time";
    public static final String CORE_REQUESTS_QUERY_MEAN_RATE = "solr_metrics_core_mean_rate";
    public static final String CORE_UPDATE_HANDLER = "solr_metrics_core_update_handler_metrics";
    public static final String CORE_HANDLER_HANDLER_START = "solr_metrics_core_handler_start";
    public static final String CORE_REQUEST_TIMES_HIST = "solr_metrics_request_times_histogram";
    public static final String CORE_REQUEST_TIMES_SUM = "solr_metrics_request_times_summary";
    public static final String CORE_CACHE_SEARCHER_METRICS = "solr_metrics_core_cache_gauge";
    public static final String CORE_TLOG_METRICS = "solr_metrics_tlog_replicas";
    public static final String CORE_SEARCHER_METRICS = "solr_metrics_core_searcher";
    public static final String CORE_HIGHLIGHER_METRICS = "solr_metrics_core_highlighter_requests";

    public SolrPrometheusCoreMetrics(PrometheusRegistry prometheusRegistry) {
        super(prometheusRegistry);
    }

    @Override
    public SolrPrometheusCoreMetrics registerDefaultMetrics() {
        registerCounter(CORE_REQUESTS_TOTAL, "Solr requests Total", "category", "handler", "collection", "type");
        registerCounter(CORE_REQUESTS_TOTAL_TIME, "Solr requests Total", "category", "handler", "collection", "type");
        registerCounter(CORE_TLOG_METRICS, "Solr TLOG Metrics", "collection", "type", "item");
        registerCounter(CORE_HIGHLIGHER_METRICS, "Solr Highlighter Metrics", "collection", "type", "item");
        registerGauge(CORE_REQUESTS_QUERY_MEAN_RATE, "Solr requests Mean rate", "category", "handler", "collection");
        registerGauge(CORE_HANDLER_HANDLER_START, "Handler Start Time", "category", "handler", "collection", "type");
        registerGauge(CORE_UPDATE_HANDLER, "Handler Start Time", "category", "handler", "collection", "type");
        registerGauge(CORE_SEARCHER_METRICS, "SearcherMetrics", "collecton", "searcherItem");
        registerGauge(CORE_CACHE_SEARCHER_METRICS, "Search Cache Metrics", "collection", "cacheType", "item");
        registerHistogram(CORE_REQUEST_TIMES_HIST, "Solr Requests times", "category", "handler", "collection", "type");
        registerSummary(CORE_REQUEST_TIMES_SUM, "Solr Request Times Summary", "category", "handler", "collection", "type");
        return this;
    }

    public void convertDropwizardMetric(String metricName, String coreName, Metric dropwizardMetric) {
        System.out.println(dropwizardMetric.toString());
        String[] splitString = metricName.split("\\.");
        Set<String> notFound = new HashSet<>();
        switch (splitString[0]) {
            case "ADMIN":
            case "QUERY":
            case "UPDATE":
            case "REPLICATION": {
                if (dropwizardMetric instanceof Meter) {
                    getMetricCounter(CORE_REQUESTS_TOTAL).labelValues(splitString[0], splitString[1], coreName, splitString[2]).inc(((Meter) dropwizardMetric).getCount());
                } else if (dropwizardMetric instanceof Counter) {
                    if (metricName.endsWith("requests")) {
                        getMetricCounter(CORE_REQUESTS_TOTAL).labelValues(splitString[0], splitString[1], coreName, splitString[2]).inc(((Counter) dropwizardMetric).getCount());
                    } else if (metricName.endsWith("totalTime")) {
                        getMetricCounter(CORE_REQUESTS_TOTAL_TIME).labelValues(splitString[0], splitString[1], coreName, splitString[2]).inc(((Counter) dropwizardMetric).getCount());
                    }
                } else if (dropwizardMetric instanceof Gauge) {
                    Object obj = ((Gauge<?>) dropwizardMetric).getValue();
                    double value;
                    if (obj instanceof Number) {
                        value = ((Number) obj).doubleValue();
                    } else {
                        System.out.println("FAILED");
                        break;
                    }
                    if (metricName.endsWith("handlerStart")){
                        getMetricGauge(CORE_HANDLER_HANDLER_START).labelValues(splitString[0], splitString[1], coreName, splitString[2]).set(value);
                    } else {
                        getMetricGauge(CORE_UPDATE_HANDLER).labelValues(splitString[0], splitString[1], coreName, splitString[2]).set(value);
                    }
                } else {
                    notFound.add(metricName);
                }
                break;
            }
            case "CORE": {
                System.out.println("Not possible to migrate string values to prometheus");
                break;
            }
            case "TLOG": {
                if (dropwizardMetric instanceof Meter) {
                    getMetricCounter(CORE_TLOG_METRICS).labelValues(coreName, splitString[1], splitString[2]).inc(((Meter) dropwizardMetric).getCount());
                } else {
                    System.out.println("Not possible to migrate string values to prometheus");
                    break;
                }
                break;
            }
            case "CACHE": {
                if (dropwizardMetric instanceof Gauge) {
                    Object obj = ((Gauge<?>) dropwizardMetric).getValue();
                    if (obj instanceof Number) {
                        double value;
                        value = ((Number) obj).doubleValue();
                    } else if (obj instanceof HashMap) {
                        HashMap<?, ?> itemsMap = (HashMap<?, ?>) obj;
                        for (Object item : itemsMap.keySet()) {
                            if (itemsMap.get(item) instanceof Number) {
                                getMetricGauge(CORE_CACHE_SEARCHER_METRICS).labelValues(coreName, splitString[2], (String) item).set( ((Number) itemsMap.get(item)).doubleValue());
                            } else {
                                System.out.println("This is not an number");
                            }
                        }
                    } else {
                        break;
                    }
                }
                break;
            }
            case "SEARCHER":{
                if (dropwizardMetric instanceof Gauge) {
                    Object obj = ((Gauge<?>) dropwizardMetric).getValue();
                    double value;
                    if (obj instanceof Number) {
                        value = ((Number) obj).doubleValue();
                        getMetricGauge(CORE_SEARCHER_METRICS).labelValues(coreName, splitString[2]).set(value);
                    } else if (obj instanceof Boolean) {
                        value = ((Boolean) obj) ? 1 : 0;
                    } else if (obj instanceof HashMap) {
                        HashMap<?, ?> itemsMap = (HashMap<?, ?>) obj;
                        for (Object item : itemsMap.keySet()) {
                            if (itemsMap.get(item) instanceof Number) {
                                getMetricGauge(CORE_SEARCHER_METRICS).labelValues(coreName, splitString[2]).set( ((Number) itemsMap.get(item)).doubleValue());
                            } else {
                                System.out.println("This is not an number");
                            }
                        }
                    } else {
                        System.out.println("Other type");
                        break;
                    }
                }
                break;
            }
            case "HIGHLIGHTER": {
                if (dropwizardMetric instanceof Counter) {
                    getMetricCounter(CORE_HIGHLIGHER_METRICS).labelValues(coreName, splitString[1], splitString[2]).inc(((Counter) dropwizardMetric).getCount());
                } else {
                    System.out.println("Non existent type");
                }
                break;
            }
            case "INDEX": {
                break;
            }
            default: {
                System.out.println(splitString[0]);
            }
        }
    }
}
