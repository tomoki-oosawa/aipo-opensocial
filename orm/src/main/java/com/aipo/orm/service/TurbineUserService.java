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

import java.util.List;

import com.aipo.orm.model.security.TurbineUser;
import com.aipo.orm.query.SQLTemplate;

public interface TurbineUserService {

  public SQLTemplate<TurbineUser> queryByGroupname(String groupname, int limit,
      int offset, boolean isCount);

  public int getCountByGroupname(String groupname);

  public List<TurbineUser> findByGroupname(String groupname);

  public List<TurbineUser> findByGroupname(String groupname, int limit,
      int offset);

  public TurbineUser findByUsername(String username);

  public SQLTemplate<TurbineUser> queryAll(String selectColumns, int limit,
      int offset, boolean isCount);

  public List<TurbineUser> findAll();

  public List<TurbineUser> findAll(int limit, int offset);

  public int getCountAll();
}