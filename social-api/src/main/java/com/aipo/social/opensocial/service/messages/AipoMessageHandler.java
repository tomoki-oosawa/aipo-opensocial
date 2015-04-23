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
package com.aipo.social.opensocial.service.messages;

import java.util.Set;
import java.util.concurrent.Future;

import org.apache.shindig.protocol.HandlerPreconditions;
import org.apache.shindig.protocol.Operation;
import org.apache.shindig.protocol.Service;
import org.apache.shindig.social.opensocial.service.SocialRequestItem;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.aipo.social.opensocial.spi.MessageService;
import com.google.inject.Inject;

/**
 * Message API :posts
 */
@Service(name = "messages", path = "/posts/{userId}+/{groupId}/{roomId}+")
public class AipoMessageHandler {

  private final MessageService service;

  @Inject
  public AipoMessageHandler(MessageService service) {
    this.service = service;
  }

  /**
   * メッセージ一覧 GET /messages/posts/@viewer/@self
   *
   * メッセージ詳細 GET /messages/posts/@viewer/@self/1
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "GET")
  public Future<?> get(SocialRequestItem request) {

    Set<UserId> userIds = request.getUsers();

    CollectionOptions options = new CollectionOptions(request);

    // Preconditions
    HandlerPreconditions.requireNotEmpty(userIds, "No userId specified");
    HandlerPreconditions.requireSingular(
      userIds,
      "Only one userId must be specified");
    return service.getPosts(userIds.iterator().next(), options, request
      .getFields(), request.getToken());
  }

  /**
   * メッセージ更新 PUT /messages/posts/@viewer/@self/1
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "PUT")
  public Future<?> update(SocialRequestItem request) {
    throw new UnsupportedOperationException();
  }

  /**
   * メッセージ作成 POST /messages/posts/@viewer/@self
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "POST")
  public Future<?> create(SocialRequestItem request) {
    throw new UnsupportedOperationException();
  }

  /**
   * メッセージ削除 DELETE /messages/posts/@viewer/@self/1
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "DELETE")
  public Future<?> delete(SocialRequestItem request) {
    throw new UnsupportedOperationException();
  }
}
