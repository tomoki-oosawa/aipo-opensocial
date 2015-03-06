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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.shindig.auth.AipoOAuth2SecurityToken;
import org.apache.shindig.auth.AuthenticationHandler;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.logging.i18n.MessageKeys;
import org.apache.shindig.social.core.oauth2.OAuth2AuthenticationHandler;
import org.apache.shindig.social.core.oauth2.OAuth2Exception;
import org.apache.shindig.social.core.oauth2.OAuth2NormalizedRequest;
import org.apache.shindig.social.core.oauth2.OAuth2Service;

import com.google.inject.Inject;

/**
 * @see OAuth2AuthenticationHandler
 */
public class AipoOAuth2AuthenticationHandler implements AuthenticationHandler {

  // class name for logging purpose
  private static final String classname = AipoOAuth2AuthenticationHandler.class
    .getName();

  private static final Logger LOG = Logger.getLogger(
    classname,
    MessageKeys.MESSAGES);

  private final OAuth2Service store;

  @Override
  public String getName() {
    return "OAuth2";
  }

  @Inject
  public AipoOAuth2AuthenticationHandler(OAuth2Service store) {
    this.store = store;
  }

  /**
   * Only denies authentication when an invalid bearer token is received.
   * Unauthenticated requests can pass through to other AuthenticationHandlers.
   */
  @Override
  public SecurityToken getSecurityTokenFromRequest(HttpServletRequest request)
      throws InvalidAuthenticationException {

    OAuth2NormalizedRequest normalizedReq;
    try {
      normalizedReq = new OAuth2NormalizedRequest(request);
    } catch (OAuth2Exception oae) { // request failed to normalize
      LOG.logp(
        Level.WARNING,
        classname,
        "getSecurityTokenFromRequest",
        MessageKeys.INVALID_OAUTH);
      return null;
    }
    try {
      if (normalizedReq.getAccessToken() != null) {
        store.validateRequestForResource(normalizedReq, null);
        return createSecurityTokenForValidatedRequest(normalizedReq);
      }
    } catch (OAuth2Exception oae) {
      // TODO (Eric): process OAuth2Exception properly
      throw new InvalidAuthenticationException("Something went wrong: ", oae);
    }
    return null;
  }

  @Override
  public String getWWWAuthenticateHeader(String realm) {
    return String.format("Bearer realm=\"%s\"", realm);
  }

  /**
   * Return a security token for the request.
   *
   * The request was validated against the {@link OAuth2Service}.
   *
   * @param request
   * @return the security token for the request
   * @throws InvalidAuthenticationException
   *           if the token can not be created
   */
  protected SecurityToken createSecurityTokenForValidatedRequest(
      OAuth2NormalizedRequest request) throws InvalidAuthenticationException {
    // FIXME: ownerId, viewerIdを設定する
    return new AipoOAuth2SecurityToken("", "");
  }

}
