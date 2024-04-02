package org.apache.solr.metrics.prometheus;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.Unit;

import java.util.Map;

public abstract class SolrPrometheusMetrics {
    PrometheusRegistry prometheusRegistry;
    String registryName;
    private Map<String, Counter> metricCounters;
    private Map<String, Gauge> metricGauges;

    public SolrPrometheusMetrics(PrometheusRegistry prometheusRegistry) {
        this.prometheusRegistry = prometheusRegistry;
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

    abstract SolrPrometheusMetrics registerDefaultMetrics();

    protected Counter registerCounter(String metricName, String help, String ...labelNames) {
        Counter metricCounter = io.prometheus.metrics.core.metrics.Counter.builder()
                .name(metricName)
                .help(help)
                .labelNames(labelNames)
                .withoutExemplars().register(prometheusRegistry);
        metricCounters.put(metricName, metricCounter);
        return metricCounter;
    }

    protected Gauge registerGauge(String metricName, String help, String ...labelNames) {
        Gauge metricGauge = io.prometheus.metrics.core.metrics.Gauge.builder()
                .name(metricName)
                .help(help)
                .labelNames(labelNames)
                .unit(Unit.SECONDS).withoutExemplars().register(prometheusRegistry);
        metricGauges.put(metricName, metricGauge);
        return metricGauge;
    }

}
