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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.shindig.common.Nullable;
import org.apache.shindig.common.servlet.Authority;
import org.apache.shindig.common.util.ResourceLoader;
import org.apache.shindig.gadgets.oauth2.OAuth2Accessor;
import org.apache.shindig.gadgets.oauth2.OAuth2Message;
import org.apache.shindig.gadgets.oauth2.OAuth2Token;
import org.apache.shindig.gadgets.oauth2.OAuth2Token.Type;
import org.apache.shindig.gadgets.oauth2.logger.FilteredLogger;
import org.apache.shindig.gadgets.oauth2.persistence.OAuth2Client;
import org.apache.shindig.gadgets.oauth2.persistence.OAuth2Encrypter;
import org.apache.shindig.gadgets.oauth2.persistence.OAuth2EncryptionException;
import org.apache.shindig.gadgets.oauth2.persistence.OAuth2PersistenceException;
import org.apache.shindig.gadgets.oauth2.persistence.OAuth2Persister;
import org.apache.shindig.gadgets.oauth2.persistence.OAuth2TokenPersistence;
import org.apache.shindig.gadgets.oauth2.persistence.sample.OAuth2GadgetBinding;
import org.apache.shindig.gadgets.oauth2.persistence.sample.OAuth2Provider;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aipo.orm.service.ContainerConfigDbService;
import com.aipo.orm.service.OAuthConsumerDbService;
import com.aipo.orm.service.OAuthTokenDbService;
import com.aipo.orm.service.bean.OAuthConsumer;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Persistence implementation that reads <code>config/oauth2.json</code> on
 * startup
 *
 */
@Singleton
public class AipoOAuth2Persister implements OAuth2Persister {
  private static final String ALLOW_MODULE_OVERRIDE = "allowModuleOverride";

  private static final String AUTHORIZATION_HEADER = "usesAuthorizationHeader";

  private static final String AUTHORIZATION_URL = "authorizationUrl";

  private static final String CLIENT_AUTHENTICATION = "client_authentication";

  private static final String CLIENT_NAME = "clientName";

  private static final String CLIENTS = "clients";

  private static final String ENDPOINTS = "endpoints";

  private static final String GADGET_BINDGINGS = "gadgetBindings";

  private static final String NO_CLIENT_AUTHENTICATION = "NONE";

  private static final String OAUTH2_CONFIG = "config/oauth2.json";

  private static final String PROVIDER_NAME = "providerName";

  private static final String PROVIDERS = "providers";

  private static final String TOKEN_URL = "tokenUrl";

  private static final String TYPE = "type";

  private static final String URL_PARAMETER = "usesUrlParameter";

  private static final String ALLOWED_DOMAINS = "allowedDomains";

  private final JSONObject configFile;

  private final String contextRoot;

  private final OAuth2Encrypter encrypter;

  private final String globalRedirectUri;

  private final Authority authority;

  private static final String LOG_CLASS = AipoOAuth2Persister.class.getName();

  private static final FilteredLogger LOG = FilteredLogger
    .getFilteredLogger(AipoOAuth2Persister.LOG_CLASS);

  private final OAuthConsumerDbService oAuthConsumerDbService;

  private final OAuthTokenDbService oAuthTokenDbService;

  private final ContainerConfigDbService containerConfigDbService;

  @Inject
  public AipoOAuth2Persister(final OAuth2Encrypter encrypter,
      final Authority authority, final String globalRedirectUri,
      @Nullable @Named("shindig.contextroot") final String contextRoot,
      OAuthConsumerDbService oAuthConsumerDbService,
      OAuthTokenDbService oAuthTokenDbService,
      ContainerConfigDbService containerConfigDbService)
      throws OAuth2PersistenceException {
    this.encrypter = encrypter;
    this.authority = authority;
    this.globalRedirectUri = globalRedirectUri;
    this.contextRoot = contextRoot;
    this.oAuthConsumerDbService = oAuthConsumerDbService;
    this.oAuthTokenDbService = oAuthTokenDbService;
    this.containerConfigDbService = containerConfigDbService;
    try {
      this.configFile =
        new JSONObject(AipoOAuth2Persister
          .getJSONString(AipoOAuth2Persister.OAUTH2_CONFIG));
    } catch (final Exception e) {
      if (AipoOAuth2Persister.LOG.isLoggable()) {
        AipoOAuth2Persister.LOG.log("OAuth2PersistenceException", e);
      }
      throw new OAuth2PersistenceException(e);
    }
  }

