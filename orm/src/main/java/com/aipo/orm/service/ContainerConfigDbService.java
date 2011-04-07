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

public interface ContainerConfigDbService {

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
        return "";
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

  public String get(Property key);

  public void put(Property key, String value);
}