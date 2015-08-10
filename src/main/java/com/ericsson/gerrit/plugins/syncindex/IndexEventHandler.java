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

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.extensions.events.ChangeIndexedListener;
import com.google.inject.Inject;

import java.util.concurrent.ScheduledThreadPoolExecutor;

class IndexEventHandler implements ChangeIndexedListener {
  private final ScheduledThreadPoolExecutor executor;
  private final RestSession restClient;
  private final String pluginName;

  @Inject
  IndexEventHandler(@SyncIndexExecutor ScheduledThreadPoolExecutor executor,
      @PluginName String pluginName,
      RestSession restClient) {
    this.restClient = restClient;
    this.executor = executor;
    this.pluginName = pluginName;
  }

  @Override
  public void onChangeIndexed(Event event) {
    executeIndexTask(event, false);
  }

  @Override
  public void onChangeDeleted(Event event) {
    executeIndexTask(event, true);
  }

  private void executeIndexTask(Event event, boolean removed) {
    if (!Context.isForwardedEvent()) {
      executor.execute(new SyncIndexTask(event, removed));
    }
  }

  class SyncIndexTask implements Runnable {
    private ChangeIndexedListener.Event event;
    private boolean removed;

    SyncIndexTask(Event event, boolean removed) {
      this.event = event;
      this.removed = removed;
    }

    @Override
    public void run() {
      if (removed) {
        restClient.deleteFromIndex(event);
      } else {
        restClient.index(event);
      }
    }

    @Override
    public String toString() {
      return pluginName + " - Index change in target instance";
    }
  }
}
