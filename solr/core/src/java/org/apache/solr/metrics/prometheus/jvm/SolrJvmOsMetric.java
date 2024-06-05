package org.apache.solr.metrics.prometheus.jvm;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import org.apache.solr.metrics.prometheus.SolrMetric;
import org.apache.solr.metrics.prometheus.SolrPrometheusExporter;

public class SolrJvmOsMetric extends SolrJvmMetric {

    public static final String JVM_OS_MEMORY_BYTES = "solr_metrics_jvm_os_memory_bytes";
    public static final String JVM_OS_FILE_DESCRIPTORS = "solr_metrics_jvm_os_file_descriptors";
    public static final String JVM_OS_CPU_LOAD = "solr_metrics_jvm_os_cpu_load";
    public static final String JVM_OS_CPU_TIME = "solr_metrics_jvm_os_cpu_time";
    public static final String JVM_OS_LOAD_AVERAGE = "solr_metrics_jvm_os_load_average";
    public static final String JVM_OS_THREADS = "solr_metrics_jvm_threads";

    public SolrJvmOsMetric(Metric dropwizardMetric, String metricName) {
        super(dropwizardMetric, metricName);
    }

    @Override
    public SolrMetric parseLabels() {
        String[] parsedMetric = metricName.split("\\.");
        labels.put("item", parsedMetric[parsedMetric.length - 1]);
        return this;
    }

    @Override
    public void toPrometheus(SolrPrometheusExporter exporter) {
        String[] parsedMetric = metricName.split("\\.");
        if (dropwizardMetric instanceof Gauge) {
            if (metricName.endsWith("MemorySize") || metricName.endsWith("SpaceSize")) {
                exporter.exportGauge(JVM_OS_MEMORY_BYTES, (Gauge<?>) dropwizardMetric, getLabels());
            } else if (metricName.endsWith("FileDescriptorCount")) {
                exporter.exportGauge(JVM_OS_FILE_DESCRIPTORS, (Gauge<?>) dropwizardMetric, getLabels());
            } else if (metricName.endsWith("CpuLoad")) {
                exporter.exportGauge(JVM_OS_CPU_LOAD, (Gauge<?>) dropwizardMetric, getLabels());
            } else if (metricName.equals("os.processCpuTime")) {
                exporter.exportGauge(JVM_OS_CPU_TIME, (Gauge<?>) dropwizardMetric, getLabels());
            } else if (metricName.equals("os.systemLoadAverage")) {
                exporter.exportGauge(JVM_OS_LOAD_AVERAGE, (Gauge<?>) dropwizardMetric, getLabels());
            } else if (metricName.startsWith("threads.")) {
                if (metricName.endsWith(".count")) {
                    exporter.exportGauge(JVM_OS_THREADS, (Gauge<?>) dropwizardMetric, getLabels());
                }
            }
        }
    }
}
