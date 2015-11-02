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
package com.aipo.social.core.oauth;

import java.util.List;

import org.apache.shindig.auth.AipoSecurityTokenAuthenticationHandler;
import org.apache.shindig.auth.AipoUrlParameterAuthenticationHandler;
import org.apache.shindig.social.core.oauth.AuthenticationHandlerProvider;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @see AuthenticationHandlerProvider
 */
public class AipoSecurityTokenAuthenticationHandlerProvider implements
    Provider<List<AipoSecurityTokenAuthenticationHandler>> {
  protected List<AipoSecurityTokenAuthenticationHandler> handlers;

  @Inject
  public AipoSecurityTokenAuthenticationHandlerProvider(
      AipoOAuthAuthenticationHandler threeLeggedOAuth,
      AipoUrlParameterAuthenticationHandler urlParam,
      AipoAnonymousAuthenticationHandler anonymous) {

    handlers = Lists.newArrayList(urlParam, threeLeggedOAuth, anonymous);
  }

  @Override
  public List<AipoSecurityTokenAuthenticationHandler> get() {
    return handlers;
  }
}
