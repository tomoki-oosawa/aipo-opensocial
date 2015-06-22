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

import java.util.concurrent.Future;

import org.apache.shindig.protocol.Operation;
import org.apache.shindig.protocol.Service;
import org.apache.shindig.social.opensocial.service.SocialRequestItem;

import com.google.inject.Inject;

/**
 * Message API :files
 *
 */
@Service(name = "messages", path = "/messageFiles/{userId}+/{groupId}/{fileId}+")
public class AipoMessageFileHandler {

  @Inject
  public AipoMessageFileHandler() {
  }

  /**
   * ファイル GET /messageFiles/@viewer/@self/1
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "GET")
  public Future<?> get(SocialRequestItem request) {
    /*-
    {
      files : [ { fileId: 1, fileName : "sample.jpg", fileSize : 11223 }, { fileId: 2, fileName : "sample.pdf", fileSize : 123123 } ]
    }
     */
    throw new UnsupportedOperationException();
  }
}
