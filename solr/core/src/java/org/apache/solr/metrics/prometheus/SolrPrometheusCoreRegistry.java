package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.*;
import io.prometheus.metrics.model.registry.PrometheusRegistry;

import static org.apache.solr.metrics.prometheus.SolrCoreCacheMetric.CORE_CACHE_SEARCHER_METRICS;
import static org.apache.solr.metrics.prometheus.SolrCoreHandlerMetric.*;
import static org.apache.solr.metrics.prometheus.SolrCoreHighlighterMetric.CORE_HIGHLIGHER_METRICS;
import static org.apache.solr.metrics.prometheus.SolrCoreIndexMetric.CORE_INDEX_METRICS;
import static org.apache.solr.metrics.prometheus.SolrCoreSearcherMetric.CORE_SEARCHER_CACHE_METRICS;
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
        createCounter(CORE_REQUESTS_TOTAL, "Solr requests Total", "category", "handler", "collection", "type");
        createCounter(CORE_REQUESTS_TOTAL_TIME, "Solr requests Total", "category", "handler", "collection", "type");
        createCounter(CORE_TLOG_METRICS, "Solr TLOG Metrics", "collection", "type", "item");
        createCounter(CORE_HIGHLIGHER_METRICS, "Solr Highlighter Metrics", "collection", "type", "item");
        createGauge(CORE_HANDLER_HANDLER_START, "Handler Start Time", "category", "handler", "collection", "type");
        createGauge(CORE_UPDATE_HANDLER, "Handler Start Time", "category", "handler", "collection", "type");
        createGauge(CORE_SEARCHER_METRICS, "SearcherMetrics", "collection", "searcherItem");
        createGauge(CORE_CACHE_SEARCHER_METRICS, "Searcher Cache Metrics", "collection", "cacheType", "item");
        createGauge(CORE_INDEX_METRICS, "Index Metrics", "collection", "type");
        return this;
    }

    public void exportDropwizardMetric(String metricName, Metric dropwizardMetric) {
        String metricCategory = metricName.split("\\.")[0];
        SolrCoreMetric solrCoreMetric = categorizeCorePrefix(dropwizardMetric, metricCategory);
        solrCoreMetric.toPrometheus(this, metricName);
    }

    private SolrCoreMetric categorizeCorePrefix(Metric dropwizardMetric, String metricCategory) {
        switch (metricCategory) {
            case "ADMIN":
            case "QUERY":
            case "UPDATE":
            case "REPLICATION": {
                return new SolrCoreHandlerMetric(dropwizardMetric);
            }
            case "TLOG": {
                return new SolrCoreTlogMetric(dropwizardMetric);
            }
            case "CACHE": {
                return new SolrCoreCacheMetric(dropwizardMetric);
            }
            case "SEARCHER":{
                return new SolrCoreSearcherMetric(dropwizardMetric);
            }
            case "HIGHLIGHTER": {
                return new SolrCoreHighlighterMetric(dropwizardMetric);
            }
            case "INDEX": {
                return new SolrCoreIndexMetric(dropwizardMetric);
            }
            case "CORE":
            default: {
                return new SolrCoreNoOpMetric(dropwizardMetric);
            }
        }
    }

}