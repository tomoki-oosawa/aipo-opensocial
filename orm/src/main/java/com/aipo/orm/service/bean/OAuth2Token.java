package com.aipo.orm.service.bean;

import java.io.Serializable;
import java.util.Date;

// TODO:
public class OAuth2Token implements Serializable {
  public static final String CODE_TYPE_ACCESS_TOKEN = "ACCESS_TOKEN";

  private String userId;

  private String token;

  private Date createDate;

  private Date expireTime;

  private String scope;

  private String tokenType;

  private String codeType;

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

}