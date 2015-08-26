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

import com.aipo.orm.Database;
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
    if (username == null || password == null) {
      throwError(
        ErrorType.INVALID_REQUEST,
        "username and password are required");
    }
    TurbineUser user = turbineUserDbService.auth(username, password);
    if (user == null) {
      throwError(ErrorType.INVALID_GRANT, "Bad username or password");
    }
    req.put("orgId", Database.getDomainName());
    req.put("username", user.getLoginName());
  }

  /**
   * @return
   */
  @Override
  public String getGrantType() {
    return "password";
  }

  private void throwError(ErrorType errorType, String msg)
      throws OAuth2Exception {
    OAuth2NormalizedResponse response = new OAuth2NormalizedResponse();
    response.setError(errorType.toString());
    response.setErrorDescription(msg);
    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    response.setBodyReturned(true);
    throw new OAuth2Exception(response);
  }
}
