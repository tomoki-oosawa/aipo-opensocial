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
 * Represents an exception while dancing with OAuth 2.0.
 */
public class OAuth2Exception extends Exception {

  private static final long serialVersionUID = -5892464438773813010L;

  private final OAuth2NormalizedResponse response;

  /**
   * Constructs an OAuth2Exception.
   *
   * @param response
   *          is the normalized response that should be used to formulate a
   *          server response.
   */
  public OAuth2Exception(OAuth2NormalizedResponse response) {
    super(response.getErrorDescription());
    this.response = response;
  }

  /**
   * Retrieves the normalized response.
   *
   * @return OAuth2NormalizedResponse encapsulates the OAuth error
   */
  public OAuth2NormalizedResponse getNormalizedResponse() {
    return response;
  }
}
