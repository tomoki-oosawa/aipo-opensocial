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
package com.aipo.container.gadgets.render;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.apache.shindig.gadgets.render.DefaultRpcServiceLookup;
import org.apache.shindig.gadgets.render.RpcServiceLookup;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * @see DefaultRpcServiceLookup
 */
@Singleton
public class AipoRpcServiceLookup implements RpcServiceLookup {

  private final ConcurrentMap<String, Multimap<String, String>> containerServices;

  private final AipoServiceFetcher fetcher;

  @Inject
  public AipoRpcServiceLookup(
      AipoServiceFetcher fetcher,
      @Named("org.apache.shindig.serviceExpirationDurationMinutes") Long duration) {
    containerServices =
      new MapMaker().expiration(duration * 60, TimeUnit.SECONDS).makeMap();
    this.fetcher = fetcher;
  }

  @Override
  public Multimap<String, String> getServicesFor(String container, String host) {
    // Support empty container or host by providing empty services:
    if (container == null || container.length() == 0 || host == null) {
      return ImmutableMultimap.<String, String> builder().build();
    }

    Multimap<String, String> foundServices = containerServices.get(container);
    if (foundServices == null) {
      foundServices = fetcher.getServicesForContainer(container, host);
      if (foundServices != null) {
        setServicesFor(container, foundServices);
      }
    }
    if (foundServices == null) {
      foundServices = ImmutableMultimap.<String, String> builder().build();
    }
    return foundServices;
  }

  void setServicesFor(String container, Multimap<String, String> foundServices) {
    containerServices.put(container, foundServices);
  }

}