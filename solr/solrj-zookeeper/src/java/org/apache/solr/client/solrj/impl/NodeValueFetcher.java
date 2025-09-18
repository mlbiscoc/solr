/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.solr.client.solrj.impl;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrRequest.SolrRequestType;
import org.apache.solr.client.solrj.request.GenericSolrRequest;
import org.apache.solr.client.solrj.response.SimpleSolrResponse;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.StrUtils;

/**
 * This class is responsible for fetching metrics and other attributes from a given node in Solr
 * cluster. This is a helper class that is used by {@link SolrClientNodeStateProvider}
 */
// NOCOMMIT: Need to removed hardcoded references to Dropwizard metrics for OTEL conversion, and
// probably change enum structure to be more compatible with OTEL naming
public class NodeValueFetcher {
  // well known tags
  public static final String NODE = "node";
  public static final String PORT = "port";
  public static final String HOST = "host";
  public static final String CORES = "cores";
  public static final String SYSPROP = "sysprop.";
  public static final Set<String> tags =
      //      Set.of(NODE, PORT, HOST, CORES, Tags.FREEDISK.tagName, Tags.HEAPUSAGE.tagName);
      Set.of(NODE, PORT, HOST, CORES);
  public static final Pattern hostAndPortPattern = Pattern.compile("(?:https?://)?([^:]+):(\\d+)");
  public static final String METRICS_PREFIX = "metrics:";

  /** Various well known tags that can be fetched from a node */
  public enum Metrics {
    FREEDISK("freedisk", "solr_cores_filesystem_disk_space", "type", "usable_space"),
    TOTALDISK("totaldisk", "solr_cores_filesystem_disk_space", "type", "total_space"),
    CORES("cores", "solr_cores_loaded") {
      @Override
      public Object extractResult(NamedList<Object> root) {
        Object metrics = root.get("stream");

        if (metrics == null || metricName == null) {
          return null;
        }
        try (InputStream in = (InputStream) metrics) {
          String output = new String(in.readAllBytes(), StandardCharsets.UTF_8);

          String[] lines = output.split("\n");
          int count = 0;
          for (String line : lines) {
            if (line.startsWith("#")) continue;

            if (!line.startsWith(metricName)) {
              throw new SolrException(
                  SolrException.ErrorCode.SERVER_ERROR,
                  "Response should only contain "
                      + metricName
                      + " metric in response. Found: "
                      + line);
            }
            count += extractPrometheusValue(line);
          }
          return count;
        } catch (Exception e) {
          throw new SolrException(
              SolrException.ErrorCode.SERVER_ERROR, "Unable to read prometheus metrics output", e);
        }
      }
    },
    SYSLOADAVG("sysLoadAvg", "jvm_system_cpu_utilization_ratio");
    //    HEAPUSAGE("heapUsage", "jvm_memory_used_bytes");

    public final String tagName;
    public final String metricName;
    public final String labelKey;
    public final String labelValue;

    Metrics(String name, String metricName) {
      this(name, metricName, null, null);
    }

    Metrics(String name, String metricName, String labelKey, String labelValue) {
      this.tagName = name;
      this.metricName = metricName;
      this.labelKey = labelKey;
      this.labelValue = labelValue;
    }

    public Object extractResult(NamedList<Object> root) {
      if (labelKey != null && labelValue != null) {
        return extractFromPrometheusResponseWithLabel(root, metricName, labelKey, labelValue);
      } else {
        return extractFromPrometheusResponse(root, metricName);
      }
    }

    private Long extractFromPrometheusResponse(NamedList<Object> root, String metricName) {
      Object metrics = root.get("stream");

      if (metrics == null || metricName == null) {
        return null;
      }

      try (InputStream in = (InputStream) metrics) {
        String output = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        String[] lines = output.split("\n");
        for (String line : lines) {
          if (line.startsWith("#")) continue;
          if (!line.startsWith(metricName)) {
            throw new SolrException(
                SolrException.ErrorCode.SERVER_ERROR,
                "Response should only contain "
                    + metricName
                    + " metric in response. Found: "
                    + line);
          }
          return extractPrometheusValue(line);
        }
      } catch (Exception e) {
        throw new SolrException(
            SolrException.ErrorCode.SERVER_ERROR, "Unable to read prometheus metrics output", e);
      }

      return null;
    }

    private Long extractFromPrometheusResponseWithLabel(
        NamedList<Object> root, String metricName, String labelKey, String labelValue) {
      Object metrics = root.get("stream");

      if (metrics == null || metricName == null) {
        return null;
      }

      try (InputStream in = (InputStream) metrics) {
        String output = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        String[] lines = output.split("\n");
        for (String line : lines) {
          if (line.startsWith("#")) continue;
          if (!line.startsWith(metricName)) {
            continue; // Skip lines that don't match our metric
          }

          // Check if the line contains the expected label
          String expectedLabel = labelKey + "=\"" + labelValue + "\"";
          if (line.contains(expectedLabel)) {
            return extractPrometheusValue(line);
          }
        }
      } catch (Exception e) {
        throw new SolrException(
            SolrException.ErrorCode.SERVER_ERROR, "Unable to read prometheus metrics output", e);
      }

      return null;
    }

    public static long extractPrometheusValue(String line) {
      line = line.trim();
      String actualValue;
      if (line.contains("}")) {
        actualValue = line.substring(line.lastIndexOf("} ") + 1);
      } else {
        actualValue = line.split(" ")[1];
      }
      return (long) Double.parseDouble(actualValue);
    }
  }

