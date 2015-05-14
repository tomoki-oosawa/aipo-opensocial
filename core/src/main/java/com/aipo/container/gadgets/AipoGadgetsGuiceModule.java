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
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.shindig.common.crypto.BasicBlobCrypter;
import org.apache.shindig.common.servlet.Authority;
import org.apache.shindig.common.servlet.BasicAuthority;
import org.apache.shindig.common.servlet.GuiceServletContextListener;
import org.apache.shindig.common.util.GenericDigestUtils;
import org.apache.shindig.gadgets.DefaultGuiceModule;
import org.apache.shindig.gadgets.LockedDomainService;
import org.apache.shindig.gadgets.config.DefaultConfigContributorModule;
import org.apache.shindig.gadgets.http.AbstractHttpCache;
import org.apache.shindig.gadgets.http.HttpResponse;
import org.apache.shindig.gadgets.http.InvalidationHandler;
import org.apache.shindig.gadgets.js.JsCompilerModule;
import org.apache.shindig.gadgets.js.JsServingPipelineModule;
import org.apache.shindig.gadgets.preload.PreloadModule;
import org.apache.shindig.gadgets.render.RenderModule;
import org.apache.shindig.gadgets.render.RpcServiceLookup;
import org.apache.shindig.gadgets.servlet.GadgetsHandler;
import org.apache.shindig.gadgets.servlet.HttpRequestHandler;
import org.apache.shindig.gadgets.servlet.JsonRpcHandler;
import org.apache.shindig.gadgets.templates.TemplateModule;
import org.apache.shindig.gadgets.uri.ProxyUriBase;
import org.apache.shindig.gadgets.variables.SubstituterModule;

import com.aipo.container.gadgets.parse.AipoParseModule;
import com.aipo.container.gadgets.render.AipoRpcServiceLookup;
import com.aipo.container.gadgets.rewrite.AipoRewriteModule;
import com.aipo.container.gadgets.servlet.AipoJsonRpcHandler;
import com.aipo.container.gadgets.uri.AipoUriModule;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
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

    bind(ExecutorService.class).to(ShindigExecutorService.class);
    bind(Executor.class)
      .annotatedWith(Names.named("shindig.concat.executor"))
      .to(ShindigExecutorService.class);

    bind(Authority.class).to(BasicAuthority.class);

    bindConstant().annotatedWith(Names.named("shindig.jsload.ttl-secs")).to(
      60 * 60); // 1 hour
    bindConstant().annotatedWith(
      Names.named("shindig.jsload.require-onload-with-jsload")).to(true);

    install(new DefaultConfigContributorModule());
    install(new AipoParseModule());
    install(new PreloadModule());
    install(new RenderModule());
    install(new AipoRewriteModule());
    install(new SubstituterModule());
    install(new TemplateModule());
    install(new AipoUriModule());
    install(new JsCompilerModule());
    install(new JsServingPipelineModule());

    // We perform static injection on HttpResponse for cache TTLs.
    requestStaticInjection(HttpResponse.class);
    requestStaticInjection(AbstractHttpCache.class);
    requestStaticInjection(ProxyUriBase.class);
    requestStaticInjection(GenericDigestUtils.class);
    requestStaticInjection(BasicBlobCrypter.class);
    registerGadgetHandlers();
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
    return ImmutableList.<String> builder().addAll(
      Splitter.on(',').split(features)).addAll(extended).build();
  }

  /**
   * A thread factory that sets the daemon flag to allow for clean servlet
   * shutdown.
   */
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

  /**
   * An Executor service that mimics
   * Executors.newCachedThreadPool(DAEMON_THREAD_FACTORY); Registers a cleanup
   * handler to shutdown the thread.
   */
  @Singleton
  public static class ShindigExecutorService extends ThreadPoolExecutor
      implements GuiceServletContextListener.CleanupCapable {
    @Inject
    public ShindigExecutorService(
        GuiceServletContextListener.CleanupHandler cleanupHandler) {
      super(
        0,
        Integer.MAX_VALUE,
        60L,
        TimeUnit.SECONDS,
        new SynchronousQueue<Runnable>(),
        DAEMON_THREAD_FACTORY);
      cleanupHandler.register(this);
    }

    @Override
    public void cleanup() {
      this.shutdown();
    }
  }
}
