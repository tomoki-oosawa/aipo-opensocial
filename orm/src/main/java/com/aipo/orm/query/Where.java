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

import java.util.List;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;

public class Where {

  protected Expression exp = null;

  protected boolean isOr = false;

  public Where eq(String property, Object value) {
    concat(ExpressionFactory.matchExp(property, value));
    return this;
  }

  public Where ne(String property, Object value) {
    concat(ExpressionFactory.noMatchExp(property, value));
    return this;
  }

  public Where lt(String property, Object value) {
    concat(ExpressionFactory.lessExp(property, value));
    return this;
  }

  public Where le(String property, Object value) {
    concat(ExpressionFactory.lessOrEqualExp(property, value));
    return this;
  }

  public Where gt(String property, Object value) {
    concat(ExpressionFactory.greaterExp(property, value));
    return this;
  }

  public Where ge(String property, Object value) {
    concat(ExpressionFactory.greaterOrEqualExp(property, value));
    return this;
  }

  public Where in(String property, Object... values) {
    concat(ExpressionFactory.inExp(property, values));
    return this;
  }

  public Where in(String property, List<?> values) {
    concat(ExpressionFactory.inExp(property, values));
    return this;
  }

  public Where notIn(String property, Object... values) {
    concat(ExpressionFactory.notInExp(property, values));
    return this;
  }

  public Where notIn(String property, List<?> values) {
    concat(ExpressionFactory.notInExp(property, values));
    return this;
  }

  public Where like(String property, Object value) {
    concat(ExpressionFactory.likeExp(property, value));
    return this;
  }

  public Where contains(String property, Object value) {
    concat(ExpressionFactory.likeExp(property, "%" + value + "%"));
    return this;
  }

  public Where startWith(String property, Object value) {
    concat(ExpressionFactory.likeExp(property, value + "%"));
    return this;
  }

  public Where endWith(String property, Object value) {
    concat(ExpressionFactory.likeExp(property, "%" + value));
    return this;
  }

  public Where or() {
    isOr = true;
    return this;
  }

  public Where or(Where where) {
    if (exp == null) {
      exp = where.exp;
    } else {
      exp.orExp(where.exp);
    }
    return this;
  }

  public Where and(Where where) {
    if (exp == null) {
      exp = where.exp;
    } else {
      exp.andExp(where.exp);
    }
    return this;
  }

  protected void concat(Expression exp) {
    if (this.exp == null) {
      this.exp = exp;
    } else {
      if (isOr) {
        this.exp.orExp(exp);
        isOr = false;
      } else {
        this.exp.andExp(exp);
      }
    }
  }
}
