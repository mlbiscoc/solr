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
package org.apache.solr.common.cloud;

import java.util.ArrayList;
import java.util.List;
import org.apache.curator.framework.AuthInfo;

public class DefaultZkCredentialsProvider implements ZkCredentialsProvider {

  private volatile List<AuthInfo> zkCredentials;
  protected ZkCredentialsInjector zkCredentialsInjector;

  public DefaultZkCredentialsProvider() {
    this(new DefaultZkCredentialsInjector(), null);
  }

  public DefaultZkCredentialsProvider(List<AuthInfo> zkCredentials) {
    this(new DefaultZkCredentialsInjector(), zkCredentials);
  }

  public DefaultZkCredentialsProvider(ZkCredentialsInjector zkCredentialsInjector) {
    this(zkCredentialsInjector, null);
  }

  public DefaultZkCredentialsProvider(
      ZkCredentialsInjector zkCredentialsInjector, List<AuthInfo> zkCredentials) {
    this.zkCredentialsInjector = zkCredentialsInjector;
    this.zkCredentials = zkCredentials;
  }

  @Override
  public void setZkCredentialsInjector(ZkCredentialsInjector zkCredentialsInjector) {
    this.zkCredentialsInjector = zkCredentialsInjector;
  }

  @Override
  public List<AuthInfo> getCredentials() {
    if (zkCredentials == null) {
      synchronized (this) {
        if (zkCredentials == null) zkCredentials = createCredentials();
      }
    }
    return zkCredentials;
  }

  protected List<AuthInfo> createCredentials() {
    return new ArrayList<>();
  }
}
