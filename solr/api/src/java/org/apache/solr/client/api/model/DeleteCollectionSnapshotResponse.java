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

package org.apache.solr.client.api.model;

import static org.apache.solr.client.api.model.Constants.COLLECTION;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/** The Response for {@link org.apache.solr.client.api.endpoint.CollectionSnapshotApis.Delete} */
public class DeleteCollectionSnapshotResponse extends AsyncJerseyResponse {
  @Schema(description = "The name of the collection.")
  @JsonProperty(COLLECTION)
  public String collection;

  @Schema(description = "The name of the snapshot to be deleted.")
  @JsonProperty("snapshot")
  public String snapshotName;

  @Schema(description = "A flag that treats the collName parameter as a collection alias.")
  @JsonProperty("followAliases")
  public boolean followAliases;
}
