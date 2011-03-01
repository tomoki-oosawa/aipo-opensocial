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

package com.aipo.orm;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.apache.cayenne.BaseContext;
import org.apache.cayenne.CayenneException;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.DataObjectUtils;
import org.apache.cayenne.DataRow;
import org.apache.cayenne.ObjectId;
import org.apache.cayenne.Persistent;
import org.apache.cayenne.access.DataContext;
import org.apache.cayenne.access.DataDomain;
import org.apache.cayenne.access.DataNode;
import org.apache.cayenne.access.Transaction;
import org.apache.cayenne.conf.Configuration;
import org.apache.cayenne.conf.CustomDBCPDataSourceFactory;
import org.apache.cayenne.conf.DBCPDataSourceFactory;
import org.apache.cayenne.dba.AutoAdapter;
import org.apache.cayenne.exp.Expression;
import org.apache.log4j.Logger;

import com.aipo.orm.query.SQLTemplate;
import com.aipo.orm.query.SelectQuery;

/**
 * データベース操作ユーティリティ
 * 
 */
public class Database {

  private static final Logger logger = Logger.getLogger(Database.class
    .getName());

  protected static final String SHARED_DOMAIN = "SharedDomain";

  /**
   * 検索用クエリを作成します。
   * 
   * @param <M>
   * @param modelClass
   * @return
   */
  public static <M> SelectQuery<M> query(Class<M> modelClass) {
    return new SelectQuery<M>(modelClass);
  }

  /**
   * 検索用クエリを作成します。
   * 
   * @param <M>
   * @param dataContext
   * @param modelClass
   * @return
   */
  public static <M> SelectQuery<M> query(DataContext dataContext,
      Class<M> modelClass) {
    return new SelectQuery<M>(dataContext, modelClass);
  }

  /**
   * 検索用クエリを作成します。
   * 
   * @param <M>
   * @param modelClass
   * @param exp
   * @return
   */
  public static <M> SelectQuery<M> query(Class<M> modelClass, Expression exp) {
    return new SelectQuery<M>(modelClass, exp);
  }

  /**
   * 検索用クエリを作成します。
   * 
   * @param <M>
   * @param dataContext
   * @param modelClass
   * @param exp
   * @return
   */
  public static <M> SelectQuery<M> query(DataContext dataContext,
      Class<M> modelClass, Expression exp) {
    return new SelectQuery<M>(dataContext, modelClass, exp);
  }

  /**
   * SQL検索クエリを作成します。
   * 
   * @param <M>
   * @param modelClass
   * @param sql
   * @return
   */
  public static <M> SQLTemplate<M> sql(Class<M> modelClass, String sql) {
    return new SQLTemplate<M>(modelClass, sql);
  }

  /**
   * SQL検索クエリを作成します。
   * 
   * @param <M>
   * @param dataContext
   * @param modelClass
   * @param sql
   * @return
   */
  public static <M> SQLTemplate<M> sql(DataContext dataContext,
      Class<M> modelClass, String sql) {
    return new SQLTemplate<M>(dataContext, modelClass, sql);
  }

  /**
   * プライマリキーで指定されたオブジェクトモデルを取得します。
   * 
   * @param <M>
   * @param modelClass
   * @param primaryKey
   * @return
   */
  public static <M> M get(Class<M> modelClass, Object primaryKey) {
    return get(
      (DataContext) BaseContext.getThreadObjectContext(),
      modelClass,
      primaryKey);
  }

  /**
   * 指定されたオブジェクトモデルを取得します。
   * 
   * @param <M>
   * @param dataContext
   * @param modelClass
   * @param primaryKey
   * @return
   */
  public static <M> M get(DataContext dataContext, Class<M> modelClass,
      Object primaryKey) {
    return DataObjectUtils.objectForPK(dataContext, modelClass, primaryKey);
  }

  /**
   * 
   * @param <M>
   * @param modelClass
   * @param key
   * @param value
   * @return
   */
  public static <M> M get(Class<M> modelClass, String key, Object value) {
    return get(
      (DataContext) BaseContext.getThreadObjectContext(),
      modelClass,
      key,
      value);
  }

  /**
   * 
   * @param <M>
   * @param dataContext
   * @param modelClass
   * @param key
   * @param value
   * @return
   */
  @SuppressWarnings({ "unchecked", "deprecation" })
  public static <M> M get(DataContext dataContext, Class<M> modelClass,
      String key, Object value) {
    return (M) dataContext.refetchObject(new ObjectId(modelClass
      .getSimpleName(), key, value));
  }

  /**
   * オブジェクトモデルを新規作成します。
   * 
   * @param <M>
   * @param modelClass
   * @return
   */
  public static <M> M create(Class<M> modelClass) {
    return create(
      (DataContext) BaseContext.getThreadObjectContext(),
      modelClass);
  }

  /**
   * オブジェクトモデルを新規作成します。
   * 
   * @param <M>
   * @param dataContext
   * @param modelClass
   * @return
   */
  public static <M> M create(DataContext dataContext, Class<M> modelClass) {
    return dataContext.newObject(modelClass);

  }

  /**
   * オブジェクトモデルを削除します。
   * 
   * @param target
   */
  public static void delete(Persistent target) {
    delete((DataContext) BaseContext.getThreadObjectContext(), target);
  }

