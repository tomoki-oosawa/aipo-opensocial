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

import java.util.Set;

import org.apache.shindig.common.crypto.BlobCrypter;
import org.apache.shindig.common.servlet.Authority;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.GadgetException.Code;
import org.apache.shindig.gadgets.oauth2.AipoOAuth2Accessor;
import org.apache.shindig.gadgets.oauth2.OAuth2Accessor;
import org.apache.shindig.gadgets.oauth2.OAuth2CallbackState;
import org.apache.shindig.gadgets.oauth2.OAuth2FetcherConfig;
import org.apache.shindig.gadgets.oauth2.OAuth2Store;
import org.apache.shindig.gadgets.oauth2.OAuth2Token;
import org.apache.shindig.gadgets.oauth2.logger.FilteredLogger;
import org.apache.shindig.gadgets.oauth2.persistence.OAuth2Cache;
import org.apache.shindig.gadgets.oauth2.persistence.OAuth2CacheException;
import org.apache.shindig.gadgets.oauth2.persistence.OAuth2Client;
import org.apache.shindig.gadgets.oauth2.persistence.OAuth2Encrypter;
import org.apache.shindig.gadgets.oauth2.persistence.OAuth2PersistenceException;
import org.apache.shindig.gadgets.oauth2.persistence.OAuth2Persister;
import org.apache.shindig.gadgets.oauth2.persistence.OAuth2TokenPersistence;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * see {@link OAuth2Store}
 *
 * @see AipoOAuth2Store
 *
 *      Default OAuth2Store. Handles a persistence scenario with a separate
 *      cache and persistence layer.
 *
 *      Uses 3 Guice bindings to achieve storage implementation.
 *
 *      1) {@link OAuth2Persister} 2) {@link OAuth2Cache} 3)
 *      {@link OAuth2Encrypter}
 *
 */
public class AipoOAuth2Store implements OAuth2Store {
  private static final String LOG_CLASS = AipoOAuth2Store.class.getName();

  private static final FilteredLogger LOG = FilteredLogger
    .getFilteredLogger(AipoOAuth2Store.LOG_CLASS);

  private final OAuth2Cache cache;

  private final String globalRedirectUri;

  private final Authority authority;

  private final String contextRoot;

  private final OAuth2Persister persister;

  private final OAuth2Encrypter encrypter;

  private final BlobCrypter stateCrypter;

  @Inject
  public AipoOAuth2Store(
      final OAuth2Cache cache,
      final OAuth2Persister persister,
      final OAuth2Encrypter encrypter,
      final String globalRedirectUri,
      final Authority authority,
      final String contextRoot,
      @Named(OAuth2FetcherConfig.OAUTH2_STATE_CRYPTER) final BlobCrypter stateCrypter) {
    this.cache = cache;
    this.persister = persister;
    this.globalRedirectUri = globalRedirectUri;
    this.authority = authority;
    this.contextRoot = contextRoot;
    this.encrypter = encrypter;
    this.stateCrypter = stateCrypter;
    if (AipoOAuth2Store.LOG.isLoggable()) {
      AipoOAuth2Store.LOG.log("this.cache = {0}", this.cache);
      AipoOAuth2Store.LOG.log("this.persister = {0}", this.persister);
      AipoOAuth2Store.LOG.log(
        "this.globalRedirectUri = {0}",
        this.globalRedirectUri);
      AipoOAuth2Store.LOG.log("this.encrypter = {0}", this.encrypter);
      AipoOAuth2Store.LOG.log("this.stateCrypter = {0}", this.stateCrypter);
    }
  }

  @Override
  public boolean clearCache() throws GadgetException {
    final boolean isLogging = AipoOAuth2Store.LOG.isLoggable();
    if (isLogging) {
      AipoOAuth2Store.LOG.entering(AipoOAuth2Store.LOG_CLASS, "clearCache");
    }

    try {
      this.cache.clearClients();
      this.cache.clearTokens();
      this.cache.clearAccessors();
    } catch (final OAuth2PersistenceException e) {
      if (isLogging) {
        AipoOAuth2Store.LOG.log("Error clearing OAuth2 cache", e);
      }
      throw new GadgetException(
        Code.OAUTH_STORAGE_ERROR,
        "Error clearing OAuth2 cache",
        e);
    }

    if (isLogging) {
      AipoOAuth2Store.LOG
        .exiting(AipoOAuth2Store.LOG_CLASS, "clearCache", true);
    }

    return true;
  }

