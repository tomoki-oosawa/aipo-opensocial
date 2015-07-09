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

package com.aipo.container.http;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 *
 */
public class BufferedServletRequestWrapper extends HttpServletRequestWrapper {

  private final HttpServletRequest request;

  private byte[] reqBytes;

  private boolean firstTime = true;

  public BufferedServletRequestWrapper(HttpServletRequest request) {
    super(request);
    this.request = request;
  }

  @Override
  public BufferedReader getReader() throws IOException {

    if (firstTime) {
      firstTime();
    }

    InputStreamReader isr =
      new InputStreamReader(new ByteArrayInputStream(reqBytes));
    return new BufferedReader(isr);
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {

    if (firstTime) {
      firstTime();
    }

    ServletInputStream sis = new ServletInputStream() {

      ByteArrayInputStream inputStream = new ByteArrayInputStream(reqBytes);

      private int i;

      @Override
      public int read() throws IOException {
        return inputStream.read();
      }

      @Override
      public int read(byte[] b, int off, int len) throws IOException {
        return inputStream.read(b, off, len);
      }

      @Override
      public int available() throws IOException {
        return inputStream.available();
      }

    };

    return sis;
  }

  private void firstTime() throws IOException {
    firstTime = false;

    ServletInputStream is = request.getInputStream();
    int length = request.getContentLength();
    reqBytes = new byte[length];

    is.read(reqBytes, 0, length);

  }
}