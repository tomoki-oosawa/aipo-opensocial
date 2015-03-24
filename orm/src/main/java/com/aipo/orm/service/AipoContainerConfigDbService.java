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

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.access.DataContext;

import com.aipo.orm.Database;
import com.aipo.orm.model.social.ContainerConfig;
import com.aipo.orm.query.Operations;
import com.google.inject.Singleton;

@Singleton
public class AipoContainerConfigDbService implements ContainerConfigDbService {

  public String get(Property key) {

    selectDefaultDataDomain();
    ContainerConfig config =
      Database
        .query(ContainerConfig.class)
        .where(Operations.eq(ContainerConfig.NAME_PROPERTY, key.toString()))
        .fetchSingle();

    if (config == null) {
      return key.defaultValue();
    }

    return config.getValue();
  }

  public void put(Property key, String value) {
    try {
      selectDefaultDataDomain();
      ContainerConfig config =
        Database
          .query(ContainerConfig.class)
          .where(Operations.eq(ContainerConfig.NAME_PROPERTY, key.toString()))
          .fetchSingle();
      if (config == null) {
        config = Database.create(ContainerConfig.class);
        config.setName(key.toString());
      }
      config.setValue(value);
      Database.commit();
    } catch (Throwable t) {
      Database.rollback();
      throw new RuntimeException(t);
    }
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