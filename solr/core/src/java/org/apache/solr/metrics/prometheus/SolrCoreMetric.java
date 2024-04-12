package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Metric;

public abstract class SolrCoreMetric {
    public Metric dropwizardMetric;
    public String coreName;
    public String metricName;
    abstract void toPrometheus(SolrPrometheusCoreRegistry solrPrometheusCoreRegistry);
}
