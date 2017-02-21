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
package com.aipo.orm.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;

import com.aipo.orm.Database;
import com.aipo.orm.model.account.EipTAclPortletFeature;
import com.aipo.orm.model.account.EipTAclRole;
import com.aipo.orm.model.account.EipTAclUserRoleMap;
import com.aipo.orm.model.security.TurbineUser;
import com.aipo.util.AipoToolkit;
import com.aipo.util.AipoToolkit.SystemUser;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AipoRoleDbService implements RoleDbService {

  private final TurbineUserDbService turbineUserDbService;

  @Inject
  public AipoRoleDbService(TurbineUserDbService turbineUserDbService) {
    this.turbineUserDbService = turbineUserDbService;
  }

  /**
   * @param userId
   * @param featureName
   * @param aclType
   * @return
   */
  @Override
  public boolean hasAuthority(String username, String featureName, int aclType) {

    Integer userId = null;
    TurbineUser turbineUser = turbineUserDbService.findByUsername(username);
    if (turbineUser == null) {
      SystemUser systemUser = AipoToolkit.getSystemUser(username);
      if (systemUser != null) {
        userId = systemUser.getUserId();
      }
    } else {
      userId = turbineUser.getUserId();
    }
    if (userId == null) {
      return false;
    }
    Map<String, EipTAclRole> roleMap = getAclRoleMap(userId);
    EipTAclRole role = roleMap.get(featureName);

    if (role == null) {
      return false;
    }

    int dbAclType = role.getAclType().intValue();

    return ((dbAclType & aclType) == aclType);
  }

  public Map<String, EipTAclRole> getAclRoleMap(int userId) {
    Map<String, EipTAclRole> roleMap = new HashMap<String, EipTAclRole>();

    Expression exp = ExpressionFactory
      .matchDbExp(
        EipTAclRole.EIP_TACL_USER_ROLE_MAPS_PROPERTY + "." + EipTAclUserRoleMap.TURBINE_USER_PROPERTY + "." + TurbineUser.USER_ID_PK_COLUMN,
        userId);

    List<EipTAclRole> roleList = Database
      .query(EipTAclRole.class, exp)
      .fetchList();

    List<EipTAclPortletFeature> featureList = Database.query(
      EipTAclPortletFeature.class).fetchList();

    Map<Integer, String> _map = new HashMap<Integer, String>();

    for (EipTAclPortletFeature feature : featureList) {
      _map.put(feature.getFeatureId(), feature.getFeatureName());
    }

    String _featureName;
    for (EipTAclRole _role : roleList) {
      _featureName = _map.get(_role.getFeatureId());
      roleMap.put(_featureName, _role);
    }
    return roleMap;
  }
}
