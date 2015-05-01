/*** Eclipse Class Decompiler plugin, copyright (c) 2012 Chao Chen (cnfree2000@hotmail.com) ***/
package com.aipo.container.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.servlet.HttpUtil;
import org.apache.shindig.protocol.ApiServlet;
import org.apache.shindig.protocol.ContentTypes;
import org.apache.shindig.protocol.DataCollection;
import org.apache.shindig.protocol.ResponseItem;
import org.apache.shindig.protocol.RestHandler;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.protocol.conversion.BeanConverter;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class AipoDataServiceServlet extends ApiServlet {
  private static final Logger LOG = Logger
    .getLogger(AipoDataServiceServlet.class.getName());

  public static final Set<String> ALLOWED_CONTENT_TYPES =
    new ImmutableSet.Builder()
      .addAll(ContentTypes.ALLOWED_JSON_CONTENT_TYPES)
      .addAll(ContentTypes.ALLOWED_XML_CONTENT_TYPES)
      .addAll(ContentTypes.ALLOWED_ATOM_CONTENT_TYPES)
      .build();

  protected static final String X_HTTP_METHOD_OVERRIDE =
    "X-HTTP-Method-Override";

  public AipoDataServiceServlet() {
  }

  @Override
  protected void doGet(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws ServletException, IOException {
    executeRequest(servletRequest, servletResponse);
  }

  @Override
  protected void doPut(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws ServletException, IOException {
    try {
      ContentTypes.checkContentTypes(ALLOWED_CONTENT_TYPES, servletRequest
        .getContentType());
      executeRequest(servletRequest, servletResponse);
    } catch (ContentTypes.InvalidContentTypeException icte) {
      sendError(servletResponse, new ResponseItem(400, icte.getMessage()));
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
      ContentTypes.checkContentTypes(ALLOWED_CONTENT_TYPES, servletRequest
        .getContentType());
      executeRequest(servletRequest, servletResponse);
    } catch (ContentTypes.InvalidContentTypeException icte) {
      sendError(servletResponse, new ResponseItem(400, icte.getMessage()));
    }
  }

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
    HttpUtil.setCORSheader(servletResponse, this.containerConfig.getList(token
      .getContainer(), "gadgets.parentOrigins"));

    BeanConverter converter = getConverterForRequest(servletRequest);

    handleSingleRequest(servletRequest, servletResponse, token, converter);
  }

  @Override
  protected void sendError(HttpServletResponse servletResponse,
      ResponseItem responseItem) throws IOException {
    int errorCode = responseItem.getErrorCode();
    if (errorCode < 0) {
      switch (errorCode) {
        case -32700:
        case -32602:
        case -32600:
          errorCode = 400;
          break;
        case -32601:
          errorCode = 501;
          break;
        case -32603:
        default:
          errorCode = 500;
      }
    }
    servletResponse.sendError(responseItem.getErrorCode(), responseItem
      .getErrorMessage());
  }

  private void handleSingleRequest(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse, SecurityToken token,
      BeanConverter converter) throws IOException {
    RestHandler handler = getRestHandler(servletRequest);

    Reader bodyReader = null;
    if ((!servletRequest.getMethod().equals("GET"))
      && (!servletRequest.getMethod().equals("HEAD"))) {
      bodyReader = servletRequest.getReader();
    }
    Map<String, String[]> parameterMap = servletRequest.getParameterMap();
    Future<?> future =
      handler.execute(parameterMap, bodyReader, token, converter);

    ResponseItem responseItem = getResponseItem(future);

    // Byte列を返却する場合、contentTypeは異なる。
    // また、Byte列を返す処理はconverterではできない。
    servletResponse.setContentType(converter.getContentType());
    if ((responseItem.getErrorCode() >= 200)
      && (responseItem.getErrorCode() < 400)) {
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
      PrintWriter writer = servletResponse.getWriter();
      if ((!(response instanceof DataCollection))
        && (!(response instanceof RestfulCollection))) {
        response = ImmutableMap.of("entry", response);
      }
      String callback =
        (HttpUtil.isJSONP(servletRequest))
          && ("application/json".equals(converter.getContentType()))
          ? servletRequest.getParameter("callback")
          : null;
      if (callback != null) {
        writer.write(callback + '(');
      }
      writer.write(converter.convertToString(response));
      if (callback != null) {
        writer.write(");\n");
      }
    } else {
      sendError(servletResponse, responseItem);
    }
  }

  protected RestHandler getRestHandler(HttpServletRequest servletRequest) {
    String path = servletRequest.getPathInfo();

    String method = servletRequest.getParameter("X-HTTP-Method-Override");
    if (method == null) {
      method = servletRequest.getMethod();
    }
    return this.dispatcher.getRestHandler(path, method.toUpperCase());
  }

  public BeanConverter getConverterForRequest(HttpServletRequest servletRequest) {
    String formatString = null;
    BeanConverter converter = null;
    String contentType = null;
    try {
      formatString = servletRequest.getParameter("format");
    } catch (Throwable t) {
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("Unexpected error : format param is null " + t.toString());
      }
    }
    try {
      contentType = servletRequest.getContentType();
    } catch (Throwable t) {
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("Unexpected error : content type is null " + t.toString());
      }
    }
    if (contentType != null) {
      if (ContentTypes.ALLOWED_JSON_CONTENT_TYPES.contains(contentType)) {
        converter = this.jsonConverter;
      } else if (ContentTypes.ALLOWED_ATOM_CONTENT_TYPES.contains(contentType)) {
        converter = this.atomConverter;
      } else if (ContentTypes.ALLOWED_XML_CONTENT_TYPES.contains(contentType)) {
        converter = this.xmlConverter;
      } else if (formatString == null) {
        converter = this.jsonConverter;
      }
    } else if (formatString != null) {
      if (formatString.equals("atom")) {
        converter = this.atomConverter;
      } else if (formatString.equals("xml")) {
        converter = this.xmlConverter;
      } else {
        converter = this.jsonConverter;
      }
    } else {
      converter = this.jsonConverter;
    }
    return converter;
  }
}
