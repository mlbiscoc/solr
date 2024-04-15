package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Metric;

import java.util.HashMap;
import java.util.Map;

public abstract class SolrCoreMetric {
    public Metric dropwizardMetric;
    public String coreName;
    public String metricName;
    public Map<String, String> labels = new HashMap<>();

    public SolrCoreMetric(Metric dropwizardMetric, String coreName, String metricName, boolean cloudMode) {
        this.dropwizardMetric = dropwizardMetric;
        this.coreName = coreName;
        this.metricName = metricName;
        labels.put("core", coreName);
        if (cloudMode) {
            String[] coreNameParsed = coreName.split("_");
            labels.put("collection", coreNameParsed[1]);
            labels.put("shard", coreNameParsed[2]);
            labels.put("replica", coreNameParsed[3] + "_" + coreNameParsed[4]);
        }
    }
    abstract SolrCoreMetric parseLabels();
    abstract void toPrometheus(SolrPrometheusCoreRegistry solrPrometheusCoreRegistry);
}
