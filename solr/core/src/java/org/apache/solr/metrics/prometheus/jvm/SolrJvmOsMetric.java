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
        if(parsedMetric[0].equals("threads")){
            labels.put("item", parsedMetric[1]);
        } else {
            labels.put("item", parsedMetric[parsedMetric.length - 1]);
        }
        return this;
    }

    @Override
    public void toPrometheus(SolrPrometheusExporter exporter) {
        String exportName = "";
        if (dropwizardMetric instanceof Gauge) {
            if (metricName.endsWith("MemorySize") || metricName.endsWith("SpaceSize")) {
                exportName = JVM_OS_MEMORY_BYTES;
            } else if (metricName.endsWith("FileDescriptorCount")) {
                exportName = JVM_OS_FILE_DESCRIPTORS;
            } else if (metricName.endsWith("CpuLoad")) {
                exportName = JVM_OS_CPU_LOAD;
            } else if (metricName.equals("os.processCpuTime")) {
                exportName = JVM_OS_CPU_TIME;
            } else if (metricName.equals("os.systemLoadAverage")) {
                exportName = JVM_OS_LOAD_AVERAGE;
            } else if (metricName.startsWith("threads.")) {
                if (metricName.endsWith(".count")) {
                    exportName = JVM_OS_THREADS;
                }
            }
        }
        if (!exportName.isEmpty()) {
            exporter.exportGauge(exportName, (Gauge<?>) dropwizardMetric, getLabels());
        }
    }
}
