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
package com.aipo.orm.service;

import java.util.Date;
import java.util.List;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.access.DataContext;

import com.aipo.orm.Database;
import com.aipo.orm.query.Operations;
import com.aipo.orm.service.bean.OAuth2Token;

// TODO:
public class AipoOAuth2TokenDbService implements OAuth2TokenDbService {

  /**
   * @param hashCode
   * @return
   */
  @Override
  public OAuth2Token get(String token, String codeType) {
    selectDefaultDataDomain();
    com.aipo.orm.model.social.OAuth2Token model =
      Database.query(com.aipo.orm.model.social.OAuth2Token.class).where(
        Operations.eq(
          com.aipo.orm.model.social.OAuth2Token.CODE_TYPE_PROPERTY,
          codeType)).where(
        Operations.eq(
          com.aipo.orm.model.social.OAuth2Token.TOKEN_PROPERTY,
          token)).fetchSingle();
    if (model != null) {
      OAuth2Token oAuth2Token = new OAuth2Token();
      oAuth2Token.setUserId(model.getUserId());
      oAuth2Token.setToken(model.getToken());
      oAuth2Token.setCreateDate(model.getCreateDate());
      oAuth2Token.setExpireTime(model.getExpireTime());
      oAuth2Token.setScope(model.getScope());
      oAuth2Token.setTokenType(model.getTokenType());
      oAuth2Token.setCodeType(model.getCodeType());
      oAuth2Token.setClientId(model.getClientId());
      return oAuth2Token;
    }
    return null;
  }

  /**
   * @param oAuthToken
   */
  @Override
  public void put(OAuth2Token oAuth2Token) {
    try {
      selectDefaultDataDomain();
      com.aipo.orm.model.social.OAuth2Token model =
        Database.create(com.aipo.orm.model.social.OAuth2Token.class);
      model.setToken(oAuth2Token.getToken());
      model.setUserId(oAuth2Token.getUserId());
      model.setCreateDate(oAuth2Token.getCreateDate());
      model.setExpireTime(oAuth2Token.getExpireTime());
      model.setScope(oAuth2Token.getScope());
      model.setTokenType(oAuth2Token.getTokenType());
      model.setCodeType(oAuth2Token.getCodeType());
      model.setClientId(oAuth2Token.getClientId());
      Database.commit();
    } catch (Throwable t) {
      Database.rollback();
      throw new RuntimeException(t);
    }
  }

  /**
   * @param hashCode
   */
  @Override
  public void remove(String token) {
    try {
      selectDefaultDataDomain();
      com.aipo.orm.model.social.OAuth2Token model =
        Database.query(com.aipo.orm.model.social.OAuth2Token.class).where(
          Operations.eq(
            com.aipo.orm.model.social.OAuth2Token.TOKEN_PROPERTY,
            token)).fetchSingle();
      if (model == null) {
        return;
      }
      Database.delete(model);
      Database.commit();
    } catch (Throwable t) {
      Database.rollback();
      throw new RuntimeException(t);
    }
  }

  @Override
  public void removeExpired() {
    try {
      selectDefaultDataDomain();
      List<com.aipo.orm.model.social.OAuth2Token> tokens =
        Database.query(com.aipo.orm.model.social.OAuth2Token.class).where(
          Operations.le(
            com.aipo.orm.model.social.OAuth2Token.EXPIRE_TIME_PROPERTY,
            new Date())).fetchList();
      for (com.aipo.orm.model.social.OAuth2Token token : tokens) {
        try {
          Database.delete(token);
          Database.commit();
        } catch (Throwable t) {
          Database.rollback();
        }
      }

    } catch (Throwable t) {
      Database.rollback();
      throw new RuntimeException(t);
    }
  }

  private void selectDefaultDataDomain() {
    ObjectContext dataContext = null;
    try {
      dataContext = DataContext.getThreadObjectContext();
    } catch (IllegalStateException ignore) {
      // first
    }
    if (dataContext == null) {
      try {
        dataContext = Database.createDataContext("org001");
        DataContext.bindThreadObjectContext(dataContext);
      } catch (Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }
}