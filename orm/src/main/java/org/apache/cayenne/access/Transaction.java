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
package org.apache.cayenne.access;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.cayenne.CayenneException;
import org.apache.cayenne.CayenneRuntimeException;
import org.apache.log4j.Level;

/**
 * A Cayenne transaction. Currently supports managing JDBC connections.
 * 
 * @author Andrus Adamchik
 * @since 1.1
 */
public abstract class Transaction {

  /**
   * A ThreadLocal that stores current thread transaction.
   * 
   * @since 1.2
   */
  static final ThreadLocal currentTransaction = new ThreadLocal();

  private static final Transaction NO_TRANSACTION = new Transaction() {

    @Override
    public void begin() {

    }

    @Override
    public void commit() {

    }

    @Override
    public void rollback() {

    }
  };

  public static final int STATUS_ACTIVE = 1;

  public static final int STATUS_COMMITTING = 2;

  public static final int STATUS_COMMITTED = 3;

  public static final int STATUS_ROLLEDBACK = 4;

  public static final int STATUS_ROLLING_BACK = 5;

  public static final int STATUS_NO_TRANSACTION = 6;

  public static final int STATUS_MARKED_ROLLEDBACK = 7;

  protected Map connections;

  protected int status;

  protected TransactionDelegate delegate;

  /**
   * @deprecated since 1.2
   */
  @Deprecated
  protected Level logLevel;

  static String decodeStatus(int status) {
    switch (status) {
      case STATUS_ACTIVE:
        return "STATUS_ACTIVE";
      case STATUS_COMMITTING:
        return "STATUS_COMMITTING";
      case STATUS_COMMITTED:
        return "STATUS_COMMITTED";
      case STATUS_ROLLEDBACK:
        return "STATUS_ROLLEDBACK";
      case STATUS_ROLLING_BACK:
        return "STATUS_ROLLING_BACK";
      case STATUS_NO_TRANSACTION:
        return "STATUS_NO_TRANSACTION";
      case STATUS_MARKED_ROLLEDBACK:
        return "STATUS_MARKED_ROLLEDBACK";
      default:
        return "Unknown Status - " + status;
    }
  }

  /**
   * Binds a Transaction to the current thread.
   * 
   * @since 1.2
   */
  public static void bindThreadTransaction(Transaction transaction) {
    currentTransaction.set(transaction);
  }

  /**
   * Returns a Transaction associated with the current thread, or null if there
   * is no such Transaction.
   * 
   * @since 1.2
   */
  public static Transaction getThreadTransaction() {
    return (Transaction) currentTransaction.get();
  }

  /**
   * Factory method returning a new transaction instance that would propagate
   * commit/rollback to participating connections. Connections will be closed
   * when commit or rollback is called.
   */
  public static Transaction internalTransaction(TransactionDelegate delegate) {
    return new InternalTransaction(delegate);
  }

  /**
   * Factory method returning a new transaction instance that would NOT
   * propagate commit/rollback to participating connections. Connections will
   * still be closed when commit or rollback is called.
   */
  public static Transaction externalTransaction(TransactionDelegate delegate) {
    return new ExternalTransaction(delegate);
  }

  /**
   * Factory method returning a transaction instance that does not alter the
   * state of participating connections in any way. Commit and rollback methods
   * do not do anything.
   */
  public static Transaction noTransaction() {
    return NO_TRANSACTION;
  }

  /**
   * Creates new inactive transaction.
   */
  protected Transaction() {
    status = STATUS_NO_TRANSACTION;
  }

  /**
   * Helper method that wraps a number of queries in this transaction, runs
   * them, and commits or rolls back depending on the outcome. This method
   * allows users to define their own custom Transactions and wrap Cayenne
   * queries in them.
   * 
   * @deprecated since 1.2 this method is not used in Cayenne and is deprecated.
   *             Thread-bound transactions should be used instead.
   */
  @Deprecated
  public void performQueries(QueryEngine engine, Collection queries,
      OperationObserver observer) throws CayenneRuntimeException {

    Transaction old = Transaction.getThreadTransaction();
    Transaction.bindThreadTransaction(this);

    try {
      // implicit begin..
      engine.performQueries(queries, observer);

      // don't commit iterated queries - leave it up to the caller
      // at the same time rollbacks of iterated queries must be processed here,
      // since caller will no longer be processing stuff on exception
      if (!observer.isIteratedResult()
        && (getStatus() == Transaction.STATUS_ACTIVE)) {
        commit();
      }
    } catch (Exception ex) {
      setRollbackOnly();

      // must rethrow
      if (ex instanceof CayenneRuntimeException) {
        throw (CayenneRuntimeException) ex;
      } else {
        throw new CayenneRuntimeException(ex);
      }
    } finally {
      Transaction.bindThreadTransaction(old);
      if (getStatus() == Transaction.STATUS_MARKED_ROLLEDBACK) {
        try {
          rollback();
        } catch (Exception rollbackEx) {
        }
      }
    }
  }

  /**
   * @deprecated since 1.2 unused
   */
  @Deprecated
  public Level getLogLevel() {
    return logLevel != null ? logLevel : Level.INFO;
  }

  /**
   * @deprecated since 1.2 unused
   */
  @Deprecated
  public void setLogLevel(Level logLevel) {
    this.logLevel = logLevel;
  }

  public TransactionDelegate getDelegate() {
    return delegate;
  }

  public void setDelegate(TransactionDelegate delegate) {
    this.delegate = delegate;
  }

  public int getStatus() {
    return status;
  }

  public synchronized void setRollbackOnly() {
    setStatus(STATUS_MARKED_ROLLEDBACK);
  }

  public synchronized void setStatus(int status) {
    if (delegate != null
      && status == STATUS_MARKED_ROLLEDBACK
      && !delegate.willMarkAsRollbackOnly(this)) {
      return;
    }

    this.status = status;
  }

  /**
   * Starts a Transaction. If Transaction is not started explicitly, it will be
   * started when the first connection is added.
   */
  public abstract void begin();

  /**
   * @deprecated since 1.2 use {@link #addConnection(String, Connection)}.
   */
  @Deprecated
  public void addConnection(Connection connection)
      throws IllegalStateException, SQLException, CayenneException {
    addConnection("x" + System.currentTimeMillis(), connection);
  }

  public abstract void commit() throws IllegalStateException, SQLException,
      CayenneException;

  public abstract void rollback() throws IllegalStateException, SQLException,
      CayenneException;

  /**
   * @since 1.2
   */
  public Connection getConnection(String name) {
    return (connections != null) ? (Connection) connections.get(name) : null;
  }

  /**
   * @since 1.2
   */
  public boolean addConnection(String name, Connection connection)
      throws SQLException {
    if (delegate != null && !delegate.willAddConnection(this, connection)) {
      return false;
    }

    if (connections == null) {
      connections = new HashMap();
    }

    return connections.put(name, connection) != connection;
  }
}
