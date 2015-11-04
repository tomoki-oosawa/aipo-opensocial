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
package com.aipo.container.http;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 */
public class ContainerFilter implements Filter {

  private FilterConfig filterConfig;

  /**
   * @param filterConfig
   * @throws ServletException
   */
  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    this.filterConfig = filterConfig;
  }

  /**
   * @param request
   * @param response
   * @param chain
   * @throws IOException
   * @throws ServletException
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    ServletContext prevServletContext = ServletContextLocator.get();
    HttpServletRequest prevHttpServletRequest = HttpServletRequestLocator.get();
    HttpServletResponse prevHttpServletResponse =
      HttpServletResponseLocator.get();
    try {
      ServletContextLocator.set(filterConfig.getServletContext());
      HttpServletRequestLocator.set((HttpServletRequest) request);
      HttpServletResponseLocator.set((HttpServletResponse) response);
      chain.doFilter(request, response);
    } finally {
      ServletContextLocator.set(prevServletContext);
      HttpServletRequestLocator.set(prevHttpServletRequest);
      HttpServletResponseLocator.set(prevHttpServletResponse);
    }
  }

  /**
   *
   */
  @Override
  public void destroy() {
  }

}
