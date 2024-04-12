package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.*;
import io.prometheus.metrics.model.registry.PrometheusRegistry;

import java.util.HashMap;

import static org.apache.solr.metrics.prometheus.SolrCoreHandlerMetric.*;

public class SolrPrometheusCoreRegistry extends SolrPrometheusRegistry {
    public static final String CORE_REQUESTS_QUERY_MEAN_RATE = "solr_metrics_core_mean_rate";
    public static final String CORE_CACHE_SEARCHER_METRICS = "solr_metrics_core_cache_gauge";
    public static final String CORE_TLOG_METRICS = "solr_metrics_tlog_replicas";
    public static final String CORE_SEARCHER_METRICS = "solr_metrics_core_searcher";
    public static final String CORE_HIGHLIGHER_METRICS = "solr_metrics_core_highlighter_requests";
    public static final String CORE_INDEX_METRICS = "solr_metrics_index";
    public final String coreName;

    public SolrPrometheusCoreRegistry(PrometheusRegistry prometheusRegistry, String coreName) {
        super(prometheusRegistry);
        this.coreName = coreName;
    }

    @Override
    public SolrPrometheusCoreRegistry registerDefaultMetrics() {
        createCounter(CORE_REQUESTS_TOTAL, "Solr requests Total", "category", "handler", "collection", "type");
        createCounter(CORE_REQUESTS_TOTAL_TIME, "Solr requests Total", "category", "handler", "collection", "type");
        createCounter(CORE_TLOG_METRICS, "Solr TLOG Metrics", "collection", "type", "item");
        createCounter(CORE_HIGHLIGHER_METRICS, "Solr Highlighter Metrics", "collection", "type", "item");
        createGauge(CORE_REQUESTS_QUERY_MEAN_RATE, "Solr requests Mean rate", "category", "handler", "collection");
        createGauge(CORE_HANDLER_HANDLER_START, "Handler Start Time", "category", "handler", "collection", "type");
        createGauge(CORE_UPDATE_HANDLER, "Handler Start Time", "category", "handler", "collection", "type");
        createGauge(CORE_SEARCHER_METRICS, "SearcherMetrics", "collection", "searcherItem");
        createGauge(CORE_CACHE_SEARCHER_METRICS, "Searcher Cache Metrics", "collection", "cacheType", "item");
        createGauge(CORE_INDEX_METRICS, "Index Metrics", "collection", "type");
        return this;
    }

    public void convertDropwizardMetric(String metricName, Metric dropwizardMetric) {
        String metricCategory = metricName.split("\\.")[0];
        String[] parsedMetric = metricName.split("\\.");
        // categorize the metric from SolrCoreMetric to correct object
        // Call toPrometheus function()
        switch (metricCategory) {
            case "ADMIN":
            case "QUERY":
            case "UPDATE":
            case "REPLICATION": {
                SolrCoreHandlerMetric solrCoreHandlerMetric = new SolrCoreHandlerMetric(dropwizardMetric);
                solrCoreHandlerMetric.toPrometheus(this, metricName);
                break;
            }
            case "CORE": {
                System.out.println("Not possible to migrate string values to prometheus");
                break;
            }
            case "TLOG": {
                if (dropwizardMetric instanceof Meter) {
                    getMetricCounter(CORE_TLOG_METRICS).labelValues(coreName, parsedMetric[1], parsedMetric[2]).inc(((Meter) dropwizardMetric).getCount());
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
                                getMetricGauge(CORE_CACHE_SEARCHER_METRICS).labelValues(coreName, parsedMetric[2], (String) item).set( ((Number) itemsMap.get(item)).doubleValue());
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
                        getMetricGauge(CORE_SEARCHER_METRICS).labelValues(coreName, parsedMetric[2]).set(value);
                    } else if (obj instanceof Boolean) {
                        value = ((Boolean) obj) ? 1 : 0;
                    } else if (obj instanceof HashMap) {
                        HashMap<?, ?> itemsMap = (HashMap<?, ?>) obj;
                        for (Object item : itemsMap.keySet()) {
                            if (itemsMap.get(item) instanceof Number) {
                                getMetricGauge(CORE_SEARCHER_METRICS).labelValues(coreName, parsedMetric[2]).set( ((Number) itemsMap.get(item)).doubleValue());
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
                    getMetricCounter(CORE_HIGHLIGHER_METRICS).labelValues(coreName, parsedMetric[1], parsedMetric[2]).inc(((Counter) dropwizardMetric).getCount());
                } else {
                    System.out.println("Non existent type");
                }
                break;
            }
            case "INDEX": {
                if (dropwizardMetric instanceof Gauge) {
                    Object obj = ((Gauge<?>) dropwizardMetric).getValue();
                    double value;
                    if (obj instanceof Number) {
                        value = ((Number) obj).doubleValue();
                        getMetricGauge(CORE_INDEX_METRICS).labelValues(coreName, parsedMetric[1]).set(value);
                    }
                }
                break;
            }
            default: {
                System.out.println(parsedMetric[0]);
            }
        }
    }


    private void convertHandlerMetrics(Metric dropwizardMetric, String metricName) {

    }

}
