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

package com.aipo.orm.access.jdbc;

import java.sql.Connection;

import org.apache.cayenne.access.jdbc.ColumnDescriptor;
import org.apache.cayenne.access.jdbc.SelectAction;
import org.apache.cayenne.access.trans.SelectTranslator;
import org.apache.cayenne.dba.DbAdapter;
import org.apache.cayenne.map.EntityResolver;

import com.aipo.orm.access.trans.CustomSelectTranslator;
import com.aipo.orm.query.AbstractCustomQuery;

/**
 * 
 */
public class CustomSelectAction extends SelectAction {

  private final String customScript;

  private final ColumnDescriptor[] columns;

  private final String[] columnNames;

  private final int offset;

  private final int limit;

  public CustomSelectAction(AbstractCustomQuery arg0, DbAdapter arg1,
      EntityResolver arg2, String customScript, ColumnDescriptor[] columns,
      String[] columnNames, int limit, int offset) {
    super(arg0, arg1, arg2);
    this.customScript = customScript;
    this.columns = columns;
    this.columnNames = columnNames;
    this.offset = offset;
    this.limit = limit;
  }

  @Override
  protected SelectTranslator createTranslator(Connection connection) {
    CustomSelectTranslator translator = new CustomSelectTranslator();
    translator.setQuery(query);
    translator.setAdapter(adapter);
    translator.setEntityResolver(getEntityResolver());
    translator.setConnection(connection);
    translator.setCustomScript(customScript);
    translator.setCustomColumns(columns);
    translator.setCustomColumnNames(columnNames);
    translator.setFetchOffset(offset);
    translator.setFetchLimit(limit);
    return translator;
  }
}
