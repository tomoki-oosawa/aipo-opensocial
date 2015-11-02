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
package org.apache.shindig.auth;

import java.util.Enumeration;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import net.oauth.OAuth;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

/**
 * @see UrlParameterAuthenticationHandler
 */
public class AipoUrlParameterAuthenticationHandler implements
    AipoSecurityTokenAuthenticationHandler {
  private static final String SECURITY_TOKEN_PARAM = "st";

  private final SecurityTokenCodec securityTokenCodec;

  private static final Pattern COMMAWHITESPACE = Pattern.compile("\\s*,\\s*");

  @Inject
  public AipoUrlParameterAuthenticationHandler(
      SecurityTokenCodec securityTokenCodec) {
    this.securityTokenCodec = securityTokenCodec;
  }

  @Override
  public String getName() {
    return AuthenticationMode.SECURITY_TOKEN_URL_PARAMETER.name();
  }

  @Override
  public SecurityToken getSecurityTokenFromRequest(HttpServletRequest request)
      throws InvalidAuthenticationException {
    Map<String, String> parameters = getMappedParameters(request);
    try {
      if (parameters.get(SecurityTokenCodec.SECURITY_TOKEN_NAME) == null) {
        return null;
      }
      return securityTokenCodec.createToken(parameters);
    } catch (SecurityTokenException e) {
      throw new InvalidAuthenticationException("Malformed security token "
        + parameters.get(SecurityTokenCodec.SECURITY_TOKEN_NAME), e);
    }
  }

  @Override
  public String getWWWAuthenticateHeader(String realm) {
    return null;
  }

  protected SecurityTokenCodec getSecurityTokenCodec() {
    return this.securityTokenCodec;
  }

  // From OAuthMessage
  private static final Pattern AUTHORIZATION = Pattern
    .compile("\\s*(\\w*)\\s+(.*)");

  private static final Pattern NVP = Pattern
    .compile("(\\S*)\\s*\\=\\s*\"([^\"]*)\"");

  @SuppressWarnings("unchecked")
  protected Map<String, String> getMappedParameters(
      final HttpServletRequest request) {
    Map<String, String> params = Maps.newHashMap();
    String token = null;

    // old style security token
    if (token == null) {
      token = request.getParameter(SECURITY_TOKEN_PARAM);
    }

    // OAuth2 token as a param
    // NOTE: if oauth_signature_method is present then we have a OAuth 1.0
    // request
    if (token == null
      && request.isSecure()
      && request.getParameter(OAuth.OAUTH_SIGNATURE_METHOD) == null) {
      token = request.getParameter(OAuth.OAUTH_TOKEN);
    }

    // token in authorization header
    if (token == null) {
      for (Enumeration<String> headers = request.getHeaders("Authorization"); headers != null
        && headers.hasMoreElements();) {
        Matcher m = AUTHORIZATION.matcher(headers.nextElement());
        if (m.matches() && "Token".equalsIgnoreCase(m.group(1))) {
          for (String nvp : COMMAWHITESPACE.split(m.group(2))) {
            m = NVP.matcher(nvp);
            if (m.matches() && "token".equals(m.group(1))) {
              token = OAuth.decodePercent(m.group(2));
            }
          }
        }
      }
    }

    params.put(SecurityTokenCodec.SECURITY_TOKEN_NAME, token);
    params.put(SecurityTokenCodec.ACTIVE_URL_NAME, getActiveUrl(request));
    return params;
  }

  protected String getActiveUrl(HttpServletRequest request) {
    return request.getRequestURL().toString();
  }
}
