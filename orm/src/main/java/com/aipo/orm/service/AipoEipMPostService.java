/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2010 Aimluck,Inc.
 * http://aipostyle.com/
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

import com.aipo.orm.Database;
import com.aipo.orm.model.account.EipMPost;
import com.google.inject.Singleton;

@Singleton
public class AipoEipMPostService implements EipMPostService {

  public List<EipMPost> findAll() {
    return Database
      .query(EipMPost.class)
      .orderAscending("postName")
      .fetchList();
  }

  public List<EipMPost> findAll(int limit, int offset) {
    return Database
      .query(EipMPost.class)
      .limit(limit)
      .offset(offset)
      .orderAscending("postName")
      .fetchList();
  }

  public int getCountAll() {
    return Database.query(EipMPost.class).getCount();
  }
}