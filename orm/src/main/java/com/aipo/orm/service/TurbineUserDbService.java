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

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import com.aipo.orm.model.security.TurbineUser;
import com.aipo.orm.service.request.SearchOptions;

public interface TurbineUserDbService {

  public int getCountByGroupname(String groupname, SearchOptions options);

  public List<TurbineUser> findByGroupname(String groupname,
      SearchOptions options);

  public TurbineUser findByUsername(String username);

  public TurbineUser findByUsernameWithDisabled(String username);

  public List<TurbineUser> findByUsername(Set<String> username);

  public List<TurbineUser> findByUsernameWithDisabled(Set<String> username);

  public List<TurbineUser> find(SearchOptions options);

  public int getCount(SearchOptions options);

  public TurbineUser auth(String username, String password);

  public InputStream getPhoto(String username);

  public void setPhoto(String username, byte[] profileIcon,
      byte[] profileIconSmartPhone);
}
