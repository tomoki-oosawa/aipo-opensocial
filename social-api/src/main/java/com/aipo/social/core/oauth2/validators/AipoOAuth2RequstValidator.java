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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.shindig.social.core.oauth2.OAuth2DataService;
import org.apache.shindig.social.core.oauth2.OAuth2Exception;
import org.apache.shindig.social.core.oauth2.OAuth2NormalizedRequest;
import org.apache.shindig.social.core.oauth2.OAuth2NormalizedResponse;
import org.apache.shindig.social.core.oauth2.OAuth2Types.ErrorType;
import org.apache.shindig.social.core.oauth2.validators.AccessTokenRequestValidator;
import org.apache.shindig.social.core.oauth2.validators.OAuth2GrantValidator;
import org.apache.shindig.social.core.oauth2.validators.OAuth2RequestValidator;

import com.aipo.orm.service.TurbineUserDbService;
import com.google.inject.Inject;

/**
 * @see AccessTokenRequestValidator
 */
public class AipoOAuth2RequstValidator implements OAuth2RequestValidator {

  private OAuth2DataService store = null;

  private final List<OAuth2GrantValidator> grantValidators; // grant validators

  @Inject
  public AipoOAuth2RequstValidator(OAuth2DataService store,
      TurbineUserDbService turbineUserDbService) {
    this.grantValidators = new ArrayList<OAuth2GrantValidator>();
    // grantValidators.add(new AuthCodeGrantValidator(store));
    // grantValidators.add(new ClientCredentialsGrantValidator(store));
    grantValidators.add(new PasswordGrantValidator(turbineUserDbService));
    grantValidators.add(new RefreshTokenGrantValidator(store));
    this.store = store;
  }

  /**
   * @param req
   * @throws OAuth2Exception
   */
  @Override
  public void validateRequest(OAuth2NormalizedRequest req)

  throws OAuth2Exception {
    if (req.getGrantType() != null) {
      for (OAuth2GrantValidator validator : grantValidators) {
        if (validator.getGrantType().equals(req.getGrantType())) {
          validator.validateRequest(req);
          return; // request validated
        }
      }
      OAuth2NormalizedResponse response = new OAuth2NormalizedResponse();
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.setError(ErrorType.UNSUPPORTED_GRANT_TYPE.toString());
      response.setErrorDescription("Unsupported grant type");
      response.setBodyReturned(true);
      throw new OAuth2Exception(response);
    } else {
      OAuth2NormalizedResponse response = new OAuth2NormalizedResponse();
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.setError(ErrorType.UNSUPPORTED_GRANT_TYPE.toString());
      response.setErrorDescription("Unsupported grant type");
      response.setBodyReturned(true);
      throw new OAuth2Exception(response);

      // implicit flow does not include grant type
      /*-
      if (req.getResponseType() == null
        || !req.getResponseType().equals("token")) {
        OAuth2NormalizedResponse resp = new OAuth2NormalizedResponse();
        resp.setError(ErrorType.UNSUPPORTED_RESPONSE_TYPE.toString());
        resp.setErrorDescription("Unsupported response type");
        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        throw new OAuth2Exception(resp);
      }
      OAuth2Client client = store.getClient(req.getClientId());
      if (client == null || client.getFlow() != Flow.IMPLICIT) {
        OAuth2NormalizedResponse resp = new OAuth2NormalizedResponse();
        resp.setError(ErrorType.INVALID_CLIENT.toString());
        resp.setErrorDescription(req.getClientId()
          + " is not a registered implicit client");
        resp.setBodyReturned(true);
        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        throw new OAuth2Exception(resp);
      }
      if (req.getRedirectURI() == null && client.getRedirectURI() == null) {
        OAuth2NormalizedResponse resp = new OAuth2NormalizedResponse();
        resp.setError(ErrorType.INVALID_REQUEST.toString());
        resp
          .setErrorDescription("No redirect_uri registered or received in request");
        resp.setBodyReturned(true);
        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        throw new OAuth2Exception(resp);
      }
      if (req.getRedirectURI() != null
        && !req.getRedirectURI().equals(client.getRedirectURI())) {
        OAuth2NormalizedResponse resp = new OAuth2NormalizedResponse();
        resp.setError(ErrorType.INVALID_REQUEST.toString());
        resp
          .setErrorDescription("Redirect URI does not match the one registered for this client");
        resp.setBodyReturned(true);
        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        throw new OAuth2Exception(resp);
      }
      return; // request validated
       */
    }
  }

}
