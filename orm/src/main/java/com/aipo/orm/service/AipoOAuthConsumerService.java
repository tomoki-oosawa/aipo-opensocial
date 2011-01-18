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

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.access.DataContext;

import com.aipo.orm.Database;
import com.aipo.orm.model.social.Application;
import com.aipo.orm.query.Operations;
import com.aipo.orm.service.bean.OAuthConsumer;
import com.google.inject.Singleton;

@Singleton
public class AipoOAuthConsumerService implements OAuthConsumerService {

  public OAuthConsumer get(String appId, String serviceName) {

    selectDefaultDataDomain();

    Application app =
      Database.query(Application.class).where(
        Operations.eq(Application.APP_ID_PROPERTY, appId)).fetchSingle();

    if (app == null) {
      return null;
    }

    List<com.aipo.orm.model.social.OAuthConsumer> list = app.getOauthConsumer();

    for (com.aipo.orm.model.social.OAuthConsumer service : list) {
      if (serviceName.equals(service.getName())) {
        OAuthConsumer consumer = new OAuthConsumer();
        consumer.setConsumerKey(service.getConsumerKey());
        consumer.setConsumerSecret(service.getConsumerSecret());
        consumer.setName(service.getName());
        consumer.setType(service.getType());
        return consumer;
      }
    }

    return null;
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