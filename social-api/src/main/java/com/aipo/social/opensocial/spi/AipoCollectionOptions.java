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
package com.aipo.social.opensocial.spi;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.shindig.protocol.RequestItem;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;

import com.google.common.base.Functions;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;

/**
 *
 */
public class AipoCollectionOptions extends CollectionOptions {

  public static enum Parameter {
    KEYWORD("keyword"), UNTIL_ID("until_id"), SINCE_ID("since_id"), PRIORITY(
        "priority"), EXTERNAL_ID("externalId");

    private static final Map<String, Parameter> LOOKUP = Maps.uniqueIndex(
      EnumSet.allOf(Parameter.class),
      Functions.toStringFunction());

    public static final Set<String> ALL_PARAMETERS = LOOKUP.keySet();

    private String value;

    private Parameter(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return this.value;
    }

    public static Parameter getParameter(String key) {
      return LOOKUP.get(key);
    }
  }

  private final Map<String, String> map = new HashMap<String, String>();

  public AipoCollectionOptions(RequestItem request) {
    super(request);

    Iterator<String> iterator = Parameter.ALL_PARAMETERS.iterator();
    while (iterator.hasNext()) {
      String next = iterator.next();
      String value = request.getParameter(next);
      if (!StringUtils.isEmpty(value)) {
        map.put(next, value);
      }
    }
  }

  /**
   *
   * @param key
   * @return
   */
  public String getParameter(String key) {
    return map.get(key);
  }

  public Map<String, String> getParameters() {
    return map;
  }

  public Integer getParameterInt(String key) {
    try {
      return Integer.valueOf(map.get(key));
    } catch (Throwable ignore) {
      //
    }
    return null;
  }

  /**
   *
   * @param key
   * @param value
   */
  public void set(String key, String value) {
    map.put(key, value);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    AipoCollectionOptions actual = (AipoCollectionOptions) o;
    return super.equals(o) && (this.map.equals(actual.map));
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(new Object[] {
      getSortBy(),
      getSortOrder(),
      getFilter(),
      getFilterOperation(),
      getFilterValue(),
      Integer.valueOf(getFirst()),
      Integer.valueOf(getMax()) }, this.map);
  }
}
