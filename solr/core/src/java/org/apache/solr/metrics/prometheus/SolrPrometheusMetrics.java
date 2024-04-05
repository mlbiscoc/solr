package org.apache.solr.metrics.prometheus;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.core.metrics.Histogram;
import io.prometheus.metrics.core.metrics.Summary;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.Unit;

import java.util.HashMap;
import java.util.Map;

public abstract class SolrPrometheusMetrics {
    PrometheusRegistry prometheusRegistry;
    String registryName;
    private final Map<String, Counter> metricCounters;
    private final Map<String, Gauge> metricGauges;
    private final Map<String, Histogram> metricHistograms;
    private final Map<String, Summary> metricSummaries;

    public SolrPrometheusMetrics(PrometheusRegistry prometheusRegistry) {
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

    abstract SolrPrometheusMetrics registerDefaultMetrics();

    protected void registerCounter(String metricName, String help, String ...labelNames) {
        Counter metricCounter = io.prometheus.metrics.core.metrics.Counter.builder()
                .name(metricName)
                .help(help)
                .labelNames(labelNames)
                .register(prometheusRegistry);
        metricCounters.put(metricName, metricCounter);
    }

    protected void registerGauge(String metricName, String help, String ...labelNames) {
        Gauge metricGauge = io.prometheus.metrics.core.metrics.Gauge.builder()
                .name(metricName)
                .help(help)
                .labelNames(labelNames)
                .register(prometheusRegistry);
        metricGauges.put(metricName, metricGauge);
    }

    protected void registerHistogram(String metricName, String help, String ...labelNames) {
        Histogram metricHistogram = Histogram.builder()
                        .name(metricName)
                        .help(help)
                        .labelNames(labelNames)
                                .unit(Unit.SECONDS)
                                        .register(prometheusRegistry);
        metricHistograms.put(metricName, metricHistogram);
    }

    protected void registerSummary(String metricName, String help, String ...labelNames) {
        Summary metricSummary = Summary.builder().name(metricName).help(help).labelNames(labelNames).unit(Unit.SECONDS).register(prometheusRegistry);
        metricSummaries.put(metricName, metricSummary);
    }

}
