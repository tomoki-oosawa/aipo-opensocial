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

package com.aipo.orm.service;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.access.DataContext;

import com.aipo.orm.Database;
import com.aipo.orm.model.social.Application;
import com.aipo.orm.query.Operations;
import com.google.inject.Singleton;

@Singleton
public class AipoApplicationDbService implements ApplicationDbService {

  /**
   * @param consumerKey
   * @return
   */
  public String getConsumerSecret(String consumerKey) {

    selectDefaultDataDomain();
    Application app =
      Database
        .query(Application.class)
        .where(Operations.eq(Application.CONSUMER_KEY_PROPERTY, consumerKey))
        .fetchSingle();
    if (app == null) {
      return null;
    }
    return app.getConsumerSecret();
  }

  /**
   * @param appId
   * @return
   */
  public Application get(String appId) {
    selectDefaultDataDomain();

    Application app = Database.get(Application.class, appId);
    if (app == null) {
      return null;
    }
    return app;
  }

  private void selectDefaultDataDomain() {
    ObjectContext dataContext = null;
    try {
      dataContext = DataContext.getThreadObjectContext();
    } catch (IllegalStateException ignore) {
      // first
    }
    if (dataContext == null) {
      try {
        dataContext = Database.createDataContext("org001");
        DataContext.bindThreadObjectContext(dataContext);
      } catch (Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

}