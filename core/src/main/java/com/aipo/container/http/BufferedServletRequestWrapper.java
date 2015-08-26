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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 *
 */
public class BufferedServletRequestWrapper extends HttpServletRequestWrapper {

  private final byte[] buffer;

  public BufferedServletRequestWrapper(HttpServletRequest request)
      throws IOException {
    super(request);

    InputStream is = request.getInputStream();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte buff[] = new byte[1024];
    int read;
    while ((read = is.read(buff)) > 0) {
      baos.write(buff, 0, read);
    }

    this.buffer = baos.toByteArray();
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    return new BufferedServletInputStream(this.buffer);
  }

  @Override
  public BufferedReader getReader() throws IOException {
    return new BufferedReader(new InputStreamReader(
      new BufferedServletInputStream(this.buffer)));
  }
}