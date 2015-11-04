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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.shindig.social.core.oauth2.AipoOAuth2Code;
import org.apache.shindig.social.core.oauth2.OAuth2Client;
import org.apache.shindig.social.core.oauth2.OAuth2Client.ClientType;
import org.apache.shindig.social.core.oauth2.OAuth2Code;
import org.apache.shindig.social.core.oauth2.OAuth2DataService;
import org.apache.shindig.social.core.oauth2.OAuth2Exception;
import org.apache.shindig.social.core.oauth2.OAuth2NormalizedRequest;
import org.apache.shindig.social.core.oauth2.OAuth2NormalizedResponse;
import org.apache.shindig.social.core.oauth2.OAuth2Service;
import org.apache.shindig.social.core.oauth2.OAuth2ServiceImpl;
import org.apache.shindig.social.core.oauth2.OAuth2Types.CodeType;
import org.apache.shindig.social.core.oauth2.OAuth2Types.ErrorType;
import org.apache.shindig.social.core.oauth2.validators.AuthorizationCodeRequestValidator;
import org.apache.shindig.social.core.oauth2.validators.OAuth2ProtectedResourceValidator;
import org.apache.shindig.social.core.oauth2.validators.OAuth2RequestValidator;

import com.aipo.container.protocol.AipoScope;
import com.aipo.orm.service.OAuth2TokenDbService;
import com.aipo.orm.service.TurbineUserDbService;
import com.aipo.social.core.oauth2.validators.AipoOAuth2ProtectedResourceValidator;
import com.aipo.social.core.oauth2.validators.AipoOAuth2RequstValidator;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * @see OAuth2ServiceImpl
 */
public class AipoOAuth2Service implements OAuth2Service {

  private final OAuth2DataService store; // underlying OAuth data store

  private final long authCodeExpires;

  private final long accessTokenExpires;

  private final long refreshTokenExpires;

  // validators
  private final OAuth2RequestValidator accessTokenValidator;

  private final OAuth2RequestValidator authCodeValidator;

  private final OAuth2ProtectedResourceValidator resourceReqValidator;

  @Inject
  public AipoOAuth2Service(OAuth2DataService store,
      TurbineUserDbService turbineUserDbService, OAuth2TokenDbService db,

      @Named("shindig.oauth2.authCodeExpiration") long authCodeExpires,
      @Named("shindig.oauth2.accessTokenExpiration") long accessTokenExpires,
      @Named("shindig.oauth2.refreshTokenExpiration") long refreshTokenExpires) {
    this.store = store;

    this.authCodeExpires = authCodeExpires;
    this.accessTokenExpires = accessTokenExpires;
    this.refreshTokenExpires = refreshTokenExpires;

    authCodeValidator = new AuthorizationCodeRequestValidator(store);
    accessTokenValidator =
      new AipoOAuth2RequstValidator(store, turbineUserDbService);
    resourceReqValidator = new AipoOAuth2ProtectedResourceValidator(store);
  }

  /**
   * @return
   */
  @Override
  public OAuth2DataService getDataService() {
    return store;
  }

  protected long getAuthCodeExpires() {
    return authCodeExpires;
  }

  protected long getAccessTokenExpires() {
    return accessTokenExpires;
  }

  /**
   * @param req
   * @throws OAuth2Exception
   */
  @Override
  public void authenticateClient(OAuth2NormalizedRequest req)
      throws OAuth2Exception {
    OAuth2Client client = store.getClient(req.getClientId());
    if (client == null) {
      OAuth2NormalizedResponse resp = new OAuth2NormalizedResponse();
      resp.setError(ErrorType.INVALID_CLIENT.toString());
      resp.setErrorDescription("The client ID is invalid or not registered");
      resp.setBodyReturned(true);
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      throw new OAuth2Exception(resp);
    }
    String realSecret = client.getSecret();
    String reqSecret = req.getClientSecret();
    if (realSecret != null
      || reqSecret != null
      || client.getType() == ClientType.CONFIDENTIAL) {
      if (realSecret == null
        || reqSecret == null
        || !realSecret.equals(reqSecret)) {
        OAuth2NormalizedResponse resp = new OAuth2NormalizedResponse();
        resp.setError(ErrorType.UNAUTHORIZED_CLIENT.toString());
        resp.setErrorDescription("The client failed to authorize");
        resp.setBodyReturned(true);
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        throw new OAuth2Exception(resp);
      }
    }
  }

  /**
   * @param req
   * @throws OAuth2Exception
   */
  @Override
  public void validateRequestForAuthCode(OAuth2NormalizedRequest req)
      throws OAuth2Exception {
    authCodeValidator.validateRequest(req);
  }

  /**
   * @param req
   * @throws OAuth2Exception
   */
  @Override
  public void validateRequestForAccessToken(OAuth2NormalizedRequest req)
      throws OAuth2Exception {
    accessTokenValidator.validateRequest(req);
  }

  /**
   * @param req
   * @param resourceRequest
   * @throws OAuth2Exception
   */
  @Override
  public void validateRequestForResource(OAuth2NormalizedRequest req,
      Object resourceRequest) throws OAuth2Exception {
    resourceReqValidator.validateRequestForResource(req, resourceRequest);
  }

  /**
   * @param req
   * @return
   */
  @Override
  public OAuth2Code grantAuthorizationCode(OAuth2NormalizedRequest req) {
    OAuth2Code authCode = generateAuthorizationCode(req);
    store.registerAuthorizationCode(req.getClientId(), authCode);
    return authCode;
  }