  public OAuth2Token createToken() {
    return new OAuth2TokenPersistence(this.encrypter);
  }

  @Override
  public OAuth2Client findClient(final String appId, final String gadgetUri,
      final String serviceName) throws OAuth2PersistenceException {
    OAuthConsumer consumer = oAuthConsumerDbService.get(appId, serviceName);
    OAuth2Client client = new OAuth2Client(this.encrypter);
    client.setGadgetUri(gadgetUri);
    client.setServiceName(serviceName);
    client.setClientId(consumer.getConsumerKey());
    client.setGrantType(OAuth2Message.AUTHORIZATION);
    client.setAllowModuleOverride(true);
    try {
      client.setClientSecret(consumer.getConsumerSecret().getBytes());
    } catch (OAuth2EncryptionException e) {
      if (AipoOAuth2Persister.LOG.isLoggable()) {
        AipoOAuth2Persister.LOG.log("OAuth2EncryptionException", e);
      }
    }
    return client;
  }

  @Override
  public OAuth2Token findToken(final String appId, final String gadgetUri,
      final String providerName, final String user, final String scope,
      final Type type) throws OAuth2PersistenceException {
    // TODO
    return null;
  }

  @Override
  public void insertToken(final OAuth2Token token) {
    // TODO
  }

  @Override
  public Set<OAuth2Client> loadClients() throws OAuth2PersistenceException {
    final Map<String, OAuth2GadgetBinding> gadgetBindings =
      this.loadGadgetBindings();
    final Map<String, OAuth2Provider> providers = this.loadProviders();

    final Map<String, OAuth2Client> internalMap = Maps.newHashMap();

    try {
      final JSONObject clients =
        this.configFile.getJSONObject(AipoOAuth2Persister.CLIENTS);
      for (final Iterator<?> j = clients.keys(); j.hasNext();) {
        final String clientName = (String) j.next();
        final JSONObject settings = clients.getJSONObject(clientName);

        final OAuth2Client client = new OAuth2Client(this.encrypter);

        final String providerName =
          settings.getString(AipoOAuth2Persister.PROVIDER_NAME);
        final OAuth2Provider provider = providers.get(providerName);
        client.setAuthorizationUrl(provider.getAuthorizationUrl());
        client.setClientAuthenticationType(provider
          .getClientAuthenticationType());
        client.setAuthorizationHeader(provider.isAuthorizationHeader());
        client.setUrlParameter(provider.isUrlParameter());
        client.setTokenUrl(provider.getTokenUrl());

        String redirectUri =
          settings.optString(OAuth2Message.REDIRECT_URI, null);
        if (redirectUri == null) {
          redirectUri = this.globalRedirectUri;
        }
        final String secret = settings.optString(OAuth2Message.CLIENT_SECRET);
        final String clientId = settings.getString(OAuth2Message.CLIENT_ID);
        final String typeS = settings.optString(AipoOAuth2Persister.TYPE, null);
        String grantType = settings.optString(OAuth2Message.GRANT_TYPE, null);
        final String sharedToken =
          settings.optString(OAuth2Message.SHARED_TOKEN, "false");
        if ("true".equalsIgnoreCase(sharedToken)) {
          client.setSharedToken(true);
        }

        try {
          client.setEncryptedSecret(secret.getBytes("UTF-8"));
        } catch (final OAuth2EncryptionException e) {
          throw new OAuth2PersistenceException(e);
        }

        client.setClientId(clientId);

        if (this.authority != null) {
          redirectUri =
            redirectUri.replace("%authority%", this.authority.getAuthority());
          redirectUri = redirectUri.replace("%contextRoot%", this.contextRoot);
          redirectUri =
            redirectUri.replace("%origin%", this.authority.getOrigin());
          redirectUri =
            redirectUri.replace("%scheme", this.authority.getScheme());
        }
        client.setRedirectUri(redirectUri);

        if (grantType == null || grantType.length() == 0) {
          grantType = OAuth2Message.AUTHORIZATION;
        }

        client.setGrantType(grantType);

        OAuth2Accessor.Type type = OAuth2Accessor.Type.UNKNOWN;
        if (OAuth2Message.CONFIDENTIAL_CLIENT_TYPE.equals(typeS)) {
          type = OAuth2Accessor.Type.CONFIDENTIAL;
        } else if (OAuth2Message.PUBLIC_CLIENT_TYPE.equals(typeS)) {
          type = OAuth2Accessor.Type.PUBLIC;
        }
        client.setType(type);

        final JSONArray dArray =
          settings.optJSONArray(AipoOAuth2Persister.ALLOWED_DOMAINS);
        if (dArray != null) {
          final ArrayList<String> domains = new ArrayList<String>();
          for (int i = 0; i < dArray.length(); i++) {
            domains.add(dArray.optString(i));
          }
          client.setAllowedDomains(domains.toArray(new String[0]));
        }

        internalMap.put(clientName, client);
      }
    } catch (final Exception e) {
      if (AipoOAuth2Persister.LOG.isLoggable()) {
        AipoOAuth2Persister.LOG.log("OAuth2PersistenceException", e);
      }
      throw new OAuth2PersistenceException(e);
    }

    final Set<OAuth2Client> ret =
      new HashSet<OAuth2Client>(gadgetBindings.size());
    for (final OAuth2GadgetBinding binding : gadgetBindings.values()) {
      final String clientName = binding.getClientName();
      final OAuth2Client cachedClient = internalMap.get(clientName);
      final OAuth2Client client = cachedClient.clone();
      client.setGadgetUri(binding.getGadgetUri());
      client.setServiceName(binding.getGadgetServiceName());
      client.setAllowModuleOverride(binding.isAllowOverride());
      ret.add(client);
    }

    return ret;
  }

