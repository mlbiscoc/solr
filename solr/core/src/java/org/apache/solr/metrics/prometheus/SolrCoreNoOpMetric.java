package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Metric;

public class SolrCoreNoOpMetric extends SolrCoreMetric {

    public SolrCoreNoOpMetric(Metric dropwizardMetric, String coreName, String metricName) {
        this.dropwizardMetric = dropwizardMetric;
        this.coreName = coreName;
        this.metricName = metricName;
    }
    @Override
    void toPrometheus(SolrPrometheusCoreRegistry solrPrometheusCoreRegistry) {
        System.out.println("Cannot export string metrics");
    }
}
