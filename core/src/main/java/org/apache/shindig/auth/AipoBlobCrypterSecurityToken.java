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

import java.util.Map;

import org.apache.shindig.common.crypto.BlobCrypter;
import org.apache.shindig.common.crypto.BlobCrypterException;

/**
 * 
 */
public class AipoBlobCrypterSecurityToken extends BlobCrypterSecurityToken {

  protected static final String APP_KEY = "a";

  protected String appId;

  /**
   * @param crypter
   * @param container
   * @param domain
   */
  public AipoBlobCrypterSecurityToken(BlobCrypter crypter, String container,
      String domain) {
    super(crypter, container, domain);
  }

  static BlobCrypterSecurityToken decrypt(BlobCrypter crypter,
      String container, String domain, String token, String activeUrl)
      throws BlobCrypterException {
    Map<String, String> values = crypter.unwrap(token, MAX_TOKEN_LIFETIME_SECS);
    AipoBlobCrypterSecurityToken t =
      new AipoBlobCrypterSecurityToken(crypter, container, domain);
    setTokenValues(t, values);
    t.setActiveUrl(activeUrl);
    return t;
  }

  protected static void setTokenValues(BlobCrypterSecurityToken token,
      Map<String, String> values) {
    token.setOwnerId(values.get(OWNER_KEY));
    token.setViewerId(values.get(VIEWER_KEY));
    token.setAppUrl(values.get(GADGET_KEY));
    String moduleId = values.get(GADGET_INSTANCE_KEY);
    if (moduleId != null) {
      token.setModuleId(Long.parseLong(moduleId));
    }
    String expiresAt = values.get(EXPIRES_KEY);
    if (expiresAt != null) {
      token.setExpiresAt(Long.parseLong(expiresAt));
    }
    token.setTrustedJson(values.get(TRUSTED_JSON_KEY));

    // Custom
    if (token instanceof AipoBlobCrypterSecurityToken) {
      AipoBlobCrypterSecurityToken aipoToken =
        (AipoBlobCrypterSecurityToken) token;
      aipoToken.setAppId(values.get(APP_KEY));
    }
  }

  @Override
  protected Map<String, String> buildValuesMap() {
    Map<String, String> values = super.buildValuesMap();
    if (appId != null) {
      values.put(APP_KEY, appId);
    }
    return values;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  @Override
  public String getAppId() {
    return this.appId;
  }
}
