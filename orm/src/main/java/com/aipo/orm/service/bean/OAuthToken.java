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
package com.aipo.orm.service.bean;

import java.io.Serializable;

public class OAuthToken implements Serializable {

  private static final long serialVersionUID = -1198109011102969067L;

  private int key;

  private String accessToken;

  /**
   * @return key
   */
  public int getKey() {
    return key;
  }

  /**
   * @param key
   *          セットする key
   */
  public void setKey(int key) {
    this.key = key;
  }

  /**
   * @return accessToken
   */
  public String getAccessToken() {
    return accessToken;
  }

  /**
   * @param accessToken
   *          セットする accessToken
   */
  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  /**
   * @return sessionHandle
   */
  public String getSessionHandle() {
    return sessionHandle;
  }

  /**
   * @param sessionHandle
   *          セットする sessionHandle
   */
  public void setSessionHandle(String sessionHandle) {
    this.sessionHandle = sessionHandle;
  }

  /**
   * @return tokenExpireMilis
   */
  public long getTokenExpireMilis() {
    return tokenExpireMilis;
  }

  /**
   * @param tokenExpireMilis
   *          セットする tokenExpireMilis
   */
  public void setTokenExpireMilis(long tokenExpireMilis) {
    this.tokenExpireMilis = tokenExpireMilis;
  }

  /**
   * @return tokenSecret
   */
  public String getTokenSecret() {
    return tokenSecret;
  }

  /**
   * @param tokenSecret
   *          セットする tokenSecret
   */
  public void setTokenSecret(String tokenSecret) {
    this.tokenSecret = tokenSecret;
  }

  private String sessionHandle;

  private long tokenExpireMilis;

  private String tokenSecret;

}