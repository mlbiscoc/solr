package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;

public class SolrCoreTlogMetric extends SolrCoreMetric {
    public static final String CORE_TLOG_METRICS = "solr_metrics_tlog_replicas";

    public SolrCoreTlogMetric(Metric dropwizardMetric) {
        this.dropwizardMetric = dropwizardMetric;
    }

    @Override
    public void toPrometheus(SolrPrometheusCoreRegistry solrPrometheusCoreRegistry, String metricName) {
        String coreName = solrPrometheusCoreRegistry.coreName;
        String[] splitString = metricName.split("\\.");
        if (dropwizardMetric instanceof Meter) {
            solrPrometheusCoreRegistry.exportMeter((Meter) dropwizardMetric, CORE_TLOG_METRICS, coreName, splitString[1], splitString[2]);
        } else {
            System.out.println("Not possible to migrate string values to prometheus");
        }
    }
}
