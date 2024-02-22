package org.apache.solr.metrics;

import com.codahale.metrics.MetricRegistry;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SolrPrometheusMetricManager {

    private final ConcurrentMap<String, PrometheusRegistry> registries = new ConcurrentHashMap<>();

    public SolrPrometheusMetricManager() {
    }

    public PrometheusRegistry getRegistry(String registryName) {
        return registries.get(registryName);
    }

    public void createRegistry(String registryName) {
        if (registries.containsKey(registryName)) {
            System.out.println("Registry already exists not creating another");
            return;
        }
        registries.put(registryName, new PrometheusRegistry());
    }

    public Counter registerCounter(String registryName, String metricName) {
        if (!registries.containsKey(registryName)) {
            System.out.println("Does not contain registry. Creating");
            createRegistry(registryName);
        }
        PrometheusRegistry registry = registries.get(registryName);
        Counter temp;
        try {
             temp = Counter.builder().name(registryName.concat(metricName)).labelNames("httpEndpoint").help("TBD").register(registry);
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
        return temp;
    }

}
