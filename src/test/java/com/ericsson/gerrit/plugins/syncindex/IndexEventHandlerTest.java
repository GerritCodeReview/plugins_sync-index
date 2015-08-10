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
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.reset;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.gerrit.extensions.events.ChangeIndexedListener.Event;

import com.ericsson.gerrit.plugins.syncindex.IndexEventHandler.SyncIndexTask;

import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Executor;

public class IndexEventHandlerTest extends EasyMockSupport {
  private static final String PLUGIN_NAME = "sync-index";
  private static final int CHANGE_ID = 1;

  private Event eventMock;
  private IndexEventHandler indexEventHandler;
  private Executor poolMock;
  private RestSession restClientMock;

  @Before
  public void setUpMocks() {
    eventMock = createNiceMock(Event.class);
    expect(eventMock.getChangeId()).andReturn(CHANGE_ID).anyTimes();
    poolMock = createMock(Executor.class);
    poolMock.execute(anyObject(SyncIndexTask.class));
    expectLastCall().andDelegateTo(MoreExecutors.directExecutor());
    restClientMock = createMock(RestSession.class);
    indexEventHandler =
        new IndexEventHandler(poolMock, PLUGIN_NAME, restClientMock);
  }

  @Test
  public void shouldIndexInRemoteOnChangeIndexedEvent() throws Exception {
    expect(restClientMock.index(CHANGE_ID)).andReturn(true);
    replayAll();
    indexEventHandler.onChangeIndexed(eventMock);
    verifyAll();
  }

  @Test
  public void shouldDeleteFromIndexInRemoteOnChangeDeletedEvent()
      throws Exception {
    expect(restClientMock.deleteFromIndex(CHANGE_ID)).andReturn(true);
    replayAll();
    indexEventHandler.onChangeDeleted(eventMock);
    verifyAll();
  }

  @Test
  public void shouldNotCallRemoteWhenEventIsForwarded() throws Exception {
    reset(poolMock);
    replayAll();
    Context.setForwardedEvent(true);
    indexEventHandler.onChangeIndexed(eventMock);
    indexEventHandler.onChangeDeleted(eventMock);
    Context.unsetForwardedEvent();
    verifyAll();
  }

  @Test
  public void duplicateEventOfAQueuedEventShouldGetDiscarded() {
    reset(poolMock);
    poolMock.execute(indexEventHandler.new SyncIndexTask(CHANGE_ID, false));
    expectLastCall().once();
    replayAll();
    indexEventHandler.onChangeIndexed(eventMock);
    indexEventHandler.onChangeIndexed(eventMock);
    verifyAll();
  }

  @Test
  public void testSyncIndexTaskToString() throws Exception {
    SyncIndexTask syncIndexTask =
        indexEventHandler.new SyncIndexTask(CHANGE_ID, false);
    assertThat(syncIndexTask.toString()).isEqualTo(
        String.format("[%s] Index change %s in target instance", PLUGIN_NAME,
            CHANGE_ID));
  }

  @Test
  public void testSyncIndexTaskHashCodeAndEquals() {
    SyncIndexTask task = indexEventHandler.new SyncIndexTask(CHANGE_ID, false);

    assertThat(task.equals(task)).isTrue();
    assertThat(task.hashCode()).isEqualTo(task.hashCode());

    SyncIndexTask identicalTask =
        indexEventHandler.new SyncIndexTask(CHANGE_ID, false);
    assertThat(task.equals(identicalTask)).isTrue();
    assertThat(task.hashCode()).isEqualTo(identicalTask.hashCode());

    assertThat(task.equals(null)).isFalse();
    assertThat(task.equals("test")).isFalse();
    assertThat(task.hashCode()).isNotEqualTo("test".hashCode());

    SyncIndexTask differentChangeIdTask =
        indexEventHandler.new SyncIndexTask(123, false);
    assertThat(task.equals(differentChangeIdTask)).isFalse();
    assertThat(task.hashCode()).isNotEqualTo(differentChangeIdTask.hashCode());

    SyncIndexTask removeTask =
        indexEventHandler.new SyncIndexTask(CHANGE_ID, true);
    assertThat(task.equals(removeTask)).isFalse();
    assertThat(task.hashCode()).isNotEqualTo(removeTask.hashCode());
  }
}
