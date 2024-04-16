package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Metric;
import io.prometheus.metrics.model.registry.PrometheusRegistry;

public class SolrPrometheusCoreRegistry extends SolrPrometheusRegistry {
  public final String coreName;
  public final boolean cloudMode;
  public final static String ADMIN = "ADMIN";
  public final static String QUERY = "QUERY";
  public final static String UPDATE = "UPDATE";
  public final static String REPLICATION = "REPLICATION";
  public final static String TLOG = "TLOG";
  public final static String CACHE = "CACHE";
  public final static String SEARCHER = "SEARCHER";
  public final static String HIGHLIGHTER = "HIGHLIGHTER";
  public final static String INDEX = "INDEX";
  public final static String CORE = "CORE";

  public SolrPrometheusCoreRegistry(
      PrometheusRegistry prometheusRegistry, String coreName, boolean cloudMode) {
    super(prometheusRegistry);
    this.coreName = coreName;
    this.cloudMode = cloudMode;
  }

  public void exportDropwizardMetric(String metricName, Metric dropwizardMetric) {
    SolrCoreMetric solrCoreMetric = categorizeCoreMetric(dropwizardMetric, metricName);
    solrCoreMetric.parseLabels().toPrometheus(this);
  }

  private SolrCoreMetric categorizeCoreMetric(Metric dropwizardMetric, String metricName) {
    String metricCategory = metricName.split("\\.")[0];
    switch (metricCategory) {
      case ADMIN:
      case QUERY:
      case UPDATE:
      case REPLICATION:
        {
          return new SolrCoreHandlerMetric(dropwizardMetric, coreName, metricName, cloudMode);
        }
      case TLOG:
        {
          return new SolrCoreTlogMetric(dropwizardMetric, coreName, metricName, cloudMode);
        }
      case CACHE:
        {
          return new SolrCoreCacheMetric(dropwizardMetric, coreName, metricName, cloudMode);
        }
      case SEARCHER:
        {
          return new SolrCoreSearcherMetric(dropwizardMetric, coreName, metricName, cloudMode);
        }
      case HIGHLIGHTER:
        {
          return new SolrCoreHighlighterMetric(dropwizardMetric, coreName, metricName, cloudMode);
        }
      case INDEX:
        {
          return new SolrCoreIndexMetric(dropwizardMetric, coreName, metricName, cloudMode);
        }
      case CORE:
      default:
        {
          return new SolrCoreNoOpMetric(dropwizardMetric, coreName, metricName, cloudMode);
        }
    }
  }
}
