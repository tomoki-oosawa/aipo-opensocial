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

import com.aipo.orm.Database;
import com.aipo.orm.model.social.ContainerConfig;
import com.aipo.orm.query.Operations;

public class ContainerConfigService {

  public static enum Property {

    LOCKED_DOMAIN_REQUIRED("lockedDomainRequired") {

      @Override
      public String defaultValue() {
        return "false";
      }
    },

    LOCKED_DOMAIN_SUFFIX("lockedDomainSuffix") {
      @Override
      public String defaultValue() {
        return ".example.com";
      }
    },
    UNLOCKED_DOMAIN("unLockedDomain") {
      @Override
      public String defaultValue() {
        return "www.example.com";
      }
    };

    private final String property;

    private Property(String property) {
      this.property = property;
    }

    @Override
    public String toString() {
      return this.property;
    }

    public abstract String defaultValue();
  }

  public String get(Property key) {

    ContainerConfig config =
      Database
        .query(ContainerConfig.class)
        .where(Operations.eq(ContainerConfig.KEY_PROPERTY, key.toString()))
        .fetchSingle();

    if (config == null) {
      return key.defaultValue();
    }

    return config.getValue();
  }

  public void put(Property key, String value) {
    try {
      ContainerConfig config =
        Database
          .query(ContainerConfig.class)
          .where(Operations.eq(ContainerConfig.KEY_PROPERTY, key.toString()))
          .fetchSingle();
      if (config == null) {
        config = Database.create(ContainerConfig.class);
        config.setKey(key.toString());
      }
      config.setValue(value);
      Database.commit();
    } catch (Throwable t) {
      Database.rollback();
      throw new RuntimeException(t);
    }
  }
}