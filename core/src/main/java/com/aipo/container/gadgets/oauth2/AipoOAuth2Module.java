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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;
import org.apache.shindig.common.Nullable;
import org.apache.shindig.common.crypto.BasicBlobCrypter;
import org.apache.shindig.common.crypto.BlobCrypter;
import org.apache.shindig.common.crypto.Crypto;
import org.apache.shindig.common.logging.i18n.MessageKeys;
import org.apache.shindig.common.servlet.Authority;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.http.HttpFetcher;
import org.apache.shindig.gadgets.oauth2.AipoOAuth2Request;
import org.apache.shindig.gadgets.oauth2.BasicOAuth2Request;
import org.apache.shindig.gadgets.oauth2.BasicOAuth2RequestParameterGenerator;
import org.apache.shindig.gadgets.oauth2.BasicOAuth2Store;
import org.apache.shindig.gadgets.oauth2.OAuth2FetcherConfig;
import org.apache.shindig.gadgets.oauth2.OAuth2Module;
import org.apache.shindig.gadgets.oauth2.OAuth2Request;
import org.apache.shindig.gadgets.oauth2.OAuth2RequestParameterGenerator;
import org.apache.shindig.gadgets.oauth2.OAuth2Store;
import org.apache.shindig.gadgets.oauth2.handler.AuthorizationEndpointResponseHandler;
import org.apache.shindig.gadgets.oauth2.handler.ClientAuthenticationHandler;
import org.apache.shindig.gadgets.oauth2.handler.GrantRequestHandler;
import org.apache.shindig.gadgets.oauth2.handler.ResourceRequestHandler;
import org.apache.shindig.gadgets.oauth2.handler.TokenEndpointResponseHandler;
import org.apache.shindig.gadgets.oauth2.logger.FilteredLogger;
import org.apache.shindig.gadgets.oauth2.persistence.OAuth2Cache;
import org.apache.shindig.gadgets.oauth2.persistence.OAuth2Encrypter;
import org.apache.shindig.gadgets.oauth2.persistence.OAuth2PersistenceException;
import org.apache.shindig.gadgets.oauth2.persistence.OAuth2Persister;
import org.apache.shindig.gadgets.oauth2.persistence.sample.JSONOAuth2Persister;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * Injects the default OAuth2 implementation for {@link BasicOAuth2Request} and
 * {@link BasicOAuth2Store}
 *
 * @see OAuth2Module
 *
 */
public class AipoOAuth2Module extends AbstractModule {
  private static final String CLASS_NAME = AipoOAuth2Module.class.getName();

  static final FilteredLogger LOG = FilteredLogger
    .getFilteredLogger(AipoOAuth2Module.CLASS_NAME);

  public static final String OAUTH2_IMPORT = "shindig.oauth2.import";

  public static final String OAUTH2_IMPORT_CLEAN =
    "shindig.oauth2.import.clean";

  public static final String OAUTH2_REDIRECT_URI =
    "shindig.oauth2.global-redirect-uri";

  public static final String SEND_TRACE_TO_CLIENT =
    "shindig.oauth2.send-trace-to-client";

  public static class OAuth2RequestProvider implements Provider<OAuth2Request> {
    private final List<AuthorizationEndpointResponseHandler> authorizationEndpointResponseHandlers;

    private final List<ClientAuthenticationHandler> clientAuthenticationHandlers;

    private final OAuth2FetcherConfig config;

    private final HttpFetcher fetcher;

    private final List<GrantRequestHandler> grantRequestHandlers;

    private final List<ResourceRequestHandler> resourceRequestHandlers;

    private final List<TokenEndpointResponseHandler> tokenEndpointResponseHandlers;

    private final boolean sendTraceToClient;

    private final OAuth2RequestParameterGenerator requestParameterGenerator;

    @Inject
    public OAuth2RequestProvider(
        final OAuth2FetcherConfig config,
        final HttpFetcher fetcher,
        final List<AuthorizationEndpointResponseHandler> authorizationEndpointResponseHandlers,
        final List<ClientAuthenticationHandler> clientAuthenticationHandlers,
        final List<GrantRequestHandler> grantRequestHandlers,
        final List<ResourceRequestHandler> resourceRequestHandlers,
        final List<TokenEndpointResponseHandler> tokenEndpointResponseHandlers,
        @Named(AipoOAuth2Module.SEND_TRACE_TO_CLIENT) final boolean sendTraceToClient,
        final OAuth2RequestParameterGenerator requestParameterGenerator) {
      this.config = config;
      this.fetcher = fetcher;
      this.authorizationEndpointResponseHandlers =
        authorizationEndpointResponseHandlers;
      this.clientAuthenticationHandlers = clientAuthenticationHandlers;
      this.grantRequestHandlers = grantRequestHandlers;
      this.resourceRequestHandlers = resourceRequestHandlers;
      this.tokenEndpointResponseHandlers = tokenEndpointResponseHandlers;
      this.sendTraceToClient = sendTraceToClient;
      this.requestParameterGenerator = requestParameterGenerator;
    }

