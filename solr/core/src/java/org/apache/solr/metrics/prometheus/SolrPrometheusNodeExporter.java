package org.apache.solr.metrics.prometheus;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import io.prometheus.metrics.model.snapshots.Labels;
import org.apache.solr.metrics.prometheus.jvm.SolrJvmBuffersMetric;
import org.apache.solr.metrics.prometheus.jvm.SolrJvmMemoryMetric;
import org.apache.solr.metrics.prometheus.jvm.SolrJvmMetric;
import org.apache.solr.metrics.prometheus.jvm.SolrJvmNoOpMetric;
import org.apache.solr.metrics.prometheus.jvm.SolrJvmOsMetric;
import org.apache.solr.metrics.prometheus.node.SolrNodeContainerMetric;
import org.apache.solr.metrics.prometheus.node.SolrNodeHandlerMetric;
import org.apache.solr.metrics.prometheus.node.SolrNodeMetric;
import org.apache.solr.metrics.prometheus.node.SolrNodeNoOpMetric;

import static org.apache.solr.metrics.prometheus.node.SolrNodeMetric.NODE_THREAD_POOL;

public class SolrPrometheusNodeExporter extends SolrPrometheusExporter {
    public SolrPrometheusNodeExporter() {super();}

    @Override
    public void exportDropwizardMetric(Metric dropwizardMetric, String metricName) {
         String[] parsedMetric = metricName.split("\\.");
        if (metricName.contains(".threadPool.")) {
            Labels labels;
            String handlerLabel = "";
            String executer = "";
            if (parsedMetric.length >= 5) {
                handlerLabel = parsedMetric[1];
                executer = parsedMetric[3];
                labels = Labels.of("category", parsedMetric[0], "handler", handlerLabel, "executer", executer, "task", parsedMetric[parsedMetric.length - 1]);
            } else {
                executer = parsedMetric[2];
                labels = Labels.of("category", parsedMetric[0], "executer", executer, "task", parsedMetric[parsedMetric.length - 1]);
            }
            if (dropwizardMetric instanceof Counter) {
                exportCounter(NODE_THREAD_POOL, (Counter) dropwizardMetric, labels);
            } else if (dropwizardMetric instanceof Meter){
                exportMeter(NODE_THREAD_POOL, (Meter) dropwizardMetric, labels);
            }
            return;
        }

        SolrNodeMetric solrNodeMetric = categorizeMetric(dropwizardMetric, metricName);
        solrNodeMetric.parseLabels().toPrometheus(this);
    }

    private SolrNodeMetric categorizeMetric(Metric dropwizardMetric, String metricName) {
        String metricCategory = metricName.split("\\.", 2)[0];
        switch (metricCategory) {
            case "ADMIN":
            case "UPDATE":
                return new SolrNodeHandlerMetric(dropwizardMetric, metricName);
            case "CONTAINER":
                return new SolrNodeContainerMetric(dropwizardMetric, metricName);
            case "CACHE":
            default:
                return new SolrNodeNoOpMetric(dropwizardMetric, metricName);
        }
    }
}
