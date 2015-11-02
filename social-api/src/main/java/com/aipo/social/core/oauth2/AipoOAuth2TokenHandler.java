/*
 * Aipo is a groupware program developed by TOWN, Inc.
 * Copyright (C) 2004-2015 TOWN, Inc.
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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shindig.social.core.oauth2.OAuth2Code;
import org.apache.shindig.social.core.oauth2.OAuth2Exception;
import org.apache.shindig.social.core.oauth2.OAuth2NormalizedRequest;
import org.apache.shindig.social.core.oauth2.OAuth2NormalizedResponse;
import org.apache.shindig.social.core.oauth2.OAuth2Service;
import org.apache.shindig.social.core.oauth2.OAuth2TokenHandler;
import org.apache.shindig.social.core.oauth2.OAuth2Types.ErrorType;
import org.apache.shindig.social.core.oauth2.OAuth2Types.GrantType;
import org.apache.shindig.social.core.oauth2.OAuth2Types.TokenFormat;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;

/**
 * @see OAuth2TokenHandler
 */
public class AipoOAuth2TokenHandler {

  private final OAuth2Service service;

  private final long accessTokenExpires;

  /**
   * Constructs the token handler with the OAuth2Service.
   *
   * @param service
   *          is the service that will support this handler
   * @param accessTokenExpires
   */
  public AipoOAuth2TokenHandler(OAuth2Service service, long accessTokenExpires) {
    this.service = service;
    this.accessTokenExpires = accessTokenExpires;
  }

  /**
   * Handles an OAuth 2.0 request to the token endpoint.
   *
   * @param request
   *          is the servlet request object
   * @param response
   *          is the servlet response object
   * @return OAuth2NormalizedResponse encapsulates the request's response
   *
   * @throws ServletException
   * @throws IOException
   */
  public OAuth2NormalizedResponse handle(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    try {
      // normalize the request
      OAuth2NormalizedRequest normalizedReq =
        new OAuth2NormalizedRequest(request);

      // grant access token
      service.authenticateClient(normalizedReq);
      service.validateRequestForAccessToken(normalizedReq);
      OAuth2Code accessToken = service.grantAccessToken(normalizedReq);

      // send response
      OAuth2NormalizedResponse normalizedResp = new OAuth2NormalizedResponse();
      normalizedResp.setAccessToken(accessToken.getValue());
      if (normalizedReq.getGrantType().equals(
        GrantType.REFRESH_TOKEN.toString())) {
        normalizedResp.setRefreshToken(normalizedReq
          .get("refresh_token")
          .toString());
      } else {
        OAuth2Code refreshToken = service.grantRefreshToken(normalizedReq);
        normalizedResp.setRefreshToken(refreshToken.getValue());
      }
      normalizedResp.setTokenType(TokenFormat.BEARER.toString());
      normalizedResp.setExpiresIn((accessTokenExpires / 1000) + "");
      normalizedResp.setScope(listToString(accessToken.getScope()));
      normalizedResp.setStatus(HttpServletResponse.SC_OK);
      normalizedResp.setBodyReturned(true);
      if (normalizedReq.getState() != null) {
        normalizedResp.setState(normalizedReq.getState());
      }
      return normalizedResp;
    } catch (OAuth2Exception oae) {
      return oae.getNormalizedResponse();
    } catch (Throwable t) {
      t.printStackTrace();
      OAuth2NormalizedResponse error = new OAuth2NormalizedResponse();
      error.setError(ErrorType.SERVER_ERROR.toString());
      error.setErrorDescription("Server error occurred");
      error.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      error.setBodyReturned(true);
      return error;
    }
  }

  /**
   * Private utility to comma-delimit a list of Strings
   */
  @VisibleForTesting
  protected static String listToString(List<String> list) {
    if (list == null) {
      return "";
    }
    return Joiner.on(' ').join(list);
  }
}
