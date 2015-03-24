/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2015 Aimluck,Inc.
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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.aipo.orm.Database;
import com.aipo.orm.model.security.TurbineUser;
import com.aipo.orm.model.social.AppData;
import com.aipo.orm.query.Operations;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AipoAppDataDbService implements AppDataDbService {

  private final TurbineUserDbService turbineUserDbService;

  @Inject
  public AipoAppDataDbService(TurbineUserDbService turbineUserDbService) {
    this.turbineUserDbService = turbineUserDbService;
  }

  /**
   * 
   * @param usernames
   * @param appId
   * @param fields
   * @return
   */
  public List<AppData> get(Set<String> usernames, String appId,
      Set<String> fields) {
    try {
      return Database
        .query(AppData.class)
        .where(Operations.eq(AppData.APP_ID_PROPERTY, appId))
        .where(Operations.in(AppData.NAME_PROPERTY, fields.toArray()))
        .where(Operations.in(AppData.LOGIN_NAME_PROPERTY, usernames.toArray()))
        .fetchList();
    } catch (Throwable t) {
      Database.rollback();
      throw new RuntimeException(t);
    }
  }

  /**
   * 
   * @param username
   * @param appId
   * @param values
   */
  public void put(String username, String appId, Map<String, String> values) {
    try {
      TurbineUser user = null;
      if (!"@admin".equals(username)) {
        user = turbineUserDbService.findByUsername(username);
        if (user == null) {
          return;
        }
      }
      Iterator<Entry<String, String>> iterator = values.entrySet().iterator();
      while (iterator.hasNext()) {
        Entry<String, String> next = iterator.next();
        String key = next.getKey();
        String value = next.getValue();
        AppData appData =
          Database.query(AppData.class).where(
            Operations.eq(AppData.NAME_PROPERTY, key)).where(
            Operations.eq(AppData.APP_ID_PROPERTY, appId)).where(
            Operations.eq(AppData.LOGIN_NAME_PROPERTY, username)).fetchSingle();
        if (appData == null) {
          appData = Database.create(AppData.class);
          appData.setName(key);
        }
        appData.setValue(value);
        appData.setAppId(appId);
        appData.setLoginName(user == null ? "@admin" : user.getLoginName());
      }
      Database.commit();
    } catch (Throwable t) {
      Database.rollback();
      throw new RuntimeException(t);
    }
  }

  /**
   * @param username
   * @param appId
   * @param fields
   */
  public void delete(String username, String appId, Set<String> fields) {
    try {
      TurbineUser user = null;
      if (!"@admin".equals(username)) {
        user = turbineUserDbService.findByUsername(username);
        if (user == null) {
          return;
        }
      }
      List<AppData> fetchList =
        Database.query(AppData.class).where(
          Operations.eq(AppData.APP_ID_PROPERTY, appId)).where(
          Operations.in(AppData.NAME_PROPERTY, fields.toArray())).where(
          Operations.eq(AppData.LOGIN_NAME_PROPERTY, username)).fetchList();
      if (fetchList.size() == 0) {
        Database.rollback();
        return;
      }
      Database.deleteAll(fetchList);
      Database.commit();
    } catch (Throwable t) {
      Database.rollback();
      throw new RuntimeException(t);
    }
  }
}