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

import org.apache.shindig.gadgets.oauth2.persistence.OAuth2Cache;
import org.apache.shindig.gadgets.oauth2.persistence.OAuth2Encrypter;
import org.apache.shindig.gadgets.oauth2.persistence.OAuth2Persister;
import org.apache.shindig.gadgets.oauth2.persistence.sample.NoOpEncrypter;
import org.apache.shindig.gadgets.oauth2.persistence.sample.OAuth2PersistenceModule;

import com.google.inject.AbstractModule;

/**
 * Binds default persistence classes for shindig.
 *
 * @see OAuth2PersistenceModule
 *
 */
public class AipoOAuth2PersistenceModule extends AbstractModule {

  @Override
  protected void configure() {
    this.bind(OAuth2Persister.class).to(AipoOAuth2Persister.class);
    this.bind(OAuth2Cache.class).to(AipoOAuth2Cache.class);
    this.bind(OAuth2Encrypter.class).to(NoOpEncrypter.class);
  }
}
