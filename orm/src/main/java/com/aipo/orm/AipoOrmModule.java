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

package com.aipo.orm;

import com.aipo.orm.service.AipoApplicationService;
import com.aipo.orm.service.AipoContainerConfigService;
import com.aipo.orm.service.AipoEipMPostService;
import com.aipo.orm.service.AipoOAuthConsumerService;
import com.aipo.orm.service.AipoOAuthTokenService;
import com.aipo.orm.service.AipoTurbineUserService;
import com.aipo.orm.service.ApplicationService;
import com.aipo.orm.service.ContainerConfigService;
import com.aipo.orm.service.EipMPostService;
import com.aipo.orm.service.OAuthConsumerService;
import com.aipo.orm.service.OAuthTokenService;
import com.aipo.orm.service.TurbineUserService;
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

    bind(TurbineUserService.class).to(AipoTurbineUserService.class).in(
      Scopes.SINGLETON);
    bind(EipMPostService.class).to(AipoEipMPostService.class).in(
      Scopes.SINGLETON);
    bind(ApplicationService.class).to(AipoApplicationService.class).in(
      Scopes.SINGLETON);
    bind(ContainerConfigService.class).to(AipoContainerConfigService.class).in(
      Scopes.SINGLETON);
    bind(OAuthConsumerService.class).to(AipoOAuthConsumerService.class).in(
      Scopes.SINGLETON);
    bind(OAuthTokenService.class).to(AipoOAuthTokenService.class).in(
      Scopes.SINGLETON);
  }

}
