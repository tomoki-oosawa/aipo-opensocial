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
package com.aipo.social.opensocial.spi;

import org.apache.shindig.social.opensocial.oauth.OAuthDataStore;
import org.apache.shindig.social.opensocial.service.GroupHandler;
import org.apache.shindig.social.opensocial.spi.ActivityService;
import org.apache.shindig.social.opensocial.spi.AlbumService;
import org.apache.shindig.social.opensocial.spi.AppDataService;
import org.apache.shindig.social.opensocial.spi.GroupService;
import org.apache.shindig.social.opensocial.spi.MediaItemService;
import org.apache.shindig.social.opensocial.spi.MessageService;
import org.apache.shindig.social.opensocial.spi.PersonService;
import org.apache.shindig.social.sample.oauth.SampleOAuthDataStore;
import org.apache.shindig.social.sample.spi.JsonDbOpensocialService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

/**
 *
 */
public class AipoSocialModule extends AbstractModule {

  /**
   * {@inheritDoc}
   * 
   * @see com.google.inject.AbstractModule#configure()
   */
  @Override
  protected void configure() {
    bind(String.class)
      .annotatedWith(Names.named("shindig.canonical.json.db"))
      .toInstance("sampledata/canonicaldb.json");
    bind(ActivityService.class).to(JsonDbOpensocialService.class);
    bind(AlbumService.class).to(JsonDbOpensocialService.class);
    bind(MediaItemService.class).to(JsonDbOpensocialService.class);
    bind(AppDataService.class).to(JsonDbOpensocialService.class);
    // bind(PersonService.class).to(JsonDbOpensocialService.class);
    bind(PersonService.class).to(PersonServiceDb.class).in(Scopes.SINGLETON);
    bind(GroupService.class).to(GroupServiceDb.class).in(Scopes.SINGLETON);
    bind(MessageService.class).to(JsonDbOpensocialService.class);
    bind(OAuthDataStore.class).to(SampleOAuthDataStore.class);

    Multibinder<Object> handlerBinder =
      Multibinder.newSetBinder(binder(), Object.class, Names
        .named("org.apache.shindig.handlers"));
    handlerBinder.addBinding().toInstance(GroupHandler.class);
  }
}