  @Override
  public OAuth2Token createToken() {
    final boolean isLogging = AipoOAuth2Store.LOG.isLoggable();
    if (isLogging) {
      AipoOAuth2Store.LOG.entering(AipoOAuth2Store.LOG_CLASS, "createToken");
    }

    final OAuth2Token ret = this.internalCreateToken();

    if (isLogging) {
      AipoOAuth2Store.LOG.exiting(AipoOAuth2Store.LOG_CLASS, "clearCache", ret);
    }

    return ret;
  }

  public OAuth2Client getClient(final String appId, final String gadgetUri,
      final String serviceName) throws GadgetException {
    final boolean isLogging = AipoOAuth2Store.LOG.isLoggable();
    if (isLogging) {
      AipoOAuth2Store.LOG.entering(
        AipoOAuth2Store.LOG_CLASS,
        "getClient",
        new Object[] { gadgetUri, serviceName });
    }

    OAuth2Client client = this.cache.getClient(gadgetUri, serviceName);

    if (isLogging) {
      AipoOAuth2Store.LOG.log("client from cache = {0}", client);
    }

    if (client == null) {
      try {
        client = this.persister.findClient(appId, gadgetUri, serviceName);
        if (client != null) {
          this.cache.storeClient(client);
        }
      } catch (final OAuth2PersistenceException e) {
        if (isLogging) {
          AipoOAuth2Store.LOG.log("Error loading OAuth2 client ", e);
        }
        throw new GadgetException(
          Code.OAUTH_STORAGE_ERROR,
          "Error loading OAuth2 client " + serviceName,
          e);
      }
    }

    if (isLogging) {
      AipoOAuth2Store.LOG.exiting(
        AipoOAuth2Store.LOG_CLASS,
        "getClient",
        client);
    }

    return client;
  }

  @Override
  public OAuth2Accessor getOAuth2Accessor(final OAuth2CallbackState state) {
    final boolean isLogging = AipoOAuth2Store.LOG.isLoggable();
    if (isLogging) {
      AipoOAuth2Store.LOG.entering(
        AipoOAuth2Store.LOG_CLASS,
        "getOAuth2Accessor",
        state);
    }

    OAuth2Accessor ret = this.cache.getOAuth2Accessor(state);
    if (ret == null) {
      try {
        ret =
          getOAuth2Accessor(state.getAppId(), state.getGadgetUri(), state
            .getServiceName(), state.getUser(), state.getScope());
      } catch (GadgetException e) {
        if (isLogging) {
          AipoOAuth2Store.LOG.log("getOAuth2Accessor", e);
        }
      }
    }

    if (isLogging) {
      AipoOAuth2Store.LOG.exiting(
        AipoOAuth2Store.LOG_CLASS,
        "getOAuth2Accessor",
        ret);
    }

    return ret;
  }

  @Override
  public OAuth2Accessor getOAuth2Accessor(final String appId,
      final String gadgetUri, final String serviceName, final String user,
      final String scope) throws GadgetException {
    final boolean isLogging = AipoOAuth2Store.LOG.isLoggable();
    if (isLogging) {
      AipoOAuth2Store.LOG.entering(
        AipoOAuth2Store.LOG_CLASS,
        "getOAuth2Accessor",
        new Object[] { gadgetUri, serviceName, user, scope });
    }

    final OAuth2CallbackState state =
      new OAuth2CallbackState(this.stateCrypter);
    state.setGadgetUri(gadgetUri);
    state.setServiceName(serviceName);
    state.setUser(user);
    state.setScope(scope);
    state.setAppId(appId);

    OAuth2Accessor ret = this.cache.getOAuth2Accessor(state);

    if (ret == null || !ret.isValid()) {
      final OAuth2Client client = this.getClient(appId, gadgetUri, serviceName);

      if (client != null) {
        final OAuth2Token accessToken =
          this.getToken(
            appId,
            gadgetUri,
            serviceName,
            user,
            scope,
            OAuth2Token.Type.ACCESS);
        final OAuth2Token refreshToken =
          this.getToken(
            appId,
            gadgetUri,
            serviceName,
            user,
            scope,
            OAuth2Token.Type.REFRESH);

        final AipoOAuth2Accessor newAccessor =
          new AipoOAuth2Accessor(
            appId,
            gadgetUri,
            serviceName,
            user,
            scope,
            client.isAllowModuleOverride(),
            this,
            this.globalRedirectUri,
            this.authority,
            this.contextRoot);
        newAccessor.setAccessToken(accessToken);
        newAccessor.setAuthorizationUrl(client.getAuthorizationUrl());
        newAccessor.setClientAuthenticationType(client
          .getClientAuthenticationType());
        newAccessor.setAuthorizationHeader(client.isAuthorizationHeader());
        newAccessor.setUrlParameter(client.isUrlParameter());
        newAccessor.setClientId(client.getClientId());
        newAccessor.setClientSecret(client.getClientSecret());
        newAccessor.setGrantType(client.getGrantType());
        newAccessor.setRedirectUri(client.getRedirectUri());
        newAccessor.setRefreshToken(refreshToken);
        newAccessor.setTokenUrl(client.getTokenUrl());
        newAccessor.setType(client.getType());
        newAccessor.setAllowedDomains(client.getAllowedDomains());
        ret = newAccessor;

        this.storeOAuth2Accessor(ret);
      }
    }

    if (isLogging) {
      AipoOAuth2Store.LOG.exiting(
        AipoOAuth2Store.LOG_CLASS,
        "getOAuth2Accessor",
        ret);
    }

    return ret;
  }

