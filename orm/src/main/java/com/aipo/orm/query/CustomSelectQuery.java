/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2011 Aimluck,Inc.
 * http://www.aipo.com/
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

import java.util.ArrayList;
import java.util.List;

import org.apache.cayenne.access.jdbc.ColumnDescriptor;
import org.apache.cayenne.exp.Expression;

/**
 * 
 */
public class CustomSelectQuery extends AbstractCustomQuery {

  private static final long serialVersionUID = -3136415467464119829L;

  private final List<String> fetchColumns = new ArrayList<String>();

  public CustomSelectQuery() {
    super();
  }

  public CustomSelectQuery(Class<?> rootClass) {
    super(rootClass);
  }

  /**
   * @param rootClass
   * @param qualifier
   */
  public CustomSelectQuery(Class<?> rootClass, Expression qualifier) {
    super(rootClass, qualifier);
  }

  @Override
  protected String getCustomScript() {
    if (fetchColumns.size() == 0) {
      return null;
    }
    boolean isFirst = true;
    StringBuilder b = new StringBuilder();
    if (isDistinct()) {
      b.append("DISTINCT ");
    }
    for (String column : fetchColumns) {
      if (!isFirst) {
        b.append(", ");
      } else {
        isFirst = false;
      }
      b.append(column);
    }
    return b.toString();
  }

  @Override
  protected ColumnDescriptor[] getCustomColumnDescriptor() {
    return null;
  }

  @Override
  protected String[] getCustomColumnNames() {
    return fetchColumns.toArray(new String[fetchColumns.size()]);
  }

  public void addCustomColumn(String column) {
    fetchColumns.add(column);
  }

  public void addCustomColumns(String... columns) {
    for (String column : columns) {
      fetchColumns.add(column);
    }
  }

}
