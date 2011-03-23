/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2011 Aimluck,Inc.
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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.ImmediateFuture;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.aipo.orm.model.social.Application;
import com.aipo.orm.service.ActivityDbService;
import com.aipo.orm.service.ApplicationDbService;
import com.aipo.social.opensocial.model.Activity;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

/**
 * 
 */
public class AipoActivityService extends AbstractService implements
    ActivityService {

  private final ActivityDbService activityDbService;

  private final ApplicationDbService applicationDbService;

  /**
   * 
   */
  @Inject
  public AipoActivityService(ActivityDbService activityDbService,
      ApplicationDbService applicationDbService) {
    this.activityDbService = activityDbService;
    this.applicationDbService = applicationDbService;
  }

  /**
   * @param userIds
   * @param groupId
   * @param appId
   * @param fields
   * @param options
   * @param token
   * @throws ProtocolException
   */
  public Future<RestfulCollection<Activity>> getActivities(Set<UserId> userIds,
      GroupId groupId, String appId, Set<String> fields,
      CollectionOptions options, SecurityToken token) throws ProtocolException {
    // NOT SUPPORTED
    return ImmediateFuture.newInstance(null);
  }

  /**
   * @param userId
   * @param groupId
   * @param appId
   * @param fields
   * @param options
   * @param activityIds
   * @param token
   * @return
   * @throws ProtocolException
   */
  public Future<RestfulCollection<Activity>> getActivities(UserId userId,
      GroupId groupId, String appId, Set<String> fields,
      CollectionOptions options, Set<String> activityIds, SecurityToken token)
      throws ProtocolException {
    // NOT SUPPORTED
    return ImmediateFuture.newInstance(null);
  }

  /**
   * @param userId
   * @param groupId
   * @param appId
   * @param fields
   * @param activityId
   * @param token
   * @return
   * @throws ProtocolException
   */
  public Future<Activity> getActivity(UserId userId, GroupId groupId,
      String appId, Set<String> fields, String activityId, SecurityToken token)
      throws ProtocolException {
    // NOT SUPPORTED
    return ImmediateFuture.newInstance(null);
  }

  /**
   * @param userId
   * @param groupId
   * @param appId
   * @param activityIds
   * @param token
   * @return
   * @throws ProtocolException
   */
  public Future<Void> deleteActivities(UserId userId, GroupId groupId,
      String appId, Set<String> activityIds, SecurityToken token)
      throws ProtocolException {
    // NOT SUPPORTED
    return ImmediateFuture.newInstance(null);
  }

  /**
   * @param userId
   * @param groupId
   * @param appId
   * @param fields
   * @param activity
   * @param token
   * @return
   * @throws ProtocolException
   */
  public Future<Void> createActivity(UserId userId, GroupId groupId,
      String appId, Set<String> fields, Activity activity, SecurityToken token)
      throws ProtocolException {

    setUp(token);
    // appId が NULL の場合、SecurityToken から抽出
    if (appId == null) {
      appId = token.getAppId();
    }
    // 同じアプリのみアクセス可能
    checkSameAppId(appId, token);
    // 自分（Viewer）の Activity のみ更新可能
    checkSameViewer(userId, token);
    // タイトル 1 ～ 40 文字以内
    checkInputRange(activity.getTitle(), 1, 40);

    String username = getUserId(userId, token);
    Map<String, Object> values = Maps.newHashMap();
    // TODO: BODY サポート時にコメントイン
    // values.put("body", activity.getBody());
    values.put("externalId", activity.getExternalId());
    values.put("priority", activity.getPriority());
    values.put("title", activity.getTitle());
    List<String> recipients = activity.getRecipients();
    Set<String> users = Sets.newHashSet();
    if (recipients != null && recipients.size() > 0) {
      for (String recipient : recipients) {
        users.add(getUserId(recipient, token));
      }
    }
    values.put("recipients", users);
    Application application = applicationDbService.get(appId);
    if (application != null) {
      values.put("icon", application.getIcon());
    }
    activityDbService.create(username, appId, values);

    // TODO: Generate された Activity ID を返却する
    return ImmediateFuture.newInstance(null);
  }
}
