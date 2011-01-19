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

package com.aipo.container.gadgets.oauth;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.apache.shindig.common.crypto.BasicBlobCrypter;
import org.apache.shindig.common.crypto.BlobCrypter;
import org.apache.shindig.common.crypto.Crypto;
import org.apache.shindig.gadgets.http.HttpFetcher;
import org.apache.shindig.gadgets.oauth.OAuthFetcherConfig;
import org.apache.shindig.gadgets.oauth.OAuthRequest;
import org.apache.shindig.gadgets.oauth.OAuthStore;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * @OAuthModule
 */
public class AipoOAuthModule extends AbstractModule {

  private static final Logger logger = Logger.getLogger(AipoOAuthModule.class
    .getName());

  @Override
  protected void configure() {
    bind(BlobCrypter.class).annotatedWith(
      Names.named(OAuthFetcherConfig.OAUTH_STATE_CRYPTER)).toProvider(
      OAuthCrypterProvider.class);
    bind(OAuthStore.class).to(AipoOAuthStore.class).in(Scopes.SINGLETON);
    bind(OAuthRequest.class).toProvider(OAuthRequestProvider.class);
  }

  @Singleton
  public static class OAuthCrypterProvider implements Provider<BlobCrypter> {

    private final BlobCrypter crypter;

    @Inject
    public OAuthCrypterProvider(
        @Named("shindig.signing.state-key") String stateCrypterPath)
        throws IOException {
      if (StringUtils.isBlank(stateCrypterPath)) {
        logger.info("Using random key for OAuth client-side state encryption");
        crypter =
          new BasicBlobCrypter(Crypto
            .getRandomBytes(BasicBlobCrypter.MASTER_KEY_MIN_LEN));
      } else {
        logger.info("Using file "
          + stateCrypterPath
          + " for OAuth client-side state encryption");
        crypter = new BasicBlobCrypter(new File(stateCrypterPath));
      }
    }

    public BlobCrypter get() {
      return crypter;
    }
  }

  public static class OAuthRequestProvider implements Provider<OAuthRequest> {
    private final HttpFetcher fetcher;

    private final OAuthFetcherConfig config;

    @Inject
    public OAuthRequestProvider(HttpFetcher fetcher, OAuthFetcherConfig config) {
      this.fetcher = fetcher;
      this.config = config;
    }

    public OAuthRequest get() {
      return new OAuthRequest(config, fetcher);
    }
  }

}
