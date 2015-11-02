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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.cayenne.BaseContext;
import org.apache.cayenne.DataRow;
import org.apache.cayenne.access.DataContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.SortOrder;

public class SelectQuery<M> extends AbstractQuery<M> {

  protected CustomSelectQuery delegate;

  protected CountQuery countQuery;

  protected int page = 1;

  public SelectQuery(Class<M> rootClass) {
    super(rootClass);
    delegate = new CustomSelectQuery(rootClass);
    countQuery = new CountQuery(rootClass);
    dataContext = (DataContext) BaseContext.getThreadObjectContext();
  }

  public SelectQuery(DataContext dataContext, Class<M> rootClass) {
    super(dataContext, rootClass);
    this.rootClass = rootClass;
    delegate = new CustomSelectQuery(rootClass);
    countQuery = new CountQuery(rootClass);
    this.dataContext = dataContext;
  }

  public SelectQuery(Class<M> rootClass, Expression qualifier) {
    super(rootClass);

    this.rootClass = rootClass;
    delegate = new CustomSelectQuery(rootClass, qualifier);
    countQuery = new CountQuery(rootClass);
    dataContext = (DataContext) BaseContext.getThreadObjectContext();
  }

  public SelectQuery(DataContext dataContext, Class<M> rootClass,
      Expression qualifier) {
    super(dataContext, rootClass);
    this.rootClass = rootClass;
    delegate = new CustomSelectQuery(rootClass, qualifier);
    countQuery = new CountQuery(rootClass);
    this.dataContext = dataContext;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<M> fetchList() {
    if (delegate.isFetchingDataRows()) {
      List<DataRow> dataRows = dataContext.performQuery(delegate);
      List<M> results = new ArrayList<M>();

      for (DataRow dataRow : dataRows) {
        M model = newInstanceFromRowData(dataRow, rootClass);
        if (model != null) {
          results.add(model);
        }
      }
      return results;
    } else {
      return dataContext.performQuery(delegate);
    }
  }

  public ResultList<M> getResultList() {
    countQuery.setCustomColumns(getPrimaryKey());
    int totalCount = countQuery.count(dataContext, delegate.isDistinct());
    int pageSize = delegate.getFetchLimit();
    if (pageSize > 0) {
      int num = ((int) (Math.ceil(totalCount / (double) pageSize)));
      if ((num > 0) && (num < page)) {
        page = num;
      }
      int offset = pageSize * (page - 1);
      offset(offset);
    } else {
      page = 1;
    }
    List<M> fetchList = fetchList();
    return new ResultList<M>(
      fetchList,
      page,
      delegate.getPageSize(),
      totalCount);
  }

  public int getCount() {
    countQuery.setCustomColumns(getPrimaryKey());
    return countQuery.count(dataContext, delegate.isDistinct());
  }

  public SelectQuery<M> where(Where where) {
    delegate.andQualifier(where.exp);
    countQuery.andQualifier(where.exp);
    return this;
  }

  public SelectQuery<M> where(Where... where) {
    List<Where> list = Arrays.asList(where);
    for (Where w : list) {
      delegate.andQualifier(w.exp);
      countQuery.andQualifier(w.exp);
    }
    return this;
  }

  public SelectQuery<M> setQualifier(Expression qualifier) {
    delegate.setQualifier(qualifier);
    countQuery.setQualifier(qualifier);
    return this;
  }

  public SelectQuery<M> andQualifier(Expression qualifier) {
    delegate.andQualifier(qualifier);
    countQuery.andQualifier(qualifier);
    return this;
  }

  public SelectQuery<M> andQualifier(String qualifier) {
    delegate.andQualifier(Expression.fromString(qualifier));
    countQuery.andQualifier(Expression.fromString(qualifier));
    return this;
  }

  public SelectQuery<M> orQualifier(Expression qualifier) {
    delegate.orQualifier(qualifier);
    countQuery.orQualifier(qualifier);
    return this;
  }

  public SelectQuery<M> orQualifier(String qualifier) {
    delegate.orQualifier(Expression.fromString(qualifier));
    countQuery.orQualifier(Expression.fromString(qualifier));
    return this;
  }

  public SelectQuery<M> orderAscending(String ordering) {
    delegate.addOrdering(ordering, SortOrder.ASCENDING);
    return this;
  }

  public SelectQuery<M> orderDesending(String ordering) {
    delegate.addOrdering(ordering, SortOrder.DESCENDING);
    return this;
  }

  public SelectQuery<M> page(int page) {
    this.page = page;
    return this;
  }

  public SelectQuery<M> limit(int limit) {
    delegate.setLimit(limit);
    return this;
  }

  public SelectQuery<M> offset(int offset) {
    delegate.setOffset(offset);
    return this;
  }

  public SelectQuery<M> select(String column) {
    delegate.addCustomColumn(column);
    delegate.setFetchingDataRows(true);
    // delegate.addCustomDbAttribute(column);
    return this;
  }

  public SelectQuery<M> select(String... columns) {
    delegate.addCustomColumns(columns);
    delegate.setFetchingDataRows(true);
    // delegate.addCustomDbAttributes(Arrays.asList(columns));
    return this;
  }

  public SelectQuery<M> distinct() {
    delegate.setDistinct(true);
    countQuery.setDistinct(true);
    return this;
  }

  public SelectQuery<M> distinct(boolean isDistinct) {
    delegate.setDistinct(isDistinct);
    countQuery.setDistinct(isDistinct);
    return this;
  }

  public SelectQuery<M> prefetch(String column) {
    delegate.addPrefetch(column);
    return this;
  }

  public org.apache.cayenne.query.SelectQuery getQuery() {
    return delegate;
  }

  protected String getPrimaryKey() {
    ObjEntity objEntity =
      dataContext.getEntityResolver().lookupObjEntity(rootClass);
    List<DbAttribute> primaryKey =
      new ArrayList<DbAttribute>(objEntity.getDbEntity().getPrimaryKeys());
    DbAttribute dbAttribute = primaryKey.get(0);
    return dbAttribute.getName();
  }
}
