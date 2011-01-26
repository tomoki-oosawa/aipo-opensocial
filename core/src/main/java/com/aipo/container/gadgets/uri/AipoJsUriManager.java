/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2011 Aimluck,Inc.
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

package com.aipo.container.gadgets.uri;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.common.uri.UriBuilder;
import org.apache.shindig.config.ContainerConfig;
import org.apache.shindig.gadgets.Gadget;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.GadgetException.Code;
import org.apache.shindig.gadgets.RenderingContext;
import org.apache.shindig.gadgets.http.HttpResponse;
import org.apache.shindig.gadgets.uri.DefaultJsUriManager;
import org.apache.shindig.gadgets.uri.JsUriManager;
import org.apache.shindig.gadgets.uri.UriCommon.Param;
import org.apache.shindig.gadgets.uri.UriStatus;

import com.aipo.container.util.ContainerToolkit;
import com.aipo.orm.service.ContainerConfigDbService;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * @see DefaultJsUriManager
 */
public class AipoJsUriManager implements JsUriManager {

  static final String JS_HOST_PARAM = "gadgets.uri.js.host";

  static final String JS_PATH_PARAM = "gadgets.uri.js.path";

  static final JsUri INVALID_URI = new JsUri(UriStatus.BAD_URI, Lists
    .<String> newArrayList());

  protected static final String JS_SUFFIX = ".js";

  protected static final String JS_DELIMITER = ":";

  private final ContainerConfig config;

  private final ContainerConfigDbService containerConfigDbService;

  private final Versioner versioner;

  @Inject
  public AipoJsUriManager(ContainerConfig config, Versioner versioner,
      ContainerConfigDbService containerConfigDbService) {
    this.config = config;
    this.versioner = versioner;
    this.containerConfigDbService = containerConfigDbService;
  }

  public Uri makeExternJsUri(Gadget gadget, Collection<String> extern) {
    String container = gadget.getContext().getContainer();
    // String jsHost = getReqConfig(container, JS_HOST_PARAM);
    String jsPathBase = getReqConfig(container, JS_PATH_PARAM);

    // We somewhat cheat in that jsHost may contain protocol/scheme as well.
    UriBuilder uri = new UriBuilder();
    uri.setAuthority(ContainerToolkit.getHost(containerConfigDbService));
    uri.setScheme(ContainerToolkit.getScheme());

    // Add JS info to path and set it in URI.
    StringBuilder jsPath = new StringBuilder(jsPathBase);
    if (!jsPathBase.endsWith("/")) {
      jsPath.append('/');
    }
    jsPath.append(addJsLibs(extern));
    jsPath.append(JS_SUFFIX);
    uri.setPath(jsPath.toString());

    // Standard container param, as JS may be container-specific.
    uri.addQueryParameter(Param.CONTAINER.getKey(), container);

    // Pass through nocache param for dev purposes.
    uri.addQueryParameter(Param.NO_CACHE.getKey(), gadget
      .getContext()
      .getIgnoreCache() ? "1" : "0");

    // Pass through debug param for debugging use.
    uri.addQueryParameter(Param.DEBUG.getKey(), gadget.getContext().getDebug()
      ? "1"
      : "0");

    uri.addQueryParameter(Param.CONTAINER_MODE.getKey(), gadget
      .getContext()
      .getRenderingContext() == RenderingContext.CONTAINER ? "1" : "0");

    // Pass through gadget Uri
    if (addGadgetUri()) {
      uri.addQueryParameter(Param.URL.getKey(), gadget
        .getSpec()
        .getUrl()
        .toString());
    }

    // Finally, version it, but only if !nocache.
    if (versioner != null && !gadget.getContext().getIgnoreCache()) {
      uri.addQueryParameter(Param.VERSION.getKey(), versioner.version(gadget
        .getContext()
        .getUrl(), container, extern));
    }

    return uri.toUri();
  }

  /**
   * Essentially pulls apart a Uri created by makeExternJsUri, validating its
   * contents, especially the version key.
   */
  public JsUri processExternJsUri(Uri uri) throws GadgetException {
    // Validate basic Uri structure and params
    String container = uri.getQueryParameter(Param.CONTAINER.getKey());
    if (container == null) {
      container = ContainerConfig.DEFAULT_CONTAINER;
    }

    // Get config values up front.
    getReqConfig(container, JS_HOST_PARAM); // validate that it exists
    String jsPrefix = getReqConfig(container, JS_PATH_PARAM);

    String host = uri.getAuthority();
    if (host == null) {
      issueUriFormatError("Unexpected: Js Uri has no host");
      return INVALID_URI;
    }

    // Pull out the collection of features referenced by the Uri.
    String path = uri.getPath();
    if (path == null) {
      issueUriFormatError("Unexpected: Js Uri has no path");
      return INVALID_URI;
    }
    if (!path.startsWith(jsPrefix)) {
      issueUriFormatError("Js Uri path invalid, expected prefix: "
        + jsPrefix
        + ", is: "
        + path);
      return INVALID_URI;
    }
    path = path.substring(jsPrefix.length());

    // Convenience suffix: pull off .js if present; leave alone otherwise.
    if (path.endsWith(JS_SUFFIX)) {
      path = path.substring(0, path.length() - JS_SUFFIX.length());
    }

    while (path.startsWith("/")) {
      path = path.substring(1);
    }

    Collection<String> libs = getJsLibs(path);
    UriStatus status = UriStatus.VALID_UNVERSIONED;
    String version = uri.getQueryParameter(Param.VERSION.getKey());
    if (version != null && versioner != null) {
      Uri gadgetUri = null;
      String gadgetParam = uri.getQueryParameter(Param.URL.getKey());
      if (gadgetParam != null) {
        gadgetUri = Uri.parse(gadgetParam);
      }
      status = versioner.validate(gadgetUri, container, libs, version);
    }

    return new JsUri(status, libs);
  }

  static String addJsLibs(Collection<String> extern) {
    return StringUtils.join(extern, JS_DELIMITER);
  }

  static Collection<String> getJsLibs(String path) {
    return Arrays.asList(StringUtils.split(path, JS_DELIMITER));
  }

  private String getReqConfig(String container, String key) {
    String ret = config.getString(container, key);
    if (ret == null) {
      ret = config.getString(ContainerConfig.DEFAULT_CONTAINER, key);
      if (ret == null) {
        throw new RuntimeException("Container '"
          + container
          + "' missing config for required param: "
          + key);
      }
    }
    return ret;
  }

  // May be overridden to report errors in an alternate way to the user.
  protected void issueUriFormatError(String err) throws GadgetException {
    throw new GadgetException(
      Code.INVALID_PARAMETER,
      err,
      HttpResponse.SC_BAD_REQUEST);
  }

  // Overridable in the event that a Versioner implementation is injected
  // that uses the gadget itself to perform intelligent optimization and
  // versioning.
  // This isn't the cleanest logic, so should be cleaned up when better concrete
  // examples of this behavior exist.
  protected boolean addGadgetUri() {
    return false;
  }
}
