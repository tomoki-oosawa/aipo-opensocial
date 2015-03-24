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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.aipo.orm.Database;
import com.aipo.orm.model.security.TurbineGroup;
import com.aipo.orm.model.security.TurbineUser;
import com.aipo.orm.query.Operations;
import com.aipo.orm.query.SelectQuery;
import com.aipo.orm.service.request.SearchOptions;
import com.aipo.orm.service.request.SearchOptions.FilterOperation;
import com.aipo.orm.service.request.SearchOptions.SortOrder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AipoTurbineGroupDbService implements TurbineGroupDbService {

  public static int MAX_LIMIT = 1000;

  private final TurbineUserDbService turbineUserDbService;

  @Inject
  public AipoTurbineGroupDbService(TurbineUserDbService turbineUserDbService) {
    this.turbineUserDbService = turbineUserDbService;
  }

  public List<TurbineGroup> find(String username, SearchOptions options) {
    TurbineUser turbineUser = turbineUserDbService.findByUsername(username);
    if (turbineUser == null) {
      return new ArrayList<TurbineGroup>();
    }
    SelectQuery<TurbineGroup> query =
      buildQuery(turbineUser.getUserId(), options, false);
    return query.fetchList();
  }

  /**
   * @return
   */
  public int getCount(String username, SearchOptions options) {
    TurbineUser turbineUser = turbineUserDbService.findByUsername(username);
    if (turbineUser == null) {
      return 0;
    }
    SelectQuery<TurbineGroup> query =
      buildQuery(turbineUser.getUserId(), options, true);
    return query.getCount();
  }

  protected SelectQuery<TurbineGroup> buildQuery(int ownerId,
      SearchOptions options, boolean isCount) {
    SelectQuery<TurbineGroup> query = Database.query(TurbineGroup.class);
    int limit = options.getLimit();
    int offset = options.getOffset();

    // Range
    if (!isCount) {
      if (limit > MAX_LIMIT) {
        limit = MAX_LIMIT;
      }

      if (limit > 0) {
        query.limit(limit);
      }

      if (offset > 0) {
        query.offset(offset);
      }
    }

    // Filter
    String filter = options.getFilterBy();
    FilterOperation filterOperation = options.getFilterOperation();
    String filterValue = options.getFilterValue();
    // グループ名
    if ("title".equals(filter)) {
      switch (filterOperation) {
        case equals:
          query.where(Operations.eq(
            TurbineGroup.GROUP_ALIAS_NAME_PROPERTY,
            filterValue));
          break;
        case contains:
          query.where(Operations.contains(
            TurbineGroup.GROUP_ALIAS_NAME_PROPERTY,
            filterValue));
          break;
        case present:
          // not supported.
          break;
        case startsWith:
          query.where(Operations.startWith(
            TurbineGroup.GROUP_ALIAS_NAME_PROPERTY,
            filterValue));
          break;
        default:
          break;
      }
    }
    // 部署 or マイグループ
    boolean typeFilter = false;
    if ("type".equals(filter)) {
      if ("unit".equals(filterValue)) {
        query.where(Operations.eq(TurbineGroup.OWNER_ID_PROPERTY, 1));
        typeFilter = true;
      }
      if ("mygroup".equals(filterValue)) {
        query.where(Operations.eq(TurbineGroup.OWNER_ID_PROPERTY, ownerId));
        typeFilter = true;
      }
    }
    if (!typeFilter) {
      query.where(Operations.in(TurbineGroup.OWNER_ID_PROPERTY, Arrays.asList(
        1,
        ownerId).toArray()));
    }

    // Sort
    if (!isCount) {
      boolean isOrder = false;
      String sort = options.getSortBy();
      SortOrder sortOrder = options.getSortOrder();
      if ("title".equals(sort)) {
        if (SortOrder.ascending.equals(sortOrder)) {
          query.orderAscending("groupAliasName");
        } else {
          query.orderDesending("groupAliasName");
        }
        isOrder = true;
      }
      if (!isOrder) {
        query.orderAscending("groupAliasName");
      }
    }

    return query;

  }

}