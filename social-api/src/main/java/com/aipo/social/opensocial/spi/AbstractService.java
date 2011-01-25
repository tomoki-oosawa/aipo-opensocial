/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2011 Aimluck,Inc.
 * http://www.aipo.com/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aipo.social.opensocial.spi;

import javax.servlet.http.HttpServletResponse;

import org.apache.cayenne.access.DataContext;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.aipo.orm.Database;
import com.aipo.orm.model.security.TurbineUser;
import com.aipo.orm.service.TurbineUserService;
import com.google.inject.Inject;

/**
 * 
 */
public abstract class AbstractService {

  @Inject
  private TurbineUserService turbineUserSercice;

  private String orgId;

  private String viewerId;

  protected void setUp(SecurityToken token) {

    try {
      String viewer = token.getViewerId();
      String[] split = viewer.split(":");

      if (split.length != 2) {
        throw new RuntimeException();
      }

      orgId = split[0];
      viewerId = split[1];

      selectDataDomain(orgId);
    } catch (Throwable t) {
      throw new ProtocolException(
        HttpServletResponse.SC_BAD_REQUEST,
        "Org ID not recognized");
    }
    checkViewerExists(token);

  }

  protected void selectDataDomain(String orgId) throws Exception {
    DataContext dataContext = Database.createDataContext(orgId);
    DataContext.bindThreadObjectContext(dataContext);
  }

  protected String getOrgId(SecurityToken token) {
    if (orgId == null) {
      setUp(token);
    }
    return orgId;
  }

  protected String getViewerId(SecurityToken token) {
    if (viewerId == null) {
      setUp(token);
    }
    return viewerId;
  }

  protected String getUserId(UserId userId, SecurityToken token) {
    String userIdStr = userId.getUserId(token);
    String[] split = userIdStr.split(":");

    if (split.length != 2) {
      throw new RuntimeException();
    }

    String currentOrgId = split[0];
    String currentUserId = split[1];

    if (!checkOrgId(currentOrgId, token)) {
      throw new ProtocolException(
        HttpServletResponse.SC_BAD_REQUEST,
        "Org ID not recognized");
    }

    return currentUserId;

  }

  protected boolean checkOrgId(String orgId, SecurityToken token) {
    if (orgId == null || orgId == "") {
      return false;
    }
    return orgId.equals(getOrgId(token));
  }

  protected void checkViewerExists(SecurityToken token)
      throws ProtocolException {
    String viewerId = getViewerId(token);
    boolean result = false;
    try {
      TurbineUser user = turbineUserSercice.findByUsername(viewerId);
      result = user != null;
    } catch (Throwable t) {
      result = false;
    }
    if (!result) {
      throw new ProtocolException(
        HttpServletResponse.SC_BAD_REQUEST,
        "Viewer ID not recognized");
    }
  }
}
