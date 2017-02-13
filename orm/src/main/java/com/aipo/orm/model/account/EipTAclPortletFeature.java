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
package com.aipo.orm.model.account;

import org.apache.cayenne.ObjectId;

import com.aipo.orm.model.account.auto._EipTAclPortletFeature;

public class EipTAclPortletFeature extends _EipTAclPortletFeature {

  public static final String FEATURE_NAME_COLUMN = "FEATURE_NAME";

  public static final String ACL_TYPE_COLUMN = "ACL_TYPE";

  public Integer getFeatureId() {
    if (getObjectId() != null && !getObjectId().isTemporary()) {
      Object obj = getObjectId().getIdSnapshot().get(FEATURE_ID_PK_COLUMN);
      if (obj instanceof Long) {
        Long value = (Long) obj;
        return Integer.valueOf(value.intValue());
      } else {
        return (Integer) obj;
      }
    } else {
      return null;
    }
  }

  public void setFeatureId(int id) {
    setObjectId(new ObjectId("EipTAclPortletFeature", FEATURE_ID_PK_COLUMN, id));
  }

}
