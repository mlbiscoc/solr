package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.*;
import io.prometheus.metrics.model.registry.PrometheusRegistry;

public class SolrPrometheusCoreRegistry extends SolrPrometheusRegistry {
    public final String coreName;

    public SolrPrometheusCoreRegistry(PrometheusRegistry prometheusRegistry, String coreName) {
        super(prometheusRegistry);
        this.coreName = coreName;
    }

    public void exportDropwizardMetric(String metricName, Metric dropwizardMetric) {
        SolrCoreMetric solrCoreMetric = categorizeCorePrefix(dropwizardMetric, metricName);
        solrCoreMetric.parseLabels().toPrometheus(this);
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