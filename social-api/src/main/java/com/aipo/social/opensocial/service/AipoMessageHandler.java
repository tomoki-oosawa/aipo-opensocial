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

import org.apache.shindig.protocol.Operation;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.Service;
import org.apache.shindig.social.opensocial.service.SocialRequestItem;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.aipo.container.protocol.AipoErrorCode;
import com.aipo.container.protocol.AipoPreconditions;
import com.aipo.container.protocol.AipoProtocolException;
import com.aipo.container.protocol.AipoScope;
import com.aipo.container.protocol.StreamContent;
import com.aipo.social.opensocial.model.ALFile;
import com.aipo.social.opensocial.model.ALMessage;
import com.aipo.social.opensocial.spi.AipoCollectionOptions;
import com.aipo.social.opensocial.spi.MessageService;
import com.aipo.social.opensocial.spi.StorageService;
import com.google.inject.Inject;

/**
 * RPC/REST handler for Messages API
 */
@Service(name = "messages")
public class AipoMessageHandler {

  private final StorageService storageService;

  private final MessageService messageService;

  @Inject
  public AipoMessageHandler(StorageService storageService,
      MessageService messageService) {
    this.storageService = storageService;
    this.messageService = messageService;
  }

  /**
   * メッセージ一覧 <br>
   * <code>
   * GET /messages/:roomId
   * </code><br>
   * <code>
   * osapi.messages.get( { roomId: :roomId })
   * </code>
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "GET", path = "/{roomId}")
  public Future<?> get(SocialRequestItem request) {
    return getPosts(request);
  }

  /**
   * メッセージ詳細 <br>
   * <code>
   * GET /messages/:roomId/posts/:messageId
   * </code><br>
   * <code>
   * osapi.messages.get( { roomId: :roomId, messageId: messageId })
   * osapi.messages.posts.get( { roomId: :roomId, messageId: messageId })
   * </code>
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "GET", name = "posts.get", path = "/{roomId}/posts/{messageId}")
  public Future<?> getPosts(SocialRequestItem request) {
    try {
      Set<UserId> userIds = request.getUsers();
      String roomId = request.getParameter("roomId");
      String messageId = request.getParameter("messageId");

      // Preconditions
      AipoPreconditions.validateScope(request.getToken(), AipoScope.R_ALL);
      AipoPreconditions.required("userId", userIds);
      AipoPreconditions.notMultiple("userId", userIds);
      AipoPreconditions.required("roomId", roomId);
      int messageIdInt =
        AipoPreconditions.isIntegerOrNull("messageId", messageId);

      return messageService.getMessages(
        userIds.iterator().next(),
        new AipoCollectionOptions(request),
        request.getFields(),
        roomId,
        messageIdInt,
        request.getToken());
    } catch (ProtocolException e) {
      throw e;
    } catch (Throwable t) {
      throw new AipoProtocolException(AipoErrorCode.INTERNAL_ERROR);
    }
  }

  /**
   * メッセージ作成<br>
   * <code>
   * POST /messages/:roomId
   * </code><br>
   * <code>
   * osapi.messages.create( { roomId: :roomId })
   * </code>
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "POST", path = "/{roomId}")
  public Future<ALMessage> create(SocialRequestItem request) {
    try {
      Set<UserId> userIds = request.getUsers();
      String roomId = request.getParameter("roomId");
      String message = request.getParameter("message");
      String transactionId = request.getParameter("transactionId");

      // Preconditions
      AipoPreconditions.validateScope(request.getToken(), AipoScope.W_ALL);
      AipoPreconditions.required("userId", userIds);
      AipoPreconditions.notMultiple("userId", userIds);
      AipoPreconditions.required("roomId", roomId);
      AipoPreconditions.required("message", message);
      AipoPreconditions.maxLength("message", message, 10000);
      AipoPreconditions.isUTF8("message", message);

      return messageService.postMessage(
        userIds.iterator().next(),
        roomId,
        message,
        transactionId,
        request.getToken());
    } catch (ProtocolException e) {
      throw e;
    } catch (Throwable t) {
      throw new AipoProtocolException(AipoErrorCode.INTERNAL_ERROR);
    }
  }

  /**
   * メッセージ更新 <br>
   * <code>
   * PUT /messages/:roomId/posts/:messageId
   * </code><br>
   * <code>
   * osapi.messages.put( { roomId: :roomId, messageId: messageId })
   * </code>
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "PUT", path = "/{roomId}/posts/{messageId}")
  public Future<?> update(SocialRequestItem request) {
    try {
      Set<UserId> userIds = request.getUsers();
      String roomId = request.getParameter("roomId");
      String messageId = request.getParameter("messageId");

      // Preconditions
      AipoPreconditions.validateScope(request.getToken(), AipoScope.W_ALL);
      AipoPreconditions.required("userId", userIds);
      AipoPreconditions.notMultiple("userId", userIds);
      AipoPreconditions.required("roomId", roomId);
      AipoPreconditions.required("messageId", messageId);
      int messageIdInt = AipoPreconditions.isInteger("messageId", messageId);

      throw new AipoProtocolException(AipoErrorCode.UNSUPPORTED_OPERATION);
    } catch (ProtocolException e) {
      throw e;
    } catch (Throwable t) {
      throw new AipoProtocolException(AipoErrorCode.INTERNAL_ERROR);
    }
  }

  /**
   * メッセージ削除 <br>
   * <code>
   * DELETE /messages/:roomId/posts/:messageId
   * </code><br>
   * <code>
   * osapi.messages['delete']( { roomId: :roomId, messageId: messageId })
   * </code>
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "DELETE", path = "/{roomId}/posts/{messageId}")
  public Future<?> delete(SocialRequestItem request) {
    try {
      Set<UserId> userIds = request.getUsers();
      String roomId = request.getParameter("roomId");
      String messageId = request.getParameter("messageId");

      // Preconditions
      AipoPreconditions.validateScope(request.getToken(), AipoScope.W_ALL);
      AipoPreconditions.required("userId", userIds);
      AipoPreconditions.notMultiple("userId", userIds);
      AipoPreconditions.required("roomId", roomId);
      AipoPreconditions.required("messageId", messageId);
      int messageIdInt = AipoPreconditions.isInteger("messageId", messageId);

      return messageService.deleteMessage(
        userIds.iterator().next(),
        roomId,
        messageIdInt,
        request.getToken());
    } catch (ProtocolException e) {
      throw e;
    } catch (Throwable t) {
      throw new AipoProtocolException(AipoErrorCode.INTERNAL_ERROR);
    }
  }

  /**
   * 既読 <br>
   * <code>
   * PUT /messages/:roomId/read/:messageId
   * </code><br>
   * <code>
   * osapi.messages.read.update( { roomId: :roomId })
   * </code>
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = { "PUT", "POST" }, name = "read.update", path = "/{roomId}/read/{messageId}")
  public Future<?> read(SocialRequestItem request) {
    try {
      Set<UserId> userIds = request.getUsers();
      String roomId = request.getParameter("roomId");
      String messageId = request.getParameter("messageId");

      // Preconditions
      AipoPreconditions.validateScope(request.getToken(), AipoScope.W_ALL);
      AipoPreconditions.required("userId", userIds);
      AipoPreconditions.notMultiple("userId", userIds);
      AipoPreconditions.required("roomId", roomId);
      AipoPreconditions.required("messageId", messageId);
      int messageIdInt = AipoPreconditions.isInteger("messageId", messageId);

      return messageService.read(
        userIds.iterator().next(),
        roomId,
        messageIdInt,
        request.getToken());
    } catch (ProtocolException e) {
      throw e;
    } catch (Throwable t) {
      throw new AipoProtocolException(AipoErrorCode.INTERNAL_ERROR);
    }
  }

  /**
   * ファイル情報 <br>
   * <code>
   * GET /messages/:roomId/files/info/:fileId
   * </code><br>
   * <code>
   * osapi.messages.files.info( { roomId: :roomId, fileId: fileId })
   * </code>
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "GET", name = "files.info", path = "/{roomId}/files/info/{fileId}")
  public Future<?> getFilesInfo(SocialRequestItem request) {
    try {
      Set<UserId> userIds = request.getUsers();
      String roomId = request.getParameter("roomId");
      String fileId = request.getParameter("fileId");

      // Preconditions
      AipoPreconditions.validateScope(request.getToken(), AipoScope.R_ALL);
      AipoPreconditions.required("userId", userIds);
      AipoPreconditions.notMultiple("userId", userIds);
      AipoPreconditions.required("roomId", roomId);
      AipoPreconditions.required("fileId", fileId);
      int fileIdInt = AipoPreconditions.isInteger("fileId", fileId);

      return messageService.getMessageFilesInfo(
        userIds.iterator().next(),
        fileIdInt,
        request.getToken());
    } catch (ProtocolException e) {
      throw e;
    } catch (Throwable t) {
      throw new AipoProtocolException(AipoErrorCode.INTERNAL_ERROR);
    }
  }

  /**
   * ファイルダウンロード <br>
   * <code>
   * GET /messages/:roomId/files/download/:fileId
   * </code><br>
   * <code>
   * osapi.messages.files.download( { roomId: :roomId, fileId: fileId })
   * </code>
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "GET", name = "files.download", path = "/{roomId}/files/download/{fileId}")
  public StreamContent getFiles(SocialRequestItem request) {
    try {
      Set<UserId> userIds = request.getUsers();
      String roomId = request.getParameter("roomId");
      String fileId = request.getParameter("fileId");

      // Preconditions
      AipoPreconditions.validateScope(request.getToken(), AipoScope.R_ALL);
      AipoPreconditions.required("userId", userIds);
      AipoPreconditions.notMultiple("userId", userIds);
      AipoPreconditions.required("roomId", roomId);
      AipoPreconditions.required("fileId", fileId);
      int fileIdInt = AipoPreconditions.isInteger("fileId", fileId);

      Future<ALFile> file =
        messageService.getMessageFiles(
          userIds.iterator().next(),
          fileIdInt,
          request.getToken());
      if (file == null) {
        throw new AipoProtocolException(AipoErrorCode.FILE_NOT_FOUND);
      }

      InputStream stream =
        storageService.getFile(file.get(), request.getToken());
      if (stream == null) {
        throw new AipoProtocolException(AipoErrorCode.FILE_NOT_FOUND);
      }
      String contentType =
        storageService.getContentType(file.get(), request.getToken());
      if (contentType == null) {
        throw new AipoProtocolException(AipoErrorCode.FILE_NOT_FOUND);
      }
      return new StreamContent(contentType, stream);
    } catch (ProtocolException e) {
      throw e;
    } catch (Throwable t) {
      throw new AipoProtocolException(AipoErrorCode.INTERNAL_ERROR);
    }
  }

  /**
   * 添付ファイル サムネイル画像<br>
   * <code>
   * GET  /messages/:roomId/files/thumbnail/:fileId
   * </code><br>
   * <code>
   * osapi.rooms.thumbnail.get( { roomId: :roomId, fileId: fileId })
   * </code>
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "GET", name = "thumbnail.get", path = "/{roomId}/files/thumbnail/{fileId}")
  public StreamContent getThumbnail(SocialRequestItem request)
      throws ProtocolException {
    try {
      Set<UserId> userIds = request.getUsers();
      String roomId = request.getParameter("roomId");
      String fileId = request.getParameter("fileId");

      // Preconditions
      AipoPreconditions.validateScope(request.getToken(), AipoScope.R_ALL);
      AipoPreconditions.required("userId", userIds);
      AipoPreconditions.notMultiple("userId", userIds);
      AipoPreconditions.required("roomId", roomId);
      AipoPreconditions.required("fileId", fileId);
      int fileIdInt = AipoPreconditions.isInteger("fileId", fileId);

      return new StreamContent("image/jpeg", messageService
        .getMessageFilesThumbnail(userIds.iterator().next(), fileIdInt, request
          .getToken()));
    } catch (ProtocolException e) {
      throw e;
    } catch (Throwable t) {
      throw new AipoProtocolException(AipoErrorCode.INTERNAL_ERROR);
    }
  }
}
