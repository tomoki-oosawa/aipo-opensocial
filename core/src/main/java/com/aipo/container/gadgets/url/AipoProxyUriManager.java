/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2011 Aimluck,Inc.
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

package com.aipo.container.gadgets.url;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.common.uri.UriBuilder;
import org.apache.shindig.common.util.Utf8UrlCoder;
import org.apache.shindig.config.ContainerConfig;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.http.HttpResponse;
import org.apache.shindig.gadgets.uri.DefaultProxyUriManager;
import org.apache.shindig.gadgets.uri.ProxyUriManager;
import org.apache.shindig.gadgets.uri.UriCommon.Param;
import org.apache.shindig.gadgets.uri.UriStatus;

import com.aipo.container.util.ContainerToolkit;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.internal.Nullable;
import com.google.inject.name.Named;

/**
 * @see DefaultProxyUriManager
 */
public class AipoProxyUriManager implements ProxyUriManager {

  public static final String PROXY_HOST_PARAM = "gadgets.uri.proxy.host";

  public static final String PROXY_PATH_PARAM = "gadgets.uri.proxy.path";

  static final String CHAINED_PARAMS_TOKEN = "%chained_params%";

  private final ContainerConfig config;

  private final Versioner versioner;

  private boolean strictParsing = false;

  @Inject
  public AipoProxyUriManager(ContainerConfig config,
      @Nullable Versioner versioner) {
    this.config = config;
    this.versioner = versioner;
  }

  @Inject(optional = true)
  public void setUseStrictParsing(
      @Named("shindig.uri.proxy.use-strict-parsing") boolean useStrict) {
    this.strictParsing = useStrict;
  }

  public List<Uri> make(List<ProxyUri> resources, Integer forcedRefresh) {
    if (resources.isEmpty()) {
      return Collections.emptyList();
    }

    List<Uri> resourceUris = Lists.newArrayListWithCapacity(resources.size());

    for (ProxyUri puc : resources) {
      resourceUris.add(puc.getResource());
    }

    Map<Uri, String> versions;
    if (versioner == null) {
      versions = Collections.emptyMap();
    } else {
      versions = Maps.newHashMapWithExpectedSize(resources.size());
      List<String> versionList =
        versioner.version(resourceUris, resources.get(0).getContainer());
      if (versionList != null && versionList.size() == resources.size()) {
        // This should always be the case.
        // Should we error if not, or just WARNING?
        Iterator<String> versionIt = versionList.iterator();
        for (ProxyUri puc : resources) {
          versions.put(puc.getResource(), versionIt.next());
        }
      }
    }

    List<Uri> result = Lists.newArrayListWithCapacity(resources.size());
    for (ProxyUri puc : resources) {
      result.add(makeProxiedUri(puc, forcedRefresh, versions.get(puc
        .getResource())));
    }

    return result;
  }

  private Uri makeProxiedUri(ProxyUri puc, Integer forcedRefresh, String version) {
    UriBuilder queryBuilder = puc.makeQueryParams(forcedRefresh, version);

    String container = puc.getContainer();
    UriBuilder uri = new UriBuilder();

    uri.setAuthority(ContainerToolkit.getHost());
    uri.setScheme(ContainerToolkit.getScheme());

    // Chained vs. query-style syntax is determined by the presence of
    // CHAINED_PARAMS_TOKEN
    String path = getReqConfig(container, PROXY_PATH_PARAM);
    if (path.contains(CHAINED_PARAMS_TOKEN)) {
      // Chained proxy syntax. Stuff query params into the path and append URI
      // verbatim at the end
      path = path.replace(CHAINED_PARAMS_TOKEN, queryBuilder.getQuery());
      uri.setPath(path);
      String uriStr = uri.toString();
      String curUri =
        uriStr
          + (!uriStr.endsWith("/") ? "/" : "")
          + puc.getResource().toString();
      return Uri.parse(curUri);
    }

    // Query-style syntax. Use path as normal and append query params at the
    // end.
    queryBuilder.addQueryParameter(Param.URL.getKey(), puc
      .getResource()
      .toString());
    uri.setQuery(queryBuilder.getQuery());
    uri.setPath(path);

    return uri.toUri();
  }

