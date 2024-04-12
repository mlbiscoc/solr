package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.*;
import io.prometheus.metrics.model.registry.PrometheusRegistry;

import static org.apache.solr.metrics.prometheus.SolrCoreCacheMetric.CORE_CACHE_SEARCHER_METRICS;
import static org.apache.solr.metrics.prometheus.SolrCoreHandlerMetric.*;
import static org.apache.solr.metrics.prometheus.SolrCoreHighlighterMetric.CORE_HIGHLIGHER_METRICS;
import static org.apache.solr.metrics.prometheus.SolrCoreIndexMetric.CORE_INDEX_METRICS;
import static org.apache.solr.metrics.prometheus.SolrCoreSearcherMetric.CORE_SEARCHER_METRICS;
import static org.apache.solr.metrics.prometheus.SolrCoreTlogMetric.CORE_TLOG_METRICS;

public class SolrPrometheusCoreRegistry extends SolrPrometheusRegistry {
    public final String coreName;

    public SolrPrometheusCoreRegistry(PrometheusRegistry prometheusRegistry, String coreName) {
        super(prometheusRegistry);
        this.coreName = coreName;
    }

    @Override
    public SolrPrometheusCoreRegistry registerDefaultMetrics() {
        createCounter(CORE_REQUESTS_TOTAL, "Solr requests Total", "collection", "category", "handler", "type");
        createGauge(CORE_HANDLER_HANDLER_START, "Handler Start Time", "collection", "category", "handler", "type");
        createCounter(CORE_REQUESTS_TOTAL_TIME, "Solr requests Total", "collection", "category", "handler", "type");
        createGauge(CORE_UPDATE_HANDLER, "Handler Start Time", "collection", "category", "handler", "type");
        createCounter(CORE_TLOG_METRICS, "Solr TLOG Metrics", "collection", "type", "item");
        createGauge(CORE_CACHE_SEARCHER_METRICS, "Searcher Cache Metrics", "collection", "cacheType", "item");
        createGauge(CORE_SEARCHER_METRICS, "SearcherMetrics", "collection", "searcherItem");
        createCounter(CORE_HIGHLIGHER_METRICS, "Solr Highlighter Metrics", "collection", "type", "item");
        createGauge(CORE_INDEX_METRICS, "Index Metrics", "collection", "type");
        return this;
    }

    public void exportDropwizardMetric(String metricName, Metric dropwizardMetric) {
        SolrCoreMetric solrCoreMetric = categorizeCorePrefix(dropwizardMetric, metricName);
        solrCoreMetric.toPrometheus(this);
    }

    private SolrCoreMetric categorizeCorePrefix(Metric dropwizardMetric, String metricName) {
        String metricCategory = metricName.split("\\.")[0];
        switch (metricCategory) {
            case "ADMIN":
            case "QUERY":
            case "UPDATE":
            case "REPLICATION": {
                return new SolrCoreHandlerMetric(dropwizardMetric, coreName, metricName);
            }
            case "TLOG": {
                return new SolrCoreTlogMetric(dropwizardMetric, coreName, metricName);
            }
            case "CACHE": {
                return new SolrCoreCacheMetric(dropwizardMetric, coreName, metricName);
            }
            case "SEARCHER":{
                return new SolrCoreSearcherMetric(dropwizardMetric, coreName, metricName);
            }
            case "HIGHLIGHTER": {
                return new SolrCoreHighlighterMetric(dropwizardMetric, coreName, metricName);
            }
            case "INDEX": {
                return new SolrCoreIndexMetric(dropwizardMetric, coreName, metricName);
            }
            case "CORE":
            default: {
                return new SolrCoreNoOpMetric(dropwizardMetric, coreName, metricName);
            }
        }
    }

}