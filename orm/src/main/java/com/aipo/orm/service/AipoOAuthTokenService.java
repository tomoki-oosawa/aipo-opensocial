/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2010 Aimluck,Inc.
 * http://aipostyle.com/
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

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.access.DataContext;

import com.aipo.orm.Database;
import com.aipo.orm.service.bean.OAuthToken;
import com.google.inject.Singleton;

@Singleton
public class AipoOAuthTokenService implements OAuthTokenService {

  /**
   * @param hashCode
   * @return
   */
  public OAuthToken get(int hashCode) {
    selectDefaultDataDomain();
    com.aipo.orm.model.social.OAuthToken model =
      Database.get(com.aipo.orm.model.social.OAuthToken.class, hashCode);
    if (model != null) {
      OAuthToken oAuthToken = new OAuthToken();
      oAuthToken.setKey(model.getKey());
      oAuthToken.setAccessToken(model.getAccessToken());
      oAuthToken.setSessionHandle(model.getSessionHandle());
      oAuthToken.setTokenSecret(model.getTokenSecret());
      oAuthToken.setTokenExpireMilis(model.getTokenExpireMilis());
      return oAuthToken;
    }
    return null;
  }

  public void put(OAuthToken oAuthToken) {
    try {
      selectDefaultDataDomain();
      com.aipo.orm.model.social.OAuthToken model =
        Database.get(com.aipo.orm.model.social.OAuthToken.class, oAuthToken
          .getKey());
      if (model == null) {
        model = Database.create(com.aipo.orm.model.social.OAuthToken.class);
        model.setKey(oAuthToken.getKey());
      }
      model.setAccessToken(oAuthToken.getAccessToken());
      model.setTokenSecret(oAuthToken.getTokenSecret());
      model.setSessionHandle(oAuthToken.getSessionHandle());
      model.setTokenExpireMilis((int) oAuthToken.getTokenExpireMilis());
      Database.commit();
    } catch (Throwable t) {
      Database.rollback();
      throw new RuntimeException(t);
    }
  }

  /**
   * @param hashCode
   */
  public void remove(int hashCode) {
    try {
      selectDefaultDataDomain();
      com.aipo.orm.model.social.OAuthToken model =
        Database.get(com.aipo.orm.model.social.OAuthToken.class, hashCode);
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