  @SuppressWarnings("deprecation")
  public ProxyUri process(Uri uriIn) throws GadgetException {
    UriStatus status = UriStatus.BAD_URI;
    Uri uri = null;

    // First determine if the URI is chained-syntax or query-style.
    String container = uriIn.getQueryParameter(Param.CONTAINER.getKey());
    if (container == null) {
      container = uriIn.getQueryParameter(Param.SYND.getKey());
    }
    String uriStr = null;
    Uri queryUri = null;
    if (container != null
      && config.getString(container, PROXY_PATH_PARAM) != null
      && config.getString(container, PROXY_PATH_PARAM).equalsIgnoreCase(
        uriIn.getPath())) {
      // Query-style. Has container param and path matches.
      uriStr = uriIn.getQueryParameter(Param.URL.getKey());
      queryUri = uriIn;
    } else {
      // Check for chained query string in the path.
      String containerStr = Param.CONTAINER.getKey() + '=';
      String path = uriIn.getPath();
      // It is possible to get decoded url ('=' converted to %3d)
      // for example from CssResponseRewriter, so we should support it
      boolean doDecode = (!path.contains(containerStr));
      if (doDecode) {
        path = Utf8UrlCoder.decode(path);
      }
      int start = path.indexOf(containerStr);
      if (start > 0) {
        start += containerStr.length();
        int end = path.indexOf('&', start);
        if (end < start) {
          end = path.indexOf('/', start);
        }
        if (end > start) {
          // Looks like chained proxy syntax. Pull out params.
          container = path.substring(start, end);
        }
        if (container != null) {
          String proxyPath = config.getString(container, PROXY_PATH_PARAM);
          if (proxyPath != null) {
            String[] chainedChunks =
              StringUtils.splitByWholeSeparatorPreserveAllTokens(
                proxyPath,
                CHAINED_PARAMS_TOKEN);

            // Parse out the URI of the actual resource. This URI is found as
            // the
            // substring of the "full" URI, after the chained proxy prefix. We
            // first search for the pre- and post-fixes of the original
            // /pre/%chained_params%/post
            // ContainerConfig value, and take the URI as everything beyond that
            // point.
            String startToken = chainedChunks[0];
            String endToken = "/";
            if (chainedChunks.length == 2 && chainedChunks[1].length() > 0) {
              endToken = chainedChunks[1];
            }

            // Pull URI out of original inUri's full representation.
            String fullProxyUri = uriIn.toString();
            int startIx =
              fullProxyUri.indexOf(startToken) + startToken.length();
            int endIx = fullProxyUri.indexOf(endToken, startIx);
            if (startIx > 0 && endIx > 0) {
              String chainedQuery = fullProxyUri.substring(startIx, endIx);
              if (doDecode) {
                chainedQuery = Utf8UrlCoder.decode(chainedQuery);
              }
              queryUri = new UriBuilder().setQuery(chainedQuery).toUri();
              uriStr = fullProxyUri.substring(endIx + endToken.length());
              while (uriStr.startsWith("/")) {
                uriStr = uriStr.substring(1);
              }

            }
          }
        }
      }
    }

    if (!strictParsing && container != null && StringUtils.isEmpty(uriStr)) {
      // Query-style despite the container being configured for chained style.
      uriStr = uriIn.getQueryParameter(Param.URL.getKey());
      queryUri = uriIn;
    }

    // Parameter validation.
    if (StringUtils.isEmpty(uriStr) || StringUtils.isEmpty(container)) {
      throw new GadgetException(
        GadgetException.Code.MISSING_PARAMETER,
        "Missing required parameter(s):"
          + (StringUtils.isEmpty(uriStr) ? ' ' + Param.URL.getKey() : "")
          + (StringUtils.isEmpty(container)
            ? ' ' + Param.CONTAINER.getKey()
            : ""),
        HttpResponse.SC_BAD_REQUEST);
    }

    String queryHost = config.getString(container, PROXY_HOST_PARAM);
    if (strictParsing) {
      if (queryHost == null
        || !queryHost.equalsIgnoreCase(uriIn.getAuthority())) {
        throw new GadgetException(
          GadgetException.Code.INVALID_PATH,
          "Invalid proxy host",
          HttpResponse.SC_BAD_REQUEST);
      }
    }

    try {
      uri = Uri.parse(uriStr);
    } catch (Exception e) {
      // NullPointerException or InvalidArgumentException.
      throw new GadgetException(
        GadgetException.Code.INVALID_PARAMETER,
        "Invalid " + Param.URL.getKey() + ": " + uriStr,
        HttpResponse.SC_BAD_REQUEST);
    }

    // URI is valid.
    status = UriStatus.VALID_UNVERSIONED;

    String version = queryUri.getQueryParameter(Param.VERSION.getKey());
    if (versioner != null && version != null) {
      status = versioner.validate(uri, container, version);
    }

    ProxyUri proxied = new ProxyUri(status, uri, queryUri);
    proxied.setHtmlTagContext(uriIn.getQueryParameter(Param.HTML_TAG_CONTEXT
      .getKey()));
    return proxied;
  }

  private String getReqConfig(String container, String key) {
    String val = config.getString(container, key);
    if (val == null) {
      throw new RuntimeException("Missing required container config key: "
        + key
        + " for "
        + "container: "
        + container);
    }
    return val;
  }

}