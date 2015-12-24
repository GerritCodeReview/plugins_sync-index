// Copyright (C) 2015 Ericsson
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.ericsson.gerrit.plugins.syncindex;

import static com.google.common.truth.Truth.assertThat;
import static org.easymock.EasyMock.expect;

import org.apache.http.impl.client.CloseableHttpClient;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

public class HttpClientProviderTest extends EasyMockSupport {
  private static final int TIME_INTERVAL = 1000;
  private static final String EMPTY = "";

  private Configuration config;
  private HttpClientProvider httpClientProvider;

  @Before
  public void setUp() throws Exception {
    config = createNiceMock(Configuration.class);
    expect(config.getUrl()).andReturn(EMPTY);
    expect(config.getUser()).andReturn(EMPTY);
    expect(config.getPassword()).andReturn(EMPTY);
    expect(config.getMaxTries()).andReturn(1);
    expect(config.getConnectionTimeout()).andReturn(TIME_INTERVAL);
    expect(config.getSocketTimeout()).andReturn(TIME_INTERVAL);
    expect(config.getRetryInterval()).andReturn(TIME_INTERVAL);
    replayAll();
    httpClientProvider = new HttpClientProvider(config);
  }

  @Test
  public void testGet() throws Exception {
    CloseableHttpClient httpClient1 = httpClientProvider.get();
    assertThat(httpClient1).isNotNull();
    CloseableHttpClient httpClient2 = httpClientProvider.get();
    assertThat(httpClient1).isEqualTo(httpClient2);
  }

}
