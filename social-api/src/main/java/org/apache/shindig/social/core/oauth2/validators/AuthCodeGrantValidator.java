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
import org.apache.shindig.social.core.oauth2.OAuth2Code;
import org.apache.shindig.social.core.oauth2.OAuth2DataService;
import org.apache.shindig.social.core.oauth2.OAuth2Exception;
import org.apache.shindig.social.core.oauth2.OAuth2NormalizedRequest;
import org.apache.shindig.social.core.oauth2.OAuth2NormalizedResponse;
import org.apache.shindig.social.core.oauth2.OAuth2Client.Flow;
import org.apache.shindig.social.core.oauth2.OAuth2Types.ErrorType;

import com.google.inject.Inject;

public class AuthCodeGrantValidator implements OAuth2GrantValidator {

  private final OAuth2DataService service;

  @Inject
  public AuthCodeGrantValidator(OAuth2DataService service) {
    this.service = service;
  }

  @Override
  public String getGrantType() {
    return "authorization_code";
  }

  @Override
  public void validateRequest(OAuth2NormalizedRequest servletRequest)
      throws OAuth2Exception {
    OAuth2Client client = service.getClient(servletRequest.getClientId());
    if (client == null || client.getFlow() != Flow.AUTHORIZATION_CODE) {
      OAuth2NormalizedResponse resp = new OAuth2NormalizedResponse();
      resp.setError(ErrorType.INVALID_CLIENT.toString());
      resp.setErrorDescription("Invalid client");
      resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
      throw new OAuth2Exception(resp);
    }
    OAuth2Code authCode =
      service.getAuthorizationCode(servletRequest.getClientId(), servletRequest
        .getAuthorizationCode());
    if (authCode == null) {
      OAuth2NormalizedResponse response = new OAuth2NormalizedResponse();
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.setError(ErrorType.INVALID_GRANT.toString());
      response.setErrorDescription("Bad authorization code");
      response.setBodyReturned(true);
      throw new OAuth2Exception(response);
    }
    if (authCode.getRedirectURI() != null
      && !authCode.getRedirectURI().equals(servletRequest.getRedirectURI())) {
      OAuth2NormalizedResponse response = new OAuth2NormalizedResponse();
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.setError(ErrorType.INVALID_GRANT.toString());
      response
        .setErrorDescription("The redirect URI does not match the one used in the authorization request");
      response.setBodyReturned(true);
      throw new OAuth2Exception(response);
    }

    // ensure authorization code has not already been used
    if (authCode.getRelatedAccessToken() != null) {
      service.unregisterAccessToken(client.getId(), authCode
        .getRelatedAccessToken()
        .getValue());
      OAuth2NormalizedResponse response = new OAuth2NormalizedResponse();
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.setError(ErrorType.INVALID_GRANT.toString());
      response
        .setErrorDescription("The authorization code has already been used to generate an access token");
      response.setBodyReturned(true);
      throw new OAuth2Exception(response);
    }
  }
}
