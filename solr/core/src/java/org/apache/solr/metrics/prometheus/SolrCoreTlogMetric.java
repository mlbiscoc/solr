package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;

import java.util.Map;

public class SolrCoreTlogMetric extends SolrCoreMetric {
    public static final String CORE_TLOG_METRICS = "solr_metrics_tlog_replicas";

    public SolrCoreTlogMetric(Metric dropwizardMetric, String coreName, String metricName) {
        this.dropwizardMetric = dropwizardMetric;
        this.coreName = coreName;
        this.metricName = metricName;
    }

    @Override
    public SolrCoreMetric parseLabels() {
        String[] parsedMetric = metricName.split("\\.");
        if (dropwizardMetric instanceof Meter) {
            String item = parsedMetric[1];
            String type = parsedMetric[2];
            this.labels = Map.of(
                    "core", coreName,
                    "item", item,
                    "type", type);
        }
        return this;
    }

    @Override
    public void toPrometheus(SolrPrometheusCoreRegistry solrPrometheusCoreRegistry) {
        if (dropwizardMetric instanceof Meter) {
            solrPrometheusCoreRegistry.exportMeter((Meter) dropwizardMetric, CORE_TLOG_METRICS, labels);
        } else {
            System.out.println("Not possible to migrate string values to prometheus");
        }
    }
}
