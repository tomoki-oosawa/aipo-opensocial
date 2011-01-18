/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2010 Aimluck,Inc.
 * http://aipostyle.com/
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

/**
 * 
 */
public abstract class AbstractService {

  private String orgId;

  private String viewerId;

  protected void setUp(SecurityToken token) {

    try {
      String viewerId = token.getViewerId();
      String[] split = viewerId.split(":");

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
}
