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

package com.aipo.orm.query;

import java.util.Map;

import org.apache.cayenne.CayenneException;
import org.apache.cayenne.access.DataContext;
import org.apache.cayenne.access.ResultIterator;
import org.apache.cayenne.access.jdbc.ColumnDescriptor;
import org.apache.cayenne.dba.TypesMapping;
import org.apache.log4j.Logger;

/**
 * 
 */
public class CountQuery extends AbstractCustomQuery {

  private static final long serialVersionUID = 7286490109199978350L;

  private static final Logger logger = Logger.getLogger(CountQuery.class
    .getName());

  private boolean isDistinct = false;

  private String customColumns;

  public CountQuery() {
    super();
  }

  public CountQuery(Class<?> rootClass) {
    super(rootClass);
  }

  public int count(DataContext dataContext) {
    return count(dataContext, isDistinct);
  }

  @SuppressWarnings("unchecked")
  public int count(DataContext dataContext, boolean isDistinct) {
    this.isDistinct = isDistinct;
    int ret = 0;
    Map<String, Integer> row = null;
    try {
      ResultIterator it = dataContext.performIteratedQuery(this);
      if (it != null && it.hasNextRow()) {
        row = (Map<String, Integer>) it.nextRow();
      }
      if (row != null) {
        ret = row.get("C");
      }
    } catch (CayenneException e) {
      logger.error(e.getMessage(), e);
    }
    return ret;
  }

  public void setCustomColumns(String customColumns) {
    this.customColumns = customColumns;
  }

  @Override
  protected String getCustomScript() {
    if (customColumns == null) {
      return "COUNT(*) AS C";
    }
    if (isDistinct) {
      return "COUNT(DISTINCT t0." + customColumns + ") AS C";
    } else {
      return "COUNT(*) AS C";
    }
  }

  @Override
  protected ColumnDescriptor[] getCustomColumnDescriptor() {
    return new ColumnDescriptor[] { new ColumnDescriptor("C", TypesMapping
      .getSqlTypeByJava(Integer.class), TypesMapping.JAVA_INTEGER) };
  }

  @Override
  protected String[] getCustomColumnNames() {
    return null;
  }
}
