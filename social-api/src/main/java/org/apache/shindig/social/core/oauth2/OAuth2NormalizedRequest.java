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
package org.apache.shindig.social.core.oauth2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.shindig.common.logging.i18n.MessageKeys;
import org.apache.shindig.social.core.oauth2.OAuth2Types.ErrorType;
import org.apache.shindig.social.core.oauth2.OAuth2Types.GrantType;
import org.apache.shindig.social.core.oauth2.OAuth2Types.ResponseType;

/**
 * Normalizes an OAuth 2.0 request by extracting OAuth 2.0 related fields.
 *
 * TODO (Eric): implement scope handling.
 */
public class OAuth2NormalizedRequest extends HashMap<String, Object> {

  private static final long serialVersionUID = -7849581704967135322L;

  private HttpServletRequest httpReq = null;

  private static final Pattern FORM_URL_REGEX = Pattern
    .compile("application/(x-www-)?form-url(-)?encoded");

  // class name for logging purpose
  private static final String classname = OAuth2NormalizedRequest.class
    .getName();

  private static final Logger LOG = Logger.getLogger(
    classname,
    MessageKeys.MESSAGES);

  @SuppressWarnings("unchecked")
  public OAuth2NormalizedRequest(HttpServletRequest request)
      throws OAuth2Exception {
    super();
    setHttpServletRequest(request);
    String contentType = request.getContentType();
    if (contentType != null) {
      Matcher match = FORM_URL_REGEX.matcher(contentType);
      if (match.matches()) {
        normalizeBody(getBodyAsString(request));
      }
    }
    Enumeration<String> keys = request.getParameterNames();
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();
      put(key, request.getParameter(key));
    }
    normalizeClientSecret(request);
    normalizeAccessToken(request);
    setIpAddress(request);

  }

  // --------------------------- NORMALIZED GETTERS ---------------------------
  public String getClientId() {
    return getString("client_id");
  }

  public String getClientSecret() {
    return getString("client_secret");
  }

  public String getResponseType() {
    return getString("response_type");
  }

  public String getGrantType() {
    return getString("grant_type");
  }

  public String getRedirectURI() {
    return getString("redirect_uri");
  }

  public String getAccessToken() {
    return getString("access_token");
  }

  public String getAuthorizationCode() {
    return getString("code");
  }

  public String getState() {
    return getString("state");
  }

  public String getScope() {
    return getString("scope");
  }

  public String getIpAddress() {
    return getString("ip_address");
  }

  public ResponseType getEnumeratedResponseType() throws OAuth2Exception {
    String respType = getResponseType();
    if (respType == null) {
      return null;
    }
    if (respType.equals("code")) {
      return ResponseType.CODE;
    } else if (respType.equals("token")) {
      return ResponseType.TOKEN;
    } else {
      OAuth2NormalizedResponse resp = new OAuth2NormalizedResponse();
      resp.setError(ErrorType.UNSUPPORTED_RESPONSE_TYPE.toString());
      resp.setErrorDescription("Unsupported response type");
      resp.setStatus(HttpServletResponse.SC_FOUND);
      resp.setBodyReturned(false);
      resp.setHeader("Location", OAuth2Utils.buildUrl(getRedirectURI(), resp
        .getResponseParameters(), null));
      throw new OAuth2Exception(resp);
    }
  }

  public GrantType getEnumeratedGrantType() {
    String grantType = getGrantType();
    if (grantType == null) {
      return null;
    }
    if (grantType.equals("refresh_token")) {
      return GrantType.REFRESH_TOKEN;
    } else if (grantType.equals("authorization_code")) {
      return GrantType.AUTHORIZATION_CODE;
    } else if (grantType.equals("password")) {
      return GrantType.PASSWORD;
    } else if (grantType.equals("client_credentials")) {
      return GrantType.CLIENT_CREDENTIALS;
    } else {
      return GrantType.CUSTOM;
    }
  }

  public String getString(String key) {
    if (!containsKey(key)) {
      return null;
    }
    return (String) get(key);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (String key : keySet()) {
      sb.append(key);
      sb.append(": ");
      sb.append(get(key));
      sb.append('\n');
    }
    return sb.toString();
  }

  // -------------------------- PRIVATE HELPERS -------------------------------

  private void normalizeAccessToken(HttpServletRequest req) {
    String bearerToken = getString("access_token");
    if (bearerToken == null || bearerToken.equals("")) {
      String header = req.getHeader("Authorization");
      if (header != null && header.toLowerCase().startsWith("bearer")) {
        String[] parts = header.split("[ \\t]+");
        bearerToken = parts[parts.length - 1];
      }
    }
    put("access_token", bearerToken);
  }

  private void setIpAddress(HttpServletRequest req) {
    String ipAddress = req.getRemoteAddr();
    put("ip_address", ipAddress);
  }

  private void normalizeClientSecret(HttpServletRequest request)
      throws OAuth2Exception {
    String secret = getClientSecret();
    if (secret == null || secret.equals("")) {
      String header = request.getHeader("Authorization");
      if (header != null && header.toLowerCase().startsWith("basic")) {
        String[] parts = header.split("[ \\t]+");
        String temp = parts[parts.length - 1];
        byte[] decodedSecret = Base64.decodeBase64(temp);
        try {
          temp = new String(decodedSecret, "UTF-8");
          parts = temp.split(":");
          if (parts != null && parts.length == 2) {
            secret = parts[1];
            String queryId = getString("client_id");
            if (queryId != null && !queryId.equals(parts[0])) {
              OAuth2NormalizedResponse response =
                new OAuth2NormalizedResponse();
              response.setError(ErrorType.INVALID_REQUEST.toString());
              response
                .setErrorDescription("Request contains mismatched client ids");
              response.setStatus(HttpServletResponse.SC_FORBIDDEN);
              throw new OAuth2Exception(response);
            }
            // Lets set the client id from the Basic auth header if not already
            // set in query,
            // needed for client_credential flow.
            if (queryId == null) {
              put("client_id", parts[0]);
            }
          }
        } catch (UnsupportedEncodingException e) {
          LOG.logp(
            Level.WARNING,
            classname,
            "normalizeClientSecret",
            MessageKeys.INVALID_OAUTH,
            e);
          return;
        }
      }
    }
    put("client_secret", secret);
  }

  private void normalizeBody(String body) throws OAuth2Exception {
    if (body == null || body.length() == 0) {
      return;
    }
    List<NameValuePair> params;
    try {
      params =
        URLEncodedUtils
          .parse(new URI("http://localhost:8080?" + body), "UTF-8");
      for (NameValuePair param : params) {
        put(param.getName(), param.getValue());
      }
    } catch (URISyntaxException e) {
      OAuth2NormalizedResponse response = new OAuth2NormalizedResponse();
      response.setError(ErrorType.INVALID_REQUEST.toString());
      response.setErrorDescription("The message body's syntax is incorrect");
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      throw new OAuth2Exception(response);
    }
  }

  private String getBodyAsString(HttpServletRequest request) {
    if (request.getContentLength() == 0) {
      return "";
    }
    InputStream is = null;
    try {
      String line;
      StringBuilder sb = new StringBuilder();
      is = request.getInputStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
      is.close();
      return sb.toString();
    } catch (IOException ioe) {
      LOG.logp(
        Level.WARNING,
        classname,
        "getBodyAsString",
        MessageKeys.INVALID_OAUTH,
        ioe);
      return null;
    } finally {
      IOUtils.closeQuietly(is);
    }
  }

  public void setHttpServletRequest(HttpServletRequest httpReq) {
    this.httpReq = httpReq;
  }

  public HttpServletRequest getHttpServletRequest() {
    return httpReq;
  }
}
