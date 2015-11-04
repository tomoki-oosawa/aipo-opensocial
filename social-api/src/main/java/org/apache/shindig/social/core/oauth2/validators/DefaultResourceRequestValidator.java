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
package org.apache.shindig.social.core.oauth2.validators;

import javax.servlet.http.HttpServletResponse;

import org.apache.shindig.social.core.oauth2.OAuth2Code;
import org.apache.shindig.social.core.oauth2.OAuth2DataService;
import org.apache.shindig.social.core.oauth2.OAuth2Exception;
import org.apache.shindig.social.core.oauth2.OAuth2NormalizedRequest;
import org.apache.shindig.social.core.oauth2.OAuth2NormalizedResponse;
import org.apache.shindig.social.core.oauth2.OAuth2Types.ErrorType;

import com.google.inject.Inject;

public class DefaultResourceRequestValidator implements
    OAuth2ProtectedResourceValidator {

  private OAuth2DataService store = null;

  @Inject
  public DefaultResourceRequestValidator(OAuth2DataService store) {
    this.store = store;
  }

  @Override
  public void validateRequest(OAuth2NormalizedRequest req)
      throws OAuth2Exception {
    validateRequestForResource(req, null);

  }

  /**
   * TODO (Matt): implement scope handling.
   */
  @Override
  public void validateRequestForResource(OAuth2NormalizedRequest req,
      Object resourceRequest) throws OAuth2Exception {

    OAuth2Code token = store.getAccessToken(req.getAccessToken());
    if (token == null) {
      throwAccessDenied("Access token is invalid.");
    }
    if (token.getExpiration() > -1
      && token.getExpiration() < System.currentTimeMillis()) {
      throwAccessDenied("Access token has expired.");
    }
    if (resourceRequest != null) {
      // TODO (Matt): validate that requested resource is within scope
    }
  }

  // TODO(plindner): change this into a constructor or .create() on
  // OAuth2Exception
  private void throwAccessDenied(String msg) throws OAuth2Exception {
    OAuth2NormalizedResponse resp = new OAuth2NormalizedResponse();
    resp.setError(ErrorType.ACCESS_DENIED.toString());
    resp.setErrorDescription(msg);
    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
    throw new OAuth2Exception(resp);
  }
}
