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

import java.util.Arrays;
import java.util.Date;

import org.apache.shindig.social.core.oauth2.AipoOAuth2Code;
import org.apache.shindig.social.core.oauth2.OAuth2Client;
import org.apache.shindig.social.core.oauth2.OAuth2Client.ClientType;
import org.apache.shindig.social.core.oauth2.OAuth2Code;
import org.apache.shindig.social.core.oauth2.OAuth2DataService;
import org.apache.shindig.social.core.oauth2.OAuth2DataServiceImpl;
import org.apache.shindig.social.core.oauth2.OAuth2Types.CodeType;
import org.apache.shindig.social.core.oauth2.OAuth2Types.TokenFormat;

import com.aipo.orm.service.OAuth2ClientDbService;
import com.aipo.orm.service.OAuth2TokenDbService;
import com.aipo.orm.service.bean.OAuth2Token;
import com.google.inject.Inject;

/**
 * @see OAuth2DataServiceImpl
 */
public class AipoOAuth2DataService implements OAuth2DataService {

  private OAuth2TokenDbService store = null;

  private OAuth2ClientDbService client = null;

  @Inject
  public AipoOAuth2DataService(OAuth2TokenDbService store,
      OAuth2ClientDbService client) throws Exception {
    this.store = store;
    this.client = client;
  }

  /**
   * @param clientId
   * @return
   */
  @Override
  public OAuth2Client getClient(String clientId) {
    com.aipo.orm.service.bean.OAuth2Client oauth2Cient = client.get(clientId);
    if (oauth2Cient == null) {
      return null;
    }
    OAuth2Client client = new OAuth2Client();
    if (oauth2Cient.getFlow() != null) {
      client.setFlow(oauth2Cient.getFlow().toString());
    }
    client.setId(oauth2Cient.getId());
    client.setSecret(oauth2Cient.getSecret());
    if (oauth2Cient.getType() != null) {
      client.setType(oauth2Cient.getType().toString().equals(
        ClientType.PUBLIC.toString())
        ? ClientType.PUBLIC
        : ClientType.CONFIDENTIAL);
    }
    if (oauth2Cient.getFlow() != null) {
      client.setFlow(oauth2Cient.getFlow().toString());
    }
    client.setIconUrl(oauth2Cient.getIconUrl());
    client.setRedirectURI(oauth2Cient.getRedirectURI());
    client.setTitle(oauth2Cient.getTitle());
    return client;
  }

  /**
   * @param clientId
   * @param authCode
   * @return
   */
  @Override
  public OAuth2Code getAuthorizationCode(String clientId, String authCode) {
    if (authCode == null) {
      return null;
    }
    OAuth2Token token =
      store.get(authCode, CodeType.AUTHORIZATION_CODE.toString());
    if (token == null) {
      return null;
    }
    AipoOAuth2Code code = new AipoOAuth2Code();
    code.setUserId(token.getUserId());
    code.setExpiration(token.getExpireTime().getTime());
    code.setType(CodeType.AUTHORIZATION_CODE);
    code.setValue(token.getToken());
    String scopes = token.getScope();
    if (scopes != null) {
      String[] split = scopes.split(" ");
      code.setScope(Arrays.asList(split));
    }
    return code;
  }

  /**
   * @param clientId
   * @param authCode
   */
  @Override
  public void registerAuthorizationCode(String clientId, OAuth2Code authCode) {
    if (authCode == null) {
      return;
    }
    OAuth2Token token = new OAuth2Token();
    token.setCodeType(CodeType.AUTHORIZATION_CODE.toString());
    token.setToken(authCode.getValue());
    if (authCode instanceof AipoOAuth2Code) {
      token.setUserId(((AipoOAuth2Code) authCode).getUserId());
    } else {
      throw new UnsupportedOperationException();
    }
    token.setCreateDate(new Date());
    token.setExpireTime(new Date(authCode.getExpiration()));
    StringBuilder scopes = new StringBuilder();
    if (authCode.getScope() != null) {
      boolean isFirst = true;
      for (String scope : authCode.getScope()) {
        if (!isFirst) {
          scopes.append(" ");
        } else {
          isFirst = false;
        }
        scopes.append(scope);
      }
    }
    token.setScope(scopes.toString());
    token.setTokenType(TokenFormat.BEARER.toString());
    token.setClientId(clientId);
    store.put(token);
  }

  /**
   * @param clientId
   * @param authCode
   */
  @Override
  public void unregisterAuthorizationCode(String clientId, String authCode) {
    if (authCode == null) {
      return;
    }
    store.remove(authCode);
  }

