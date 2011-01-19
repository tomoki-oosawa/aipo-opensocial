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

package com.aipo.orm.access.trans;

import java.util.ArrayList;
import java.util.List;

import org.apache.cayenne.access.jdbc.ColumnDescriptor;
import org.apache.cayenne.access.trans.SelectTranslator;

/**
 * 
 */
public class CustomSelectTranslator extends SelectTranslator {

  private String customScript;

  private ColumnDescriptor[] columns;

  private String[] columnNames;

  private int offset;

  private int limit;

  @Override
  public String createSqlString() throws Exception {
    String ret = null;
    ret = super.createSqlString();
    String postStr = "";
    if (limit > 0) {
      postStr += " LIMIT " + limit;
    }
    if (offset > 0) {
      postStr += " OFFSET " + offset;
    }
    if (customScript == null) {
      return ret + postStr;
    }
    int fromIndex = ret.indexOf(" FROM ");
    ret = String.format("SELECT %s %s", customScript, ret.substring(fromIndex));
    return ret + postStr;
  }

  @Override
  public ColumnDescriptor[] getResultColumns() {
    if (columnNames == null || columnNames.length == 0) {
      if (columns == null || columns.length == 0) {
        return super.getResultColumns();
      } else {
        return columns;
      }
    } else {
      ColumnDescriptor[] resultColumns = super.getResultColumns();
      List<ColumnDescriptor> list = new ArrayList<ColumnDescriptor>();
      for (String columnName : columnNames) {
        for (ColumnDescriptor resultColumn : resultColumns) {
          if (resultColumn.getName().equalsIgnoreCase(columnName)) {
            list.add(resultColumn);
            break;
          }
        }
      }
      return list.toArray(new ColumnDescriptor[list.size()]);
    }
  }

  public void setCustomScript(String customScript) {
    this.customScript = customScript;
  }

  public void setCustomColumns(ColumnDescriptor[] columns) {
    this.columns = columns;
  }

  public void setCustomColumnNames(String[] columnNames) {
    this.columnNames = columnNames;
  }

  public void setFetchOffset(int offset) {
    this.offset = offset;
  }

  public void setFetchLimit(int limit) {
    this.limit = limit;
  }
}