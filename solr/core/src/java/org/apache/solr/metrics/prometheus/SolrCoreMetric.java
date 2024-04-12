package org.apache.solr.metrics.prometheus;

@FunctionalInterface
public interface SolrCoreMetric {
    void toPrometheus(SolrPrometheusCoreRegistry solrPrometheusCoreRegistry, String metricName);
}
