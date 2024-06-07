package org.apache.solr.metrics.prometheus.jvm;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import org.apache.solr.metrics.prometheus.SolrMetric;
import org.apache.solr.metrics.prometheus.exporters.SolrPrometheusExporter;

/* Dropwizard metrics of name gc.* and memory.* */
public class SolrJvmMemoryMetric extends SolrJvmMetric {

  public static String JVM_GC = "solr_metrics_jvm_gc";
  public static String JVM_GC_SECONDS = "solr_metrics_jvm_gc_seconds";
  public static String JVM_MEMORY_HEAP_BYTES = "solr_metrics_jvm_memory_heap_bytes";
  public static String JVM_MEMORY_NON_HEAP_BYTES = "solr_metrics_jvm_memory_non_heap_bytes";
  public static String JVM_MEMORY_POOL_BYTES = "solr_metrics_jvm_memory_pools_bytes";
  public static String JVM_MEMORY_BYTES = "solr_metrics_jvm_memory_bytes";

  public SolrJvmMemoryMetric(Metric dropwizardMetric, String metricName) {
    super(dropwizardMetric, metricName);
  }

  @Override
  public SolrMetric parseLabels() {
    String[] parsedMetric = metricName.split("\\.");
    if (metricName.startsWith("memory")) {
      labels.put("item", parsedMetric[parsedMetric.length - 1]);
    } else {
      labels.put("item", parsedMetric[1]);
    }
    return this;
  }

  @Override
  public void toPrometheus(SolrPrometheusExporter exporter) {
    String[] parsedMetric = metricName.split("\\.");
    String exportName = "";
    if (dropwizardMetric instanceof Gauge) {
      if (metricName.endsWith(".count")) {
        exportName = JVM_GC;
      } else if (metricName.endsWith(".time")) {
        exportName = JVM_GC_SECONDS;
      } else if (metricName.startsWith("memory.total.")) {
        exportName = JVM_MEMORY_BYTES;
      } else if (!metricName.endsWith(".usage")) {
        if (metricName.startsWith("memory.heap.")) {
          exportName = JVM_MEMORY_HEAP_BYTES;
        } else if (metricName.startsWith("memory.non-heap.")) {
          exportName = JVM_MEMORY_NON_HEAP_BYTES;
        } else if (metricName.startsWith("memory.pools.")) {
          exportName = JVM_MEMORY_POOL_BYTES;
          labels.put("space", parsedMetric[2]);
        }
      }
      if (!exportName.isEmpty()) {
        exporter.exportGauge(exportName, (Gauge<?>) dropwizardMetric, getLabels());
      }
    }
  }
}
