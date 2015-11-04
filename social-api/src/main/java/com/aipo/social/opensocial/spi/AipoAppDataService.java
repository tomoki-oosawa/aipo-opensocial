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
package com.aipo.social.opensocial.spi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.ImmediateFuture;
import org.apache.shindig.protocol.DataCollection;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.social.opensocial.spi.AppDataService;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.aipo.container.protocol.AipoErrorCode;
import com.aipo.container.protocol.AipoProtocolException;
import com.aipo.orm.model.security.TurbineUser;
import com.aipo.orm.model.social.AppData;
import com.aipo.orm.service.AppDataDbService;
import com.aipo.orm.service.TurbineUserDbService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.internal.Sets;

/**
 *
 */
public class AipoAppDataService extends AbstractService implements
    AppDataService {

  private final TurbineUserDbService turbineUserDbService;

  private final AppDataDbService appDataDbService;

  /**
   *
   */
  @Inject
  public AipoAppDataService(TurbineUserDbService turbineUserDbService,
      AppDataDbService appDataDbService) {
    this.turbineUserDbService = turbineUserDbService;
    this.appDataDbService = appDataDbService;
  }

  /**
   * @param userId
   * @param groupId
   * @param fields
   * @param values
   * @param token
   * @return
   * @throws ProtocolException
   */
  @Override
  public Future<Void> updatePersonData(UserId userId, GroupId groupId,
      String appId, Set<String> fields, Map<String, String> values,
      SecurityToken token) throws ProtocolException {

    setUp(token);
    // appId が NULL の場合、SecurityToken から抽出
    if (appId == null) {
      appId = token.getAppId();
    }
    // 同じアプリのみアクセス可能
    checkSameAppId(appId, token);
    // 自分（Viewer）の AppData のみ更新可能
    boolean isAdmin = false;
    if ("@admin".equals(userId.getUserId())) {
      isAdmin = true;
    } else {
      checkSameViewer(userId, token);
    }

    Iterator<Entry<String, String>> iterator = values.entrySet().iterator();
    while (iterator.hasNext()) {
      Entry<String, String> next = iterator.next();
      String key = next.getKey();
      String value = next.getValue();
      // キー ： 32 byte 以内
      checkInputByte(key, 1, 32);
      // 値： 1024 byte 以内
      checkInputByte(value, 0, 1024);
    }

    String username = isAdmin ? "@admin" : getUserId(userId, token);
    appDataDbService.put(username, appId, values);

    return ImmediateFuture.newInstance(null);
  }

  /**
   * @param userId
   * @param groupId
   * @param appId
   * @param fields
   * @param token
   * @return
   * @throws ProtocolException
   */
  @Override
  public Future<Void> deletePersonData(UserId userId, GroupId groupId,
      String appId, Set<String> fields, SecurityToken token)
      throws ProtocolException {

    setUp(token);
    // appId が NULL の場合、SecurityToken から抽出
    if (appId == null) {
      appId = token.getAppId();
    }
    // 同じアプリのみアクセス可能
    checkSameAppId(appId, token);
    // 自分（Viewer）の AppData のみ更新可能
    boolean isAdmin = false;
    if ("@admin".equals(userId.getUserId())) {
      isAdmin = true;
    } else {
      checkSameViewer(userId, token);
    }

    String username = isAdmin ? "@admin" : getUserId(userId, token);
    appDataDbService.delete(username, appId, fields);

    return ImmediateFuture.newInstance(null);
  }

  /**
   * @param userIds
   * @param groupId
   * @param appId
   * @param fields
   * @param token
   * @return
   * @throws ProtocolException
   */
  @Override
  public Future<DataCollection> getPersonData(Set<UserId> userIds,
      GroupId groupId, String appId, Set<String> fields, SecurityToken token)
      throws ProtocolException {

    setUp(token);
    // appId が NULL の場合、SecurityToken から抽出
    if (appId == null) {
      appId = token.getAppId();
    }
    // 同じアプリのみアクセス可能
    checkSameAppId(appId, token);

    boolean hasAdmin = false;
    List<TurbineUser> list = null;
    switch (groupId.getType()) {
      case all:
      case friends:
        // {guid} が閲覧できるすべてのユーザーを取得
        // @all = @friends
        // TODO: 検索件数の制限を設ける
        // NOT SUPPORTED
        // list = turbineUserDbService.find(SearchOptions.build());
        break;
      case groupId:
        // {guid} が閲覧できるすべてのユーザーで {groupId} グループに所属しているものを取得
        // TODO: 検索件数の制限を設ける
        // NOT SUPPORTED
        /*-
         list =
          turbineUserDbService.findByGroupname(
            groupId.getGroupId(),
            SearchOptions.build());
         */
        break;
      case deleted:
        // {guid} が閲覧できる無効なユーザーを取得
        list = Lists.newArrayList();
        break;
      case self:
        // {guid} 自身のユーザー情報を取得
        Set<String> users = Sets.newHashSet();
        for (UserId userId : userIds) {
          if (!"@admin".equals(userId.getUserId())) {
            users.add(getUserId(userId, token));
          } else {
            hasAdmin = true;
          }
        }
        list = turbineUserDbService.findByUsername(users);
        break;
      default:
        throw new AipoProtocolException(
          AipoErrorCode.VALIDATE_ACCESS_NOT_DENIED);
    }
    Set<String> usernames = Sets.newHashSet();
    if (list != null) {
      for (TurbineUser user : list) {
        usernames.add(user.getLoginName());
      }
    }
    if (hasAdmin) {
      usernames.add("@admin");
    }
    List<AppData> appDataList = appDataDbService.get(usernames, appId, fields);

    Map<String, Map<String, String>> results =
      new HashMap<String, Map<String, String>>();

    // only add in the fields
    if (fields == null || fields.isEmpty()) {
      for (String username : usernames) {
        results.put(
          convertUserId(username, token),
          new HashMap<String, String>());
      }
    } else {
      for (AppData appData : appDataList) {
        String loginName = appData.getLoginName();
        String userId =
          "@admin".equals(loginName) ? "@admin" : convertUserId(
            loginName,
            token);
        Map<String, String> map = results.get(userId);
        if (map == null) {
          map = Maps.newHashMap();
        }
        for (String f : fields) {
          if (f.equals(appData.getName())) {
            map.put(f, appData.getValue());
            break;
          }
        }
        results.put(userId, map);
      }
    }

    return ImmediateFuture.newInstance(new DataCollection(results));
  }
}
