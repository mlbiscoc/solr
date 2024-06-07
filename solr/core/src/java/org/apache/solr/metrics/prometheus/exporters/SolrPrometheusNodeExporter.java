package org.apache.solr.metrics.prometheus.exporters;

import static org.apache.solr.metrics.prometheus.node.SolrNodeMetric.NODE_THREAD_POOL;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import io.prometheus.metrics.model.snapshots.Labels;
import org.apache.solr.metrics.prometheus.SolrMetric;
import org.apache.solr.metrics.prometheus.SolrNoOpMetric;
import org.apache.solr.metrics.prometheus.node.SolrNodeContainerMetric;
import org.apache.solr.metrics.prometheus.node.SolrNodeHandlerMetric;

/**
 * This class maintains a {@link io.prometheus.metrics.model.snapshots.MetricSnapshot}s exported
 * from solr.node {@link com.codahale.metrics.MetricRegistry}
 */
public class SolrPrometheusNodeExporter extends SolrPrometheusExporter
    implements PrometheusNodeExporterInfo {
  public SolrPrometheusNodeExporter() {
    super();
  }

  @Override
  public void exportDropwizardMetric(Metric dropwizardMetric, String metricName) {
    if (metricName.contains(".threadPool.")) {
      exportThreadPoolMetric(dropwizardMetric, metricName);
      return;
    }

    SolrMetric solrNodeMetric = categorizeMetric(dropwizardMetric, metricName);
    solrNodeMetric.parseLabels().toPrometheus(this);
  }

  @Override
  public SolrMetric categorizeMetric(Metric dropwizardMetric, String metricName) {
    String metricCategory = metricName.split("\\.", 2)[0];
    switch (NodeCategory.valueOf(metricCategory)) {
      case ADMIN:
      case UPDATE:
        return new SolrNodeHandlerMetric(dropwizardMetric, metricName);
      case CONTAINER:
        return new SolrNodeContainerMetric(dropwizardMetric, metricName);
      default:
        return new SolrNoOpMetric();
    }
  }

  private void exportThreadPoolMetric(Metric dropwizardMetric, String metricName) {
    Labels labels;
    String[] parsedMetric = metricName.split("\\.");
    if (parsedMetric.length >= 5) {
      labels =
          Labels.of(
              "category",
              parsedMetric[0],
              "handler",
              parsedMetric[1],
              "executer",
              parsedMetric[3],
              "task",
              parsedMetric[parsedMetric.length - 1]);
    } else {
      labels =
          Labels.of(
              "category",
              parsedMetric[0],
              "executer",
              parsedMetric[2],
              "task",
              parsedMetric[parsedMetric.length - 1]);
    }
    if (dropwizardMetric instanceof Counter) {
      exportCounter(NODE_THREAD_POOL, (Counter) dropwizardMetric, labels);
    } else if (dropwizardMetric instanceof Meter) {
      exportMeter(NODE_THREAD_POOL, (Meter) dropwizardMetric, labels);
    }
  }
}
