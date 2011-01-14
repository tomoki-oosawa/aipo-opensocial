/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2010 Aimluck,Inc.
 * http://aipostyle.com/
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

package com.aipo.container.auth;

import java.util.Map;

import org.apache.shindig.auth.AipoBlobCrypterSecurityTokenCodec;
import org.apache.shindig.auth.BasicSecurityTokenCodec;
import org.apache.shindig.auth.DefaultSecurityTokenCodec;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.auth.SecurityTokenCodec;
import org.apache.shindig.auth.SecurityTokenException;
import org.apache.shindig.config.ContainerConfig;

import com.google.inject.Inject;

/**
 * @see DefaultSecurityTokenCodec
 */
public class AipoSecurityTokenCodec implements SecurityTokenCodec {

  private static final String SECURITY_TOKEN_TYPE = "gadgets.securityTokenType";

  private final SecurityTokenCodec codec;

  @Inject
  public AipoSecurityTokenCodec(ContainerConfig config) {
    String tokenType =
      config.getString(ContainerConfig.DEFAULT_CONTAINER, SECURITY_TOKEN_TYPE);
    if ("insecure".equals(tokenType)) {
      codec = new BasicSecurityTokenCodec();
    } else if ("secure".equals(tokenType)) {
      codec = new AipoBlobCrypterSecurityTokenCodec(config);
    } else {
      throw new RuntimeException("Unknown security token type specified in "
        + ContainerConfig.DEFAULT_CONTAINER
        + " container configuration. "
        + SECURITY_TOKEN_TYPE
        + ": "
        + tokenType);
    }
  }

  /**
   * 
   * @param tokenParameters
   * @return
   * @throws SecurityTokenException
   */
  public SecurityToken createToken(Map<String, String> tokenParameters)
      throws SecurityTokenException {

    return codec.createToken(tokenParameters);
  }

  /**
   * 
   * @param token
   * @return
   * @throws SecurityTokenException
   */
  public String encodeToken(SecurityToken token) throws SecurityTokenException {
    if (token == null) {
      return null;
    }
    return codec.encodeToken(token);
  }

}
