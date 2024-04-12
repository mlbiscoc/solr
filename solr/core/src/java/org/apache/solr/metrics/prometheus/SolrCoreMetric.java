package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Metric;

public abstract class SolrCoreMetric {
    public Metric dropwizardMetric;
    abstract void toPrometheus(SolrPrometheusCoreRegistry solrPrometheusCoreRegistry, String metricName);
}
