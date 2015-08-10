// Copyright (C) 2016 Ericsson
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

import static org.easymock.EasyMock.expect;

import com.google.gerrit.reviewdb.server.ReviewDb;
import com.google.gerrit.server.ChangeUtil;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.index.ChangeIndexer;
import com.google.gerrit.server.project.ChangeControl;
import com.google.gerrit.server.query.change.ChangeData.Factory;
import com.google.gwtorm.client.KeyUtil;
import com.google.gwtorm.server.OrmException;
import com.google.gwtorm.server.SchemaFactory;
import com.google.gwtorm.server.StandardKeyEncoder;
import com.google.inject.Provider;

import org.easymock.EasyMockSupport;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SyncIndexRestApiServletTest extends EasyMockSupport {
  private static final String CHANGE_NUMBER = "1";
  private static final String INDEX = "index";
  private static final String DELETE = "delete";

  private SyncIndexRestApiServlet syncIndexRestApiServlet;
  private HttpServletRequest req;
  private HttpServletResponse rsp;

  @BeforeClass
  public static void setup() {
    KeyUtil.setEncoderImpl(new StandardKeyEncoder());
  }

  @Test
  public void testDoPostIndex() throws Exception {
    setupMocks(false, INDEX, false);
    verify();
  }

  @Test
  public void testDoPostDelete() throws Exception {
    setupMocks(false, DELETE, false);
    verify();
  }

  @Test
  public void testDoPostChangesIsEmpty() throws Exception {
    setupMocks(true, INDEX, false);
    verify();
  }

  @Test
  public void testDoPostSchemaThrowsOrmException() throws Exception {
    setupMocks(false, INDEX, true);
    verify();
  }

  @SuppressWarnings("unchecked")
  private void setupMocks(boolean emptyChanges, String operation,
      boolean exception) throws OrmException {
    Factory changeDataFactory = createNiceMock(Factory.class);
    ChangeUtil changeUtil = createNiceMock(ChangeUtil.class);
    ChangeIndexer indexer = createNiceMock(ChangeIndexer.class);
    SchemaFactory<ReviewDb> schemaFactory = createNiceMock(SchemaFactory.class);
    Provider<CurrentUser> userProvider = createMock(Provider.class);
    CurrentUser currentUser = createNiceMock(CurrentUser.class);
    req = createNiceMock(HttpServletRequest.class);
    rsp = createNiceMock(HttpServletResponse.class);

    List<ChangeControl> changeControls = new ArrayList<>();
    if (!emptyChanges) {
      ChangeControl changeCtrl = createNiceMock(ChangeControl.class);
      changeControls.add(changeCtrl);
    }

    if (operation.equals(INDEX)) {
      expect(userProvider.get()).andReturn(currentUser);
      expect(changeUtil.findChanges(CHANGE_NUMBER, currentUser))
          .andReturn(changeControls);
    }
    expect(req.getPathInfo()).andReturn("/" + operation + "/" + CHANGE_NUMBER);
    if (exception) {
      expect(schemaFactory.open()).andThrow(new OrmException(""));
    }
    syncIndexRestApiServlet = new SyncIndexRestApiServlet(changeDataFactory,
        indexer, changeUtil, schemaFactory, userProvider);
    replayAll();
  }

  private void verify() throws IOException, ServletException {
    syncIndexRestApiServlet.doPost(req, rsp);
    verifyAll();
  }
}
