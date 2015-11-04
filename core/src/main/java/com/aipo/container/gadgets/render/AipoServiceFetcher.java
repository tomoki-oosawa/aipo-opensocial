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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.config.ContainerConfig;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.http.HttpFetcher;
import org.apache.shindig.gadgets.http.HttpRequest;
import org.apache.shindig.gadgets.http.HttpResponse;
import org.apache.shindig.gadgets.render.DefaultServiceFetcher;
import org.apache.shindig.gadgets.render.Renderer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aipo.container.http.HttpServletRequestLocator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;

/**
 * @see DefaultServiceFetcher
 */
public class AipoServiceFetcher {

  static final Logger logger = Logger.getLogger(Renderer.class.getName());

  static final String JSON_RESPONSE_WRAPPER_ELEMENT = "result";

  static final String OSAPI_FEATURE_CONFIG = "osapi";

  static final String OSAPI_SERVICES = "osapi.services";

  static final String GADGETS_FEATURES_CONFIG = "gadgets.features";

  static final String SYSTEM_LIST_METHODS_METHOD = "system.listMethods";

  /** Key in container config that lists the endpoints offering services */
  static final String OSAPI_BASE_ENDPOINTS = "endPoints";

  private final ContainerConfig containerConfig;

  private final HttpFetcher fetcher;

  /**
   * @param config
   *          Container Config for looking up endpoints
   */
  @Inject
  public AipoServiceFetcher(ContainerConfig config, HttpFetcher fetcher) {
    this.containerConfig = config;
    this.fetcher = fetcher;
  }

  /**
   * Returns the services, keyed by endpoint for the given container.
   *
   * @param container
   *          The particular container whose services we want.
   * @return Map endpoints and their serviceMethod list
   */
  public Multimap<String, String> getServicesForContainer(String container,
      String host) {
    if (containerConfig == null) {
      return ImmutableMultimap.<String, String> builder().build();
    }
    LinkedHashMultimap<String, String> endpointServices =
      LinkedHashMultimap.create();

    // First check services directly declared in container config
    @SuppressWarnings("unchecked")
    Map<String, Object> declaredServices =
      (Map<String, Object>) containerConfig.getMap(
        container,
        GADGETS_FEATURES_CONFIG).get(OSAPI_SERVICES);
    if (declaredServices != null) {
      for (Map.Entry<String, Object> entry : declaredServices.entrySet()) {
        @SuppressWarnings("unchecked")
        Iterable<String> entryValue = (Iterable<String>) entry.getValue();
        endpointServices.putAll(entry.getKey(), entryValue);
      }
    }

    HttpServletRequest request = HttpServletRequestLocator.get();
    // Merge services lazily loaded from the endpoints if any
    List<String> endpoints = getEndpointsFromContainerConfig(container, host);
    for (String endpoint : endpoints) {
      if (endpoint.startsWith("//")) {
        endpoint = request.getScheme() + ":" + endpoint;
      }
      Set<String> merge = endpointServices.get("gadgets.rpc");
      endpointServices.putAll(endpoint, merge);
    }

    return ImmutableMultimap.copyOf(endpointServices);
  }

  @SuppressWarnings("unchecked")
  private List<String> getEndpointsFromContainerConfig(String container,
      String host) {
    Map<String, Object> properties =
      (Map<String, Object>) containerConfig.getMap(
        container,
        GADGETS_FEATURES_CONFIG).get(OSAPI_FEATURE_CONFIG);

    if (properties != null) {
      return (List<String>) properties.get(OSAPI_BASE_ENDPOINTS);
    }
    return ImmutableList.of();
  }

  private Set<String> retrieveServices(String endpoint) {
    Uri url = Uri.parse(endpoint + "?method=" + SYSTEM_LIST_METHODS_METHOD);
    HttpRequest request = new HttpRequest(url);
    try {
      HttpResponse response = fetcher.fetch(request);
      if (response.getHttpStatusCode() == HttpResponse.SC_OK) {
        return getServicesFromJsonResponse(response.getResponseAsString());
      } else {
        logger.log(Level.SEVERE, "HTTP Error "
          + response.getHttpStatusCode()
          + " fetching service methods from endpoint "
          + endpoint);
      }
    } catch (GadgetException ge) {
      logger.log(
        Level.SEVERE,
        "Failed to fetch services methods from endpoint "
          + endpoint
          + ". Error "
          + ge.getMessage());
    } catch (JSONException je) {
      logger.log(
        Level.SEVERE,
        "Failed to parse services methods from endpoint "
          + endpoint
          + ". "
          + je.getMessage());
    }
    return ImmutableSet.of();
  }

  private Set<String> getServicesFromJsonResponse(String content)
      throws JSONException {
    ImmutableSet.Builder<String> services = ImmutableSet.builder();
    JSONObject js = new JSONObject(content);
    JSONArray json = js.getJSONArray(JSON_RESPONSE_WRAPPER_ELEMENT);
    for (int i = 0; i < json.length(); i++) {
      String o = json.getString(i);
      services.add(o);
    }
    return services.build();
  }
}
