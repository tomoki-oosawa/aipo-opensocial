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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.shindig.common.crypto.BasicBlobCrypter;
import org.apache.shindig.common.crypto.BlobCrypter;
import org.apache.shindig.common.crypto.BlobCrypterException;
import org.apache.shindig.common.util.CharsetUtil;
import org.apache.shindig.common.util.HMACType;
import org.apache.shindig.common.util.ResourceLoader;
import org.apache.shindig.config.ContainerConfig;

import com.google.common.base.Charsets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 *
 * @see BlobCrypterSecurityTokenCodec
 */
@Singleton
public class AipoBlobCrypterSecurityTokenCodec implements SecurityTokenCodec {

  // Logging
  private static final String CLASSNAME =
    AipoBlobCrypterSecurityTokenCodec.class.getName();

  private static final Logger LOG = Logger.getLogger(CLASSNAME);

  public static final String SECURITY_TOKEN_KEY = "gadgets.securityTokenKey";

  public static final String SIGNED_FETCH_DOMAIN = "gadgets.signedFetchDomain";

  protected BlobCrypter crypter = null;

  /**
   * Keys are container ids, values are domains used for signed fetch.
   */
  protected String domain = "shindig";

  protected String container = "default";

  private final Integer tokenTTL =
    AipoBlobCrypterSecurityToken.DEFAULT_MAX_TOKEN_TTL;

  @Inject
  public AipoBlobCrypterSecurityTokenCodec(ContainerConfig config) {
    try {
      String key = config.getString(container, SECURITY_TOKEN_KEY);
      crypter = loadCrypter(key);
    } catch (Throwable e) {
      // Someone specified securityTokenKeyFile, but we couldn't load the key.
      // That merits killing
      // the server.
      LOG.log(
        Level.SEVERE,
        "Error while initializing AipoBlobCrypterSecurityTokenCodec",
        e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Load a BlobCrypter using the specified key. Override this if you have your
   * own BlobCrypter implementation.
   *
   * @param key
   *          The security token key.
   * @return The BlobCrypter.
   */
  protected BlobCrypter loadCrypter(String key) {
    return new BasicBlobCrypter(key, HMACType.HMACSHA1);
  }

  /**
   * Decrypt and verify the provided security token.
   */
  @Override
  public SecurityToken createToken(Map<String, String> tokenParameters)
      throws SecurityTokenException {
    String token = tokenParameters.get(SecurityTokenCodec.SECURITY_TOKEN_NAME);
    if (StringUtils.isBlank(token)) {
      // No token is present, assume anonymous access
      return new AnonymousSecurityToken();
    }
    String[] fields = StringUtils.split(token, ':');
    if (fields.length != 2) {
      throw new SecurityTokenException("Invalid security token " + token);
    }
    String activeUrl = tokenParameters.get(SecurityTokenCodec.ACTIVE_URL_NAME);
    String crypted = fields[1];
    try {
      AipoBlobCrypterSecurityToken st =
        new AipoBlobCrypterSecurityToken(container, domain, activeUrl, crypter
          .unwrap(crypted));
      return st.enforceNotExpired();
    } catch (BlobCrypterException e) {
      throw new SecurityTokenException(e);
    }
  }

  /**
   * Encrypt and sign the token. The returned value is *not* web safe, it should
   * be URL encoded before being used as a form parameter.
   */
  @Override
  public String encodeToken(SecurityToken token) throws SecurityTokenException {
    if (!token.getAuthenticationMode().equals(
      AuthenticationMode.SECURITY_TOKEN_URL_PARAMETER.name())) {
      throw new SecurityTokenException(
        "Can only encode BlobCrypterSecurityTokens");
    }

    // Test code sends in real AbstractTokens, they have modified time sources
    // in them so
    // that we can test token expiration, production tokens are proxied via the
    // SecurityToken interface.
    AbstractSecurityToken aToken =
      token instanceof AbstractSecurityToken
        ? (AbstractSecurityToken) token
        : BlobCrypterSecurityToken.fromToken(token);

    try {
      if (tokenTTL != null) {
        aToken.setExpires(tokenTTL);
      } else {
        aToken.setExpires();
      }
      return aToken.getContainer() + ':' + crypter.wrap(aToken.toMap());
    } catch (BlobCrypterException e) {
      throw new SecurityTokenException(e);
    }
  }

  @Override
  public int getTokenTimeToLive() {
    return AbstractSecurityToken.DEFAULT_MAX_TOKEN_TTL;
  }

  @Override
  public int getTokenTimeToLive(String container) {
    if (tokenTTL == null) {
      return getTokenTimeToLive();
    }
    return tokenTTL;
  }

  /**
   * Load a BlobCrypter from the specified file. Override this if you have your
   * own BlobCrypter implementation.
   */
  protected BlobCrypter loadCrypterFromFile(String file) throws IOException {
    BufferedReader reader = null;
    byte[] keyBytes = null;
    try {
      reader =
        new BufferedReader(new InputStreamReader(
          ResourceLoader.open(file),
          Charsets.UTF_8));
      String line = reader.readLine();
      if (line == null) {
        throw new IOException("Unexpectedly empty keyfile: " + file);
      }
      line = line.trim();
      keyBytes = CharsetUtil.getUtf8Bytes(line);
    } finally {
      try {
        if (reader != null) {
          reader.close();
        }
      } catch (IOException e) {
        // oh well.
      }
    }
    return new BasicBlobCrypter(keyBytes);
  }

}