  /**
   * @param req
   * @return
   */
  @Override
  public OAuth2Code grantAccessToken(OAuth2NormalizedRequest req) {
    OAuth2Code accessToken = generateAccessToken(req);
    OAuth2Code authCode =
      store.getAuthorizationCode(req.getClientId(), req.getAuthorizationCode());
    if (authCode != null) {
      authCode.setRelatedAccessToken(accessToken);
    }
    store.registerAccessToken(req.getClientId(), accessToken);
    return accessToken;
  }

  /**
   * @param req
   * @return
   */
  @Override
  public OAuth2Code grantRefreshToken(OAuth2NormalizedRequest req) {
    OAuth2Code refreshToken = generateRefreshToken(req);
    store.registerRefreshToken(req.getClientId(), refreshToken);
    return refreshToken;
  }

  /**
   * @param req
   * @return
   */
  @Override
  public OAuth2Code generateAuthorizationCode(OAuth2NormalizedRequest req) {
    AipoOAuth2Code authCode = new AipoOAuth2Code();
    authCode.setValue(UUID.randomUUID().toString());
    authCode.setExpiration(System.currentTimeMillis() + authCodeExpires);
    OAuth2Client client = store.getClient(req.getClientId());
    authCode.setClient(client);
    if (req.getRedirectURI() != null) {
      authCode.setRedirectURI(req.getRedirectURI());
    } else {
      authCode.setRedirectURI(client.getRedirectURI());
    }
    if (req.getScope() != null) {
      List<String> scopes = new ArrayList<String>();
      String scope = req.getScope();
      String[] split = scope.split("[, ]");
      for (String value : split) {
        AipoScope aipoScope = AipoScope.getScope(value);
        if (aipoScope != null) {
          scopes.add(aipoScope.toString());
        }
      }
      if (scopes.size() > 0) {
        authCode.setScope(scopes);
      }
    }
    return authCode;
  }

  /**
   * @param req
   * @return
   */
  @Override
  public OAuth2Code generateAccessToken(OAuth2NormalizedRequest req) {
    // generate token value
    AipoOAuth2Code accessToken = new AipoOAuth2Code();
    accessToken.setType(CodeType.ACCESS_TOKEN);
    accessToken.setValue(UUID.randomUUID().toString());
    accessToken.setExpiration(System.currentTimeMillis() + accessTokenExpires);
    accessToken.setUserId((String) req.get("orgId")
      + ":"
      + (String) req.get("username"));
    if (req.getRedirectURI() != null) {
      accessToken.setRedirectURI(req.getRedirectURI());
    } else {
      accessToken.setRedirectURI(store
        .getClient(req.getClientId())
        .getRedirectURI());
    }
    if (req.getScope() != null) {
      List<String> scopes = new ArrayList<String>();
      String scope = req.getScope();
      String[] split = scope.split("[, ]");
      for (String value : split) {
        AipoScope aipoScope = AipoScope.getScope(value);
        if (aipoScope != null) {
          scopes.add(aipoScope.toString());
        }
      }
      if (scopes.size() > 0) {
        accessToken.setScope(scopes);
      }
    }

    // associate with existing authorization code, if an auth code exists.
    if (req.getAuthorizationCode() != null) {
      OAuth2Code authCode =
        store.getAuthorizationCode(req.getClientId(), req
          .getAuthorizationCode());
      accessToken.setRelatedAuthCode(authCode);
      accessToken.setClient(authCode.getClient());
      if (authCode.getScope() != null) {
        accessToken.setScope(new ArrayList<String>(authCode.getScope()));
      }
    }
    return accessToken;
  }

  /**
   * @param req
   * @return
   */
  @Override
  public OAuth2Code generateRefreshToken(OAuth2NormalizedRequest req) {
    // generate token value
    AipoOAuth2Code refreshToken = new AipoOAuth2Code();
    refreshToken.setType(CodeType.REFRESH_TOKEN);
    refreshToken.setValue(UUID.randomUUID().toString());
    if (refreshTokenExpires > 0) {
      refreshToken.setExpiration(System.currentTimeMillis()
        + refreshTokenExpires);
    }
    refreshToken.setUserId((String) req.get("orgId")
      + ":"
      + (String) req.get("username"));
    if (req.getRedirectURI() != null) {
      refreshToken.setRedirectURI(req.getRedirectURI());
    } else {
      refreshToken.setRedirectURI(store
        .getClient(req.getClientId())
        .getRedirectURI());
    }
    if (req.getScope() != null) {
      List<String> scopes = new ArrayList<String>();
      String scope = req.getScope();
      String[] split = scope.split("[, ]");
      for (String value : split) {
        AipoScope aipoScope = AipoScope.getScope(value);
        if (aipoScope != null) {
          scopes.add(aipoScope.toString());
        }
      }
      if (scopes.size() > 0) {
        refreshToken.setScope(scopes);
      }
    }

    // associate with existing authorization code, if an auth code exists.
    if (req.getAuthorizationCode() != null) {
      OAuth2Code accessToken = store.getAccessToken(req.getAccessToken());
      refreshToken.setRelatedAccessToken(accessToken);
      refreshToken.setClient(accessToken.getClient());
      if (accessToken.getScope() != null) {
        refreshToken.setScope(new ArrayList<String>(accessToken.getScope()));
      }
    }
    return refreshToken;
  }
}
