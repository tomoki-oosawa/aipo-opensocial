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

import org.apache.cayenne.CayenneRuntimeException;

import com.aipo.orm.Database;
import com.aipo.orm.model.account.EipMConfig;
import com.aipo.orm.query.Operations;

/**
 *
 */
public class AipoConfigDbService implements ConfigDbService {

  /**
   * @param property
   * @param value
   */
  @Override
  public void put(String property, String value) {
    try {
      EipMConfig config =
        Database.query(EipMConfig.class).where(
          Operations.eq(EipMConfig.NAME_PROPERTY, property)).fetchSingle();
      if (config == null) {
        config = Database.create(EipMConfig.class);
        config.setName(property.toString());
      }
      config.setValue(value);
      Database.commit();
    } catch (Throwable t) {
      Database.rollback();
      if (t instanceof CayenneRuntimeException) {
        throw (CayenneRuntimeException) t;
      } else {
        throw new RuntimeException(t);
      }
    }
  }

  /**
   * @param property
   * @param defaultValue
   * @return
   */
  @Override
  public String get(String property, String defaultValue) {

    EipMConfig config = null;
    try {
      config =
        Database
          .query(EipMConfig.class)
          .where(Operations.eq(EipMConfig.NAME_PROPERTY, property.toString()))
          .fetchSingle();

    } catch (CayenneRuntimeException e) {
      throw e;
    } catch (Exception ignore) {
    }

    if (config == null) {
      return defaultValue;
    }
    return config.getValue();
  }
}
