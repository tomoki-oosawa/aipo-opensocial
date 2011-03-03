/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2011 Aimluck,Inc.
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

package com.aipo.orm;

import com.aipo.orm.service.ActivityDbService;
import com.aipo.orm.service.AipoActivityDbService;
import com.aipo.orm.service.AipoAppDataDbService;
import com.aipo.orm.service.AipoApplicationDbService;
import com.aipo.orm.service.AipoContainerConfigDbService;
import com.aipo.orm.service.AipoEipMPostDbService;
import com.aipo.orm.service.AipoOAuthConsumerDbService;
import com.aipo.orm.service.AipoOAuthEntryDbService;
import com.aipo.orm.service.AipoOAuthTokenDbService;
import com.aipo.orm.service.AipoTurbineUserDbService;
import com.aipo.orm.service.AppDataDbService;
import com.aipo.orm.service.ApplicationDbService;
import com.aipo.orm.service.ContainerConfigDbService;
import com.aipo.orm.service.EipMPostDbService;
import com.aipo.orm.service.OAuthConsumerDbService;
import com.aipo.orm.service.OAuthEntryDbService;
import com.aipo.orm.service.OAuthTokenDbService;
import com.aipo.orm.service.TurbineUserDbService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * 
 */
public class AipoOrmModule extends AbstractModule {

  /**
   * 
   */
  @Override
  protected void configure() {

    bind(ActivityDbService.class).to(AipoActivityDbService.class).in(
      Scopes.SINGLETON);
    bind(AppDataDbService.class).to(AipoAppDataDbService.class).in(
      Scopes.SINGLETON);
    bind(TurbineUserDbService.class).to(AipoTurbineUserDbService.class).in(
      Scopes.SINGLETON);
    bind(EipMPostDbService.class).to(AipoEipMPostDbService.class).in(
      Scopes.SINGLETON);
    bind(ApplicationDbService.class).to(AipoApplicationDbService.class).in(
      Scopes.SINGLETON);
    bind(ContainerConfigDbService.class)
      .to(AipoContainerConfigDbService.class)
      .in(Scopes.SINGLETON);
    bind(OAuthConsumerDbService.class).to(AipoOAuthConsumerDbService.class).in(
      Scopes.SINGLETON);
    bind(OAuthTokenDbService.class).to(AipoOAuthTokenDbService.class).in(
      Scopes.SINGLETON);
    bind(OAuthEntryDbService.class).to(AipoOAuthEntryDbService.class).in(
      Scopes.SINGLETON);
  }

}
