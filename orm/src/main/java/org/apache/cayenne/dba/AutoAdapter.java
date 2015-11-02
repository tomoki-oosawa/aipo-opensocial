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
package org.apache.cayenne.dba;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.apache.cayenne.CayenneRuntimeException;
import org.apache.cayenne.access.DataNode;
import org.apache.cayenne.access.QueryLogger;
import org.apache.cayenne.access.trans.QualifierTranslator;
import org.apache.cayenne.access.trans.QueryAssembler;
import org.apache.cayenne.access.types.ExtendedTypeMap;
import org.apache.cayenne.dba.db2.DB2Sniffer;
import org.apache.cayenne.dba.derby.DerbySniffer;
import org.apache.cayenne.dba.frontbase.FrontBaseSniffer;
import org.apache.cayenne.dba.h2.H2Sniffer;
import org.apache.cayenne.dba.hsqldb.HSQLDBSniffer;
import org.apache.cayenne.dba.ingres.IngresSniffer;
import org.apache.cayenne.dba.mysql.MySQLSniffer;
import org.apache.cayenne.dba.openbase.OpenBaseSniffer;
import org.apache.cayenne.dba.oracle.OracleSniffer;
import org.apache.cayenne.dba.postgres.PostgresSniffer;
import org.apache.cayenne.dba.sqlite.SQLiteSniffer;
import org.apache.cayenne.dba.sqlserver.SQLServerSniffer;
import org.apache.cayenne.dba.sybase.SybaseSniffer;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.DbEntity;
import org.apache.cayenne.map.DbRelationship;
import org.apache.cayenne.merge.MergerFactory;
import org.apache.cayenne.query.Query;
import org.apache.cayenne.query.SQLAction;

/**
 * A DbAdapter that automatically detects the kind of database it is running on
 * and instantiates an appropriate DB-specific adapter, delegating all
 * subsequent method calls to this adapter.
 *
 * @since 1.2
 */
public class AutoAdapter implements DbAdapter {

  final static String DEFAULT_QUOTE_SQL_IDENTIFIERS_CHAR_START = "\"";

  final static String DEFAULT_QUOTE_SQL_IDENTIFIERS_CHAR_END = "\"";

  static final List<DbAdapterFactory> defaultFactories;
  static {
    defaultFactories = new ArrayList<DbAdapterFactory>();

    // hardcoded factories for adapters that we know how to auto-detect
    defaultFactories.addAll(Arrays.asList(
      new MySQLSniffer(),
      new PostgresSniffer(),
      new OracleSniffer(),
      new SQLServerSniffer(),
      new HSQLDBSniffer(),
      new DB2Sniffer(),
      new SybaseSniffer(),
      new DerbySniffer(),
      new OpenBaseSniffer(),
      new FrontBaseSniffer(),
      new IngresSniffer(),
      new SQLiteSniffer(),
      new H2Sniffer()));
  }

  /**
   * Allows application code to add a sniffer to detect a custom adapter.
   *
   * @since 3.0
   */
  public static void addFactory(DbAdapterFactory factory) {
    defaultFactories.add(factory);
  }

  /**
   * Returns a DbAdapterFactory configured to detect all databases officially
   * supported by Cayenne.
   */
  public static DbAdapterFactory getDefaultFactory() {
    return new DbAdapterFactoryChain(defaultFactories);
  }

  protected DbAdapterFactory adapterFactory;

  protected DataSource dataSource;

  protected PkGenerator pkGenerator;

  /**
   * The actual adapter that is delegated method execution.
   */
  DbAdapter adapter;

  /**
   * Creates an AutoAdapter that can detect adapters known to Cayenne.
   */
  public AutoAdapter(DataSource dataSource) {
    this(null, dataSource);
  }

  /**
   * Creates an AutoAdapter with specified adapter factory and DataSource. If
   * adapterFactory is null, default factory is used.
   */
  public AutoAdapter(DbAdapterFactory adapterFactory, DataSource dataSource) {
    // sanity check
    if (dataSource == null) {
      throw new CayenneRuntimeException("Null dataSource");
    }

    this.adapterFactory =
      adapterFactory != null ? adapterFactory : createDefaultFactory();
    this.dataSource = dataSource;
  }

  /**
   * Called from constructor to initialize factory in case no factory was
   * specified by the object creator.
   */
  protected DbAdapterFactory createDefaultFactory() {
    return getDefaultFactory();
  }

  /**
   * Returns a proxied DbAdapter, lazily creating it on first invocation.
   */
  public DbAdapter getAdapter() {
    if (adapter == null) {
      synchronized (this) {
        if (adapter == null) {
          this.adapter = loadAdapter();
        }
      }
    }

    return adapter;
  }

