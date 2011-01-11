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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * ページャー付き検索結果を管理するクラスです。
 * 
 */
public class ResultList<T> implements List<T>, Serializable {

  private static final long serialVersionUID = -2171829667472477938L;

  protected List<T> delegate;

  protected int limit;

  protected int page;

  protected int totalCount;

  public ResultList(List<T> delegate, int page, int limit, int totalCount)
      throws NullPointerException {
    if (delegate == null) {
      throw new NullPointerException("The delegate parameter must not be null.");
    }
    this.delegate = delegate;
    this.limit = limit;
    this.page = page;
    this.totalCount = totalCount;
  }

  public ResultList(List<T> delegate) throws NullPointerException {
    if (delegate == null) {
      throw new NullPointerException("The delegate parameter must not be null.");
    }
    this.delegate = delegate;
  }

  public ResultList() {
    this.delegate = new ArrayList<T>();
  }

  public int getTotalCount() {
    return totalCount;
  }

  public int getLimit() {
    return limit;
  }

  public int getPage() {
    return page;
  }

  public void add(int index, T element) {
    delegate.add(index, element);
  }

  public boolean add(T o) {
    return delegate.add(o);
  }

  public boolean addAll(Collection<? extends T> c) {
    return delegate.addAll(c);
  }

  public boolean addAll(int index, Collection<? extends T> c) {
    return delegate.addAll(index, c);
  }

  public void clear() {
    delegate.clear();
  }

  public boolean contains(Object o) {
    return delegate.contains(o);
  }

  public boolean containsAll(Collection<?> c) {
    return delegate.containsAll(c);
  }

  public T get(int index) {
    return delegate.get(index);
  }

  public int indexOf(Object o) {
    return delegate.indexOf(o);
  }

  public boolean isEmpty() {
    return delegate.isEmpty();
  }

  public Iterator<T> iterator() {
    return delegate.iterator();
  }

  public int lastIndexOf(Object o) {
    return delegate.lastIndexOf(o);
  }

  public ListIterator<T> listIterator() {
    return delegate.listIterator();
  }

  public ListIterator<T> listIterator(int index) {
    return delegate.listIterator(index);
  }

  public T remove(int index) {
    return delegate.remove(index);
  }

  public boolean remove(Object o) {
    return delegate.remove(o);
  }

  public boolean removeAll(Collection<?> c) {
    return delegate.removeAll(c);
  }

  public boolean retainAll(Collection<?> c) {
    return delegate.retainAll(c);
  }

  public T set(int index, T element) {
    return delegate.set(index, element);
  }

  public int size() {
    return delegate.size();
  }

  public List<T> subList(int fromIndex, int toIndex) {
    return delegate.subList(fromIndex, toIndex);
  }

  public Object[] toArray() {
    return delegate.toArray();
  }

  public <A> A[] toArray(A[] a) {
    return delegate.toArray(a);
  }

}
