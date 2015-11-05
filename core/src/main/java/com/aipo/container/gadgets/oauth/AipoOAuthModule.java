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
package com.aipo.container.gadgets.oauth;

import org.apache.shindig.common.crypto.BlobCrypter;
import org.apache.shindig.gadgets.oauth.OAuthFetcherConfig;
import org.apache.shindig.gadgets.oauth.OAuthModule;
import org.apache.shindig.gadgets.oauth.OAuthRequest;
import org.apache.shindig.gadgets.oauth.OAuthStore;

import com.google.inject.Scopes;
import com.google.inject.name.Names;

/**
 * @OAuthModule
 */
public class AipoOAuthModule extends OAuthModule {

  @Override
  protected void configure() {
    bind(BlobCrypter.class).annotatedWith(
      Names.named(OAuthFetcherConfig.OAUTH_STATE_CRYPTER)).toProvider(
      OAuthCrypterProvider.class);
    bind(OAuthStore.class).to(AipoOAuthStore.class).in(Scopes.SINGLETON);
    bind(OAuthRequest.class).toProvider(OAuthRequestProvider.class);
  }

}
