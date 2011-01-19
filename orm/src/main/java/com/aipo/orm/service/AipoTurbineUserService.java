/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2011 Aimluck,Inc.
 * http://www.aipo.com/
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

import org.apache.cayenne.DataRow;

import com.aipo.orm.Database;
import com.aipo.orm.model.security.TurbineUser;
import com.aipo.orm.query.SQLTemplate;
import com.google.inject.Singleton;

@Singleton
public class AipoTurbineUserService implements TurbineUserService {

  public SQLTemplate<TurbineUser> queryByGroupname(String groupname, int limit,
      int offset, boolean isCount) {
    if (groupname == null) {
      return null;
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
    if (!isCount) {
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

    String query = b.toString();

    return Database.sql(TurbineUser.class, query).param("groupname", groupname);
  }

  public int getCountByGroupname(String groupname) {
    List<DataRow> dataRows =
      queryByGroupname(groupname, -1, -1, true).fetchListAsDataRow();
    if (dataRows.size() == 1) {
      DataRow dataRow = dataRows.get(0);
      return ((Long) dataRow.get("count")).intValue();
    }
    return 0;
  }

  public List<TurbineUser> findByGroupname(String groupname) {
    SQLTemplate<TurbineUser> selectBySql =
      queryByGroupname(groupname, -1, -1, false);
    if (selectBySql == null) {
      return new ArrayList<TurbineUser>(0);
    }
    return selectBySql.fetchList();
  }

  public List<TurbineUser> findByGroupname(String groupname, int limit,
      int offset) {
    SQLTemplate<TurbineUser> selectBySql =
      queryByGroupname(groupname, limit, offset, false);
    if (selectBySql == null) {
      return new ArrayList<TurbineUser>(0);
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

  public SQLTemplate<TurbineUser> queryAll(String selectColumns, int limit,
      int offset, boolean isCount) {

    StringBuilder b = new StringBuilder();
    b.append(" SELECT ");
    b.append(selectColumns);
    b.append(" FROM turbine_user AS B ");
    b.append(" LEFT JOIN eip_m_user_position AS D ");
    b.append(" ON B.USER_ID = D.USER_ID ");
    b.append(" WHERE B.USER_ID > 3 AND B.DISABLED = 'F' ");
    if (!isCount) {
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

    String query = b.toString();

    return Database.sql(TurbineUser.class, query);
  }

  public List<TurbineUser> findAll() {
    return queryAll(
      " B.USER_ID, B.LOGIN_NAME, B.FIRST_NAME, B.LAST_NAME, D.POSITION ",
      -1,
      -1,
      false).fetchList();
  }

  public List<TurbineUser> findAll(int limit, int offset) {
    return queryAll(
      " B.USER_ID, B.LOGIN_NAME, B.FIRST_NAME, B.LAST_NAME, D.POSITION ",
      limit,
      offset,
      false).fetchList();
  }

  public int getCountAll() {
    List<DataRow> dataRows =
      queryAll(" COUNT(*) ", -1, -1, true).fetchListAsDataRow();
    if (dataRows.size() == 1) {
      DataRow dataRow = dataRows.get(0);
      return ((Long) dataRow.get("count")).intValue();
    }
    return 0;
  }
}