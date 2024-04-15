package org.apache.solr.response;

import io.prometheus.metrics.expositionformats.PrometheusTextFormatWriter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;

@SuppressWarnings(value = "unchecked")
public class PrometheusResponseWriter extends RawResponseWriter {
  @Override
  public void write(OutputStream out, SolrQueryRequest request, SolrQueryResponse response)
      throws IOException {

    NamedList<Object> prometheusRegistries =
        (NamedList<Object>) response.getValues().get("metrics");
    Map<String, Object> registryMap = prometheusRegistries.asShallowMap();
    PrometheusTextFormatWriter prometheusTextFormatWriter = new PrometheusTextFormatWriter(false);
    registryMap.forEach(
        (name, registry) -> {
          try {
            PrometheusRegistry prometheusRegistry = (PrometheusRegistry) registry;
            prometheusTextFormatWriter.write(out, prometheusRegistry.scrape());
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
  }
}
