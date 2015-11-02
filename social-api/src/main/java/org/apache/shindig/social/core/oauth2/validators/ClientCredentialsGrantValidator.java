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
import org.apache.shindig.social.core.oauth2.OAuth2Client.ClientType;
import org.apache.shindig.social.core.oauth2.OAuth2Client.Flow;
import org.apache.shindig.social.core.oauth2.OAuth2Types.ErrorType;

import com.google.inject.Inject;

public class ClientCredentialsGrantValidator implements OAuth2GrantValidator {

  private OAuth2DataService service;

  @Inject
  public ClientCredentialsGrantValidator(OAuth2DataService service) {
    this.service = service;
  }

  public void setOAuth2DataService(OAuth2DataService service) {
    this.service = service;
  }

  @Override
  public String getGrantType() {
    return "client_credentials";
  }

  @Override
  public void validateRequest(OAuth2NormalizedRequest req)
      throws OAuth2Exception {
    OAuth2Client cl = service.getClient(req.getClientId());
    if (cl == null || cl.getFlow() != Flow.CLIENT_CREDENTIALS) {
      throwAccessDenied("Bad client id or password");
    }
    if (cl.getType() != ClientType.CONFIDENTIAL) {
      throwAccessDenied("Client credentials flow does not support public clients");
    }
    if (!cl.getSecret().equals(req.getClientSecret())) {
      throwAccessDenied("Bad client id or password");
    }
  }

  private void throwAccessDenied(String msg) throws OAuth2Exception {
    OAuth2NormalizedResponse resp = new OAuth2NormalizedResponse();
    resp.setError(ErrorType.ACCESS_DENIED.toString());
    resp.setErrorDescription(msg);
    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
    throw new OAuth2Exception(resp);
  }
}
