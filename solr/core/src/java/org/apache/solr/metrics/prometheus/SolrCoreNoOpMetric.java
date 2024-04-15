package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Metric;

import java.util.Map;

public class SolrCoreNoOpMetric extends SolrCoreMetric {

    public SolrCoreNoOpMetric(Metric dropwizardMetric, String coreName, String metricName, boolean cloudMode) {
        super(dropwizardMetric, coreName, metricName, cloudMode);
    }

    @Override
    public SolrCoreMetric parseLabels() {
        return this;
    }

    @Override
    void toPrometheus(SolrPrometheusCoreRegistry solrPrometheusCoreRegistry) {
        System.out.println("Cannot export string metrics");
    }
}
