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
package com.aipo.social.core.oauth2.validators;

import javax.servlet.http.HttpServletResponse;

import org.apache.shindig.social.core.oauth2.AipoOAuth2Code;
import org.apache.shindig.social.core.oauth2.OAuth2Code;
import org.apache.shindig.social.core.oauth2.OAuth2DataService;
import org.apache.shindig.social.core.oauth2.OAuth2Exception;
import org.apache.shindig.social.core.oauth2.OAuth2NormalizedRequest;
import org.apache.shindig.social.core.oauth2.OAuth2NormalizedResponse;
import org.apache.shindig.social.core.oauth2.OAuth2Types.ErrorType;
import org.apache.shindig.social.core.oauth2.validators.OAuth2GrantValidator;

/**
 *
 */
public class RefreshTokenGrantValidator implements OAuth2GrantValidator {

  protected final OAuth2DataService store;

  public RefreshTokenGrantValidator(OAuth2DataService store) {
    this.store = store;

  }

  /**
   * @param req
   * @throws OAuth2Exception
   */
  @Override
  public void validateRequest(OAuth2NormalizedRequest req)
      throws OAuth2Exception {
    String refreshToken = (String) req.get("refresh_token");
    if (refreshToken == null) {
      throwError(ErrorType.INVALID_REQUEST, "refresh_token is required");
    }
    OAuth2Code code = store.getRefreshToken(refreshToken);
    if (code == null) {
      throwError(ErrorType.INVALID_GRANT, "Invalid or expired token");
    }
    if (code.getExpiration() > 0
      && code.getExpiration() < System.currentTimeMillis()) {
      throwError(ErrorType.INVALID_GRANT, "Invalid or expired token");
    }
    if (code instanceof AipoOAuth2Code) {
      AipoOAuth2Code oauth2Code = (AipoOAuth2Code) code;
      String userId = oauth2Code.getUserId();
      String[] split = userId.split(":");
      req.put("orgId", split[0]);
      req.put("username", split[1]);
      StringBuilder scopes = new StringBuilder();
      if (oauth2Code.getScope() != null && oauth2Code.getScope().size() > 0) {
        boolean isFirst = true;
        for (String scope : oauth2Code.getScope()) {
          if (!isFirst) {
            scopes.append(" ");
          } else {
            isFirst = false;
          }
          scopes.append(scope);
        }
        req.put("scope", scopes.toString());
      }
    } else {
      throwError(ErrorType.INVALID_GRANT, "Invalid or expired token");
    }

  }

  /**
   * @return
   */
  @Override
  public String getGrantType() {
    return "refresh_token";
  }

  protected final void throwError(ErrorType errorType, String msg)
      throws OAuth2Exception {
    OAuth2NormalizedResponse response = new OAuth2NormalizedResponse();
    response.setError(errorType.toString());
    response.setErrorDescription(msg);
    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    response.setBodyReturned(true);
    throw new OAuth2Exception(response);
  }
}
