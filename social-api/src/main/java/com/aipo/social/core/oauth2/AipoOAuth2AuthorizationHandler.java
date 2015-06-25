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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shindig.social.core.oauth2.OAuth2AuthorizationHandler;
import org.apache.shindig.social.core.oauth2.OAuth2Code;
import org.apache.shindig.social.core.oauth2.OAuth2Exception;
import org.apache.shindig.social.core.oauth2.OAuth2NormalizedRequest;
import org.apache.shindig.social.core.oauth2.OAuth2NormalizedResponse;
import org.apache.shindig.social.core.oauth2.OAuth2Service;
import org.apache.shindig.social.core.oauth2.OAuth2Types.ErrorType;
import org.apache.shindig.social.core.oauth2.OAuth2Types.TokenFormat;
import org.apache.shindig.social.core.oauth2.OAuth2Utils;

/**
 * @see OAuth2AuthorizationHandler
 */
public class AipoOAuth2AuthorizationHandler {

  private final OAuth2Service service;

  public AipoOAuth2AuthorizationHandler(OAuth2Service service) {
    this.service = service;
  }

  /**
   * Handles an OAuth 2.0 authorization request.
   *
   * @param request
   *          is the original request
   * @param response
   *          is the response of the request
   * @return OAuth2NormalizedResponse represents the OAuth 2.0 response
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

      // process request according to flow
      OAuth2NormalizedResponse normalizedResp = new OAuth2NormalizedResponse();
      if (normalizedReq.getResponseType() != null) {
        switch (normalizedReq.getEnumeratedResponseType()) {
          case CODE:
            // authorization code flow
            service.validateRequestForAuthCode(normalizedReq);
            OAuth2Code authCode = service.grantAuthorizationCode(normalizedReq);

            // send response
            normalizedResp.setCode(authCode.getValue());
            if (normalizedReq.getState() != null) {
              normalizedResp.setState(normalizedReq.getState());
            }
            normalizedResp.setHeader("Location", OAuth2Utils.buildUrl(authCode
              .getRedirectURI(), normalizedResp.getResponseParameters(), null));
            normalizedResp.setStatus(HttpServletResponse.SC_FOUND);
            normalizedResp.setBodyReturned(false);
            return normalizedResp;
          case TOKEN:
            // implicit flow
            service.validateRequestForAccessToken(normalizedReq);
            OAuth2Code accessToken = service.grantAccessToken(normalizedReq);
            OAuth2Code refreshToken = service.grantRefreshToken(normalizedReq);

            // send response
            normalizedResp.setAccessToken(accessToken.getValue());
            normalizedResp.setTokenType(TokenFormat.BEARER.toString());
            normalizedResp.setExpiresIn((accessToken.getExpiration() - System
              .currentTimeMillis())
              + "");
            normalizedResp.setRefreshToken(refreshToken.getValue());
            if (normalizedReq.getState() != null) {
              normalizedResp.setState(normalizedReq.getState());
            }
            normalizedResp.setHeader("Location", OAuth2Utils.buildUrl(
              accessToken.getRedirectURI(),
              null,
              normalizedResp.getResponseParameters()));
            normalizedResp.setStatus(HttpServletResponse.SC_FOUND);
            normalizedResp.setBodyReturned(false);
            return normalizedResp;
          default:
            OAuth2NormalizedResponse resp = new OAuth2NormalizedResponse();
            resp.setError(ErrorType.UNSUPPORTED_RESPONSE_TYPE.toString());
            resp.setErrorDescription("Unsupported response type");
            resp.setStatus(HttpServletResponse.SC_FOUND);
            resp.setBodyReturned(false);
            throw new OAuth2Exception(resp);
        }
      } else {
        OAuth2NormalizedResponse resp = new OAuth2NormalizedResponse();
        resp.setError(ErrorType.UNSUPPORTED_RESPONSE_TYPE.toString());
        resp.setErrorDescription("Unsupported response type");
        resp.setStatus(HttpServletResponse.SC_FOUND);
        resp.setBodyReturned(false);
        throw new OAuth2Exception(resp);
      }
    } catch (OAuth2Exception oae) {
      return oae.getNormalizedResponse();
    }
  }
}
