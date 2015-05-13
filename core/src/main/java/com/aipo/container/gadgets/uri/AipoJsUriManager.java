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
package com.aipo.container.gadgets.uri;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.shindig.common.servlet.Authority;
import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.common.uri.UriBuilder;
import org.apache.shindig.common.util.Utf8UrlCoder;
import org.apache.shindig.config.ContainerConfig;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.GadgetException.Code;
import org.apache.shindig.gadgets.JsCompileMode;
import org.apache.shindig.gadgets.http.HttpResponse;
import org.apache.shindig.gadgets.uri.DefaultJsUriManager;
import org.apache.shindig.gadgets.uri.JsUriManager;
import org.apache.shindig.gadgets.uri.UriCommon.Param;
import org.apache.shindig.gadgets.uri.UriStatus;

import com.aipo.container.util.ContainerToolkit;
import com.aipo.orm.service.ContainerConfigDbService;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

/**
 * @see DefaultJsUriManager
 */
public class AipoJsUriManager implements JsUriManager {

  static final String JS_HOST_PARAM = "gadgets.uri.js.host";

  static final String JS_PATH_PARAM = "gadgets.uri.js.path";

  static final JsUri INVALID_URI = new JsUri(UriStatus.BAD_URI);

  protected static final String JS_SUFFIX = ".js";

  protected static final String JS_DELIMITER = ":";

  private static final Logger LOG = Logger.getLogger(DefaultJsUriManager.class
    .getName());

  private final ContainerConfig config;

  private final ContainerConfigDbService containerConfigDbService;

  private final Versioner versioner;

  private Authority authority;

  @Inject
  public AipoJsUriManager(ContainerConfig config, Versioner versioner,
      ContainerConfigDbService containerConfigDbService) {
    this.config = config;
    this.versioner = versioner;
    this.containerConfigDbService = containerConfigDbService;
  }

  @Inject(optional = true)
  public void setAuthority(Authority authority) {
    this.authority = authority;
  }

  @Override
  public Uri makeExternJsUri(JsUri ctx) {
    String container = ctx.getContainer();
    String jsHost = ContainerToolkit.getHost(containerConfigDbService);
    String jsPathBase = getReqConfig(container, JS_PATH_PARAM);

    // We somewhat cheat in that jsHost may contain protocol/scheme as well.
    UriBuilder uri = new UriBuilder(Uri.parse(jsHost));

    uri.setScheme(ContainerToolkit.getScheme());

    // Add JS info to path and set it in URI.
    StringBuilder jsPath = new StringBuilder(jsPathBase);
    if (!jsPathBase.endsWith("/")) {
      jsPath.append('/');
    }
    jsPath.append(addJsLibs(ctx.getLibs()));

    // Add the list of already-loaded libs
    if (!ctx.getLoadedLibs().isEmpty()) {
      jsPath.append('!').append(addJsLibs(ctx.getLoadedLibs()));
    }

    jsPath.append(JS_SUFFIX);
    uri.setPath(jsPath.toString());

    // Standard container param, as JS may be container-specific.
    uri.addQueryParameter(Param.CONTAINER.getKey(), container);

    // Pass through nocache param for dev purposes.
    uri.addQueryParameter(Param.NO_CACHE.getKey(), ctx.isNoCache() ? "1" : "0");

    // Pass through debug param for debugging use.
    uri.addQueryParameter(Param.DEBUG.getKey(), ctx.isDebug() ? "1" : "0");

    uri.addQueryParameter(Param.CONTAINER_MODE.getKey(), ctx
      .getContext()
      .getParamValue());

    // Pass through gadget Uri
    if (addGadgetUri()) {
      uri.addQueryParameter(Param.URL.getKey(), ctx.getGadget());
    }

    if (ctx.getOnload() != null) {
      uri.addQueryParameter(Param.ONLOAD.getKey(), ctx.getOnload());
    }

    if (ctx.isJsload()) {
      uri.addQueryParameter(Param.JSLOAD.getKey(), "1");
    }

    if (ctx.isNohint()) {
      uri.addQueryParameter(Param.NO_HINT.getKey(), "1");
    }

    JsCompileMode mode = ctx.getCompileMode();
    if (mode != null && mode != JsCompileMode.getDefault()) {
      uri.addQueryParameter(Param.COMPILE_MODE.getKey(), mode.getParamValue());
    }

    if (ctx.cajoleContent()) {
      uri.addQueryParameter(Param.CAJOLE.getKey(), "1");
    }

    if (ctx.getRepository() != null) {
      uri.addQueryParameter(Param.REPOSITORY_ID.getKey(), ctx.getRepository());
    }

    // Finally, version it, but only if !nocache.
    if (versioner != null && !ctx.isNoCache()) {
      String version = versioner.version(ctx);
      if (version != null && version.length() > 0) {
        uri.addQueryParameter(Param.VERSION.getKey(), version);
      }
    }
    if (ctx.getExtensionParams() != null) {
      uri.addQueryParameters(ctx.getExtensionParams());
    }

    return uri.toUri();
  }

  /**
   * Essentially pulls apart a Uri created by makeExternJsUri, validating its
   * contents, especially the version key.
   */
  @Override
  public JsUri processExternJsUri(Uri uri) throws GadgetException {
    // Validate basic Uri structure and params
    String container = uri.getQueryParameter(Param.CONTAINER.getKey());
    if (container == null) {
      container = ContainerConfig.DEFAULT_CONTAINER;
    }

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
    // Decode the path here because it is not automatically decoded when the Uri
    // object is created.
    path = Utf8UrlCoder.decode(path);

    int lastSlash = path.lastIndexOf('/');
    if (lastSlash != -1) {
      path = path.substring(lastSlash + 1);
    }

    // Convenience suffix: pull off .js if present; leave alone otherwise.
    if (path.endsWith(JS_SUFFIX)) {
      path = path.substring(0, path.length() - JS_SUFFIX.length());
    }

    while (path.startsWith("/")) {
      path = path.substring(1);
    }

    String[] splits = StringUtils.split(path, '!');
    Collection<String> libs = getJsLibs(splits.length >= 1 ? splits[0] : "");

    String haveString = (splits.length >= 2 ? splits[1] : "");
    String haveQueryParam = uri.getQueryParameter(Param.LOADED.getKey());
    if (haveQueryParam == null) {
      haveQueryParam = "";
    } else {
      LOG.log(
        Level.WARNING,
        "Using deprecated query param ?loaded=c:d in URL. "
          + "Replace by specifying it in path as /gadgets/js/a:b!c:d.js");
    }
    haveString = haveString + JS_DELIMITER + haveQueryParam;
    Collection<String> have = getJsLibs(haveString);

    UriStatus status = UriStatus.VALID_UNVERSIONED;
    String version = uri.getQueryParameter(Param.VERSION.getKey());
    JsUri jsUri = new JsUri(status, uri, libs, have);
    if (version != null && versioner != null) {
      status = versioner.validate(jsUri, version);
      if (status != UriStatus.VALID_UNVERSIONED) {
        jsUri = new JsUri(status, jsUri);
      }
    }

    return jsUri;
  }

  static String addJsLibs(Collection<String> extern) {
    return Joiner.on(JS_DELIMITER).join(extern);
  }

  static Collection<String> getJsLibs(String path) {
    return ImmutableList.copyOf(Splitter
      .on(JS_DELIMITER)
      .trimResults()
      .omitEmptyStrings()
      .split(path));
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
    if (authority != null) {
      ret = ret.replace("%authority%", authority.getAuthority());
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