  /**
   * オブジェクトモデルを削除します。
   * 
   * @param dataContext
   * @param target
   */
  public static void delete(DataContext dataContext, Persistent target) {
    dataContext.deleteObject(target);
  }

  /**
   * オブジェクトモデルをすべて削除します。
   * 
   * @param target
   */
  public static void deleteAll(List<?> target) {
    deleteAll((DataContext) BaseContext.getThreadObjectContext(), target);
  }

  /**
   * オブジェクトモデルをすべて削除します。
   * 
   * @param dataContext
   * @param target
   */
  public static void deleteAll(DataContext dataContext, List<?> target) {
    dataContext.deleteObjects(target);

  }

  /**
   * オブジェクトモデルをすべて削除します。
   * 
   * @param target
   */
  public static void deleteAll(DataObject... target) {
    deleteAll((DataContext) BaseContext.getThreadObjectContext(), target);
  }

  /**
   * オブジェクトモデルをすべて削除します。
   * 
   * @param dataContext
   * @param target
   */
  public static void deleteAll(DataContext dataContext, DataObject... target) {
    dataContext.deleteObjects(Arrays.asList(target));
  }

  /**
   * 現在までの更新をコミットします。
   * 
   */
  public static void commit() {
    commit((DataContext) BaseContext.getThreadObjectContext());
  }

  /**
   * 現在までの更新をコミットします。
   * 
   * @param dataContext
   */
  public static void commit(DataContext dataContext) {
    dataContext.commitChanges();
    Transaction threadTransaction = Transaction.getThreadTransaction();
    if (threadTransaction != null) {
      try {
        threadTransaction.commit();
      } catch (IllegalStateException e) {
        logger.error(e.getMessage(), e);
        e.printStackTrace();
      } catch (SQLException e) {
        logger.error(e.getMessage(), e);
      } catch (CayenneException e) {
        logger.error(e.getMessage(), e);
      } finally {
        Transaction.bindThreadTransaction(null);
      }
    }
  }

  /**
   * 現在までの更新をロールバックします。
   * 
   */
  public static void rollback() {
    rollback((DataContext) BaseContext.getThreadObjectContext());
  }

  /**
   * 現在までの更新をロールバックします。
   * 
   * @param dataContext
   */
  public static void rollback(DataContext dataContext) {
    try {
      dataContext.rollbackChanges();
    } catch (Throwable t) {
      logger.warn(t);
    }
  }

  /**
   * DataRow から指定したキーの値を取得します。
   * 
   * @param dataRow
   * @param key
   * @return
   */
  public static Object getFromDataRow(DataRow dataRow, Object key) {
    String lowerKey = ((String) key).toLowerCase();
    if (dataRow.containsKey(lowerKey)) {
      return dataRow.get(lowerKey);
    } else {
      return dataRow.get(((String) key).toUpperCase());
    }
  }

  public static String getDomainName() {
    try {
      return ((DataContext) BaseContext.getThreadObjectContext())
        .getParentDataDomain()
        .getName();
    } catch (Throwable ignore) {
      return null;
    }
  }

  public synchronized static DataContext createDataContext(String orgId)
      throws Exception {

    DataDomain domain = Configuration.getSharedConfiguration().getDomain(orgId);
    if (domain == null) {
      DataDomain dataDomain =
        Configuration.getSharedConfiguration().getDomain(SHARED_DOMAIN);

      DataDomain destDataDomain =
        new DataDomain(orgId, dataDomain.getProperties());
      destDataDomain.setEntityResolver(dataDomain.getEntityResolver());
      destDataDomain.setEventManager(dataDomain.getEventManager());
      destDataDomain
        .setTransactionDelegate(dataDomain.getTransactionDelegate());
      DataNode dataNode = new DataNode(orgId + "domainNode");
      dataNode.setDataMaps(dataDomain.getDataMaps());
      dataSourceFactory.initializeWithParentConfiguration(Configuration
        .getSharedConfiguration());
      DataSource dataSource =
        dataSourceFactory.getDataSource("datasource/dbcp-"
          + orgId
          + ".properties");

      dataNode.setDataSource(dataSource);
      dataNode.setAdapter(new AutoAdapter(dataSource));
      destDataDomain.addNode(dataNode);
      Configuration.getSharedConfiguration().addDomain(destDataDomain);
    }

    return DataContext.createDataContext(orgId);

  }

  protected static DBCPDataSourceFactory createDataSourceFactory() {
    String property =
      System.getProperty("com.aimluck.eip.orm.DataSourceFactory");
    if (property == null || property.isEmpty()) {
      return new CustomDBCPDataSourceFactory();
    } else {
      try {
        Class<?> forName = Class.forName(property);
        DBCPDataSourceFactory instance =
          (DBCPDataSourceFactory) forName.newInstance();
        return instance;
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      } catch (InstantiationException e) {
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static DBCPDataSourceFactory dataSourceFactory = null;

  public static void initialize(ServletContext servletContext) {
    String property =
      servletContext.getInitParameter("com.aimluck.eip.orm.DataSourceFactory");
    if (property == null || property.isEmpty()) {
      dataSourceFactory = new CustomDBCPDataSourceFactory();
    } else {
      try {
        Class<?> forName = Class.forName(property);
        dataSourceFactory = (DBCPDataSourceFactory) forName.newInstance();
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      } catch (InstantiationException e) {
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private Database() {

  }
}
