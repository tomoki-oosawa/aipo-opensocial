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
package com.aipo.orm.service.request;

/**
 * 
 */
public class SearchOptions {

  public enum FilterOperation {
    contains, equals, startsWith, present
  }

  public enum SortOrder {
    ascending, descending
  }

  private int limit = 20;

  private int offset = 0;

  private String sortBy;

  private SortOrder sortOrder = SortOrder.ascending;

  private String filterBy;

  private FilterOperation filterOperation = FilterOperation.equals;

  private String filterValue;

  public static SearchOptions build() {
    return new SearchOptions();
  }

  /**
   * @return limit
   */
  public int getLimit() {
    return limit;
  }

  /**
   * @param limit
   *          セットする limit
   */
  public void setLimit(int limit) {
    this.limit = limit;
  }

  public SearchOptions withRange(int limit, int offset) {
    setLimit(limit);
    setOffset(offset);
    return this;
  }

  public SearchOptions withLimit(int limit) {
    setLimit(limit);
    return this;
  }

  /**
   * @return offset
   */
  public int getOffset() {
    return offset;
  }

  public SearchOptions withOffset(int offset) {
    setOffset(offset);
    return this;
  }

  /**
   * @param offset
   *          セットする offset
   */
  public void setOffset(int offset) {
    this.offset = offset;
  }

  /**
   * @return sortBy
   */
  public String getSortBy() {
    return sortBy;
  }

  public SearchOptions withSort(String sortBy, SortOrder sortOrder) {
    setSortBy(sortBy);
    setSortOrder(sortOrder);
    return this;
  }

  /**
   * @param sortBy
   *          セットする sortBy
   */
  public void setSortBy(String sortBy) {
    this.sortBy = sortBy;
  }

  /**
   * @return sortOrder
   */
  public SortOrder getSortOrder() {
    return sortOrder;
  }

  /**
   * @param sortOrder
   *          セットする sortOrder
   */
  public void setSortOrder(SortOrder sortOrder) {
    this.sortOrder = sortOrder;
  }

  /**
   * @return filter
   */
  public String getFilterBy() {
    return filterBy;
  }

  public SearchOptions withFilter(String filter,
      FilterOperation filterOperation, String filterValue) {
    setFilterBy(filter);
    setFilterOperation(filterOperation);
    setFilterValue(filterValue);
    return this;
  }

  /**
   * @param filter
   *          セットする filter
   */
  public void setFilterBy(String filterBy) {
    this.filterBy = filterBy;
  }

  /**
   * @return filterOperation
   */
  public FilterOperation getFilterOperation() {
    return filterOperation;
  }

  /**
   * @param filterOperation
   *          セットする filterOperation
   */
  public void setFilterOperation(FilterOperation filterOperation) {
    this.filterOperation = filterOperation;
  }

  /**
   * @return filterValue
   */
  public String getFilterValue() {
    return filterValue;
  }

  /**
   * @param filterValue
   *          セットする filterValue
   */
  public void setFilterValue(String filterValue) {
    this.filterValue = filterValue;
  }

}
