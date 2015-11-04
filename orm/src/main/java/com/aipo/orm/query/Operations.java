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
package com.aipo.orm.query;

import java.util.List;

/**
 *
 *
 */
public class Operations {

  /**
   * 
   * @param property
   * @param value
   * @return
   */
  public static Where eq(String property, Object value) {
    return new Where().eq(property, value);
  }

  /**
   * 
   * @param property
   * @param value
   * @return
   */
  public static Where ne(String property, Object value) {
    return new Where().ne(property, value);
  }

  /**
   * 
   * @param property
   * @param value
   * @return
   */
  public static Where lt(String property, Object value) {
    return new Where().lt(property, value);
  }

  /**
   * 
   * @param property
   * @param value
   * @return
   */
  public static Where le(String property, Object value) {
    return new Where().le(property, value);
  }

  /**
   * 
   * @param property
   * @param value
   * @return
   */
  public static Where gt(String property, Object value) {
    return new Where().gt(property, value);
  }

  /**
   * 
   * @param property
   * @param value
   * @return
   */
  public static Where ge(String property, Object value) {
    return new Where().ge(property, value);
  }

  /**
   * 
   * @param property
   * @param values
   * @return
   */
  public static Where in(String property, Object... values) {
    return new Where().in(property, values);
  }

  /**
   * 
   * @param property
   * @param values
   * @return
   */
  public static Where in(String property, List<?> values) {
    return new Where().in(property, values);
  }

  /**
   * 
   * @param property
   * @param values
   * @return
   */
  public static Where notIn(String property, Object... values) {
    return new Where().notIn(property, values);
  }

  /**
   * 
   * @param property
   * @param values
   * @return
   */
  public static Where notIn(String property, List<?> values) {
    return new Where().notIn(property, values);
  }

  /**
   * 
   * @param property
   * @param value
   * @return
   */
  public static Where like(String property, Object value) {
    return new Where().like(property, value);
  }

  /**
   * 
   * @param property
   * @param value
   * @return
   */
  public static Where contains(String property, Object value) {
    return new Where().contains(property, value);
  }

  /**
   * 
   * @param property
   * @param value
   * @return
   */
  public static Where startWith(String property, Object value) {
    return new Where().startWith(property, value);
  }

  /**
   * 
   * @param property
   * @param value
   * @return
   */
  public static Where endWith(String property, Object value) {
    return new Where().endWith(property, value);
  }

  /**
   * 
   * @param where
   * @return
   */
  public static Where and(Where... where) {
    Where ww = new Where();
    for (Where w : where) {
      ww.and(w);
    }
    return ww;
  }

  /**
   * 
   * @param where
   * @return
   */
  public static Where or(Where... where) {
    Where ww = new Where();
    for (Where w : where) {
      ww.or(w);
    }
    return ww;
  }
}
