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
package org.apache.shindig.auth;

import java.util.EnumSet;
import java.util.Map;

/**
 *
 */
public class AipoBlobCrypterSecurityToken extends AbstractSecurityToken {

  private static final EnumSet<AbstractSecurityToken.Keys> MAP_KEYS = EnumSet
    .of(AbstractSecurityToken.Keys.OWNER, new AbstractSecurityToken.Keys[] {
      AbstractSecurityToken.Keys.VIEWER,
      AbstractSecurityToken.Keys.APP_ID,
      AbstractSecurityToken.Keys.APP_URL,
      AbstractSecurityToken.Keys.MODULE_ID,
      AbstractSecurityToken.Keys.EXPIRES,
      AbstractSecurityToken.Keys.TRUSTED_JSON });

  public AipoBlobCrypterSecurityToken(String container, String domain,
      String activeUrl, Map<String, String> values) {
    if (values != null) {
      loadFromMap(values);
    }
    setContainer(container).setDomain(domain).setActiveUrl(activeUrl);
  }

  @Override
  public String getUpdatedToken() {
    return null;
  }

  @Override
  public String getAuthenticationMode() {
    return AuthenticationMode.SECURITY_TOKEN_URL_PARAMETER.name();
  }

  @Override
  public boolean isAnonymous() {
    return false;
  }

  @Override
  protected EnumSet<AbstractSecurityToken.Keys> getMapKeys() {
    return MAP_KEYS;
  }

  public static BlobCrypterSecurityToken fromToken(SecurityToken token) {
    BlobCrypterSecurityToken interpretedToken =
      new BlobCrypterSecurityToken(
        token.getContainer(),
        token.getDomain(),
        token.getActiveUrl(),
        null);
    interpretedToken
      .setAppId(token.getAppId())
      .setAppUrl(token.getAppUrl())
      .setExpiresAt(token.getExpiresAt())
      .setModuleId(token.getModuleId())
      .setOwnerId(token.getOwnerId())
      .setTrustedJson(token.getTrustedJson())
      .setViewerId(token.getViewerId());

    return interpretedToken;
  }
}
