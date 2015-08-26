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
package com.aipo.social.opensocial.spi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.ImmediateFuture;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.aipo.orm.model.security.TurbineUser;
import com.aipo.orm.model.social.Activity;
import com.aipo.orm.model.social.Application;
import com.aipo.orm.service.ActivityDbService;
import com.aipo.orm.service.ApplicationDbService;
import com.aipo.orm.service.TurbineUserDbService;
import com.aipo.orm.service.request.SearchOptions;
import com.aipo.orm.service.request.SearchOptions.FilterOperation;
import com.aipo.orm.service.request.SearchOptions.SortOrder;
import com.aipo.social.core.model.ALActivityImpl;
import com.aipo.social.opensocial.model.ALActivity;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

/**
 *
 */
public class AipoActivityService extends AbstractService implements
    ActivityService {

  private final ActivityDbService activityDbService;

  private final TurbineUserDbService turbineUserDbService;

  private final ApplicationDbService applicationDbService;

  /**
   *
   */
  @Inject
  public AipoActivityService(ActivityDbService activityDbService,
      TurbineUserDbService turbineUserDbService,
      ApplicationDbService applicationDbService) {
    this.activityDbService = activityDbService;
    this.turbineUserDbService = turbineUserDbService;
    this.applicationDbService = applicationDbService;
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
  @Override
  public Future<RestfulCollection<ALActivity>> getActivities(UserId userId,
      GroupId groupId, String appId, Set<String> fields,
      AipoCollectionOptions collectionOptions, Set<String> activityIds,
      SecurityToken token) throws ProtocolException {

    setUp(token);
    // 自分（Viewer）の Activity のみ取得可能
    checkSameViewer(userId, token);

    String username = getUserId(userId, token);

    // 検索オプション
    SearchOptions options =
      SearchOptions
        .build()
        .withRange(collectionOptions.getMax(), collectionOptions.getFirst())
        .withFilter(
          collectionOptions.getFilter(),
          collectionOptions.getFilterOperation() == null
            ? FilterOperation.equals
            : FilterOperation.valueOf(collectionOptions
              .getFilterOperation()
              .toString()),
          collectionOptions.getFilterValue())
        .withSort(
          collectionOptions.getSortBy(),
          collectionOptions.getSortOrder() == null
            ? SortOrder.ascending
            : SortOrder.valueOf(collectionOptions.getSortOrder().toString()))
        .withParameters(collectionOptions.getParameters());

    List<Activity> list = activityDbService.find(username, appId, options);
    int totalResults = activityDbService.getCount(username, appId, options);

    List<ALActivity> result = new ArrayList<ALActivity>(list.size());
    Map<String, TurbineUser> users = new HashMap<String, TurbineUser>();
    for (Activity activity : list) {
      result.add(assignActivity(activity, fields, token, users));
    }

    RestfulCollection<ALActivity> restCollection =
      new RestfulCollection<ALActivity>(
        result,
        collectionOptions.getFirst(),
        totalResults,
        collectionOptions.getMax());

    return ImmediateFuture.newInstance(restCollection);
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
  @Override
  public Future<ALActivity> getActivity(UserId userId, GroupId groupId,
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
  @Override
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
  @Override
  public Future<Void> createActivity(UserId userId, GroupId groupId,
      String appId, Set<String> fields, ALActivity activity, SecurityToken token)
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
    // タイトル 64 byte 以内
    checkInputByte(activity.getTitle(), 1, 64);
    String username = getUserId(userId, token);
    Map<String, Object> values = Maps.newHashMap();
    // TODO: BODY サポート時にコメントイン
    // values.put("body", activity.getBody());
    values.put("externalId", activity.getExternalId());
    values.put("priority", activity.getPriority());
    values.put("title", activity.getTitle());
    values.put("moduleId", token.getModuleId());

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

  private ALActivity assignActivity(Activity model, Set<String> fields,
      SecurityToken token, Map<String, TurbineUser> users) {
    ALActivity activity = new ALActivityImpl();
    activity.setId(String.valueOf(model.getId()));
    activity.setAppId(model.getAppId());
    activity.setTitle(model.getTitle());
    activity.setPriority(model.getPriority());
    activity.setExternalId(model.getExternalId());
    activity.setUpdated(model.getUpdateDate());
    if (!StringUtils.isEmpty(model.getPortletParams())) {
      activity.setPortletParams(model.getPortletParams());
    }
    if (!StringUtils.isEmpty(model.getIcon())) {
      activity.setIcon(model.getIcon());
    }

    String userId = model.getLoginName();
    try {
      TurbineUser tuserCache = users.get(userId);
      if (tuserCache != null) {
        activity.setDisplayName(tuserCache.getLastName()
          + " "
          + tuserCache.getFirstName());
        activity.setUserId(userId);
      } else {
        TurbineUser tuser = turbineUserDbService.findByUsername(userId);
        if (tuser != null) {
          activity.setDisplayName(tuser.getLastName()
            + " "
            + tuser.getFirstName());
          activity.setUserId(userId);
          users.put(userId, tuser);
        } else {
          activity.setDisplayName(model.getLoginName());
        }
      }
    } catch (Throwable ignore) {

    }
    return activity;
  }
}
