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
package org.apache.shindig.auth;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.aipo.container.protocol.AipoErrorCode;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * @see AuthenticationServletFilter
 */
public class AipoAuthenticationServletFilter extends
    AuthenticationServletFilter {
  public static final String AUTH_TYPE_OAUTH = "OAuth";

  // At some point change this to a container specific realm
  private static final String realm = "shindig";

  private List<AuthenticationHandler> handlers;

  private static final Logger LOG = Logger
    .getLogger(AipoAuthenticationServletFilter.class.getName());

  @Override
  @Inject
  public void setAuthenticationHandlers(List<AuthenticationHandler> handlers) {
    this.handlers = handlers;
  }

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {

    if (!(request instanceof HttpServletRequest && response instanceof HttpServletResponse)) {
      throw new ServletException("Auth filter can only handle HTTP");
    }

    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse resp = (HttpServletResponse) response;

    try {
      for (AuthenticationHandler handler : handlers) {
        SecurityToken token = handler.getSecurityTokenFromRequest(req);
        if (token != null) {
          new AuthInfo(req).setAuthType(handler.getName()).setSecurityToken(
            token);
          callChain(chain, req, resp);
          return;
        } else {
          String authHeader = handler.getWWWAuthenticateHeader(realm);
          if (authHeader != null) {
            resp.addHeader("WWW-Authenticate", authHeader);
          }
        }
      }

      throw new AuthenticationHandler.InvalidAuthenticationException(
        "Authorization Required.",
        null);
    } catch (AuthenticationHandler.InvalidAuthenticationException iae) {
      Throwable cause = iae.getCause();
      LOG.log(Level.INFO, iae.getMessage(), cause);

      if (iae.getAdditionalHeaders() != null) {
        for (Map.Entry<String, String> entry : iae
          .getAdditionalHeaders()
          .entrySet()) {
          resp.addHeader(entry.getKey(), entry.getValue());
        }
      }
      if (iae.getRedirect() != null) {
        resp.sendRedirect(iae.getRedirect());
      } else {
        resp.setContentType("application/json; charset=utf8");
        String str = AipoErrorCode.TOKEN_EXPIRED.responseJSON().toString();
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        OutputStream out = null;
        InputStream in = null;
        try {
          out = resp.getOutputStream();
          in = new ByteArrayInputStream(str.getBytes("UTF-8"));
          int b;
          while ((b = in.read()) != -1) {
            out.write(b);
          }
          out.flush();
        } catch (Throwable t) {
          LOG.log(Level.WARNING, t.getMessage(), t);
        } finally {
          IOUtils.closeQuietly(out);
          IOUtils.closeQuietly(in);
        }
      }
    }
  }

  private void callChain(FilterChain chain, HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {
    if (request.getAttribute(AuthenticationHandler.STASHED_BODY) != null) {
      chain.doFilter(new StashedBodyRequestwrapper(request), response);
    } else {
      chain.doFilter(request, response);
    }
  }

  private static class StashedBodyRequestwrapper extends
      HttpServletRequestWrapper {

    final InputStream rawStream;

    ServletInputStream stream;

    BufferedReader reader;

    StashedBodyRequestwrapper(HttpServletRequest wrapped) {
      super(wrapped);
      rawStream =
        new ByteArrayInputStream((byte[]) wrapped
          .getAttribute(AuthenticationHandler.STASHED_BODY));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
      Preconditions.checkState(
        reader == null,
        "The methods getInputStream() and getReader() are mutually exclusive.");

      if (stream == null) {
        stream = new ServletInputStream() {
          @Override
          public int read() throws IOException {
            return rawStream.read();
          }
        };
      }
      return stream;
    }

    @Override
    public BufferedReader getReader() throws IOException {
      Preconditions.checkState(
        stream == null,
        "The methods getInputStream() and getReader() are mutually exclusive.");

      if (reader == null) {
        Charset charset = Charset.forName(getCharacterEncoding());
        if (charset == null) {
          charset = Charsets.UTF_8;
        }
        reader = new BufferedReader(new InputStreamReader(rawStream, charset));
      }
      return reader;
    }
  }
}
