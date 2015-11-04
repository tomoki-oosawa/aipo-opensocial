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
package com.aipo.orm.service.bean;

import java.io.Serializable;
import java.util.Date;

public class OAuth2Token implements Serializable {

  private static final long serialVersionUID = -6007791100248311156L;

  public static final String CODE_TYPE_ACCESS_TOKEN = "ACCESS_TOKEN";

  private String userId;

  private String token;

  private Date createDate;

  private Date expireTime;

  private String scope;

  private String tokenType;

  private String codeType;

  private String clientId;

  /**
   * @return userId
   */
  public String getUserId() {
    return userId;
  }

  /**
   * @param userId
   *          セットする userId
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * @return token
   */
  public String getToken() {
    return token;
  }

  /**
   * @param token
   *          セットする token
   */
  public void setToken(String token) {
    this.token = token;
  }

  /**
   * @return createDate
   */
  public Date getCreateDate() {
    return createDate;
  }

  /**
   * @param createDate
   *          セットする createDate
   */
  public void setCreateDate(Date createDate) {
    this.createDate = createDate;
  }

  /**
   * @return expireTime
   */
  public Date getExpireTime() {
    return expireTime;
  }

  /**
   * @param expireTime
   *          セットする expireTime
   */
  public void setExpireTime(Date expireTime) {
    this.expireTime = expireTime;
  }

  /**
   * @return scope
   */
  public String getScope() {
    return scope;
  }

  /**
   * @param scope
   *          セットする scope
   */
  public void setScope(String scope) {
    this.scope = scope;
  }

  /**
   * @return tokenType
   */
  public String getTokenType() {
    return tokenType;
  }

  /**
   * @param tokenType
   *          セットする tokenType
   */
  public void setTokenType(String tokenType) {
    this.tokenType = tokenType;
  }

  /**
   * @return codeType
   */
  public String getCodeType() {
    return codeType;
  }

  /**
   * @param codeType
   *          セットする codeType
   */
  public void setCodeType(String codeType) {
    this.codeType = codeType;
  }

  /**
   * @return clientId
   */
  public String getClientId() {
    return clientId;
  }

  /**
   * @param clientId
   *          セットする clientId
   */
  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

}