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

import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.shindig.protocol.HandlerPreconditions;
import org.apache.shindig.protocol.Operation;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.Service;
import org.apache.shindig.social.opensocial.service.SocialRequestItem;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.aipo.container.protocol.StreamContent;
import com.aipo.social.opensocial.model.ALFile;
import com.aipo.social.opensocial.spi.MessageService;
import com.aipo.social.opensocial.spi.StorageService;
import com.google.inject.Inject;

/**
 * Message API :files
 *
 */
@Service(name = "messageFiles", path = "/{userId}+/{groupId}/{fileId}+")
public class AipoMessageFileHandler {

  private final StorageService storageService;

  private final MessageService messageService;

  @Inject
  public AipoMessageFileHandler(StorageService storageService,
      MessageService messageService) {
    this.storageService = storageService;
    this.messageService = messageService;
  }

  /**
   * ファイル GET /messageFiles/@viewer/@self/1
   *
   * @param request
   * @return
   * @throws ProtocolException
   */
  @Operation(httpMethods = "GET")
  public StreamContent get(SocialRequestItem request) throws ProtocolException {
    String fileId = request.getParameter("fileId");
    Set<UserId> userIds = request.getUsers();

    // Preconditions
    HandlerPreconditions.requireNotEmpty(userIds, "No userId specified");
    HandlerPreconditions.requireSingular(
      userIds,
      "Only one userId must be specified");

    CollectionOptions options = new CollectionOptions(request);

    Future<ALFile> file =
      messageService.getMessageFiles(
        userIds.iterator().next(),
        options,
        request.getFields(),
        fileId,
        request.getToken());
    if (file == null) {
      throw new ProtocolException(
        501,
        null,
        new UnsupportedOperationException());
    }

    try {
      InputStream stream =
        storageService.getFile(file.get(), request.getToken());
      if (stream == null) {
        throw new ProtocolException(
          501,
          null,
          new UnsupportedOperationException());
      }
      return new StreamContent("image/jpeg", stream);
    } catch (Exception e) {
      // ignore
    }

    throw new ProtocolException(501, null, new UnsupportedOperationException());
  }
}
