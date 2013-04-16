/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2011 Aimluck,Inc.
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

/**
 * The OAuthEntry class contains state information about OAuth Tokens and
 * Authorization.
 */
public class OAuthEntry implements Serializable {

  private static final long serialVersionUID = -7856442915039184780L;

  public static final long ONE_YEAR = 365 * 24 * 60 * 60 * 1000L;

  public static final long FIVE_MINUTES = 5 * 60 * 1000L;

  public static enum Type {
    REQUEST, ACCESS, DISABLED
  }

  private String appId;

  private String callbackUrl;

  private boolean callbackUrlSigned; // true if consumer supports OAuth 1.0a

  private String userId;

  private String token;

  private String tokenSecret;

  private boolean authorized;

  private String consumerKey;

  private Type type;

  private Date issueTime;

  private String domain;

  private String container;

  private String oauthVersion;

  private String callbackToken;

  private int callbackTokenAttempts;

  public OAuthEntry() {
  }

  /**
   * A copy constructor
   * 
   * @param old
   *          the OAuthEntry to duplicate
   */
  public OAuthEntry(OAuthEntry old) {
    this.appId = old.appId;
    this.callbackUrl = old.callbackUrl;
    this.callbackUrlSigned = old.callbackUrlSigned;
    this.userId = old.userId;
    this.token = old.token;
    this.tokenSecret = old.tokenSecret;
    this.authorized = old.authorized;
    this.consumerKey = old.consumerKey;
    this.type = old.type;
    this.issueTime = old.issueTime;
    this.domain = old.domain;
    this.container = old.container;
    this.oauthVersion = old.oauthVersion;
    this.callbackToken = old.callbackToken;
    this.callbackTokenAttempts = old.callbackTokenAttempts;
  }

  public boolean isExpired() {
    Date currentDate = new Date();
    return currentDate.compareTo(this.expiresAt()) > 0;
  }

  public Date expiresAt() {
    long expirationTime = issueTime.getTime();
    switch (type) {
      case REQUEST:
        expirationTime += FIVE_MINUTES;
        break;
      case ACCESS:
        expirationTime += ONE_YEAR;
        break;
      default:
        break;
    }

    return new Date(expirationTime);
  }

  public String getAppId() {
    return appId;
  }

  public String getCallbackUrl() {
    return callbackUrl;
  }

  public boolean isCallbackUrlSigned() {
    return callbackUrlSigned;
  }

  public String getUserId() {
    return userId;
  }

  public String getToken() {
    return token;
  }

  public String getTokenSecret() {
    return tokenSecret;
  }

  public boolean isAuthorized() {
    return authorized;
  }

  public String getConsumerKey() {
    return consumerKey;
  }

  public Type getType() {
    return type;
  }

  public Date getIssueTime() {
    return issueTime;
  }

  public String getDomain() {
    return domain;
  }

  public String getContainer() {
    return container;
  }

  public String getOauthVersion() {
    return oauthVersion;
  }

  public String getCallbackToken() {
    return callbackToken;
  }

  public int getCallbackTokenAttempts() {
    return callbackTokenAttempts;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public void setCallbackUrl(String callbackUrl) {
    this.callbackUrl = callbackUrl;
  }

  public void setCallbackUrlSigned(boolean callbackUrlSigned) {
    this.callbackUrlSigned = callbackUrlSigned;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public void setTokenSecret(String tokenSecret) {
    this.tokenSecret = tokenSecret;
  }

  public void setAuthorized(boolean authorized) {
    this.authorized = authorized;
  }

  public void setConsumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public void setIssueTime(Date issueTime) {
    this.issueTime = issueTime;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public void setContainer(String container) {
    this.container = container;
  }

  public void setOauthVersion(String oauthVersion) {
    this.oauthVersion = oauthVersion;
  }

  public void setCallbackToken(String callbackToken) {
    this.callbackToken = callbackToken;
  }

  public void setCallbackTokenAttempts(int callbackTokenAttempts) {
    this.callbackTokenAttempts = callbackTokenAttempts;
  }
}
