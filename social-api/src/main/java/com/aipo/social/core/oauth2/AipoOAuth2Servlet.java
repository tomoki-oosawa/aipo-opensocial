/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2015 Aimluck,Inc.
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

package com.aipo.social.core.oauth2;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.shindig.common.logging.i18n.MessageKeys;
import org.apache.shindig.common.servlet.HttpUtil;
import org.apache.shindig.common.servlet.InjectedServlet;
import org.apache.shindig.social.core.oauth2.OAuth2NormalizedResponse;
import org.apache.shindig.social.core.oauth2.OAuth2Service;
import org.apache.shindig.social.core.oauth2.OAuth2Servlet;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Main servlet to catch OAuth 2.0 requests.
 */
public class AipoOAuth2Servlet extends InjectedServlet {

  private static final String AUTHORIZE = "authorize";

  private static final String TOKEN = "token";

  private static final long serialVersionUID = -4257719224664564922L;

  private static AipoOAuth2AuthorizationHandler authorizationHandler;

  private static AipoOAuth2TokenHandler tokenHandler;

  // class name for logging purpose
  private static final String classname = OAuth2Servlet.class.getName();

  private static final Logger LOG = Logger.getLogger(
    classname,
    MessageKeys.MESSAGES);

  @Inject
  public void setOAuth2Service(OAuth2Service oauthService,
      @Named("shindig.oauth2.accessTokenExpiration") long accessTokenExpires) {
    authorizationHandler =
      new AipoOAuth2AuthorizationHandler(oauthService, accessTokenExpires);
    tokenHandler = new AipoOAuth2TokenHandler(oauthService, accessTokenExpires);
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    HttpUtil.setNoCache(response);
    String path = request.getPathInfo();
    if (path.endsWith(AUTHORIZE)) {
      sendOAuth2Response(response, authorizationHandler.handle(
        request,
        response));
    } else if (path.endsWith(TOKEN)) {
      // token endpoint must use POST method
      response.sendError(
        HttpServletResponse.SC_METHOD_NOT_ALLOWED,
        "The client MUST use the HTTP \"POST\" method "
          + "when making access token requests.");
    } else {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown URL");
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String path = request.getPathInfo();
    if (path.endsWith(TOKEN)) {
      HttpUtil.setNoCache(response);
      sendOAuth2Response(response, tokenHandler.handle(request, response));
    } else {
      // authorization endpoint must support GET method and may support POST as
      // well
      doGet(request, response);
    }
  }

  /**
   * Sends an OAuth 2.0 response based on an OAuth2NormalizedResponse object.
   *
   * @param servletResp
   *          is the servlet's response object
   * @param normalizedResp
   *          maintains the headers and body fields to respond with
   */
  private void sendOAuth2Response(HttpServletResponse servletResp,
      OAuth2NormalizedResponse normalizedResp) {
    // set status
    servletResp.setStatus(normalizedResp.getStatus());

    // set body parameters
    Map<String, String> respParams = normalizedResp.getResponseParameters();
    if (normalizedResp.isBodyReturned() && respParams != null) {
      PrintWriter out = null;
      try {
        servletResp.setHeader("Content-Type", "application/json");
        out = servletResp.getWriter();
        out.println(new JSONObject(respParams).toString());
        out.flush();
      } catch (IOException e) {
        LOG.logp(
          Level.WARNING,
          classname,
          "getBodyAsString",
          MessageKeys.INVALID_OAUTH,
          e);
        throw new RuntimeException(e);
      } finally {
        IOUtils.closeQuietly(out);
      }
    }

    // set headers
    Map<String, String> headers = normalizedResp.getHeaders();
    if (headers != null) {
      for (String key : headers.keySet()) {
        servletResp.setHeader(key, headers.get(key));
      }
    }
  }
}
