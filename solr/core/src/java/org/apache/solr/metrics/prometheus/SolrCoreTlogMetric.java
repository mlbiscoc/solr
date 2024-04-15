package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;

public class SolrCoreTlogMetric extends SolrCoreMetric {
  public static final String CORE_TLOG_METRICS = "solr_metrics_tlog_replicas";

  public SolrCoreTlogMetric(
      Metric dropwizardMetric, String coreName, String metricName, boolean cloudMode) {
    super(dropwizardMetric, coreName, metricName, cloudMode);
  }

  @Override
  public SolrCoreMetric parseLabels() {
    String[] parsedMetric = metricName.split("\\.");
    if (dropwizardMetric instanceof Meter) {
      String item = parsedMetric[1];
      String type = parsedMetric[2];
      labels.put("item", item);
      labels.put("type", type);
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
