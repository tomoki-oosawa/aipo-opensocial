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

package com.aipo.container.gadgets.uri;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.common.uri.UriBuilder;
import org.apache.shindig.config.ContainerConfig;
import org.apache.shindig.gadgets.uri.ConcatUriManager;
import org.apache.shindig.gadgets.uri.DefaultConcatUriManager;
import org.apache.shindig.gadgets.uri.UriCommon.Param;
import org.apache.shindig.gadgets.uri.UriStatus;

import com.aipo.container.util.ContainerToolkit;
import com.aipo.orm.service.ContainerConfigService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.internal.Nullable;
import com.google.inject.name.Named;

/**
 * @see DefaultConcatUriManager
 */
public class AipoConcatUriManager implements ConcatUriManager {

  public static final String CONCAT_HOST_PARAM = "gadgets.uri.concat.host";

  public static final String CONCAT_PATH_PARAM = "gadgets.uri.concat.path";

  public static final String CONCAT_JS_SPLIT_PARAM =
    "gadgets.uri.concat.js.splitToken";

  public static final String CONCAT_JS_EVAL_TPL = "eval(%s['%s']);";

  private static final ConcatUri BAD_URI = new ConcatUri(
    UriStatus.BAD_URI,
    null,
    null,
    null,
    null);

  private static final Integer START_INDEX = 1;

  private final ContainerConfig config;

  private final ContainerConfigService containerConfigService;

  private final Versioner versioner;

  private boolean strictParsing;

  @Inject
  public AipoConcatUriManager(ContainerConfig config,
      @Nullable Versioner versioner,
      ContainerConfigService containerConfigService) {
    this.config = config;
    this.versioner = versioner;
    this.containerConfigService = containerConfigService;
  }

  @Inject(optional = true)
  public void setUseStrictParsing(
      @Named("shindig.uri.concat.use-strict-parsing") boolean useStrict) {
    this.strictParsing = useStrict;
  }

  public List<ConcatData> make(List<ConcatUri> resourceUris, boolean isAdjacent) {
    List<ConcatData> concatUris =
      Lists.newArrayListWithCapacity(resourceUris.size());

    if (resourceUris.isEmpty()) {
      return concatUris;
    }

    ConcatUri exemplar = resourceUris.get(0);
    String container = exemplar.getContainer();

    List<String> versions = null;
    List<List<Uri>> batches =
      Lists.newArrayListWithCapacity(resourceUris.size());
    for (ConcatUri ctx : resourceUris) {
      batches.add(ctx.getBatch());
    }

    if (versioner != null) {
      versions = versioner.version(batches, container);
    }

    Iterator<String> versionIt = versions != null ? versions.iterator() : null;
    for (ConcatUri ctx : resourceUris) {
      String version = versionIt != null ? versionIt.next() : null;
      concatUris.add(makeConcatUri(ctx, isAdjacent, version));
    }

    return concatUris;
  }

  private ConcatData makeConcatUri(ConcatUri ctx, boolean isAdjacent,
      String version) {
    // TODO: Consider per-bundle isAdjacent plus first-bundle direct evaluation

    if (!isAdjacent && ctx.getType() != Type.JS) {
      // Split-concat is only supported for JS at the moment.
      // This situation should never occur due to ConcatLinkRewriter's
      // implementation.
      throw new UnsupportedOperationException(
        "Split concatenation only supported for JS");
    }

    UriBuilder uriBuilder = ctx.makeQueryParams(null, version);

    String concatHost = ContainerToolkit.getHost(containerConfigService);
    String concatPath = getReqVal(ctx.getContainer(), CONCAT_PATH_PARAM);
    uriBuilder.setAuthority(concatHost);
    uriBuilder.setPath(concatPath);

    uriBuilder.setScheme(ContainerToolkit.getScheme());

    uriBuilder.addQueryParameter(Param.TYPE.getKey(), ctx.getType().getType());
    List<Uri> resourceUris = ctx.getBatch();
    Map<Uri, String> snippets =
      Maps.newHashMapWithExpectedSize(resourceUris.size());

    String splitParam =
      config.getString(ctx.getContainer(), CONCAT_JS_SPLIT_PARAM);
    boolean doSplit = false;
    if (!isAdjacent
      && splitParam != null
      && !"false".equalsIgnoreCase(splitParam)) {
      uriBuilder.addQueryParameter(Param.JSON.getKey(), splitParam);
      doSplit = true;
    }

    Integer i = Integer.valueOf(START_INDEX);
    for (Uri resource : resourceUris) {
      uriBuilder.addQueryParameter(i.toString(), resource.toString());
      i++;
      if (doSplit) {
        snippets.put(resource, getJsSnippet(splitParam, resource));
      }
    }

    return new ConcatData(uriBuilder.toUri(), snippets);
  }

  static String getJsSnippet(String splitParam, Uri resource) {
    return String.format(CONCAT_JS_EVAL_TPL, splitParam, StringEscapeUtils
      .escapeJavaScript(resource.toString()));
  }

  private String getReqVal(String container, String key) {
    String val = config.getString(container, key);
    if (val == null) {
      throw new RuntimeException("Missing required config '"
        + key
        + "' for container: "
        + container);
    }
    return val;
  }

  public ConcatUri process(Uri uri) {
    String container = uri.getQueryParameter(Param.CONTAINER.getKey());
    if (strictParsing && container == null) {
      return BAD_URI;
    }

    if (strictParsing) {
      String concatHost = getReqVal(container, CONCAT_HOST_PARAM);
      String concatPath = getReqVal(container, CONCAT_PATH_PARAM);
      if (!uri.getAuthority().equalsIgnoreCase(concatHost)
        || !uri.getPath().equals(concatPath)) {
        return BAD_URI;
      }
    }

    // At this point the Uri is at least concat.
    UriStatus status = UriStatus.VALID_UNVERSIONED;
    List<Uri> uris = Lists.newLinkedList();
    Type type = Type.fromType(uri.getQueryParameter(Param.TYPE.getKey()));
    if (type == null) {
      // try "legacy" method
      type = Type.fromMime(uri.getQueryParameter("rewriteMime"));
      if (type == null) {
        return BAD_URI;
      }
    }
    String splitParam =
      type == Type.JS ? uri.getQueryParameter(Param.JSON.getKey()) : null;

    Integer i = Integer.valueOf(START_INDEX);
    String uriStr = null;
    while ((uriStr = uri.getQueryParameter(i.toString())) != null) {
      try {
        uris.add(Uri.parse(uriStr));
      } catch (IllegalArgumentException e) {
        // Malformed inbound Uri. Don't process.
        return BAD_URI;
      }
      i++;
    }

    if (versioner != null) {
      String version = uri.getQueryParameter(Param.VERSION.getKey());
      if (version != null) {
        status = versioner.validate(uris, container, version);
      }
    }

    return new ConcatUri(status, uris, splitParam, type, uri);
  }

}
