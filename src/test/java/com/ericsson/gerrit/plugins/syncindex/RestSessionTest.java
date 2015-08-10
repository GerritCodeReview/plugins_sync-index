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

import com.google.common.base.Joiner;

import com.ericsson.gerrit.plugins.syncindex.IndexResponseHandler.IndexResult;

import org.easymock.EasyMockSupport;
import org.junit.Test;

import java.io.IOException;

public class RestSessionTest extends EasyMockSupport {
  private static final int CHANGE_NUMBER = 1;
  private static final String DELETE_OP = "delete";
  private static final String INDEX_OP = "index";
  private static final String PLUGIN_NAME = "sync-index";

  private RestSession restClient;

  @Test
  public void testIndexChangeOK() throws Exception {
    setUpMocks(INDEX_OP, true, "", false);
    assertThat(restClient.index(CHANGE_NUMBER)).isTrue();
  }

  @Test
  public void testIndexChangeFailed() throws Exception {
    setUpMocks(INDEX_OP, false, "Error", false);
    assertThat(restClient.index(CHANGE_NUMBER)).isFalse();
  }

  @Test
  public void testIndexChangeThrowsException() throws Exception {
    setUpMocks(INDEX_OP, false, "Exception", true);
    assertThat(restClient.index(CHANGE_NUMBER)).isFalse();
  }

  @Test
  public void testChangeDeletedFromIndexOK() throws Exception {
    setUpMocks(DELETE_OP, true, "", false);
    assertThat(restClient.deleteFromIndex(CHANGE_NUMBER)).isTrue();
  }

  @Test
  public void testChangeDeletedFromIndexFailed() throws Exception {
    setUpMocks(DELETE_OP, false, "Error", false);
    assertThat(restClient.deleteFromIndex(CHANGE_NUMBER)).isFalse();
  }

  @Test
  public void testChangeDeletedFromThrowsException() throws Exception {
    setUpMocks(DELETE_OP, false, "Exception", true);
    assertThat(restClient.deleteFromIndex(CHANGE_NUMBER)).isFalse();
  }

  private void setUpMocks(String operation, boolean ok, String msg,
      boolean exception) throws Exception {
    String request =
        Joiner.on("/").join("/plugins", PLUGIN_NAME, operation, CHANGE_NUMBER);
    HttpSession httpSession = createNiceMock(HttpSession.class);
    if (exception) {
      expect(httpSession.post(request)).andThrow(new IOException());
    } else {
      IndexResult result = new IndexResult(ok, msg);
      expect(httpSession.post(request)).andReturn(result);
    }
    restClient = new RestSession(httpSession, PLUGIN_NAME);
    replayAll();
  }
}
