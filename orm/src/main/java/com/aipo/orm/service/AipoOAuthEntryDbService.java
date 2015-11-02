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

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.access.DataContext;

import com.aipo.orm.Database;
import com.aipo.orm.query.Operations;
import com.aipo.orm.service.bean.OAuthEntry;
import com.aipo.orm.service.bean.OAuthEntry.Type;
import com.google.inject.Singleton;

@Singleton
public class AipoOAuthEntryDbService implements OAuthEntryDbService {

  /**
   * @param oauthToken
   * @return
   */
  public OAuthEntry get(String oauthToken) {
    selectDefaultDataDomain();
    com.aipo.orm.model.social.OAuthEntry model =
      Database.query(com.aipo.orm.model.social.OAuthEntry.class).where(
        Operations.eq(
          com.aipo.orm.model.social.OAuthEntry.TOKEN_PROPERTY,
          oauthToken)).fetchSingle();
    if (model == null) {
      return null;
    }
    OAuthEntry entry = new OAuthEntry();
    entry.setAppId(model.getAppId());
    entry.setAuthorized(model.getAuthorized() != null
      && model.getAuthorized().intValue() == 1);
    entry.setCallbackToken(model.getCallbackToken());
    entry.setCallbackTokenAttempts(model.getCallbackTokenAttempts() != null
      ? model.getCallbackTokenAttempts().intValue()
      : 0);
    entry.setCallbackUrl(model.getCallbackUrl());
    entry.setCallbackUrlSigned(model.getCallbackUrlSigned() != null
      && model.getCallbackUrlSigned().intValue() == 1);
    entry.setConsumerKey(model.getConsumerKey());
    entry.setContainer(model.getContainer());
    entry.setDomain(model.getDomain());
    entry.setIssueTime(model.getIssueTime());
    entry.setOauthVersion(model.getOauthVersion());
    entry.setToken(model.getToken());
    entry.setTokenSecret(model.getTokenSecret());
    entry.setType(Type.valueOf(model.getType()));
    entry.setUserId(model.getUserId());
    return entry;
  }

  /**
   * @param oauthToken
   */
  public void remove(String oauthToken) {
    try {
      selectDefaultDataDomain();
      com.aipo.orm.model.social.OAuthEntry model =
        Database.query(com.aipo.orm.model.social.OAuthEntry.class).where(
          Operations.eq(
            com.aipo.orm.model.social.OAuthEntry.TOKEN_PROPERTY,
            oauthToken)).fetchSingle();
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

  /**
   * @param oauthToken
   */
  public void put(OAuthEntry oauthToken) {
    try {
      selectDefaultDataDomain();
      com.aipo.orm.model.social.OAuthEntry model =
        Database.query(com.aipo.orm.model.social.OAuthEntry.class).where(
          Operations.eq(
            com.aipo.orm.model.social.OAuthEntry.TOKEN_PROPERTY,
            oauthToken)).fetchSingle();
      if (model == null) {
        model = Database.create(com.aipo.orm.model.social.OAuthEntry.class);
      }
      model.setToken(oauthToken.getToken());
      model.setAppId(oauthToken.getAppId());
      model.setAuthorized(oauthToken.isAuthorized() ? 1 : 0);
      model.setCallbackToken(oauthToken.getCallbackToken());
      model.setCallbackTokenAttempts(oauthToken.getCallbackTokenAttempts());
      model.setCallbackUrl(oauthToken.getCallbackUrl());
      model.setCallbackUrlSigned(oauthToken.isCallbackUrlSigned() ? 1 : 0);
      model.setConsumerKey(oauthToken.getConsumerKey());
      model.setContainer(oauthToken.getContainer());
      model.setDomain(oauthToken.getDomain());
      model.setIssueTime(oauthToken.getIssueTime());
      model.setOauthVersion(oauthToken.getOauthVersion());
      model.setTokenSecret(oauthToken.getTokenSecret());
      model.setType(oauthToken.getType().toString());
      model.setUserId(oauthToken.getUserId());
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