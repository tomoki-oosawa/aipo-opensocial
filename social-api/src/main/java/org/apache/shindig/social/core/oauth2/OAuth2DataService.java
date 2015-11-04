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
package org.apache.shindig.social.core.oauth2;

/**
 * Services to support the management of data for the OAuth 2.0 specification.
 * Includes management of clients, authorization codes, access tokens, and
 * refresh tokens.
 *
 * TODO (Eric): client registration services
 */
public interface OAuth2DataService {

  /**
   * Retrieves a pre-registered client by ID.
   *
   * @param clientId
   *          identifies the client to retrieve
   *
   * @param OAuth2Client
   *          is the retrieved client
   */
  public OAuth2Client getClient(String clientId);

  /**
   * Retrieves an authorization code by its value.
   *
   * @param clientId
   *          identifies the client who owns the authorization code
   * @param authCode
   *          is the value of the authorization code to get
   *
   * @return OAuth2Code is the retrieved authorization code
   */
  public OAuth2Code getAuthorizationCode(String clientId, String authCode);

  /**
   * Registers an authorization code with a client.
   *
   * @param clientId
   *          identifies the client who owns the authorization code
   * @param authCode
   *          is the authorization code to register with the client
   */
  public void registerAuthorizationCode(String clientId, OAuth2Code authCode);

  /**
   * Unregisters an authorization code with a client.
   *
   * @param clientId
   *          identifies the client who owns the authorization code
   * @param authCode
   *          is the value of the authorization code to unregister
   */
  public void unregisterAuthorizationCode(String clientId, String authCode);

  /**
   * Retrieves an access token by its value.
   *
   * @param accessToken
   *          is the value of the accessToken to retrieve
   *
   * @return OAuth2Code is the retrieved access token; null if not found
   */
  public OAuth2Code getAccessToken(String accessToken);

  /**
   * Registers an access token with a client.
   *
   * @param clientId
   *          identifies the client to register the access token with
   * @param accessToken
   *          is the access token to register with the client
   */
  public void registerAccessToken(String clientId, OAuth2Code accessToken);

  /**
   * Unregisters an access token with a client.
   *
   * @param clientId
   *          identifies the client who owns the access token
   * @param accessToken
   *          is the value of the access token to unregister
   */
  public void unregisterAccessToken(String clientId, String accessToken);

  /**
   * Retrieves a refresh token by its value.
   *
   * @param refreshToken
   *          is the value of the refresh token to retrieve
   *
   * @return OAuth2Code is the retrieved refresh token; null if not found
   */
  public OAuth2Code getRefreshToken(String refreshToken);

  /**
   * Registers a refresh token with a client.
   *
   * @param clientId
   *          identifies the client who owns the refresh token
   * @param refreshToken
   *          is the refresh token to register with the client
   */
  public void registerRefreshToken(String clientId, OAuth2Code refreshToken);

  /**
   * Unregisters a refresh token with a client.
   *
   * @param clientId
   *          identifies the client who owns the refresh token
   * @param refreshToken
   *          is the value of the refresh token to unregister
   */
  public void unregisterRefreshToken(String clientId, String refreshToken);
}
