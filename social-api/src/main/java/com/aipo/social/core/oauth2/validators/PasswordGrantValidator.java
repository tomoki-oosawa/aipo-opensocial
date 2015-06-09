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

package com.aipo.social.core.oauth2.validators;

import javax.servlet.http.HttpServletResponse;

import org.apache.shindig.social.core.oauth2.OAuth2Exception;
import org.apache.shindig.social.core.oauth2.OAuth2NormalizedRequest;
import org.apache.shindig.social.core.oauth2.OAuth2NormalizedResponse;
import org.apache.shindig.social.core.oauth2.OAuth2Types.ErrorType;
import org.apache.shindig.social.core.oauth2.validators.OAuth2GrantValidator;

import com.aipo.orm.model.security.TurbineUser;
import com.aipo.orm.service.TurbineUserDbService;

/**
 *
 */
public class PasswordGrantValidator implements OAuth2GrantValidator {

  private final TurbineUserDbService turbineUserDbService;

  public PasswordGrantValidator(TurbineUserDbService turbineUserDbService) {
    this.turbineUserDbService = turbineUserDbService;

  }

  /**
   * @param req
   * @throws OAuth2Exception
   */
  @Override
  public void validateRequest(OAuth2NormalizedRequest req)
      throws OAuth2Exception {
    String username = (String) req.get("username");
    String password = (String) req.get("password");
    String orgId = "org001";
    TurbineUser user = turbineUserDbService.auth(orgId, username, password);
    if (user == null) {
      throwAccessDenied("Bad username or password");
    }
    req.put("orgId", orgId);
    req.put("username", user.getLoginName());
  }

  /**
   * @return
   */
  @Override
  public String getGrantType() {
    return "password";
  }

  private void throwAccessDenied(String msg) throws OAuth2Exception {
    OAuth2NormalizedResponse resp = new OAuth2NormalizedResponse();
    resp.setError(ErrorType.ACCESS_DENIED.toString());
    resp.setErrorDescription(msg);
    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
    throw new OAuth2Exception(resp);
  }
}
