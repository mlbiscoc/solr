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
package org.apache.solr.metrics.prometheus.jvm;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Metric;
import org.apache.solr.metrics.prometheus.SolrPrometheusCoreExporter;
import org.apache.solr.metrics.prometheus.SolrPrometheusExporter;
import org.apache.solr.metrics.prometheus.SolrPrometheusJvmExporter;
import org.apache.solr.metrics.prometheus.core.SolrCoreMetric;

public class SolrJvmNoOpMetric extends SolrJvmMetric {

  public SolrJvmNoOpMetric(
      Metric dropwizardMetric, String metricName) {
    super(dropwizardMetric, metricName);
  }

  @Override
  public SolrJvmMetric parseLabels() {
    return this;
  }

  @Override
  public void toPrometheus(SolrPrometheusExporter exporter) {
  }
}
