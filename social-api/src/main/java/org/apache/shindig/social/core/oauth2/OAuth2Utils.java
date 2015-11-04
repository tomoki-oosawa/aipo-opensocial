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

import java.util.Map;

import org.apache.shindig.common.uri.UriBuilder;

/**
 * Collection of utility classes to support OAuth 2.0 operations.
 */
public class OAuth2Utils {

  /**
   * Converts a Map<String, String> to a URL query string.
   *
   * @param params
   *          represents the Map of query parameters
   *
   * @return String is the URL encoded parameter String
   */
  public static String convertQueryString(Map<String, String> params) {
    if (params == null) {
      return "";
    }
    UriBuilder builder = new UriBuilder();
    builder.addQueryParameters(params);
    return builder.getQuery();
  }

  /**
   * Normalizes a URL and parameters. If the URL already contains parameters,
   * new parameters will be added properly.
   *
   * @param URL
   *          is the base URL to normalize
   * @param queryParams
   *          query parameters to add to the URL
   * @param fragmentParams
   *          fragment params to add to the URL
   */
  public static String buildUrl(String url, Map<String, String> queryParams,
      Map<String, String> fragmentParams) {
    UriBuilder builder = new UriBuilder();
    builder.setPath(url);
    if (queryParams != null) {
      builder.addQueryParameters(queryParams);
    }
    if (fragmentParams != null) {
      builder.addFragmentParameters(fragmentParams);
    }
    return builder.toString();
  }
}
