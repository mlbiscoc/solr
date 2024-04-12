package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Metric;

public class SolrCoreNoOpMetric extends SolrCoreMetric {

    public SolrCoreNoOpMetric(Metric dropwizardMetric) {
        this.dropwizardMetric = dropwizardMetric;
    }
    @Override
    void toPrometheus(SolrPrometheusCoreRegistry solrPrometheusCoreRegistry, String metricName) {
        System.out.println("Cannot export string metrics");
    }
}
