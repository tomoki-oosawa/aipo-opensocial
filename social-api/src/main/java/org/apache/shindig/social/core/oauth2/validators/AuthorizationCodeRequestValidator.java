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

import org.apache.shindig.social.core.oauth2.OAuth2Client;
import org.apache.shindig.social.core.oauth2.OAuth2DataService;
import org.apache.shindig.social.core.oauth2.OAuth2Exception;
import org.apache.shindig.social.core.oauth2.OAuth2NormalizedRequest;
import org.apache.shindig.social.core.oauth2.OAuth2NormalizedResponse;
import org.apache.shindig.social.core.oauth2.OAuth2Types.ErrorType;

import com.google.inject.Inject;

public class AuthorizationCodeRequestValidator implements
    OAuth2RequestValidator {

  private OAuth2DataService store = null;

  @Inject
  public AuthorizationCodeRequestValidator(OAuth2DataService store) {
    this.store = store;
  }

  @Override
  public void validateRequest(OAuth2NormalizedRequest req)
      throws OAuth2Exception {

    OAuth2Client client = store.getClient(req.getClientId());
    if (client == null) {
      OAuth2NormalizedResponse resp = new OAuth2NormalizedResponse();
      resp.setError(ErrorType.INVALID_REQUEST.toString());
      resp.setErrorDescription("The client is invalid or not registered");
      resp.setBodyReturned(true);
      resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
      throw new OAuth2Exception(resp);
    }
    String storedURI = client.getRedirectURI();
    if (storedURI == null && req.getRedirectURI() == null) {
      OAuth2NormalizedResponse resp = new OAuth2NormalizedResponse();
      resp.setError(ErrorType.INVALID_REQUEST.toString());
      resp
        .setErrorDescription("No redirect_uri registered or received in request");
      resp.setBodyReturned(true);
      resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
      throw new OAuth2Exception(resp);
    }
    if (req.getRedirectURI() != null && storedURI != null) {
      if (!req.getRedirectURI().equals(storedURI)) {
        OAuth2NormalizedResponse resp = new OAuth2NormalizedResponse();
        resp.setError(ErrorType.INVALID_REQUEST.toString());
        resp
          .setErrorDescription("Redirect URI does not match the one registered for this client");
        resp.setBodyReturned(true);
        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        throw new OAuth2Exception(resp);
      }
    }
    req.getHttpServletRequest().setAttribute("com.aipo.OAuth2Client", client);
  }
}
