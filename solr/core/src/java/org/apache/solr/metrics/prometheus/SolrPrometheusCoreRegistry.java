package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Metric;
import io.prometheus.metrics.model.registry.PrometheusRegistry;

public class SolrPrometheusCoreRegistry extends SolrPrometheusRegistry {
  public final String coreName;
  public final boolean cloudMode;
  public static final String ADMIN = "ADMIN";
  public static final String QUERY = "QUERY";
  public static final String UPDATE = "UPDATE";
  public static final String REPLICATION = "REPLICATION";
  public static final String TLOG = "TLOG";
  public static final String CACHE = "CACHE";
  public static final String SEARCHER = "SEARCHER";
  public static final String HIGHLIGHTER = "HIGHLIGHTER";
  public static final String INDEX = "INDEX";
  public static final String CORE = "CORE";

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
