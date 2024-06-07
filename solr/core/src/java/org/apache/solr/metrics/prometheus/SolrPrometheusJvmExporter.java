package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Metric;
import org.apache.solr.metrics.prometheus.jvm.SolrJvmBuffersMetric;
import org.apache.solr.metrics.prometheus.jvm.SolrJvmMemoryMetric;
import org.apache.solr.metrics.prometheus.jvm.SolrJvmMetric;
import org.apache.solr.metrics.prometheus.jvm.SolrJvmNoOpMetric;
import org.apache.solr.metrics.prometheus.jvm.SolrJvmOsMetric;

public class SolrPrometheusJvmExporter extends SolrPrometheusExporter {

    public SolrPrometheusJvmExporter() {}

    @Override
    public void exportDropwizardMetric(Metric dropwizardMetric, String metricName) {
        SolrJvmMetric solrJvmMetric = categorizeMetric(dropwizardMetric, metricName);
        solrJvmMetric.parseLabels().toPrometheus(this);
    }

    private SolrJvmMetric categorizeMetric(Metric dropwizardMetric, String metricName) {
        String metricCategory = metricName.split("\\.", 2)[0];
        switch (metricCategory) {
            case "buffers":
                return new SolrJvmBuffersMetric(dropwizardMetric, metricName);
            case "gc":
            case "memory":
                return new SolrJvmMemoryMetric(dropwizardMetric, metricName);
            case "os":
            case "threads":
                return new SolrJvmOsMetric(dropwizardMetric, metricName);
            default:
                return new SolrJvmNoOpMetric(dropwizardMetric, metricName);
        }
    }
}
