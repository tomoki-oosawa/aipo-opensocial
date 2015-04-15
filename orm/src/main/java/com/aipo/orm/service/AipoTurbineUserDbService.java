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
import java.util.List;
import java.util.Set;

import org.apache.cayenne.DataRow;

import com.aipo.orm.Database;
import com.aipo.orm.model.security.TurbineUser;
import com.aipo.orm.query.SQLTemplate;
import com.aipo.orm.service.request.SearchOptions;
import com.aipo.orm.service.request.SearchOptions.FilterOperation;
import com.aipo.orm.service.request.SearchOptions.SortOrder;
import com.google.inject.Singleton;

@Singleton
public class AipoTurbineUserDbService implements TurbineUserDbService {

  public static int MAX_LIMIT = 1000;

  public int getCountByGroupname(String groupname, SearchOptions options) {
    List<DataRow> dataRows =
      queryByGroupname(groupname, options, true).fetchListAsDataRow();
    if (dataRows.size() == 1) {
      DataRow dataRow = dataRows.get(0);
      Object count = dataRow.get("count");
      if (count == null) {
        count = dataRow.get("COUNT(*)");
      }
      return ((Long) count).intValue();
    }
    return 0;
  }

  public List<TurbineUser> findByGroupname(String groupname,
      SearchOptions options) {
    SQLTemplate<TurbineUser> selectBySql =
      queryByGroupname(groupname, options, false);
    if (selectBySql == null) {
      return new ArrayList<TurbineUser>();
    }
    return selectBySql.fetchList();
  }

  public TurbineUser findByUsername(String username) {
    if (username == null) {
      return null;
    }

    StringBuilder b = new StringBuilder();
    b.append(" SELECT B.USER_ID, B.LOGIN_NAME, B.FIRST_NAME, B.LAST_NAME ");
    b.append(" FROM turbine_user AS B ");
    b.append(" WHERE B.USER_ID > 3 AND B.DISABLED = 'F' ");
    b.append(" AND B.LOGIN_NAME = #bind($username) ");

    String query = b.toString();

    return Database
      .sql(TurbineUser.class, query)
      .param("username", username)
      .fetchSingle();
  }

  public List<TurbineUser> findByUsername(Set<String> username) {
    if (username == null || username.size() == 0) {
      return null;
    }

    StringBuilder b = new StringBuilder();
    b.append(" SELECT B.USER_ID, B.LOGIN_NAME, B.FIRST_NAME, B.LAST_NAME ");
    b.append(" FROM turbine_user AS B ");
    b.append(" WHERE B.USER_ID > 3 AND B.DISABLED = 'F' ");
    b.append(" AND B.LOGIN_NAME IN(#bind($username)) ");

    String query = b.toString();

    return Database
      .sql(TurbineUser.class, query)
      .param("username", username)
      .fetchList();
  }

  public List<TurbineUser> find(SearchOptions options) {
    return buildQuery(
      " B.USER_ID, B.LOGIN_NAME, B.FIRST_NAME, B.LAST_NAME, D.POSITION ",
      options,
      false).fetchList();
  }

  public int getCount(SearchOptions options) {
    List<DataRow> dataRows =
      buildQuery(" COUNT(*) ", options, true).fetchListAsDataRow();
    if (dataRows.size() == 1) {
      DataRow dataRow = dataRows.get(0);
      Object count = dataRow.get("count");
      if (count == null) {
        count = dataRow.get("COUNT(*)");
      }
      return ((Long) count).intValue();
    }
    return 0;
  }

  protected SQLTemplate<TurbineUser> buildQuery(String selectColumns,
      SearchOptions options, boolean isCount) {

    int limit = options.getLimit();
    int offset = options.getOffset();
    if (limit > MAX_LIMIT) {
      limit = MAX_LIMIT;
    }

    StringBuilder b = new StringBuilder();
    b.append(" SELECT ");
    b.append(selectColumns);
    b.append(" FROM turbine_user AS B ");
    b.append(" LEFT JOIN eip_m_user_position AS D ");
    b.append(" ON B.USER_ID = D.USER_ID ");
    b.append(" WHERE B.USER_ID > 3 AND B.DISABLED = 'F' ");

    // Filter
    String filter = options.getFilterBy();
    FilterOperation filterOperation = options.getFilterOperation();
    String filterValue = options.getFilterValue();
    boolean isFilter = false;
    String paramKey = "filter";
    Object paramValue = null;
    // 氏名
    if ("name".equals(filter)) {
      switch (filterOperation) {
        case equals:
          if (Database.isJdbcPostgreSQL()) {
            b.append(" AND B.LAST_NAME || B.FIRST_NAME = #bind($filter) ");
          } else {
            b.append(" AND CONCAT(B.LAST_NAME,B.FIRST_NAME) = #bind($filter) ");
          }
          paramValue = filterValue;
          isFilter = true;
          break;
        case contains:
          if (Database.isJdbcPostgreSQL()) {
            b.append(" AND B.LAST_NAME || B.FIRST_NAME like #bind($filter) ");
          } else {
            b
              .append(" AND CONCAT(B.LAST_NAME,B.FIRST_NAME) like #bind($filter) ");
          }
          paramValue = "%" + filterValue + "%";
          isFilter = true;
          break;
        case present:
          // not supported.
          break;
        case startsWith:
          if (Database.isJdbcPostgreSQL()) {
            b.append(" AND B.LAST_NAME || B.FIRST_NAME like #bind($filter) ");
          } else {
            b
              .append(" AND CONCAT(B.LAST_NAME,B.FIRST_NAME) like #bind($filter) ");
          }
          paramValue = filterValue + "%";
          isFilter = true;
          break;
        default:
          break;
      }
    }

    if (!isCount) {
      // Sort
      boolean isOrder = false;
      String sort = options.getSortBy();
      SortOrder sortOrder = options.getSortOrder();
      if ("position".equals(sort)) {
        if (SortOrder.ascending.equals(sortOrder)) {
          b.append(" ORDER BY D.POSITION ");
        } else {
          b.append(" ORDER BY D.POSITION DESC ");
        }
        isOrder = true;
      }
      if (!isOrder) {
        b.append(" ORDER BY D.POSITION ");
      }

      if (limit > 0) {
        b.append(" LIMIT ");
        b.append(limit);
      }

      if (offset > 0) {
        b.append(" OFFSET ");
        b.append(offset);
      }
    }

    String query = b.toString();

    SQLTemplate<TurbineUser> sqlTemplate =
      Database.sql(TurbineUser.class, query);
    if (isFilter) {
      sqlTemplate.param(paramKey, paramValue);
    }
    return sqlTemplate;
  }

