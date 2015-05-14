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
package com.aipo.container.gadgets.oauth2;

import java.util.Collection;
import java.util.HashMap;

import org.apache.shindig.gadgets.oauth2.OAuth2Accessor;
import org.apache.shindig.gadgets.oauth2.OAuth2CallbackState;
import org.apache.shindig.gadgets.oauth2.OAuth2Token;
import org.apache.shindig.gadgets.oauth2.OAuth2Token.Type;
import org.apache.shindig.gadgets.oauth2.persistence.OAuth2Cache;
import org.apache.shindig.gadgets.oauth2.persistence.OAuth2CacheException;
import org.apache.shindig.gadgets.oauth2.persistence.OAuth2Client;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 *
 * {@link OAuth2Cache} implementation using in-memory {@link HashMap}s.
 *
 */
@Singleton
public class AipoOAuth2Cache implements OAuth2Cache {

  @Inject
  public AipoOAuth2Cache() {

  }

  /**
   * @throws OAuth2CacheException
   */
  @Override
  public void clearAccessors() throws OAuth2CacheException {
  }

  /**
   * @throws OAuth2CacheException
   */
  @Override
  public void clearClients() throws OAuth2CacheException {
  }

  /**
   * @throws OAuth2CacheException
   */
  @Override
  public void clearTokens() throws OAuth2CacheException {
  }

  /**
   * @param gadgetUri
   * @param serviceName
   * @return
   */
  @Override
  public OAuth2Client getClient(String gadgetUri, String serviceName) {
    return null;
  }

  /**
   * @param state
   * @return
   */
  @Override
  public OAuth2Accessor getOAuth2Accessor(OAuth2CallbackState state) {
    return null;
  }

  /**
   * @param gadgetUri
   * @param serviceName
   * @param user
   * @param scope
   * @param type
   * @return
   */
  @Override
  public OAuth2Token getToken(String gadgetUri, String serviceName,
      String user, String scope, Type type) {
    return null;
  }

  /**
   * @return
   */
  @Override
  public boolean isPrimed() {
    return false;
  }

  /**
   * @param client
   * @return
   */
  @Override
  public OAuth2Client removeClient(OAuth2Client client) {
    return null;
  }

  /**
   * @param accessor
   * @return
   */
  @Override
  public OAuth2Accessor removeOAuth2Accessor(OAuth2Accessor accessor) {
    return null;
  }

  /**
   * @param token
   * @return
   */
  @Override
  public OAuth2Token removeToken(OAuth2Token token) {
    return null;
  }

  /**
   * @param client
   * @throws OAuth2CacheException
   */
  @Override
  public void storeClient(OAuth2Client client) throws OAuth2CacheException {
  }

  /**
   * @param clients
   * @throws OAuth2CacheException
   */
  @Override
  public void storeClients(Collection<OAuth2Client> clients)
      throws OAuth2CacheException {
  }

  /**
   * @param accessor
   */
  @Override
  public void storeOAuth2Accessor(OAuth2Accessor accessor) {
  }

  /**
   * @param token
   * @throws OAuth2CacheException
   */
  @Override
  public void storeToken(OAuth2Token token) throws OAuth2CacheException {
  }

  /**
   * @param tokens
   * @throws OAuth2CacheException
   */
  @Override
  public void storeTokens(Collection<OAuth2Token> tokens)
      throws OAuth2CacheException {
  }

}