  /**
   * @param accessToken
   * @return
   */
  @Override
  public OAuth2Code getAccessToken(String accessToken) {
    if (accessToken == null) {
      return null;
    }
    OAuth2Token token =
      store.get(accessToken, CodeType.ACCESS_TOKEN.toString());
    if (token == null) {
      return null;
    }
    AipoOAuth2Code code = new AipoOAuth2Code();
    code.setUserId(token.getUserId());
    code.setExpiration(token.getExpireTime().getTime());
    code.setType(CodeType.ACCESS_TOKEN);
    code.setValue(token.getToken());
    String scopes = token.getScope();
    if (scopes != null) {
      String[] split = scopes.split(" ");
      code.setScope(Arrays.asList(split));
    }
    return code;
  }

  /**
   * @param clientId
   * @param accessToken
   */
  @Override
  public void registerAccessToken(String clientId, OAuth2Code accessToken) {
    if (accessToken == null) {
      return;
    }
    OAuth2Token token = new OAuth2Token();
    token.setCodeType(CodeType.ACCESS_TOKEN.toString());
    token.setToken(accessToken.getValue());
    if (accessToken instanceof AipoOAuth2Code) {
      token.setUserId(((AipoOAuth2Code) accessToken).getUserId());
    } else {
      throw new UnsupportedOperationException();
    }
    token.setCreateDate(new Date());
    token.setExpireTime(new Date(accessToken.getExpiration()));
    StringBuilder scopes = new StringBuilder();
    if (accessToken.getScope() != null) {
      boolean isFirst = true;
      for (String scope : accessToken.getScope()) {
        if (!isFirst) {
          scopes.append(" ");
        } else {
          isFirst = false;
        }
        scopes.append(scope);
      }
    }
    token.setScope(scopes.toString());
    token.setTokenType(TokenFormat.BEARER.toString());
    token.setClientId(clientId);
    store.put(token);

    // AccessToken作成後に期限切れのAccessTokenを削除する
    Runnable removeExpired = new Runnable() {
      @Override
      public void run() {
        store.removeExpired();
      }
    };
    Thread thread = new Thread(removeExpired);
    thread.start();

  }

  /**
   * @param clientId
   * @param accessToken
   */
  @Override
  public void unregisterAccessToken(String clientId, String accessToken) {
    if (accessToken == null) {
      return;
    }
    store.remove(accessToken);
  }

  /**
   * @param refreshToken
   * @return
   */
  @Override
  public OAuth2Code getRefreshToken(String refreshToken) {
    if (refreshToken == null) {
      return null;
    }
    OAuth2Token token =
      store.get(refreshToken, CodeType.REFRESH_TOKEN.toString());
    if (token == null) {
      return null;
    }
    AipoOAuth2Code code = new AipoOAuth2Code();
    code.setUserId(token.getUserId());
    if (token.getExpireTime() != null) {
      code.setExpiration(token.getExpireTime().getTime());
    }
    code.setType(CodeType.REFRESH_TOKEN);
    code.setValue(token.getToken());
    String scopes = token.getScope();
    if (scopes != null) {
      String[] split = scopes.split(" ");
      code.setScope(Arrays.asList(split));
    }
    return code;
  }

  /**
   * @param clientId
   * @param refreshToken
   */
  @Override
  public void registerRefreshToken(String clientId, OAuth2Code refreshToken) {
    if (refreshToken == null) {
      return;
    }
    OAuth2Token token = new OAuth2Token();
    token.setCodeType(CodeType.REFRESH_TOKEN.toString());
    token.setToken(refreshToken.getValue());
    if (refreshToken instanceof AipoOAuth2Code) {
      token.setUserId(((AipoOAuth2Code) refreshToken).getUserId());
    } else {
      throw new UnsupportedOperationException();
    }
    token.setCreateDate(new Date());
    if (refreshToken.getExpiration() > 0) {
      token.setExpireTime(new Date(refreshToken.getExpiration()));
    }
    StringBuilder scopes = new StringBuilder();
    if (refreshToken.getScope() != null) {
      boolean isFirst = true;
      for (String scope : refreshToken.getScope()) {
        if (!isFirst) {
          scopes.append(" ");
        } else {
          isFirst = false;
        }
        scopes.append(scope);
      }
    }
    token.setScope(scopes.toString());
    token.setTokenType(TokenFormat.BEARER.toString());
    token.setClientId(clientId);
    store.put(token);
  }

  /**
   * @param clientId
   * @param refreshToken
   */
  @Override
  public void unregisterRefreshToken(String clientId, String refreshToken) {
    if (refreshToken == null) {
      return;
    }
    store.remove(refreshToken);
  }

}
