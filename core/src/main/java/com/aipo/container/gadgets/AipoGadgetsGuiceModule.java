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
package com.aipo.container.gadgets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.shindig.gadgets.DefaultGuiceModule;
import org.apache.shindig.gadgets.LockedDomainService;
import org.apache.shindig.gadgets.config.ConfigContributor;
import org.apache.shindig.gadgets.config.CoreUtilConfigContributor;
import org.apache.shindig.gadgets.config.OsapiServicesConfigContributor;
import org.apache.shindig.gadgets.config.ShindigAuthConfigContributor;
import org.apache.shindig.gadgets.config.XhrwrapperConfigContributor;
import org.apache.shindig.gadgets.http.HttpResponse;
import org.apache.shindig.gadgets.http.InvalidationHandler;
import org.apache.shindig.gadgets.preload.PreloadModule;
import org.apache.shindig.gadgets.render.RenderModule;
import org.apache.shindig.gadgets.render.RpcServiceLookup;
import org.apache.shindig.gadgets.servlet.GadgetsHandler;
import org.apache.shindig.gadgets.servlet.HttpRequestHandler;
import org.apache.shindig.gadgets.servlet.JsonRpcHandler;
import org.apache.shindig.gadgets.templates.TemplateModule;
import org.apache.shindig.gadgets.variables.SubstituterModule;

import com.aipo.container.gadgets.parse.AipoParseModule;
import com.aipo.container.gadgets.render.AipoRpcServiceLookup;
import com.aipo.container.gadgets.rewrite.AipoRewriteModule;
import com.aipo.container.gadgets.servlet.AipoJsonRpcHandler;
import com.aipo.container.gadgets.uri.AipoUriModule;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * @see DefaultGuiceModule
 */
public class AipoGadgetsGuiceModule extends AbstractModule {

  /** {@inheritDoc} */
  @Override
  protected void configure() {

    bind(LockedDomainService.class).to(AipoHashLockedDomainService.class).in(
      Scopes.SINGLETON);
    bind(JsonRpcHandler.class).to(AipoJsonRpcHandler.class);
    bind(RpcServiceLookup.class).to(AipoRpcServiceLookup.class);

    final ExecutorService service =
      Executors.newCachedThreadPool(DAEMON_THREAD_FACTORY);
    bind(ExecutorService.class).toInstance(service);
    bind(ExecutorService.class).annotatedWith(
      Names.named("shindig.concat.executor")).toInstance(service);

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        service.shutdownNow();
      }
    });

    install(new AipoParseModule());
    install(new PreloadModule());
    install(new RenderModule());
    install(new AipoRewriteModule());
    install(new SubstituterModule());
    install(new TemplateModule());
    install(new AipoUriModule());

    // bind(Long.class).annotatedWith(Names.named("org.apache.shindig.serviceExpirationDurationMinutes")).toInstance(60l);

    // We perform static injection on HttpResponse for cache TTLs.
    requestStaticInjection(HttpResponse.class);

    registerGadgetHandlers();
    registerConfigContributors();
    registerFeatureHandlers();
  }

  /**
   * Sets up multibinding for rpc handlers
   */
  protected void registerGadgetHandlers() {
    Multibinder<Object> handlerBinder =
      Multibinder.newSetBinder(binder(), Object.class, Names
        .named("org.apache.shindig.handlers"));
    handlerBinder.addBinding().to(InvalidationHandler.class);
    handlerBinder.addBinding().to(HttpRequestHandler.class);
    handlerBinder.addBinding().to(GadgetsHandler.class);
  }

  protected void registerConfigContributors() {
    MapBinder<String, ConfigContributor> configBinder =
      MapBinder.newMapBinder(binder(), String.class, ConfigContributor.class);
    configBinder.addBinding("core.util").to(CoreUtilConfigContributor.class);
    configBinder.addBinding("osapi").to(OsapiServicesConfigContributor.class);
    configBinder.addBinding("shindig.auth").to(
      ShindigAuthConfigContributor.class);
    configBinder.addBinding("shindig.xhrwrapper").to(
      XhrwrapperConfigContributor.class);

  }

  /**
   * Sets up the multibinding for extended feature resources
   */
  protected void registerFeatureHandlers() {
    /* Multibinder<String> featureBinder = */
    Multibinder.newSetBinder(binder(), String.class, Names
      .named("org.apache.shindig.features-extended"));
  }

  /**
   * Merges the features provided in shindig.properties with the extended
   * features from multibinding
   * 
   * @param features
   *          Comma separated string from shindig.properties key
   *          'shindig.features.default'
   * @param extended
   *          Set of paths/resources from plugins
   * @return the merged, list of all features to load.
   */
  @Provides
  @Singleton
  @Named("org.apache.shindig.features")
  protected List<String> defaultFeatures(
      @Named("shindig.features.default") String features,
      @Named("org.apache.shindig.features-extended") Set<String> extended) {
    return ImmutableList.<String> builder().addAll(extended).add(
      StringUtils.split(features, ',')).build();
  }

  public static final ThreadFactory DAEMON_THREAD_FACTORY =
    new ThreadFactory() {
      private final ThreadFactory factory = Executors.defaultThreadFactory();

      @Override
      public Thread newThread(Runnable r) {
        Thread t = factory.newThread(r);
        t.setDaemon(true);
        return t;
      }
    };
}