  @Override
  public OAuth2Token getToken(final String appId, final String gadgetUri,
      final String serviceName, final String user, final String scope,
      final OAuth2Token.Type type) throws GadgetException {

    final boolean isLogging = AipoOAuth2Store.LOG.isLoggable();
    if (isLogging) {
      AipoOAuth2Store.LOG.entering(
        AipoOAuth2Store.LOG_CLASS,
        "getToken",
        new Object[] { gadgetUri, serviceName, user, scope, type });
    }

    final String processedGadgetUri =
      this.getGadgetUri(appId, gadgetUri, serviceName);
    OAuth2Token token =
      this.cache.getToken(processedGadgetUri, serviceName, user, scope, type);
    if (token == null) {
      try {
        token =
          this.persister.findToken(
            appId,
            processedGadgetUri,
            serviceName,
            user,
            scope,
            type);
        if (token != null) {
          synchronized (token) {
            try {
              token.setGadgetUri(processedGadgetUri);
              this.cache.storeToken(token);
            } finally {
              token.setGadgetUri(gadgetUri);
            }
          }
        }
      } catch (final OAuth2PersistenceException e) {
        throw new GadgetException(
          Code.OAUTH_STORAGE_ERROR,
          "Error loading OAuth2 token",
          e);
      }
    }

    if (isLogging) {
      AipoOAuth2Store.LOG.exiting(AipoOAuth2Store.LOG_CLASS, "getToken", token);
    }

    return token;
  }

  @Override
  public boolean init() throws GadgetException {
    final boolean isLogging = AipoOAuth2Store.LOG.isLoggable();
    if (isLogging) {
      AipoOAuth2Store.LOG.entering(AipoOAuth2Store.LOG_CLASS, "init");
    }

    if (this.cache.isPrimed()) {
      if (isLogging) {
        AipoOAuth2Store.LOG.exiting(AipoOAuth2Store.LOG_CLASS, "init", false);
      }
      return false;
    }

    this.clearCache();

    try {
      final Set<OAuth2Client> clients = this.persister.loadClients();
      if (isLogging) {
        AipoOAuth2Store.LOG.log("clients = {0}", clients);
      }
      this.cache.storeClients(clients);
    } catch (final OAuth2PersistenceException e) {
      throw new GadgetException(
        Code.OAUTH_STORAGE_ERROR,
        "Error loading OAuth2 clients",
        e);
    }

    try {
      final Set<OAuth2Token> tokens = this.persister.loadTokens();
      if (isLogging) {
        AipoOAuth2Store.LOG.log("tokens = {0}", tokens);
      }
      this.cache.storeTokens(tokens);
    } catch (final OAuth2PersistenceException e) {
      throw new GadgetException(
        Code.OAUTH_STORAGE_ERROR,
        "Error loading OAuth2 tokens",
        e);
    }

    if (isLogging) {
      AipoOAuth2Store.LOG.exiting(AipoOAuth2Store.LOG_CLASS, "init", true);
    }

    return true;
  }