  protected Map<String, OAuth2GadgetBinding> loadGadgetBindings()
      throws OAuth2PersistenceException {
    final Map<String, OAuth2GadgetBinding> ret = Maps.newHashMap();

    try {
      final JSONObject bindings =
        this.configFile.getJSONObject(AipoOAuth2Persister.GADGET_BINDGINGS);
      for (final Iterator<?> i = bindings.keys(); i.hasNext();) {
        final String gadgetUriS = (String) i.next();
        String gadgetUri = null;
        if (this.authority != null) {
          gadgetUri =
            gadgetUriS.replace("%authority%", this.authority.getAuthority());
          gadgetUri = gadgetUri.replace("%contextRoot%", this.contextRoot);
          gadgetUri = gadgetUri.replace("%origin%", this.authority.getOrigin());
          gadgetUri = gadgetUri.replace("%scheme%", this.authority.getScheme());
        }

        final JSONObject binding = bindings.getJSONObject(gadgetUriS);
        for (final Iterator<?> j = binding.keys(); j.hasNext();) {
          final String gadgetServiceName = (String) j.next();
          final JSONObject settings = binding.getJSONObject(gadgetServiceName);
          final String clientName =
            settings.getString(AipoOAuth2Persister.CLIENT_NAME);
          final boolean allowOverride =
            settings.getBoolean(AipoOAuth2Persister.ALLOW_MODULE_OVERRIDE);
          final OAuth2GadgetBinding gadgetBinding =
            new OAuth2GadgetBinding(
              gadgetUri,
              gadgetServiceName,
              clientName,
              allowOverride);

          ret.put(gadgetBinding.getGadgetUri()
            + ':'
            + gadgetBinding.getGadgetServiceName(), gadgetBinding);
        }
      }

    } catch (final JSONException e) {
      if (AipoOAuth2Persister.LOG.isLoggable()) {
        AipoOAuth2Persister.LOG.log("OAuth2PersistenceException", e);
      }
      throw new OAuth2PersistenceException(e);
    }

    return ret;
  }

