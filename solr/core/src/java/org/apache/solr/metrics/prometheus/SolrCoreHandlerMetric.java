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
    public SolrCoreHandlerMetric(Metric dropwizardMetric, String coreName, String metricName) {
        this.dropwizardMetric = dropwizardMetric;
        this.coreName = coreName;
        this.metricName = metricName;
    }

    @Override
    public void toPrometheus(SolrPrometheusCoreRegistry solrPrometheusCoreRegistry) {
        String[] parsedMetric = metricName.split("\\.");
        String category = parsedMetric[0];
        String handler = parsedMetric[1];
        String type = parsedMetric[2];

        if (dropwizardMetric instanceof Meter) {
            solrPrometheusCoreRegistry.exportMeter((Meter) dropwizardMetric, CORE_REQUESTS_TOTAL, coreName, category, handler, type);
        } else if (dropwizardMetric instanceof Counter) {
            if (metricName.endsWith("requests")) {
                solrPrometheusCoreRegistry.exportCounter((Counter) dropwizardMetric, CORE_REQUESTS_TOTAL, coreName, category, handler, type);
            } else if (metricName.endsWith("totalTime")) {
                solrPrometheusCoreRegistry.exportCounter((Counter) dropwizardMetric, CORE_REQUESTS_TOTAL_TIME, coreName, category, handler, type);
            }
        } else if (dropwizardMetric instanceof Gauge) {
            if (metricName.endsWith("handlerStart")) {
                solrPrometheusCoreRegistry.exportGauge((Gauge<?>) dropwizardMetric, CORE_HANDLER_HANDLER_START, coreName, category, handler, type);
            } else {
                solrPrometheusCoreRegistry.exportGauge((Gauge<?>) dropwizardMetric, CORE_UPDATE_HANDLER, coreName, category, handler, type);
            }
        } else {
            System.out.println("This Metric does not exist");
        }
    }

}
