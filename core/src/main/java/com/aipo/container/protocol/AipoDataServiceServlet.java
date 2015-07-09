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
package com.aipo.container.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.Nullable;
import org.apache.shindig.common.servlet.HttpUtil;
import org.apache.shindig.protocol.ApiServlet;
import org.apache.shindig.protocol.ContentTypes;
import org.apache.shindig.protocol.ContentTypes.InvalidContentTypeException;
import org.apache.shindig.protocol.DataCollection;
import org.apache.shindig.protocol.DataServiceServlet;
import org.apache.shindig.protocol.ResponseItem;
import org.apache.shindig.protocol.RestHandler;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.protocol.conversion.BeanConverter;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * @see DataServiceServlet
 */
public class AipoDataServiceServlet extends ApiServlet {

  private static final long serialVersionUID = -2013432634263695142L;

  private static final Logger LOG = Logger
    .getLogger(AipoDataServiceServlet.class.getName());

  public static final Set<String> ALLOWED_CONTENT_TYPES =
    new ImmutableSet.Builder<String>().addAll(
      ContentTypes.ALLOWED_MULTIPART_CONTENT_TYPES).addAll(
      ContentTypes.ALLOWED_JSON_CONTENT_TYPES).addAll(
      ContentTypes.ALLOWED_XML_CONTENT_TYPES).addAll(
      ContentTypes.ALLOWED_ATOM_CONTENT_TYPES).addAll(
      ImmutableSet.of("application/x-www-form-urlencoded")).build();

  protected static final String X_HTTP_METHOD_OVERRIDE =
    "X-HTTP-Method-Override";

