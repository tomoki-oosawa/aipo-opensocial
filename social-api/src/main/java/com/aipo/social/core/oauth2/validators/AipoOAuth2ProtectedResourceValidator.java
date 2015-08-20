/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.aipo.social.core.oauth2.validators;

import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.apache.shindig.social.core.oauth2.OAuth2Code;
import org.apache.shindig.social.core.oauth2.OAuth2DataService;
import org.apache.shindig.social.core.oauth2.OAuth2Exception;
import org.apache.shindig.social.core.oauth2.OAuth2NormalizedRequest;
import org.apache.shindig.social.core.oauth2.OAuth2NormalizedResponse;
import org.apache.shindig.social.core.oauth2.OAuth2Types.ErrorType;
import org.apache.shindig.social.core.oauth2.validators.OAuth2ProtectedResourceValidator;

import com.google.inject.Inject;

public class AipoOAuth2ProtectedResourceValidator implements
    OAuth2ProtectedResourceValidator {

  private OAuth2DataService store = null;

  @Inject
  public AipoOAuth2ProtectedResourceValidator(OAuth2DataService store) {
    this.store = store;
  }

  @Override
  public void validateRequest(OAuth2NormalizedRequest req)
      throws OAuth2Exception {
    validateRequestForResource(req, null);

  }

  @Override
  public void validateRequestForResource(OAuth2NormalizedRequest req,
      Object resourceRequest) throws OAuth2Exception {

    OAuth2Code token = store.getAccessToken(req.getAccessToken());
    if (token == null) {
      throwAccessDenied("Invalid or expired token");
    }
    if (token.getExpiration() > 0
      && token.getExpiration() < System.currentTimeMillis()) {
      throwAccessDenied("Invalid or expired token");
      Date d = new Date();
      d.setTime(token.getExpiration());
      throwAccessDenied(d.toString());
    }
  }

  private void throwAccessDenied(String msg) throws OAuth2Exception {
    OAuth2NormalizedResponse resp = new OAuth2NormalizedResponse();
    resp.setError(ErrorType.ACCESS_DENIED.toString());
    resp.setErrorDescription(msg);
    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
    throw new OAuth2Exception(resp);
  }
}
