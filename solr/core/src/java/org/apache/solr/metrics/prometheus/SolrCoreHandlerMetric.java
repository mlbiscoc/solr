package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;

public class SolrCoreHandlerMetric extends SolrCoreMetric {
    public static final String CORE_REQUESTS_TOTAL = "solr_metrics_core_requests_total";
    public static final String CORE_REQUESTS_TOTAL_TIME = "solr_metrics_core_requests_total_time";
    public static final String CORE_HANDLER_HANDLER_START = "solr_metrics_core_handler_start";
    public static final String CORE_UPDATE_HANDLER = "solr_metrics_core_update_handler_metrics";
    public SolrCoreHandlerMetric(Metric dropwizardMetric) {
        this.dropwizardMetric = dropwizardMetric;
    }

    @Override
    public void toPrometheus(SolrPrometheusCoreRegistry solrPrometheusCoreRegistry, String metricName) {
        String coreName = solrPrometheusCoreRegistry.coreName;
        String[] splitString = metricName.split("\\.");
        if (dropwizardMetric instanceof Meter) {
            solrPrometheusCoreRegistry.exportMeter((Meter) dropwizardMetric, CORE_REQUESTS_TOTAL, splitString[0], splitString[1], coreName, splitString[2]);
        } else if (dropwizardMetric instanceof Counter) {
            if (metricName.endsWith("requests")) {
                solrPrometheusCoreRegistry.exportCounter((Counter) dropwizardMetric, CORE_REQUESTS_TOTAL, splitString[0], splitString[1], coreName, splitString[2]);
            } else if (metricName.endsWith("totalTime")) {
                solrPrometheusCoreRegistry.exportCounter((Counter) dropwizardMetric, CORE_REQUESTS_TOTAL_TIME, splitString[0], splitString[1], coreName, splitString[2]);
            }
        } else if (dropwizardMetric instanceof Gauge) {
            if (metricName.endsWith("handlerStart")) {
                solrPrometheusCoreRegistry.exportGauge((Gauge<?>) dropwizardMetric, CORE_HANDLER_HANDLER_START, splitString[0], splitString[1], coreName, splitString[2]);
            } else {
                solrPrometheusCoreRegistry.exportGauge((Gauge<?>) dropwizardMetric, CORE_UPDATE_HANDLER, splitString[0], splitString[1], coreName, splitString[2]);
            }
        }
    }

}
