/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2011 Aimluck,Inc.
 * http://www.aipo.com
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

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletResponse;

import org.apache.cayenne.access.DataContext;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.aipo.orm.Database;
import com.aipo.orm.model.security.TurbineUser;
import com.aipo.orm.service.TurbineUserDbService;
import com.google.inject.Inject;

/**
 * 
 */
public abstract class AbstractService {

  @Inject
  private TurbineUserDbService turbineUserDbService;

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

  protected String convertUserId(String username, SecurityToken token) {
    return new StringBuilder(getOrgId(token))
      .append(":")
      .append(username)
      .toString();
  }

  protected String getUserId(String userId, SecurityToken token) {
    String[] split = userId.split(":");

    if (split.length != 2) {
      throw new RuntimeException();
    }

    String currentOrgId = split[0];
    String currentUserId = split[1];

    checkSameOrgId(currentOrgId, token);

    return currentUserId;

  }

  protected String getUserId(UserId userId, SecurityToken token) {
    return getUserId(userId.getUserId(token), token);
  }

  /**
   * 指定したデータベース名が、現在選択しているデータベース名と一致しているかチェックします。
   * 
   * @param orgId
   * @param token
   */
  protected void checkSameOrgId(String orgId, SecurityToken token) {
    if (orgId != null && orgId != "") {
      if (orgId.equals(getOrgId(token))) {
        return;
      }
    }
    throw new ProtocolException(
      HttpServletResponse.SC_BAD_REQUEST,
      "Org ID not recognized");
  }

  /**
   * Viewer が存在するかどうかチェックします。
   * 
   * @param token
   * @throws ProtocolException
   */
  protected void checkViewerExists(SecurityToken token)
      throws ProtocolException {
    String viewerId = getViewerId(token);
    boolean result = false;
    try {
      TurbineUser user = turbineUserDbService.findByUsername(viewerId);
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

  /**
   * 指定されたユーザーが Viewer と一致しているかチェックします。
   * 
   * @param userId
   * @param token
   * @throws ProtocolException
   */
  protected void checkSameViewer(UserId userId, SecurityToken token)
      throws ProtocolException {
    if (!getViewerId(token).equals(getUserId(userId, token))) {
      throw new ProtocolException(
        HttpServletResponse.SC_BAD_REQUEST,
        "Access not dennied.");
    }
  }

  /**
   * 指定されたアプリが現在利用しているアプリと一致しているかチェックします。
   * 
   * @param appId
   * @param token
   */
  protected void checkSameAppId(String appId, SecurityToken token) {
    if (appId != null && appId != "") {
      if (appId.equals(token.getAppId())) {
        return;
      }
    }
    throw new ProtocolException(
      HttpServletResponse.SC_BAD_REQUEST,
      "Access not dennied.");
  }

  protected void checkInputRange(String input, int min, int max) {
    if (input == null
      || (input != null && input.length() < min)
      || (input != null && input.length() > max)) {
      throw new ProtocolException(
        HttpServletResponse.SC_BAD_REQUEST,
        "Validate error.");
    }
  }

  protected void checkInputByte(String input, int min, int max) {
    if (input == null
      || (input != null && byteLength(input) < min)
      || (input != null && byteLength(input) > max)) {
      throw new ProtocolException(
        HttpServletResponse.SC_BAD_REQUEST,
        "Validate error.");
    }
  }

  private int byteLength(String value) {
    int len = 0;
    if (value == null) {
      return len;
    }

    try {
      len = (value.getBytes("utf-8")).length;
    } catch (UnsupportedEncodingException ex) {
      len = 0;
    }

    return len;
  }
}
