/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2011 Aimluck,Inc.
 * http://www.aipo.com/
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

import org.apache.commons.lang.StringUtils;
import org.apache.shindig.common.crypto.BasicBlobCrypter;
import org.apache.shindig.common.crypto.BlobCrypter;
import org.apache.shindig.common.crypto.BlobCrypterException;
import org.apache.shindig.common.util.CharsetUtil;
import org.apache.shindig.common.util.ResourceLoader;
import org.apache.shindig.config.ContainerConfig;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * 
 * @see BlobCrypterSecurityTokenCodec
 */
@Singleton
public class AipoBlobCrypterSecurityTokenCodec implements SecurityTokenCodec {

  public static final String SECURITY_TOKEN_KEY_FILE =
    "gadgets.securityTokenKeyFile";

  public static final String SIGNED_FETCH_DOMAIN = "gadgets.signedFetchDomain";

  /**
   * Keys are container ids, values are crypters
   */
  protected final Map<String, BlobCrypter> crypters = Maps.newHashMap();

  /**
   * Keys are container ids, values are domains used for signed fetch.
   */
  protected final Map<String, String> domains = Maps.newHashMap();

  @Inject
  public AipoBlobCrypterSecurityTokenCodec(ContainerConfig config) {
    try {
      for (String container : config.getContainers()) {
        String keyFile = config.getString(container, SECURITY_TOKEN_KEY_FILE);
        if (keyFile != null) {
          BlobCrypter crypter = loadCrypterFromFile(keyFile);
          crypters.put(container, crypter);
        }
        String domain = config.getString(container, SIGNED_FETCH_DOMAIN);
        domains.put(container, domain);
      }
    } catch (IOException e) {
      // Someone specified securityTokenKeyFile, but we couldn't load the key.
      // That merits killing
      // the server.
      throw new RuntimeException(e);
    }
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

  /**
   * Decrypt and verify the provided security token.
   */
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
    String container = fields[0];
    BlobCrypter crypter = crypters.get(container);
    if (crypter == null) {
      throw new SecurityTokenException("Unknown container " + token);
    }
    String domain = domains.get(container);
    String activeUrl = tokenParameters.get(SecurityTokenCodec.ACTIVE_URL_NAME);
    String crypted = fields[1];
    try {
      return BlobCrypterSecurityToken.decrypt(
        crypter,
        container,
        domain,
        crypted,
        activeUrl);
    } catch (BlobCrypterException e) {
      throw new SecurityTokenException(e);
    }
  }

  public String encodeToken(SecurityToken token) throws SecurityTokenException {
    if (!(token instanceof BlobCrypterSecurityToken)) {
      throw new SecurityTokenException(
        "Can only encode BlogCrypterSecurityTokens");
    }

    BlobCrypterSecurityToken t = (BlobCrypterSecurityToken) token;

    try {
      return t.encrypt();
    } catch (BlobCrypterException e) {
      throw new SecurityTokenException(e);
    }
  }

}
