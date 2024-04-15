package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Metric;

import java.util.HashMap;
import java.util.Map;

public abstract class SolrCoreMetric {
    public Metric dropwizardMetric;
    public String coreName;
    public String metricName;
    public Map<String, String> labels;
    public Map<String, String> defaultLabels = Map.of();
    abstract SolrCoreMetric parseLabels();
    abstract void toPrometheus(SolrPrometheusCoreRegistry solrPrometheusCoreRegistry);
}
