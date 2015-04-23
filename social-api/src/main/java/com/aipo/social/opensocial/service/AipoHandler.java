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
package com.aipo.social.opensocial.service;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.apache.shindig.protocol.Operation;
import org.apache.shindig.protocol.RequestItem;
import org.apache.shindig.protocol.Service;

import com.aipo.container.protocol.StreamContent;
import com.google.inject.Inject;
import com.google.inject.name.Named;

@Service(name = "aipo")
public class AipoHandler {

  private final String version;

  @Inject
  public AipoHandler(@Named("aipo.version") String version) {
    this.version = version;
  }

  @Operation(httpMethods = "GET", path = "/version")
  public String version(RequestItem request) {
    return version;
  }

  @Operation(httpMethods = "GET", path = "/mytest")
  public StreamContent mytest(RequestItem request) {
    return new StreamContent("text/plain", new ByteArrayInputStream(
      "this is test".getBytes()));
  }

  @Operation(httpMethods = "GET", path = "/mypicture.png")
  public StreamContent mytest2(RequestItem request) {
    try {
      StreamContent content = new StreamContent();
      content.setInputStream(new FileInputStream(
        "/Users/develop35/aipo_logo_l.png"));
      content.setContentType("image/png");
      return content;
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    }
  }
}
