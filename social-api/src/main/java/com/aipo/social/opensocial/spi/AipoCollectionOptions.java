/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2015 Aimluck,Inc.
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

package com.aipo.social.opensocial.spi;

import org.apache.shindig.protocol.RequestItem;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;

import com.google.common.base.Objects;

/**
 *
 */
public class AipoCollectionOptions extends CollectionOptions {
  private int untilId;

  public AipoCollectionOptions(RequestItem request) {
    super(request);

    String untilId = request.getParameter("until_id");
    if (untilId != null) {
      this.untilId = Integer.valueOf(untilId).intValue();
    }
  }

  public int getUntilId() {
    return this.untilId;
  }

  public void setUntilId(int untilId) {
    this.untilId = untilId;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    AipoCollectionOptions actual = (AipoCollectionOptions) o;
    return super.equals(o) && (this.untilId == actual.untilId);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(new Object[] {
      getSortBy(),
      getSortOrder(),
      getFilter(),
      getFilterOperation(),
      getFilterValue(),
      Integer.valueOf(getFirst()),
      Integer.valueOf(getMax()) }, this.untilId);
  }
}