  @Override
  public OAuth2Accessor removeOAuth2Accessor(final OAuth2Accessor accessor) {
    final boolean isLogging = AipoOAuth2Store.LOG.isLoggable();
    if (isLogging) {
      AipoOAuth2Store.LOG.entering(
        AipoOAuth2Store.LOG_CLASS,
        "removeOAuth2Accessor",
        accessor);
    }

    final OAuth2Accessor ret = null;

    if (accessor != null) {
      return this.cache.removeOAuth2Accessor(accessor);
    }

    if (isLogging) {
      AipoOAuth2Store.LOG.exiting(
        AipoOAuth2Store.LOG_CLASS,
        "removeOAuth2Accessor",
        ret);
    }

    return ret;
  }

  @Override
  public OAuth2Token removeToken(final OAuth2Token token)
      throws GadgetException {
    final boolean isLogging = AipoOAuth2Store.LOG.isLoggable();
    if (isLogging) {
      AipoOAuth2Store.LOG.entering(
        AipoOAuth2Store.LOG_CLASS,
        "removeToken",
        token);
    }

    if (token != null) {
      if (isLogging) {
        AipoOAuth2Store.LOG.exiting(
          AipoOAuth2Store.LOG_CLASS,
          "removeToken",
          token);
      }

      try {
        synchronized (token) {
          final String origGadgetApi = token.getGadgetUri();
          final String processedGadgetUri =
            this.getGadgetUri(null, token.getGadgetUri(), token
              .getServiceName());
          token.setGadgetUri(processedGadgetUri);
          try {
            // Remove token from the cache
            this.cache.removeToken(token);
            // Token is gone from the cache, also remove it from persistence
            this.persister.removeToken(processedGadgetUri, token
              .getServiceName(), token.getUser(), token.getScope(), token
              .getType());
          } finally {
            token.setGadgetUri(origGadgetApi);
          }
        }

        return token;
      } catch (final OAuth2PersistenceException e) {
        if (isLogging) {
          AipoOAuth2Store.LOG.log("Error removing OAuth2 token ", e);
        }
        throw new GadgetException(
          Code.OAUTH_STORAGE_ERROR,
          "Error removing OAuth2 token " + token.getServiceName(),
          e);
      }
    }

    if (isLogging) {
      AipoOAuth2Store.LOG.exiting(
        AipoOAuth2Store.LOG_CLASS,
        "removeToken",
        null);
    }

    return null;
  }

  public static boolean runImport(final OAuth2Persister source,
      final OAuth2Persister target, final boolean clean) {
    if (AipoOAuth2Store.LOG.isLoggable()) {
      AipoOAuth2Store.LOG.entering(
        AipoOAuth2Store.LOG_CLASS,
        "runImport",
        new Object[] { source, target, clean });
    }

    // No import for default persistence
    return false;
  }

  @Override
  public void setToken(final OAuth2Token token) throws GadgetException {
    final boolean isLogging = AipoOAuth2Store.LOG.isLoggable();
    if (isLogging) {
      AipoOAuth2Store.LOG
        .entering(AipoOAuth2Store.LOG_CLASS, "setToken", token);
    }

    if (token != null) {
      final String gadgetUri = token.getGadgetUri();
      final String serviceName = token.getServiceName();

      final String processedGadgetUri =
        this.getGadgetUri(token.getAppId(), gadgetUri, serviceName);
      synchronized (token) {
        token.setGadgetUri(processedGadgetUri);
        try {
          final OAuth2Token existingToken =
            this.getToken(
              token.getAppId(),
              gadgetUri,
              token.getServiceName(),
              token.getUser(),
              token.getScope(),
              token.getType());
          try {
            if (existingToken == null) {
              this.persister.insertToken(token);
            } else {
              synchronized (existingToken) {
                try {
                  existingToken.setGadgetUri(processedGadgetUri);
                  this.cache.removeToken(existingToken);
                  this.persister.updateToken(token);
                } finally {
                  existingToken.setGadgetUri(gadgetUri);
                }
              }
            }
            this.cache.storeToken(token);
          } catch (final OAuth2CacheException e) {
            if (isLogging) {
              AipoOAuth2Store.LOG.log("Error storing OAuth2 token", e);
            }
            throw new GadgetException(
              Code.OAUTH_STORAGE_ERROR,
              "Error storing OAuth2 token",
              e);
          } catch (final OAuth2PersistenceException e) {
            if (isLogging) {
              AipoOAuth2Store.LOG.log("Error storing OAuth2 token", e);
            }
            throw new GadgetException(
              Code.OAUTH_STORAGE_ERROR,
              "Error storing OAuth2 token",
              e);
          }
        } finally {
          token.setGadgetUri(gadgetUri);
        }
      }
    }

    if (isLogging) {
      AipoOAuth2Store.LOG.exiting(AipoOAuth2Store.LOG_CLASS, "setToken");
    }
  }