  /**
   * Opens a connection, retrieves JDBC metadata and attempts to guess adapter
   * form it.
   */
  protected DbAdapter loadAdapter() {
    DbAdapter adapter = null;

    try {
      Connection c = dataSource.getConnection();

      try {
        adapter = adapterFactory.createAdapter(c.getMetaData());
      } finally {
        try {
          c.close();
        } catch (SQLException e) {
          // ignore...
        }
      }
    } catch (SQLException e) {
      throw new CayenneRuntimeException("Error detecting database type: "
        + e.getLocalizedMessage(), e);
    }

    if (adapter == null) {
      QueryLogger.log("Failed to detect database type, using default adapter");
      adapter = new JdbcAdapter();
    } else {
      QueryLogger.log("Detected and installed adapter: "
        + adapter.getClass().getName());
    }

    return adapter;
  }

  // ---- DbAdapter methods ----

  @Override
  public String getBatchTerminator() {
    return getAdapter().getBatchTerminator();
  }

  @Override
  public QualifierTranslator getQualifierTranslator(
      QueryAssembler queryAssembler) {
    return getAdapter().getQualifierTranslator(queryAssembler);
  }

  @Override
  public SQLAction getAction(Query query, DataNode node) {
    return getAdapter().getAction(query, node);
  }

  /**
   * @deprecated since 3.0 - almost all DB's support FK's now and also this flag
   *             is less relevant for Cayenne now.
   */
  @Deprecated
  @Override
  public boolean supportsFkConstraints() {
    return getAdapter().supportsFkConstraints();
  }

  @Override
  public boolean supportsUniqueConstraints() {
    return getAdapter().supportsUniqueConstraints();
  }

  @Override
  public boolean supportsGeneratedKeys() {
    return getAdapter().supportsGeneratedKeys();
  }

  @Override
  public boolean supportsBatchUpdates() {
    return getAdapter().supportsBatchUpdates();
  }

  /**
   * @deprecated since 3.0 as the decorated method is deprecated.
   */
  @Deprecated
  @Override
  public String dropTable(DbEntity entity) {
    return getAdapter().dropTable(entity);
  }

  @Override
  public Collection<String> dropTableStatements(DbEntity table) {
    return getAdapter().dropTableStatements(table);
  }

  @Override
  public String createTable(DbEntity entity) {
    return getAdapter().createTable(entity);
  }

  @Override
  public String createUniqueConstraint(DbEntity source,
      Collection<DbAttribute> columns) {
    return getAdapter().createUniqueConstraint(source, columns);
  }

  @Override
  public String createFkConstraint(DbRelationship rel) {
    return getAdapter().createFkConstraint(rel);
  }

  @Override
  public String[] externalTypesForJdbcType(int type) {
    return getAdapter().externalTypesForJdbcType(type);
  }

  @Override
  public ExtendedTypeMap getExtendedTypes() {
    return getAdapter().getExtendedTypes();
  }

  /**
   * Returns a primary key generator.
   */
  @Override
  public PkGenerator getPkGenerator() {
    return (pkGenerator != null) ? pkGenerator : getAdapter().getPkGenerator();
  }

  /**
   * Sets a PK generator override. If set to non-null value, such PK generator
   * will be used instead of the one provided by wrapped adapter.
   */
  public void setPkGenerator(PkGenerator pkGenerator) {
    this.pkGenerator = pkGenerator;
  }

  @Override
  public DbAttribute buildAttribute(String name, String typeName, int type,
      int size, int precision, boolean allowNulls) {

    return getAdapter().buildAttribute(
      name,
      typeName,
      type,
      size,
      precision,
      allowNulls);
  }

  @Override
  public void bindParameter(PreparedStatement statement, Object object,
      int pos, int sqlType, int precision) throws SQLException, Exception {
    getAdapter().bindParameter(statement, object, pos, sqlType, precision);
  }

  @Override
  public String tableTypeForTable() {
    return getAdapter().tableTypeForTable();
  }

  @Override
  public String tableTypeForView() {
    return getAdapter().tableTypeForView();
  }

  @Override
  public MergerFactory mergerFactory() {
    return getAdapter().mergerFactory();
  }

  @Override
  public void createTableAppendColumn(StringBuffer sqlBuffer, DbAttribute column) {
    getAdapter().createTableAppendColumn(sqlBuffer, column);
  }

  public void setDefaultQuoteSqlIdentifiersChars(boolean isQuoteSqlIdentifiers) {
  }

  public String getIdentifiersStartQuote() {
    return DEFAULT_QUOTE_SQL_IDENTIFIERS_CHAR_START;
  }

  public String getIdentifiersEndQuote() {
    return DEFAULT_QUOTE_SQL_IDENTIFIERS_CHAR_END;
  }

  @Override
  public QuotingStrategy getQuotingStrategy(boolean isQuoteStrategy) {
    return getAdapter().getQuotingStrategy(isQuoteStrategy);
  }

}
