package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.core.metrics.Histogram;
import io.prometheus.metrics.core.metrics.Summary;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.Unit;

import java.util.HashMap;
import java.util.Map;

public abstract class SolrPrometheusRegistry {
    PrometheusRegistry prometheusRegistry;
    String registryName;
    private final Map<String, Counter> metricCounters;
    private final Map<String, Gauge> metricGauges;
    private final Map<String, Histogram> metricHistograms;
    private final Map<String, Summary> metricSummaries;

    public SolrPrometheusRegistry(PrometheusRegistry prometheusRegistry) {
        this.prometheusRegistry = prometheusRegistry;
        this.metricCounters = new HashMap<>();
        this.metricGauges = new HashMap<>();
        this.metricHistograms = new HashMap<>();
        this.metricSummaries = new HashMap<>();
    }

    public PrometheusRegistry getPrometheusRegistry() {
        return prometheusRegistry;
    }

    public String getRegistryName() {
        return registryName;
    }

    public Counter getMetricCounter(String metricName) {
        return metricCounters.get(metricName);
    }

    public Gauge getMetricGauge(String metricName) {
        return metricGauges.get(metricName);
    }
    public Histogram getMetricHistogram(String metricName) { return metricHistograms.get(metricName); }

    public Summary getMetricSummary(String metricName) { return metricSummaries.get(metricName); }

    abstract SolrPrometheusRegistry registerDefaultMetrics();

    protected Counter createCounter(String metricName, String help, String ...labelNames) {
        Counter counter = io.prometheus.metrics.core.metrics.Counter.builder()
                .name(metricName)
                .help(help)
                .labelNames(labelNames)
                .register(prometheusRegistry);
        metricCounters.put(metricName, counter);
        return counter;
    }

    protected Gauge createGauge(String metricName, String help, String ...labelNames) {
        Gauge gauge = io.prometheus.metrics.core.metrics.Gauge.builder()
                .name(metricName)
                .help(help)
                .labelNames(labelNames)
                .register(prometheusRegistry);
        metricGauges.put(metricName, gauge);
        return gauge;
    }

    protected void exportMeter(Meter dropwizardMetric, String prometheusMetricName, String ...labels) {
        getMetricCounter(prometheusMetricName).labelValues(labels).inc(dropwizardMetric.getCount());
    }

    protected void exportCounter(com.codahale.metrics.Counter prometheusMetricName, String metricName, String ...labels) {
        getMetricCounter(metricName).labelValues(labels).inc(prometheusMetricName.getCount());
    }

    protected void exportGauge(com.codahale.metrics.Gauge<?> dropwizardMetric, String prometheusMetricName, String ...labels){
        if (dropwizardMetric instanceof Number) {
            getMetricGauge(prometheusMetricName).labelValues(labels).set(((Number) dropwizardMetric).doubleValue());
        }
    }

}
