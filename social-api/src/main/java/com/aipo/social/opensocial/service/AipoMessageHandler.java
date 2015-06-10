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

import java.util.Set;
import java.util.concurrent.Future;

import org.apache.shindig.protocol.HandlerPreconditions;
import org.apache.shindig.protocol.Operation;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.Service;
import org.apache.shindig.social.opensocial.service.SocialRequestItem;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.aipo.social.opensocial.model.ALMessage;
import com.aipo.social.opensocial.spi.AipoCollectionOptions;
import com.aipo.social.opensocial.spi.MessageService;
import com.google.inject.Inject;

/**
 * Message API
 */
@Service(name = "messages", path = "/{userId}+/{groupId}/{roomId}/{messageId}+")
public class AipoMessageHandler {

  private final MessageService service;

  @Inject
  public AipoMessageHandler(MessageService service) {
    this.service = service;
  }

  /**
   * メッセージ一覧 GET /messages/@viewer/@self/1/
   *
   * メッセージ詳細 GET /messages/@viewer/@self/1/1
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "GET")
  public Future<?> get(SocialRequestItem request) {

    Set<UserId> userIds = request.getUsers();
    GroupId groupId = request.getGroup();

    String roomId = request.getParameter("roomId");
    String messageId = request.getParameter("messageId");

    AipoCollectionOptions options = new AipoCollectionOptions(request);

    // Preconditions
    HandlerPreconditions.requireNotEmpty(userIds, "No userId specified");
    HandlerPreconditions.requireSingular(
      userIds,
      "Only one userId must be specified");

    return service.getMessages(userIds.iterator().next(), options, request
      .getFields(), roomId, messageId, request.getToken());

  }

  /**
   * メッセージ更新 PUT /messages/@viewer/@self/1
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "PUT", bodyParam = "body")
  public Future<?> update(SocialRequestItem request) {
    throw new ProtocolException(501, null, new UnsupportedOperationException());
  }

  /**
   * メッセージ作成 POST /messages/@viewer/@self/1
   *
   * @param request
   * @return
   * @return
   */
  @Operation(httpMethods = "POST", bodyParam = "body")
  public Future<ALMessage> create(SocialRequestItem request) {

    // エラーが出ているため一旦該当部分をコメントアウト
    Set<UserId> userIds = request.getUsers();
    GroupId groupId = request.getGroup();

    String transactionId = request.getParameter("transactionId");
    String roomId = request.getParameter("roomId");
    String message = request.getParameter("message");

    AipoCollectionOptions options = new AipoCollectionOptions(request);

    // Preconditions
    HandlerPreconditions.requireNotEmpty(userIds, "No userId specified");
    HandlerPreconditions.requireSingular(
      userIds,
      "Only one userId must be specified");

    return service.postMessage(
      userIds.iterator().next(),
      request.getFields(),
      roomId,
      "",
      message,
      request.getToken(),
      transactionId);
  }

  /**
   * メッセージ削除 DELETE /messages/@viewer/@self/1
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "DELETE")
  public Future<?> delete(SocialRequestItem request) {
    throw new ProtocolException(501, null, new UnsupportedOperationException());
  }
}