  protected Map<String, OAuth2Provider> loadProviders()
      throws OAuth2PersistenceException {
    final Map<String, OAuth2Provider> ret = Maps.newHashMap();

    try {
      final JSONObject providers =
        this.configFile.getJSONObject(AipoOAuth2Persister.PROVIDERS);
      for (final Iterator<?> i = providers.keys(); i.hasNext();) {
        final String providerName = (String) i.next();
        final JSONObject provider = providers.getJSONObject(providerName);
        final JSONObject endpoints =
          provider.getJSONObject(AipoOAuth2Persister.ENDPOINTS);

        final String clientAuthenticationType =
          provider.optString(
            AipoOAuth2Persister.CLIENT_AUTHENTICATION,
            AipoOAuth2Persister.NO_CLIENT_AUTHENTICATION);

        final boolean authorizationHeader =
          provider.optBoolean(AipoOAuth2Persister.AUTHORIZATION_HEADER, false);

        final boolean urlParameter =
          provider.optBoolean(AipoOAuth2Persister.URL_PARAMETER, false);

        String authorizationUrl =
          endpoints.optString(AipoOAuth2Persister.AUTHORIZATION_URL, null);

        if (this.authority != null && authorizationUrl != null) {
          authorizationUrl =
            authorizationUrl.replace("%authority%", this.authority
              .getAuthority());
          authorizationUrl =
            authorizationUrl.replace("%contextRoot%", this.contextRoot);
          authorizationUrl =
            authorizationUrl.replace("%origin%", this.authority.getOrigin());
          authorizationUrl =
            authorizationUrl.replace("%scheme%", this.authority.getScheme());
        }

        String tokenUrl =
          endpoints.optString(AipoOAuth2Persister.TOKEN_URL, null);
        if (this.authority != null && tokenUrl != null) {
          tokenUrl =
            tokenUrl.replace("%authority%", this.authority.getAuthority());
          tokenUrl = tokenUrl.replace("%contextRoot%", this.contextRoot);
          tokenUrl = tokenUrl.replace("%origin%", this.authority.getOrigin());
          tokenUrl = tokenUrl.replace("%scheme%", this.authority.getScheme());
        }

        final OAuth2Provider oauth2Provider = new OAuth2Provider();

        oauth2Provider.setName(providerName);
        oauth2Provider.setAuthorizationUrl(authorizationUrl);
        oauth2Provider.setTokenUrl(tokenUrl);
        oauth2Provider.setClientAuthenticationType(clientAuthenticationType);
        oauth2Provider.setAuthorizationHeader(authorizationHeader);
        oauth2Provider.setUrlParameter(urlParameter);

        ret.put(oauth2Provider.getName(), oauth2Provider);
      }
    } catch (final JSONException e) {
      if (AipoOAuth2Persister.LOG.isLoggable()) {
        AipoOAuth2Persister.LOG.log("OAuth2PersistenceException", e);
      }
      throw new OAuth2PersistenceException(e);
    }

    return ret;
  }

  @Override
  public Set<OAuth2Token> loadTokens() throws OAuth2PersistenceException {
    return Collections.emptySet();
  }

  @Override
  public boolean removeToken(final String providerName, final String gadgetUri,
      final String user, final String scope, final Type type) {
    // TODO
    return false;
  }

  @Override
  public void updateToken(final OAuth2Token token) {
    // TODO
  }

  private static String getJSONString(final String location) throws IOException {
    return ResourceLoader.getContent(location);
  }
}
