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
package com.aipo.container.gadgets.render;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.shindig.gadgets.render.DefaultRpcServiceLookup;
import org.apache.shindig.gadgets.render.RpcServiceLookup;

import com.google.common.base.Objects;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * @see DefaultRpcServiceLookup
 */
@Singleton
public class AipoRpcServiceLookup implements RpcServiceLookup {

  private final Cache<String, Multimap<String, String>> containerServices;

  private final AipoServiceFetcher fetcher;

  /**
   * @param fetcher
   *          RpcServiceFetcher to retrieve services available from endpoints
   * @param duration
   *          in seconds service definitions should remain in the cache
   */
  @Inject
  public AipoRpcServiceLookup(
      AipoServiceFetcher fetcher,
      @Named("org.apache.shindig.serviceExpirationDurationMinutes") Long duration) {
    this.containerServices =
      CacheBuilder.newBuilder().expireAfterWrite(
        duration * 60,
        TimeUnit.SECONDS).build();
    this.fetcher = fetcher;
  }

  /**
   * @param container
   *          Syndicator param identifying the container for whom we want
   *          services
   * @param host
   *          Host for which gadget is being rendered, used to do substitution
   *          in endpoints
   * @return Map of Services, by endpoint for the given container.
   */
  @Override
  public Multimap<String, String> getServicesFor(final String container,
      final String host) {
    // Support empty container or host by providing empty services:
    if (container == null || container.length() == 0 || host == null) {
      return ImmutableMultimap.of();
    }
    try {
      return containerServices.get(
        container,
        new Callable<Multimap<String, String>>() {
          @Override
          public Multimap<String, String> call() {
            return Objects.firstNonNull(fetcher.getServicesForContainer(
              container,
              host), ImmutableMultimap.<String, String> of());
          }
        });
    } catch (ExecutionException e) {
      return ImmutableMultimap.of();
    }
  }

  /**
   * Setup the services for a given container.
   *
   * @param container
   *          The param identifying this container.
   * @param foundServices
   *          Map of services, keyed by endpoint.
   */
  void setServicesFor(String container, Multimap<String, String> foundServices) {
    containerServices.asMap().put(container, foundServices);
  }

}