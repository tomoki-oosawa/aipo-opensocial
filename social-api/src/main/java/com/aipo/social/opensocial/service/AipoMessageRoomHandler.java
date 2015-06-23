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

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.shindig.protocol.HandlerPreconditions;
import org.apache.shindig.protocol.Operation;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.Service;
import org.apache.shindig.social.opensocial.service.SocialRequestItem;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.aipo.social.opensocial.spi.AipoCollectionOptions;
import com.aipo.social.opensocial.spi.MessageService;
import com.google.inject.Inject;

/**
 * Message API
 */
@Service(name = "rooms", path = "/{userId}+/{groupId}/{roomId}+")
public class AipoMessageRoomHandler {

  private final MessageService service;

  @Inject
  public AipoMessageRoomHandler(MessageService service) {
    this.service = service;
  }

  /**
   * ルーム一覧 GET /rooms/@viewer/@self
   *
   * ルーム詳細 GET /rooms/@viewer/@self/1
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "GET")
  public Future<?> get(SocialRequestItem request) {

    Set<UserId> userIds = request.getUsers();
    String roomId = request.getParameter("roomId");

    CollectionOptions options = new CollectionOptions(request);

    // Preconditions
    HandlerPreconditions.requireNotEmpty(userIds, "No userId specified");
    HandlerPreconditions.requireSingular(
      userIds,
      "Only one userId must be specified");

    return service.getRooms(userIds.iterator().next(), options, request
      .getFields(), roomId, request.getToken());
  }

  /**
   * ルーム更新 PUT /rooms/@viewer/@self/1
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "PUT")
  public Future<?> update(SocialRequestItem request) {
    Set<UserId> userIds = request.getUsers();
    GroupId groupId = request.getGroup();
    List<String> roomId = request.getListParameter("roomId");

    String name = request.getParameter("name");
    List<String> memberList = request.getListParameter("member_to");

    AipoCollectionOptions options = new AipoCollectionOptions(request);

    // Preconditions
    HandlerPreconditions.requireNotEmpty(userIds, "No userId specified");
    HandlerPreconditions.requireSingular(
      userIds,
      "Only one userId must be specified");
    HandlerPreconditions.requireNotEmpty(roomId, "No roomId specified");
    HandlerPreconditions.requireSingular(
      roomId,
      "Only one roomId must be specified");
    HandlerPreconditions.requireNotEmpty(memberList, "No member_to specified");
    HandlerPreconditions.requirePlural(
      memberList,
      "More than one member_to must be specified");

    return service.putRoom(userIds.iterator().next(), name, memberList, roomId
      .iterator()
      .next(), request.getToken());
  }

  /**
   * ルーム作成 POST /rooms/@viewer/@self
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "POST")
  public void create(SocialRequestItem request) {

    Set<UserId> userIds = request.getUsers();
    GroupId groupId = request.getGroup();

    String name = request.getParameter("name");
    List<String> memberList = request.getListParameter("member_to");

    AipoCollectionOptions options = new AipoCollectionOptions(request);

    // Preconditions
    HandlerPreconditions.requireNotEmpty(userIds, "No userId specified");
    HandlerPreconditions.requireSingular(
      userIds,
      "Only one userId must be specified");

    service.postRoom(
      userIds.iterator().next(),
      request.getFields(),
      name,
      memberList,
      request.getToken());

  }

  /**
   * ルーム削除 DELETE /rooms/@viewer/@self/1
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "DELETE")
  public Future<?> delete(SocialRequestItem request) {
    throw new ProtocolException(501, null, new UnsupportedOperationException());
  }
}
