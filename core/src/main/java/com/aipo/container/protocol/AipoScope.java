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

package com.aipo.container.protocol;

import java.util.EnumSet;
import java.util.Map;

import com.google.common.base.Functions;
import com.google.common.collect.Maps;

/**
 *
 */
public enum AipoScope {

  W_ALL("w_all"), R_ALL("r_all");

  private static final Map<String, AipoScope> lookUp = Maps.uniqueIndex(EnumSet
    .allOf(AipoScope.class), Functions.toStringFunction());

  private String value;

  private AipoScope(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return this.value;
  }

  public static AipoScope getScope(String value) {
    return lookUp.get(value);
  }
}
