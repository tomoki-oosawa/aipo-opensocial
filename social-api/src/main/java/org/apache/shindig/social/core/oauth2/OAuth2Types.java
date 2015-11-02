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

/**
 * A collection of OAuth 2.0's enumerated types.
 */
public class OAuth2Types {

  /**
   * Enumerated error types in the OAuth 2.0 specification.
   */
  public static enum ErrorType {
    INVALID_REQUEST("invalid_request"), INVALID_CLIENT("invalid_client"), INVALID_GRANT(
        "invalid_grant"), UNAUTHORIZED_CLIENT("unauthorized_client"), UNSUPPORTED_GRANT_TYPE(
        "unsupported_grant_type"), INVALID_SCOPE("invalid_scope"), ACCESS_DENIED(
        "access_denied"), UNSUPPORTED_RESPONSE_TYPE("unsupported_response_type"), SERVER_ERROR(
        "server_error"), TEMPORARILY_UNAVAILABLE("temporarily_unavailable");

    private final String name;

    private ErrorType(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  /**
   * Enumerated grant types in the OAuth 2.0 specification.
   */
  public static enum GrantType {
    REFRESH_TOKEN("refresh_token"), AUTHORIZATION_CODE("authorization_code"), PASSWORD(
        "password"), CLIENT_CREDENTIALS("client_credentials"), CUSTOM("custom");

    private final String name;

    private GrantType(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  /**
   * Enumerated response types in the OAuth 2.0 specification.
   */
  public static enum ResponseType {
    CODE("code"), TOKEN("token");

    private final String name;

    private ResponseType(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  /**
   * Enumerated token types in the OAuth 2.0 specification.
   */
  public static enum CodeType {
    AUTHORIZATION_CODE("authorization_code"), ACCESS_TOKEN("access_token"), REFRESH_TOKEN(
        "refresh_token");

    private final String name;

    private CodeType(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  /**
   * Enumerated token types in the OAuth 2.0 specification.
   */
  public static enum TokenFormat {
    BEARER("bearer"), MAC("mac");

    private final String name;

    private TokenFormat(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }
}
