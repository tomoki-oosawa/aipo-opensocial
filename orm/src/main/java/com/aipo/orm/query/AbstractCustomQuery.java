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

package com.aipo.orm.query;

import org.apache.cayenne.access.jdbc.ColumnDescriptor;
import org.apache.cayenne.dba.JdbcActionBuilder;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.query.SQLAction;
import org.apache.cayenne.query.SQLActionVisitor;

import com.aipo.orm.access.jdbc.CustomSelectAction;

/**
 * 
 */
public abstract class AbstractCustomQuery extends
    org.apache.cayenne.query.SelectQuery {

  private static final long serialVersionUID = -5620726626804050595L;

  public AbstractCustomQuery() {
    super();
  }

  public AbstractCustomQuery(Class<?> rootClass) {
    super(rootClass);
  }

  /**
   * @param rootClass
   * @param qualifier
   */
  public AbstractCustomQuery(Class<?> rootClass, Expression qualifier) {
    super(rootClass, qualifier);
  }

  @Override
  public SQLAction createSQLAction(SQLActionVisitor visitor) {
    if (visitor instanceof JdbcActionBuilder) {
      JdbcActionBuilder builder = (JdbcActionBuilder) visitor;
      String customScript = getCustomScript();
      ColumnDescriptor[] columns = getCustomColumnDescriptor();
      String[] columnNames = getCustomColumnNames();
      int limit = getFetchLimit();
      int offset = getFetchOffset();
      if (customScript != null || offset > 0 || limit > 0) {
        return new CustomSelectAction(
          this,
          builder.getAdapter(),
          builder.getEntityResolver(),
          customScript,
          columns,
          columnNames,
          limit,
          offset);
      }
    }
    return super.createSQLAction(visitor);
  }

  // protected abstract int getFetchOffset();

  protected abstract String getCustomScript();

  protected abstract ColumnDescriptor[] getCustomColumnDescriptor();

  protected abstract String[] getCustomColumnNames();
}
