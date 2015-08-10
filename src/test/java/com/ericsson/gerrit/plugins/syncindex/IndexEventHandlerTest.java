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

import com.google.gerrit.extensions.events.ChangeIndexedListener.Event;

import org.easymock.EasyMockSupport;
import org.junit.Test;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public class IndexEventHandlerTest extends EasyMockSupport {
  private static final String PLUGIN_NAME = "sync-index";

  private Event eventMock;
  private IndexEventHandler indexEventHandler;
  private ScheduledThreadPoolExecutor pool;
  private RestSession restClient;

  @Test
  public void testChangeIndexedEventHandler() throws Exception {
    setUpMocks(true);
    resetAll();
    expect(restClient.index(eventMock)).andReturn(true);
    replayAll();
    indexEventHandler.onChangeIndexed(eventMock);
    verifyAll();
  }

  @Test
  public void testChangeDeletedEventHandler() throws Exception {
    setUpMocks(true);
    resetAll();
    expect(restClient.deleteFromIndex(eventMock)).andReturn(true);
    replayAll();
    indexEventHandler.onChangeDeleted(eventMock);
    verifyAll();
  }

  @Test
  public void testIndexEventHandlerIsForwarded() throws Exception {
    setUpMocks(false);
    Context.setForwardedEvent(true);
    indexEventHandler.onChangeIndexed(eventMock);
    indexEventHandler.onChangeDeleted(eventMock);
    Context.unsetForwardedEvent();
    verifyAll();
  }

  private void setUpMocks(boolean mockRestClient) {
    eventMock = createNiceMock(Event.class);
    pool = new PoolMock(1);
    if (mockRestClient) {
      restClient = createMock(RestSession.class);
    } else {
      restClient = null;
    }
    replayAll();
    indexEventHandler = new IndexEventHandler(pool, PLUGIN_NAME, restClient);
  }

  private class PoolMock extends ScheduledThreadPoolExecutor {
    PoolMock(int corePoolSize) {
      super(corePoolSize);
    }

    @Override
    public void execute(Runnable command) {
      assertThat(command.toString()).isEqualTo(
          PLUGIN_NAME + " - Index change in target instance");
      command.run();
    }
  }
}