    @Override
    public OAuth2Request get() {
      return new AipoOAuth2Request(
        this.config,
        this.fetcher,
        this.authorizationEndpointResponseHandlers,
        this.clientAuthenticationHandlers,
        this.grantRequestHandlers,
        this.resourceRequestHandlers,
        this.tokenEndpointResponseHandlers,
        this.sendTraceToClient,
        this.requestParameterGenerator);
    }
  }

  @Singleton
  public static class OAuth2StoreProvider implements Provider<OAuth2Store> {

    private final AipoOAuth2Store store;

    @Inject
    public OAuth2StoreProvider(
        @Named(AipoOAuth2Module.OAUTH2_REDIRECT_URI) final String globalRedirectUri,
        @Named(AipoOAuth2Module.OAUTH2_IMPORT) final boolean importFromConfig,
        @Named(AipoOAuth2Module.OAUTH2_IMPORT_CLEAN) final boolean importClean,
        final Authority authority,
        final OAuth2Cache cache,
        final OAuth2Persister persister,
        final OAuth2Encrypter encrypter,
        @Nullable @Named("shindig.contextroot") final String contextRoot,
        @Named(OAuth2FetcherConfig.OAUTH2_STATE_CRYPTER) final BlobCrypter stateCrypter) {

      this.store =
        new AipoOAuth2Store(
          cache,
          persister,
          encrypter,
          globalRedirectUri,
          authority,
          contextRoot,
          stateCrypter);

      if (importFromConfig) {
        try {
          final OAuth2Persister source =
            new JSONOAuth2Persister(
              encrypter,
              authority,
              globalRedirectUri,
              contextRoot);
          AipoOAuth2Store.runImport(source, persister, importClean);
        } catch (final OAuth2PersistenceException e) {
          if (AipoOAuth2Module.LOG.isLoggable()) {
            AipoOAuth2Module.LOG.log("store init exception", e);
          }
        }
      }

      try {
        this.store.init();
      } catch (final GadgetException e) {
        if (AipoOAuth2Module.LOG.isLoggable()) {
          AipoOAuth2Module.LOG.log("store init exception", e);
        }
      }
    }

    @Override
    public OAuth2Store get() {
      return this.store;
    }
  }

  @Singleton
  public static class OAuth2CrypterProvider implements Provider<BlobCrypter> {

    private final BlobCrypter crypter;

    @Inject
    public OAuth2CrypterProvider(
        @Named("shindig.signing.oauth2.state-key") final String stateCrypterPath)
        throws IOException {
      if (StringUtils.isBlank(stateCrypterPath)) {
        AipoOAuth2Module.LOG.log(
          Level.INFO,
          "Using random key for OAuth2 client-side state encryption",
          new Object[] {});
        if (AipoOAuth2Module.LOG.isLoggable(Level.INFO)) {
          AipoOAuth2Module.LOG.log(
            Level.INFO,
            "OAuth2CrypterProvider constructor",
            MessageKeys.USING_RANDOM_KEY);
        }
        this.crypter =
          new BasicBlobCrypter(Crypto
            .getRandomBytes(BasicBlobCrypter.MASTER_KEY_MIN_LEN));
      } else {
        if (AipoOAuth2Module.LOG.isLoggable(Level.INFO)) {
          AipoOAuth2Module.LOG.log(
            Level.INFO,
            "OAuth2CrypterProvider constructor",
            new Object[] { stateCrypterPath });
        }
        this.crypter = new BasicBlobCrypter(new File(stateCrypterPath));
      }
    }

    @Override
    public BlobCrypter get() {
      return this.crypter;
    }
  }

  @Override
  protected void configure() {
    this.bind(OAuth2Store.class).toProvider(OAuth2StoreProvider.class);
    this.bind(OAuth2Request.class).toProvider(OAuth2RequestProvider.class);
    this.bind(OAuth2RequestParameterGenerator.class).to(
      BasicOAuth2RequestParameterGenerator.class);
    // Used for encrypting client-side OAuth2 state.
    this.bind(BlobCrypter.class).annotatedWith(
      Names.named(OAuth2FetcherConfig.OAUTH2_STATE_CRYPTER)).toProvider(
      OAuth2CrypterProvider.class);
  }
}
