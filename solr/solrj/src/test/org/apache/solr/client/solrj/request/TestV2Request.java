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

package org.apache.solr.client.solrj.request;

import com.carrotsearch.randomizedtesting.annotations.ThreadLeakLingering;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.V2Response;
import org.apache.solr.cloud.SolrCloudTestCase;
import org.apache.solr.common.cloud.ClusterState;
import org.apache.solr.common.util.NamedList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ThreadLeakLingering(linger = 0)
public class TestV2Request extends SolrCloudTestCase {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Before
  public void setupCluster() throws Exception {
    configureCluster(4)
        .withJettyConfig(jettyCfg -> jettyCfg.enableV2(true))
        .addConfig("config", getFile("solrj/solr/collection1/conf"))
        .configure();
  }

  public void testApiPathAvailability() throws Exception {
    V2Response rsp =
        new V2Request.Builder("/cluster/nodes")
            .forceV2(true)
            .withMethod(SolrRequest.METHOD.GET)
            .build()
            .process(cluster.getSolrClient());
    List<?> l = (List<?>) rsp._get("nodes");
    assertNotNull(l);
    assertFalse(l.isEmpty());
  }

  @After
  public void afterTest() throws Exception {
    shutdownCluster();
  }

  public void assertSuccess(SolrClient client, V2Request request)
      throws IOException, SolrServerException {
    NamedList<Object> res = client.request(request);
    assertTrue("The request failed", res.get("responseHeader").toString().contains("status=0"));
  }

  @Test
  public void testIsCollectionRequest() {
    assertFalse(new V2Request.Builder("/collections").build().isPerCollectionRequest());
    assertFalse(new V2Request.Builder("/collections/a/shards").build().isPerCollectionRequest());
    assertFalse(new V2Request.Builder("/collections/a/shards/").build().isPerCollectionRequest());
    assertTrue(new V2Request.Builder("/collections/a/update").build().isPerCollectionRequest());
    assertEquals("a", new V2Request.Builder("/collections/a/update").build().getCollection());
    assertTrue(new V2Request.Builder("/c/a/update").build().isPerCollectionRequest());
    assertTrue(new V2Request.Builder("/c/a/schema").build().isPerCollectionRequest());
    assertFalse(new V2Request.Builder("/c/a").build().isPerCollectionRequest());
  }

  @Test
  public void testHttpSolrClient() throws Exception {
    SolrClient solrClient =
        new HttpSolrClient.Builder(cluster.getJettySolrRunner(0).getBaseUrl().toString()).build();
    doTest(solrClient);
    solrClient.close();
  }

  @Test
  public void testCloudSolrClient() throws Exception {
    doTest(cluster.getSolrClient());
  }

  private void doTest(SolrClient client) throws IOException, SolrServerException {
    assertSuccess(
        client,
        new V2Request.Builder("/collections")
            .withMethod(SolrRequest.METHOD.POST)
            .withPayload(
                "{"
                    + "    \"name\" : \"test\","
                    + "    \"numShards\" : 2,"
                    + "    \"replicationFactor\" : 2,"
                    + "    \"config\" : \"config\""
                    + "}"
                    + "/* ignore comment*/")
            .build());

    String requestHandlerName = "/x" + random().nextInt();
    assertSuccess(
        client,
        new V2Request.Builder("/c/test/config")
            .withMethod(SolrRequest.METHOD.POST)
            .withPayload(
                "{'create-requesthandler' : { 'name' : '"
                    + requestHandlerName
                    + "', 'class': 'org.apache.solr.handler.DumpRequestHandler' , 'startup' : 'lazy'}}")
            .build());

    assertSuccess(
        client,
        new V2Request.Builder("/collections/test").withMethod(SolrRequest.METHOD.DELETE).build());
    NamedList<Object> res = client.request(new V2Request.Builder("/collections").build());
  }

  public void testV2Forwarding() throws Exception {
    SolrClient client = cluster.getSolrClient();
    assertSuccess(
        client,
        new V2Request.Builder("/collections")
            .withMethod(SolrRequest.METHOD.POST)
            .withPayload(
                "{"
                    + "    \"name\" : \"v2forward\","
                    + "    \"numShards\" : 1,"
                    + "    \"replicationFactor\" : 1,"
                    + "    \"config\" : \"config\""
                    + "}")
            .build());

    ClusterState cs = cluster.getSolrClient().getClusterState();
    System.out.println("livenodes: " + cs.getLiveNodes());

    String[] node = new String[1];
    cs.getCollection("v2forward").forEachReplica((s, replica) -> node[0] = replica.getNodeName());

    // find a node that does not have a replica for this collection
    final String[] testNode = new String[1];
    cs.getLiveNodes()
        .forEach(
            s -> {
              if (!s.equals(node[0])) testNode[0] = s;
            });

    String testServer = cluster.getZkStateReader().getBaseUrlForNodeName(testNode[0]);
    V2Request v2r =
        new V2Request.Builder("/c/v2forward/_introspect")
            .withMethod(SolrRequest.METHOD.GET)
            .build();

    try (SolrClient client1 = new HttpSolrClient.Builder().withBaseSolrUrl(testServer).build()) {
      V2Response rsp = v2r.process(client1);
      assertEquals("0", rsp._getStr("responseHeader/status"));
    }
  }
}