  @Override
  protected void doGet(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws ServletException, IOException {
    executeRequest(servletRequest, servletResponse);
  }

  @Override
  protected void doPut(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws ServletException, IOException {
    try {
      checkContentTypes(ALLOWED_CONTENT_TYPES, servletRequest.getContentType());
      executeRequest(servletRequest, servletResponse);
    } catch (ContentTypes.InvalidContentTypeException icte) {
      sendError(servletResponse, new ResponseItem(
        HttpServletResponse.SC_BAD_REQUEST,
        icte.getMessage()));
    }
  }

  @Override
  protected void doDelete(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws ServletException, IOException {
    executeRequest(servletRequest, servletResponse);
  }

  @Override
  protected void doPost(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws ServletException, IOException {
    try {
      checkContentTypes(ALLOWED_CONTENT_TYPES, servletRequest.getContentType());
      executeRequest(servletRequest, servletResponse);
    } catch (ContentTypes.InvalidContentTypeException icte) {
      sendError(servletResponse, new ResponseItem(
        HttpServletResponse.SC_BAD_REQUEST,
        icte.getMessage()));
    }
  }

  /**
   * Actual dispatch handling for servlet requests
   */
  void executeRequest(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws IOException {
    if (LOG.isLoggable(Level.FINEST)) {
      LOG
        .finest("Handling restful request for " + servletRequest.getPathInfo());
    }

    setCharacterEncodings(servletRequest, servletResponse);

    SecurityToken token = getSecurityToken(servletRequest);
    if (token == null) {
      sendSecurityError(servletResponse);
      return;
    }

    HttpUtil.setCORSheader(servletResponse, containerConfig.<String> getList(
      token.getContainer(),
      "gadgets.parentOrigins"));

    handleSingleRequest(servletRequest, servletResponse, token);
  }

  @Override
  protected void sendError(HttpServletResponse servletResponse,
      ResponseItem responseItem) throws IOException {
    String errorMessage = responseItem.getErrorMessage();
    int errorCode = responseItem.getErrorCode();
    if (errorCode < 0) {
      // Map JSON-RPC error codes into HTTP error codes as best we can
      // TODO: Augment the error message (if missing) with a default
      switch (errorCode) {
        case -32700:
        case -32602:
        case -32600:
          // Parse error, invalid params, and invalid request
          errorCode = HttpServletResponse.SC_BAD_REQUEST;
          break;
        case -32601:
          // Procedure doesn't exist
          errorCode = HttpServletResponse.SC_NOT_IMPLEMENTED;
          break;
        case -32603:
        default:
          // Internal server error, or any application-defined error
          errorCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
          break;
      }
    }

    servletResponse.sendError(errorCode, errorMessage);
  }

  /**
   * Handler for non-batch requests.
   */
  private void handleSingleRequest(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse, SecurityToken token)
      throws IOException {

    // Always returns a non-null handler.
    RestHandler handler = getRestHandler(servletRequest);

    // Get Content-Type
    String contentType = null;

    try {
      // TODO: First implementation causes bug when Content-Type is
      // application/atom+xml. Fix is applied.
      contentType =
        ContentTypes.extractMimePart(servletRequest.getContentType());
    } catch (Throwable t) {
      // this happens while testing
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("Unexpected error : content type is null " + t.toString());
      }
    }

    // Get BeanConverter for Request payload.
    BeanConverter requestConverter = getConverterForRequest(contentType, null);

    // Get BeanConverter for Response body.
    BeanConverter responseConverter = getConverterForFormat(null);

    Reader bodyReader = null;
    if (!servletRequest.getMethod().equals("GET")
      && !servletRequest.getMethod().equals("HEAD")) {
      bodyReader = servletRequest.getReader();
    }

    // Execute the request
    Map<String, String[]> parameterMap = loadParameters(servletRequest);
    Future<?> future =
      handler.execute(parameterMap, bodyReader, token, requestConverter);
    ResponseItem responseItem = getResponseItem(future);

    servletResponse.setContentType(responseConverter.getContentType());
    if (responseItem.getErrorCode() >= 200 && responseItem.getErrorCode() < 400) {
      Object response = responseItem.getResponse();

      if (response instanceof StreamContent) {
        InputStream is = null;
        OutputStream out = null;
        try {
          StreamContent content = (StreamContent) response;
          servletResponse.setContentType(content.getContentType());
          is = content.getInputStream();
          out = servletResponse.getOutputStream();
          int b;
          while ((b = is.read()) != -1) {
            out.write(b);
          }
        } finally {
          try {
            if (out != null) {
              out.close();
            }
            if (is != null) {
              is.close();
            }
          } catch (IOException ignore) {
            // ignore
          }
        }
        out.close();
        is.close();
        return;
      }

      // responseの中身が空だった場合404を返す
      if (response == Collections.EMPTY_MAP) {
        ResponseItem notFoundResponse =
          new ResponseItem(404, "Not Found", null);
        sendError(servletResponse, notFoundResponse);
      }

      // TODO: ugliness resulting from not using RestfulItem
      if (!(response instanceof DataCollection)
        && !(response instanceof RestfulCollection)) {
        response = ImmutableMap.of("entry", response);
      }

      // JSONP style callbacks
      String callback =
        (HttpUtil.isJSONP(servletRequest) && ContentTypes.OUTPUT_JSON_CONTENT_TYPE
          .equals(responseConverter.getContentType())) ? servletRequest
          .getParameter("callback") : null;

      PrintWriter writer = servletResponse.getWriter();
      if (callback != null) {
        writer.write(callback + '(');
      }
      writer.write(responseConverter.convertToString(response));
      if (callback != null) {
        writer.write(");\n");
      }
    } else {
      sendError(servletResponse, responseItem);
    }
  }

  protected RestHandler getRestHandler(HttpServletRequest servletRequest) {
    // TODO Rework to allow sub-services
    String path = servletRequest.getPathInfo();

    // TODO - This shouldnt be on BaseRequestItem
    String method = servletRequest.getParameter(X_HTTP_METHOD_OVERRIDE);
    if (method == null) {
      method = servletRequest.getMethod();
    }

    // Always returns a non-null handler.
    return dispatcher.getRestHandler(path, method.toUpperCase());
  }

  /*
   * Return the right BeanConverter to convert the request payload.
   */
  public BeanConverter getConverterForRequest(@Nullable String contentType,
      String format) {
    if (StringUtils.isNotBlank(contentType)) {
      return getConverterForContentType(contentType);
    } else {
      return getConverterForFormat(format);
    }
  }

  /**
   * Return BeanConverter based on content type.
   *
   * @param contentType
   *          the content type for the converter.
   * @return BeanConverter based on the contentType input param. Will default to
   *         JSON
   */
  protected BeanConverter getConverterForContentType(String contentType) {
    return ContentTypes.ALLOWED_ATOM_CONTENT_TYPES.contains(contentType)
      ? atomConverter
      : ContentTypes.ALLOWED_XML_CONTENT_TYPES.contains(contentType)
        ? xmlConverter
        : jsonConverter;
  }

  /**
   * Return BeanConverter based on format request parameter.
   *
   * @param format
   *          the format for the converter.
   * @return BeanConverter based on the format input param. Will default to JSON
   */
  protected BeanConverter getConverterForFormat(String format) {
    return ATOM_FORMAT.equals(format) ? atomConverter : XML_FORMAT
      .equals(format) ? xmlConverter : jsonConverter;
  }

  protected void checkContentTypes(Set<String> allowedContentTypes,
      String contentType) throws ContentTypes.InvalidContentTypeException {
    if (StringUtils.isEmpty(contentType)) {
      throw new InvalidContentTypeException(
        "No Content-Type specified. One of "
          + StringUtils.join(allowedContentTypes, ", ")
          + " is required");
    }
    contentType = ContentTypes.extractMimePart(contentType);
    if (allowedContentTypes.contains(contentType)) {
      return;
    }
    throw new InvalidContentTypeException("Unsupported Content-Type "
      + contentType
      + ". One of "
      + StringUtils.join(allowedContentTypes, ", ")
      + " is required");
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private Map<String, String[]> loadParameters(HttpServletRequest servletRequest) {
    Map<String, String[]> parameterMap = new HashMap<String, String[]>();

    // requestParameterを取り出す処理
    Map<String, String[]> map = servletRequest.getParameterMap();
    for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
      Map.Entry entry = (Map.Entry) it.next();
      String key = (String) entry.getKey();
      String[] _value = (String[]) entry.getValue();
      String[] value = new String[_value.length];
      for (int i = 0; i < _value.length; i++) {
        value[i] = _value[i];
      }
      parameterMap.put(key, value);
    }

    if (servletRequest.getContentType() != null) {
      // Content-typeがmultipart/form-dataの場合にパラメータを取り出す処理
      if (ContentTypes.MULTIPART_FORM_CONTENT_TYPE.equals(ContentTypes
        .extractMimePart(servletRequest.getContentType()))) {
        ServletFileUpload upload =
          new ServletFileUpload(new DiskFileItemFactory());
        List<FileItem> items = null;
        try {
          items = upload.parseRequest(servletRequest);
        } catch (FileUploadException e) {
        }
        for (FileItem val : items) {
          FileItem item = val;
          if (item.isFormField()) {
            String value = new String(item.get());
            String key = item.getFieldName();

            String[] valueArray = { value };
            if (parameterMap.containsKey(key)) {
              String[] preValue = parameterMap.get(key);
              valueArray = Arrays.copyOf(preValue, preValue.length + 1);
              valueArray[preValue.length] = value;
            }
            parameterMap.put(key, valueArray);
          } else {
            // file found!

          }
        }

      }

      // Content-typeがapplication/x-www-form-urlencodedの場合にパラメータを取り出す処理
      if ("application/x-www-form-urlencoded".equals(ContentTypes
        .extractMimePart(servletRequest.getContentType()))) {
        try {
          List<NameValuePair> params =
            URLEncodedUtils.parse(new URI("http://localhost:8080?"
              + getBodyAsString(servletRequest)), "UTF-8");
          for (NameValuePair param : params) {
            String[] valueArray = { param.getValue() };
            if (parameterMap.containsKey(param.getName())) {
              String[] preValue = parameterMap.get(param.getName());
              valueArray = Arrays.copyOf(preValue, preValue.length + 1);
              valueArray[preValue.length] = param.getValue();
            }
            parameterMap.put(param.getName(), valueArray);
          }
        } catch (URISyntaxException e) {
          // ignore
        }
      }
    }

    return parameterMap;
  }

  private String getBodyAsString(HttpServletRequest request) {
    if (request.getContentLength() == 0) {
      return "";
    }
    InputStream is = null;
    try {
      String line;
      StringBuilder sb = new StringBuilder();
      is = request.getInputStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
      is.close();
      return sb.toString();
    } catch (IOException ioe) {
      // ignore
      return null;
    } finally {
      IOUtils.closeQuietly(is);
    }
  }
}
