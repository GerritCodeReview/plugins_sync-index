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

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;

import com.google.common.base.Splitter;
import com.google.gerrit.extensions.restapi.ResourceNotFoundException;
import com.google.gerrit.reviewdb.client.Change;
import com.google.gerrit.reviewdb.server.ReviewDb;
import com.google.gerrit.server.ChangeUtil;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.index.ChangeIndexer;
import com.google.gerrit.server.project.ChangeControl;
import com.google.gerrit.server.query.change.ChangeData;
import com.google.gwtorm.server.OrmException;
import com.google.gwtorm.server.SchemaFactory;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
class SyncIndexRestApiServlet extends HttpServlet {
  private static final int OPERATION_INDEX = 1;
  private static final int ID_INDEX = 2;
  private static final long serialVersionUID = -1L;
  private static final Logger logger =
      LoggerFactory.getLogger(SyncIndexRestApiServlet.class);

  private final ChangeData.Factory changeDataFactory;
  private final ChangeIndexer indexer;
  private final ChangeUtil changeUtil;
  private final SchemaFactory<ReviewDb> schemaFactory;
  private final Provider<CurrentUser> user;

  @Inject
  SyncIndexRestApiServlet(ChangeData.Factory changeDataFactory,
      ChangeIndexer indexer,
      ChangeUtil changeUtil,
      SchemaFactory<ReviewDb> schemaFactory,
      Provider<CurrentUser> user) {
    this.indexer = indexer;
    this.changeUtil = changeUtil;
    this.changeDataFactory = changeDataFactory;
    this.schemaFactory = schemaFactory;
    this.user = user;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse rsp)
      throws IOException, ServletException {
    rsp.setContentType("text/plain");
    rsp.setCharacterEncoding("UTF-8");
    try {
      List<String> params = Splitter.on('/').splitToList(req.getPathInfo());
      String operation = params.get(OPERATION_INDEX);
      String changeId = params.get(ID_INDEX);

      Change.Id id = Change.Id.parse(changeId);
      Context.setForwardedEvent(true);
      if ("index".equals(operation)) {
        verifyChange(changeId);
        index(id);
      } else if ("delete".equals(operation)) {
        indexer.delete(id);
      }
      rsp.setStatus(SC_NO_CONTENT);
    } catch (IOException e) {
      rsp.sendError(SC_BAD_REQUEST, e.getMessage());
      logger.error("Unable to update index", e);
    } catch (ResourceNotFoundException | OrmException e) {
      rsp.sendError(SC_NOT_FOUND, "Change not found\n");
      logger.debug("Error trying to find a change ", e);
    } finally {
      Context.unsetForwardedEvent();
    }
  }

  private void verifyChange(String changeId) throws OrmException,
      ResourceNotFoundException {
    List<ChangeControl> chgCtls = changeUtil.findChanges(changeId, user.get());
    if (chgCtls.isEmpty()) {
      throw new ResourceNotFoundException(changeId);
    }
  }

  private void index(Change.Id id) throws IOException {
    try (ReviewDb db = schemaFactory.open()) {
      indexer.index(changeDataFactory.create(db, id));
    } catch (OrmException e) {
      throw new IOException(e);
    }
  }
}