  @Override
  public void storeOAuth2Accessor(final OAuth2Accessor accessor) {
    final boolean isLogging = AipoOAuth2Store.LOG.isLoggable();
    if (isLogging) {
      AipoOAuth2Store.LOG.entering(
        AipoOAuth2Store.LOG_CLASS,
        "storeOAuth2Accessor",
        accessor);
    }

    this.cache.storeOAuth2Accessor(accessor);

    if (isLogging) {
      AipoOAuth2Store.LOG.exiting(
        AipoOAuth2Store.LOG_CLASS,
        "storeOAuth2Accessor");
    }
  }

  protected String getGadgetUri(final String appId, final String gadgetUri,
      final String serviceName) throws GadgetException {
    String ret = gadgetUri;
    final OAuth2Client client = this.getClient(appId, ret, serviceName);
    if (client != null) {
      if (client.isSharedToken()) {
        ret = client.getClientId() + ':' + client.getServiceName();
      }
    }

    return ret;
  }

  protected OAuth2Token internalCreateToken() {
    return new OAuth2TokenPersistence(this.encrypter);
  }

  @Override
  public BlobCrypter getStateCrypter() {
    return this.stateCrypter;
  }

  @Override
  public OAuth2Client invalidateClient(final OAuth2Client client) {
    return this.cache.removeClient(client);
  }

  @Override
  public OAuth2Token invalidateToken(final OAuth2Token token) {
    return this.cache.removeToken(token);
  }

  @Override
  public void clearAccessorCache() throws GadgetException {
    final boolean isLogging = AipoOAuth2Store.LOG.isLoggable();
    if (isLogging) {
      AipoOAuth2Store.LOG.entering(
        AipoOAuth2Store.LOG_CLASS,
        "clearAccessorCache");
    }

    try {
      this.cache.clearAccessors();
    } catch (final OAuth2CacheException e) {
      if (isLogging) {
        AipoOAuth2Store.LOG.log("Error clearing OAuth2 Accessor cache", e);
      }
      throw new GadgetException(
        Code.OAUTH_STORAGE_ERROR,
        "Error clearing OAuth2Accessor cache",
        e);
    }

    if (isLogging) {
      AipoOAuth2Store.LOG.exiting(
        AipoOAuth2Store.LOG_CLASS,
        "clearAccessorCache");
    }
  }

  @Override
  public void clearTokenCache() throws GadgetException {
    final boolean isLogging = AipoOAuth2Store.LOG.isLoggable();
    if (isLogging) {
      AipoOAuth2Store.LOG
        .entering(AipoOAuth2Store.LOG_CLASS, "clearTokenCache");
    }

    try {
      this.cache.clearTokens();
    } catch (final OAuth2CacheException e) {
      if (isLogging) {
        AipoOAuth2Store.LOG.log("Error clearing OAuth2 Token cache", e);
      }
      throw new GadgetException(
        Code.OAUTH_STORAGE_ERROR,
        "Error clearing OAuth2Token cache",
        e);
    }

    if (isLogging) {
      AipoOAuth2Store.LOG.exiting(AipoOAuth2Store.LOG_CLASS, "clearTokenCache");
    }
  }

  @Override
  public void clearClientCache() throws GadgetException {
    final boolean isLogging = AipoOAuth2Store.LOG.isLoggable();
    if (isLogging) {
      AipoOAuth2Store.LOG.entering(
        AipoOAuth2Store.LOG_CLASS,
        "clearClientCache");
    }

    try {
      this.cache.clearClients();
    } catch (final OAuth2CacheException e) {
      if (isLogging) {
        AipoOAuth2Store.LOG.log("Error clearing OAuth2 Client cache", e);
      }
      throw new GadgetException(
        Code.OAUTH_STORAGE_ERROR,
        "Error clearing OAuth2Client cache",
        e);
    }

    if (isLogging) {
      AipoOAuth2Store.LOG
        .exiting(AipoOAuth2Store.LOG_CLASS, "clearClientCache");
    }
  }
}
