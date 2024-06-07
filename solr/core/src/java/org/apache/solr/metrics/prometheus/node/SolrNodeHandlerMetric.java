package org.apache.solr.metrics.prometheus.node;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import org.apache.solr.metrics.prometheus.SolrMetric;
import org.apache.solr.metrics.prometheus.SolrPrometheusExporter;

public class SolrNodeHandlerMetric extends SolrNodeMetric {
    public static final String NODE_CLIENT_ERRORS = "solr_metrics_node_client_errors";
    public static final String NODE_ERRORS = "solr_metrics_node_errors";
    public static final String NODE_REQUESTS = "solr_metrics_node_requests";
    public static final String NODE_SERVER_ERRORS = "solr_metrics_node_server_errors";
    public static final String NODE_TIMEOUTS = "solr_metrics_node_timeouts";
    public static final String NODE_SECONDS_TOTAL = "solr_metrics_node_time_seconds";
    public static final String NODE_CONNECTIONS = "solr_metrics_node_connections";


    public SolrNodeHandlerMetric(Metric dropwizardMetric, String metricName) {
        super(dropwizardMetric, metricName);
    }

    @Override
    public SolrMetric parseLabels() {
        String[] parsedMetric = metricName.split("\\.");
        labels.put("category", parsedMetric[0]);
        labels.put("handler", parsedMetric[1]);
        return this;
    }

    @Override
    public void toPrometheus(SolrPrometheusExporter exporter) {
        String[] parsedMetric = metricName.split("\\.");
        if (dropwizardMetric instanceof Meter) {
            if (metricName.endsWith(".clientErrors")) {
                exporter.exportMeter(NODE_CLIENT_ERRORS, (Meter) dropwizardMetric, getLabels());
            } else if (metricName.endsWith(".serverErrors")) {
                exporter.exportMeter(NODE_SERVER_ERRORS, (Meter) dropwizardMetric, getLabels());
            } else if (metricName.endsWith(".timeouts")) {
                exporter.exportMeter(NODE_TIMEOUTS, (Meter) dropwizardMetric, getLabels());
            } else if (metricName.endsWith((".errors"))) {
                exporter.exportMeter(NODE_ERRORS, (Meter) dropwizardMetric, getLabels());
            }
        } else if (dropwizardMetric instanceof Counter) {
            if (metricName.endsWith(".requests")) {
                exporter.exportCounter(NODE_REQUESTS, (Counter) dropwizardMetric, getLabels());
            } else if (metricName.endsWith(".totalTime")) {
                exporter.exportCounter(NODE_SECONDS_TOTAL, (Counter) dropwizardMetric, getLabels());
            }
        } else if (dropwizardMetric instanceof Gauge) {
            if (metricName.endsWith("Connections")) {
                labels.put("item", parsedMetric[2]);
                exporter.exportGauge(NODE_CONNECTIONS, (Gauge<?>) dropwizardMetric, getLabels());
            }
        }
    }
}
