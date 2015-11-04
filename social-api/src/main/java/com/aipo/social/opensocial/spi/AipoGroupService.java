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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.ImmediateFuture;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.GroupId.Type;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.aipo.orm.model.security.TurbineGroup;
import com.aipo.orm.service.TurbineGroupDbService;
import com.aipo.orm.service.request.SearchOptions;
import com.aipo.orm.service.request.SearchOptions.FilterOperation;
import com.aipo.orm.service.request.SearchOptions.SortOrder;
import com.aipo.social.core.model.ALGroupImpl;
import com.aipo.social.opensocial.model.ALGroup;
import com.google.inject.Inject;

/**
 *
 */
public class AipoGroupService extends AbstractService implements GroupService {

  private final TurbineGroupDbService turbineGroupDbService;

  /**
   *
   */
  @Inject
  public AipoGroupService(TurbineGroupDbService eipMPostDbService) {
    this.turbineGroupDbService = eipMPostDbService;
  }

  /**
   * @param userId
   * @param options
   * @param fields
   * @param token
   * @return
   */
  @Override
  public Future<RestfulCollection<ALGroup>> getGroups(UserId userId,
      CollectionOptions collectionOptions, Set<String> fields,
      SecurityToken token) {

    // TODO: FIELDS

    setUp(token);
    // 自分（Viewer）の Group のみ取得可能
    checkSameViewer(userId, token);

    String username = getUserId(userId, token);

    // Search
    SearchOptions options =
      SearchOptions.build().withRange(
        collectionOptions.getMax(),
        collectionOptions.getFirst()).withFilter(
        collectionOptions.getFilter(),
        collectionOptions.getFilterOperation() == null
          ? FilterOperation.equals
          : FilterOperation.valueOf(collectionOptions
            .getFilterOperation()
            .toString()),
        collectionOptions.getFilterValue()).withSort(
        collectionOptions.getSortBy(),
        collectionOptions.getSortOrder() == null
          ? SortOrder.ascending
          : SortOrder.valueOf(collectionOptions.getSortOrder().toString()));
    List<TurbineGroup> list = turbineGroupDbService.find(username, options);

    List<ALGroup> result = new ArrayList<ALGroup>();
    for (TurbineGroup post : list) {
      result.add(assginGroup(post, fields, token));
    }
    int totalResults = turbineGroupDbService.getCount(username, options);

    RestfulCollection<ALGroup> restCollection =
      new RestfulCollection<ALGroup>(
        result,
        collectionOptions.getFirst(),
        totalResults,
        collectionOptions.getMax());
    return ImmediateFuture.newInstance(restCollection);
  }

  protected ALGroup assginGroup(TurbineGroup turbineGroup, Set<String> fields,
      SecurityToken token) {
    ALGroup group = new ALGroupImpl();
    GroupId groupId = new GroupId(Type.groupId, turbineGroup.getGroupName());
    group.setId(groupId);
    group.setTitle(turbineGroup.getGroupAliasName());
    group.setType(turbineGroup.getOwnerId().intValue() == 1
      ? "unit"
      : "mygroup");
    return group;
  }
}