  /** Retrieve values of well known tags, as defined in {@link Metrics}. */
  private void getRemoteTags(
      Set<String> requestedTagNames, SolrClientNodeStateProvider.RemoteCallCtx ctx) {

    // First resolve names into actual Tags instances
    EnumSet<Metrics> requestedMetricNames = EnumSet.noneOf(Metrics.class);
    for (Metrics t : Metrics.values()) {
      if (requestedTagNames.contains(t.tagName)) {
        requestedMetricNames.add(t);
      }
    }
    if (requestedMetricNames.isEmpty()) {
      return;
    }

    ModifiableSolrParams params = new ModifiableSolrParams();
    params.add("wt", "prometheus");

    // Collect unique metric names
    Set<String> uniqueMetricNames = new HashSet<>();
    for (Metrics t : requestedMetricNames) {
      uniqueMetricNames.add(t.metricName);
    }

    // Use metric name filtering to get only the metrics we need
    params.add("name", StrUtils.join(uniqueMetricNames, ','));

    // Add label filters for disk space metrics
    for (Metrics t : requestedMetricNames) {
      if (t.labelKey != null && t.labelValue != null) {
        params.add(t.labelKey, t.labelValue);
      }
    }

    try {
      var req = new GenericSolrRequest(METHOD.GET, "/admin/metrics", SolrRequestType.ADMIN, params);
      req.setResponseParser(new InputStreamResponseParser("prometheus"));

      String baseUrl =
          ctx.zkClientClusterStateProvider.getZkStateReader().getBaseUrlForNodeName(ctx.getNode());
      SimpleSolrResponse rsp =
          ctx.cloudSolrClient.getHttpClient().requestWithBaseUrl(baseUrl, req::process);

      for (Metrics t : requestedMetricNames) {
        Object value = t.extractResult(rsp.getResponse());
        if (value != null) {
          ctx.tags.put(t.tagName, value);
        }
      }
    } catch (Exception e) {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, e);
    }
  }

  public void getTags(Set<String> requestedTags, SolrClientNodeStateProvider.RemoteCallCtx ctx) {
    try {
      if (requestedTags.contains(NODE)) ctx.tags.put(NODE, ctx.getNode());
      if (requestedTags.contains(HOST)) {
        Matcher hostAndPortMatcher = hostAndPortPattern.matcher(ctx.getNode());
        if (hostAndPortMatcher.find()) ctx.tags.put(HOST, hostAndPortMatcher.group(1));
      }
      if (requestedTags.contains(PORT)) {
        Matcher hostAndPortMatcher = hostAndPortPattern.matcher(ctx.getNode());
        if (hostAndPortMatcher.find()) ctx.tags.put(PORT, hostAndPortMatcher.group(2));
      }

      if (!ctx.isNodeAlive(ctx.getNode())) {
        // Don't try to reach out to the node if we already know it is down
        return;
      }
      getRemoteSystemProps(requestedTags, ctx);
      getRemotePropertiesAndMetrics(requestedTags, ctx);
      getRemoteTags(requestedTags, ctx);
    } catch (Exception e) {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, e);
    }
  }

  private void getRemoteSystemProps(
      Set<String> requestedTagNames, SolrClientNodeStateProvider.RemoteCallCtx ctx) {

    //      ctx.tags.put(tag.toString(), v);
    //      if (tag.startsWith(SYSPROP)) { // CHECK WITH A DEBUGGER WHAT THIS RETURNS???
    //        metricsKeyVsTag
    //            .computeIfAbsent(
    //                //                "solr.jvm:system.properties:" +
    // tag.substring(SYSPROP.length()),
    //                tag, k -> new HashSet<>())
    //            .add(tag);
    //        continue; // Lets skip system props for now
    //      }

    ModifiableSolrParams params = new ModifiableSolrParams();
    try {
      SimpleSolrResponse rsp = ctx.invokeWithRetry(ctx.getNode(), "/admin/info/properties", params);
      NamedList<?> systemPropsRsp = (NamedList<?>) rsp.getResponse().get("system.properties");
      for (String requestedProperty : requestedTagNames) {
        if (requestedProperty.startsWith(SYSPROP)) {
          Object property = systemPropsRsp.get(requestedProperty.substring(SYSPROP.length()));
          if (property != null) ctx.tags.put(requestedProperty, property.toString());
        }
      }

    } catch (Exception e) {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Error getting remote info", e);
    }
  }

  /** Retrieve values that match JVM system properties and metrics. */
  private void getRemotePropertiesAndMetrics(
      Set<String> requestedTagNames, SolrClientNodeStateProvider.RemoteCallCtx ctx) {

    for (String tag : requestedTagNames) {
      if (tag.startsWith(SYSPROP)) {
        // System properties are handled in getRemoteSystemProps
        continue;
      } else if (tag.startsWith(METRICS_PREFIX)) {
        // Handle each metric individually to properly handle label filters
        Map<String, Set<Object>> metricsKeyVsTag = new HashMap<>();
        ModifiableSolrParams params = new ModifiableSolrParams();

        metricsKeyVsTag.computeIfAbsent(tag, k -> new HashSet<>()).add(tag);

        var parseMetricString = tag.split(":");
        if (parseMetricString.length > 2) {
          // Metric has label filters
          var kvLabel = parseMetricString[2].split("=");
          params.add(kvLabel[0], kvLabel[1]);
        }
        String metricName = parseMetricString[1];
        params.add("name", metricName);

        // Fetch this specific metric
        SolrClientNodeStateProvider.fetchReplicaMetrics(
            ctx.getNode(), ctx, metricsKeyVsTag, params);

        // The result should now be in ctx.tags with the tag as the key
        // fetchReplicaMetrics should have called ctx.tags.put(tag.toString(), v)
      }
    }
  }
}
