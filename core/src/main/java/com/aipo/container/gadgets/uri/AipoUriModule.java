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

package com.aipo.container.gadgets.uri;

import org.apache.shindig.gadgets.uri.AllJsIframeVersioner;
import org.apache.shindig.gadgets.uri.ConcatUriManager;
import org.apache.shindig.gadgets.uri.DefaultIframeUriManager;
import org.apache.shindig.gadgets.uri.DefaultJsVersioner;
import org.apache.shindig.gadgets.uri.DefaultOAuthUriManager;
import org.apache.shindig.gadgets.uri.IframeUriManager;
import org.apache.shindig.gadgets.uri.JsUriManager;
import org.apache.shindig.gadgets.uri.OAuthUriManager;
import org.apache.shindig.gadgets.uri.ProxyUriManager;
import org.apache.shindig.gadgets.uri.UriModule;

import com.google.inject.AbstractModule;
import com.google.inject.util.Providers;

/**
 * @see UriModule
 */
public class AipoUriModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(IframeUriManager.class).to(DefaultIframeUriManager.class);
    bind(IframeUriManager.Versioner.class).to(AllJsIframeVersioner.class);

    bind(JsUriManager.class).to(AipoJsUriManager.class);
    bind(JsUriManager.Versioner.class).to(DefaultJsVersioner.class);

    bind(OAuthUriManager.class).to(DefaultOAuthUriManager.class);

    bind(ProxyUriManager.class).to(AipoProxyUriManager.class);
    bind(ProxyUriManager.Versioner.class).toProvider(
      Providers.<ProxyUriManager.Versioner> of(null));

    bind(ConcatUriManager.class).to(AipoConcatUriManager.class);
    bind(ConcatUriManager.Versioner.class).toProvider(
      Providers.<ConcatUriManager.Versioner> of(null));
  }
}
