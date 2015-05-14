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
package org.apache.shindig.gadgets.oauth2.persistence;

import java.util.Map;

import org.apache.shindig.gadgets.oauth2.OAuth2Error;
import org.apache.shindig.gadgets.oauth2.OAuth2Message;
import org.apache.shindig.gadgets.oauth2.OAuth2RequestException;
import org.apache.shindig.gadgets.oauth2.OAuth2Token;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

/**
 * see {@link OAuth2Token}
 *
 */
public class OAuth2TokenPersistence implements OAuth2Token {
  private static final long serialVersionUID = -169781729667228661L;

  private byte[] encryptedMacSecret;

  private byte[] encryptedSecret;

  private transient final OAuth2Encrypter encrypter;

  private long expiresAt;

  private String gadgetUri;

  private long issuedAt;

  private String macAlgorithm;

  private String macExt;

  private byte[] macSecret;

  private final Map<String, String> properties;

  private String scope;

  private byte[] secret;

  private String serviceName;

  private String tokenType;

  private Type type;

  private String user;

  private String appId;

  public OAuth2TokenPersistence() {
    this(null);
  }

  @Inject
  public OAuth2TokenPersistence(final OAuth2Encrypter encrypter) {
    this.encrypter = encrypter;
    this.properties = Maps.newHashMap();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof OAuth2Token)) {
      return false;
    }
    final OAuth2Token other = (OAuth2Token) obj;
    if (this.gadgetUri == null) {
      if (other.getGadgetUri() != null) {
        return false;
      }
    } else if (!this.gadgetUri.equals(other.getGadgetUri())) {
      return false;
    }
    if (this.serviceName == null) {
      if (other.getServiceName() != null) {
        return false;
      }
    } else if (!this.serviceName.equals(other.getServiceName())) {
      return false;
    }

    if (this.user == null) {
      if (other.getUser() != null) {
        return false;
      }
    } else if (!this.user.equals(other.getUser())) {
      return false;
    }
    if (this.scope == null) {
      if (other.getScope() != null) {
        return false;
      }
    } else if (!this.scope.equals(other.getScope())) {
      return false;
    }
    if (this.type == null) {
      if (other.getType() != null) {
        return false;
      }
    } else if (!this.type.equals(other.getType())) {
      return false;
    } else if (!this.appId.equals(other.getAppId())) {
      return false;
    }

    return true;
  }

  public byte[] getEncryptedMacSecret() {
    return this.encryptedMacSecret;
  }

  public byte[] getEncryptedSecret() {
    return this.encryptedSecret;
  }

  @Override
  public long getExpiresAt() {
    return this.expiresAt;
  }

  @Override
  public String getGadgetUri() {
    return this.gadgetUri;
  }

  @Override
  public long getIssuedAt() {
    return this.issuedAt;
  }

  @Override
  public String getMacAlgorithm() {
    return this.macAlgorithm;
  }

  @Override
  public String getMacExt() {
    return this.macExt;
  }

  @Override
  public byte[] getMacSecret() {
    return this.macSecret;
  }

  @Override
  public Map<String, String> getProperties() {
    return this.properties;
  }

  @Override
  public String getScope() {
    return this.scope;
  }

  @Override
  public byte[] getSecret() {
    return this.secret;
  }

  @Override
  public String getServiceName() {
    return this.serviceName;
  }

  @Override
  public String getTokenType() {
    if (this.tokenType == null || this.tokenType.length() == 0) {
      this.tokenType = OAuth2Message.BEARER_TOKEN_TYPE;
    }
    return this.tokenType;
  }

  @Override
  public Type getType() {
    return this.type;
  }

  @Override
  public String getUser() {
    return this.user;
  }

  @Override
  public int hashCode() {
    if (this.serviceName != null && this.gadgetUri != null) {
      return (this.serviceName
        + ':'
        + this.gadgetUri
        + ':'
        + this.user
        + ':'
        + this.scope
        + ':' + this.type).hashCode();
    }

    return 0;
  }

  public void setEncryptedMacSecret(final byte[] encryptedSecret)
      throws OAuth2EncryptionException {
    this.encryptedMacSecret = encryptedSecret;
    this.macSecret =
      this.encrypter == null ? encryptedSecret : this.encrypter
        .decrypt(encryptedSecret);
  }

  public void setEncryptedSecret(final byte[] encryptedSecret)
      throws OAuth2EncryptionException {
    this.encryptedSecret = encryptedSecret;
    this.secret =
      this.encrypter == null ? encryptedSecret : this.encrypter
        .decrypt(encryptedSecret);
  }

  @Override
  public void setExpiresAt(final long expiresAt) {
    this.expiresAt = expiresAt;
  }

  @Override
  public void setGadgetUri(final String gadgetUri) {
    this.gadgetUri = gadgetUri;
  }

  @Override
  public void setIssuedAt(final long issuedAt) {
    this.issuedAt = issuedAt;
  }

  @Override
  public void setMacAlgorithm(final String algorithm) {
    this.macAlgorithm = algorithm;
  }

  public void setMacExt(final String macExt) {
    this.macExt = macExt;
  }

  @Override
  public void setMacSecret(final byte[] secret) throws OAuth2RequestException {
    this.macSecret = secret;
    try {
      this.encryptedMacSecret =
        this.encrypter == null ? secret : this.encrypter.encrypt(secret);
    } catch (final OAuth2EncryptionException e) {
      throw new OAuth2RequestException(
        OAuth2Error.SECRET_ENCRYPTION_PROBLEM,
        "OAuth2TokenPersistence could not encrypt the mac secret",
        e);
    }
  }

  @Override
  public void setProperties(final Map<String, String> properties) {
    this.properties.clear();
    if (properties != null) {
      this.properties.putAll(properties);
    }
  }

  @Override
  public void setScope(final String scope) {
    this.scope = scope;
  }

  @Override
  public void setSecret(final byte[] secret) throws OAuth2RequestException {
    this.secret = secret;
    try {
      this.encryptedSecret =
        this.encrypter == null ? secret : this.encrypter.encrypt(secret);
    } catch (final OAuth2EncryptionException e) {
      throw new OAuth2RequestException(
        OAuth2Error.SECRET_ENCRYPTION_PROBLEM,
        "OAuth2TokenPersistence could not encrypt the token secret",
        e);
    }
  }

  @Override
  public void setServiceName(final String serviceName) {
    this.serviceName = serviceName;
  }

  @Override
  public void setTokenType(final String tokenType) {
    this.tokenType = tokenType;
  }

  @Override
  public void setType(final Type type) {
    this.type = type;
  }

  @Override
  public void setUser(final String user) {
    this.user = user;
  }

  @Override
  public String toString() {
    return "org.apache.shindig.gadgets.oauth2.persistence.sample.OAuth2TokenImpl: serviceName = "
      + this.serviceName
      + " , user = "
      + this.user
      + " , appId = "
      + this.appId
      + " , gadgetUri = "
      + this.gadgetUri
      + " , scope = "
      + this.scope
      + " , tokenType = "
      + this.getTokenType()
      + " , issuedAt = "
      + this.issuedAt
      + " , expiresAt = "
      + this.expiresAt
      + " , type = "
      + this.type;
  }

  /**
   * @return
   */
  @Override
  public String getAppId() {
    return appId;
  }

  /**
   * @param appId
   */
  @Override
  public void setAppId(String appId) {
    this.appId = appId;
  }
}
