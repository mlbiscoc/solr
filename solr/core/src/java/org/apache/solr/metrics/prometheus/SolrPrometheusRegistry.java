package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Meter;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.model.registry.PrometheusRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class SolrPrometheusRegistry {
    PrometheusRegistry prometheusRegistry;
    String registryName;
    private final Map<String, Counter> metricCounters;
    private final Map<String, Gauge> metricGauges;

    public SolrPrometheusRegistry(PrometheusRegistry prometheusRegistry) {
        this.prometheusRegistry = prometheusRegistry;
        this.metricCounters = new HashMap<>();
        this.metricGauges = new HashMap<>();
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

    protected void registerCounter(String metricName, String ...labelNames) {
        Counter counter = io.prometheus.metrics.core.metrics.Counter.builder()
                .name(metricName)
                .labelNames(labelNames)
                .register(prometheusRegistry);
        metricCounters.put(metricName, counter);
    }

    protected void registerGauge(String metricName, String ...labelNames) {
        Gauge gauge = io.prometheus.metrics.core.metrics.Gauge.builder()
                .name(metricName)
                .labelNames(labelNames)
                .register(prometheusRegistry);
        metricGauges.put(metricName, gauge);
    }

    protected void exportMeter(Meter dropwizardMetric, String prometheusMetricName, Map<String, String> labelsMap) {
        if (!metricCounters.containsKey(prometheusMetricName)) {
            ArrayList<String> labels = new ArrayList<>(labelsMap.keySet());
            registerCounter(prometheusMetricName, labels.toArray(String[]::new));
        }
        ArrayList<String> labelValues = new ArrayList<>(labelsMap.values());
        getMetricCounter(prometheusMetricName).labelValues(labelValues.toArray(String[]::new)).inc(dropwizardMetric.getCount());
    }

    protected void exportCounter(com.codahale.metrics.Counter dropwizardMetric, String prometheusMetricName, Map<String, String> labelsMap) {
        if (!metricCounters.containsKey(prometheusMetricName)) {
            ArrayList<String> labels = new ArrayList<>(labelsMap.keySet());
            registerCounter(prometheusMetricName, labels.toArray(String[]::new));
        }
        ArrayList<String> labelValues = new ArrayList<>(labelsMap.values());
        getMetricCounter(prometheusMetricName).labelValues(labelValues.toArray(String[]::new)).inc(dropwizardMetric.getCount());
    }

    protected void exportGauge(com.codahale.metrics.Gauge<?> dropwizardMetricRaw, String prometheusMetricName, Map<String, String> labelsMap) {
        Object dropwizardMetric = (dropwizardMetricRaw).getValue();
        if (!metricGauges.containsKey(prometheusMetricName)) {
            ArrayList<String> labels = new ArrayList<>(labelsMap.keySet());
            if (dropwizardMetric instanceof HashMap) {
                labels.add("item");
            }
            registerGauge(prometheusMetricName, labels.toArray(String[]::new));
        }
        ArrayList<String> labelValues = new ArrayList<>(labelsMap.values());
        String[] labels = labelValues.toArray(String[]::new);
        if (dropwizardMetric instanceof Number) {
            getMetricGauge(prometheusMetricName).labelValues(labels).set(((Number) dropwizardMetric).doubleValue());
        } else if (dropwizardMetric instanceof HashMap) {
            HashMap<?, ?> itemsMap = (HashMap<?, ?>) dropwizardMetric;
            for (Object item : itemsMap.keySet()) {
                if (itemsMap.get(item) instanceof Number) {
                    String[] newLabels = new String[labels.length + 1];
                    System.arraycopy(labels, 0, newLabels, 0, labels.length);
                    newLabels[labels.length] = (String) item;
                    getMetricGauge(prometheusMetricName).labelValues(newLabels).set(((Number) itemsMap.get(item)).doubleValue());
                } else {
                    System.out.println("This is not an number");
                }
            }
        }
    }

}