  protected SQLTemplate<TurbineUser> queryByGroupname(String groupname,
      SearchOptions options, boolean isCount) {
    if (groupname == null) {
      return null;
    }

    int limit = options.getLimit();
    int offset = options.getOffset();
    if (limit > MAX_LIMIT) {
      limit = MAX_LIMIT;
    }

    StringBuilder b = new StringBuilder();
    if (isCount) {
      b.append(" SELECT COUNT(*) ");
    } else {
      b
        .append(" SELECT B.USER_ID, B.LOGIN_NAME, B.FIRST_NAME, B.LAST_NAME, D.POSITION ");
    }
    b.append(" FROM turbine_user_group_role AS A ");
    b.append(" LEFT JOIN turbine_user AS B ");
    b.append(" ON A.USER_ID = B.USER_ID ");
    b.append(" LEFT JOIN turbine_group AS C ");
    b.append(" ON A.GROUP_ID = C.GROUP_ID ");
    b.append(" LEFT JOIN eip_m_user_position AS D ");
    b.append(" ON A.USER_ID = D.USER_ID ");
    b.append(" WHERE B.USER_ID > 3 AND B.DISABLED = 'F' ");
    b.append(" AND C.GROUP_NAME = #bind($groupname) ");

    // Filter
    String filter = options.getFilterBy();
    FilterOperation filterOperation = options.getFilterOperation();
    String filterValue = options.getFilterValue();
    boolean isFilter = false;
    String paramKey = "filter";
    Object paramValue = null;
    // 氏名
    if ("name".equals(filter)) {
      switch (filterOperation) {
        case equals:
          if (Database.isJdbcPostgreSQL()) {
            b.append(" AND B.LAST_NAME || B.FIRST_NAME = #bind($filter) ");
          } else {
            b.append(" AND CONCAT(B.LAST_NAME,B.FIRST_NAME) = #bind($filter) ");
          }
          paramValue = filterValue;
          isFilter = true;
          break;
        case contains:
          if (Database.isJdbcPostgreSQL()) {
            b.append(" AND B.LAST_NAME || B.FIRST_NAME like #bind($filter) ");
          } else {
            b
              .append(" AND CONCAT(B.LAST_NAME,B.FIRST_NAME) like #bind($filter) ");
          }
          paramValue = "%" + filterValue + "%";
          isFilter = true;
          break;
        case present:
          // not supported.
          break;
        case startsWith:
          if (Database.isJdbcPostgreSQL()) {
            b.append(" AND B.LAST_NAME || B.FIRST_NAME like #bind($filter) ");
          } else {
            b
              .append(" AND CONCAT(B.LAST_NAME,B.FIRST_NAME) like #bind($filter) ");
          }
          paramValue = filterValue + "%";
          isFilter = true;
          break;
        default:
          break;
      }
    }

    if (!isCount) {
      // Sort
      boolean isOrder = false;
      String sort = options.getSortBy();
      SortOrder sortOrder = options.getSortOrder();
      if ("position".equals(sort)) {
        if (SortOrder.ascending.equals(sortOrder)) {
          b.append(" ORDER BY D.POSITION ");
        } else {
          b.append(" ORDER BY D.POSITION DESC ");
        }
        isOrder = true;
      }
      if (!isOrder) {
        b.append(" ORDER BY D.POSITION ");
      }

      if (limit > 0) {
        b.append(" LIMIT ");
        b.append(limit);
      }

      if (offset > 0) {
        b.append(" OFFSET ");
        b.append(offset);
      }
    }

    String query = b.toString();

    SQLTemplate<TurbineUser> sqlTemplate =
      Database.sql(TurbineUser.class, query).param("groupname", groupname);
    if (isFilter) {
      sqlTemplate.param(paramKey, paramValue);
    }

    return sqlTemplate;
  }
}