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
package com.aipo.social.opensocial.service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.shindig.protocol.Operation;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.Service;
import org.apache.shindig.protocol.multipart.FormDataItem;
import org.apache.shindig.social.opensocial.service.SocialRequestItem;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.aipo.container.protocol.AipoErrorCode;
import com.aipo.container.protocol.AipoPreconditions;
import com.aipo.container.protocol.AipoProtocolException;
import com.aipo.container.protocol.AipoScope;
import com.aipo.container.protocol.StreamContent;
import com.aipo.social.opensocial.spi.AipoCollectionOptions;
import com.aipo.social.opensocial.spi.MessageService;
import com.google.inject.Inject;

/**
 * RPC/REST handler for Rooms API
 */
@Service(name = "rooms")
public class AipoMessageRoomHandler {

  private final MessageService service;

  @Inject
  public AipoMessageRoomHandler(MessageService service) {
    this.service = service;
  }

  /**
   * ルーム一覧・詳細 <br>
   * <code>
   * GET /rooms/:roomId
   * </code><br>
   * <code>
   * osapi.rooms.get()
   * osapi.rooms.get( { roomId: :roomId })
   * </code>
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "GET", path = "/{roomId}+")
  public Future<?> get(SocialRequestItem request) {
    try {
      Set<UserId> userIds = request.getUsers();
      String roomId = request.getParameter("roomId");

      // Preconditions
      AipoPreconditions.validateScope(request.getToken(), AipoScope.R_ALL);
      AipoPreconditions.required("userId", userIds);
      AipoPreconditions.notMultiple("userId", userIds);

      return service.getRooms(
        userIds.iterator().next(),
        new AipoCollectionOptions(request),
        request.getFields(),
        roomId,
        request.getToken());
    } catch (ProtocolException e) {
      throw e;
    } catch (Throwable t) {
      throw new AipoProtocolException(AipoErrorCode.INTERNAL_ERROR);
    }
  }

  /**
   * ルーム作成 <br>
   * <code>
   * POST /rooms
   * </code><br>
   * <code>
   * osapi.rooms.create()
   * </code>
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "POST")
  public Future<?> create(SocialRequestItem request) {
    try {
      Set<UserId> userIds = request.getUsers();
      String name = request.getParameter("name");
      List<String> memberList = request.getListParameter("member_to");

      // Preconditions
      AipoPreconditions.validateScope(request.getToken(), AipoScope.W_ALL);
      AipoPreconditions.required("userId", userIds);
      AipoPreconditions.notMultiple("userId", userIds);
      AipoPreconditions.required("member_to", memberList);
      AipoPreconditions.multiple("member_to", memberList);
      AipoPreconditions.maxLength("name", name, 50);
      AipoPreconditions.isUTF8("name", name);

      return service.postRoom(
        userIds.iterator().next(),
        name,
        memberList,
        request.getToken());
    } catch (ProtocolException e) {
      throw e;
    } catch (Throwable t) {
      throw new AipoProtocolException(AipoErrorCode.INTERNAL_ERROR);
    }
  }

  /**
   * ルーム更新 <br>
   * <code>
   * PUT /rooms/:roomId
   * </code><br>
   * <code>
   * osapi.rooms.update( { roomId: :roomId } )
   * </code>
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "PUT", path = "/{roomId}")
  public Future<?> update(SocialRequestItem request) {
    try {
      Set<UserId> userIds = request.getUsers();
      String name = request.getParameter("name");
      List<String> memberList = request.getListParameter("member_to");
      String roomId = request.getParameter("roomId");

      // Preconditions
      AipoPreconditions.validateScope(request.getToken(), AipoScope.W_ALL);
      AipoPreconditions.required("userId", userIds);
      AipoPreconditions.notMultiple("userId", userIds);
      AipoPreconditions.required("member_to", memberList);
      AipoPreconditions.multiple("member_to", memberList);
      AipoPreconditions.required("roomId", roomId);

      return service.putRoom(
        userIds.iterator().next(),
        name,
        memberList,
        roomId,
        request.getToken());
    } catch (ProtocolException e) {
      throw e;
    } catch (Throwable t) {
      throw new AipoProtocolException(AipoErrorCode.INTERNAL_ERROR);
    }
  }

  /**
   * ルーム削除 <br>
   * <code>
   * DELETE /rooms/:roomId
   * </code><br>
   * <code>
   * osapi.rooms['delete']( { roomId: :roomId } )
   * </code>
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "DELETE", path = "/{roomId}")
  public Future<?> delete(SocialRequestItem request) {
    try {
      Set<UserId> userIds = request.getUsers();
      String roomId = request.getParameter("roomId");

      // Preconditions
      AipoPreconditions.validateScope(request.getToken(), AipoScope.W_ALL);
      AipoPreconditions.required("userId", userIds);
      AipoPreconditions.notMultiple("userId", userIds);
      AipoPreconditions.required("roomId", roomId);
      int roomIdInt = AipoPreconditions.isInteger("roomId", roomId);

      return service.deleteRoom(userIds.iterator().next(), roomIdInt, request
        .getToken());
    } catch (ProtocolException e) {
      throw e;
    } catch (Throwable t) {
      throw new AipoProtocolException(AipoErrorCode.INTERNAL_ERROR);
    }
  }

  /**
   * ルームアイコン <br>
   * <code>
   * GET /rooms/:roomId/icon
   * </code><br>
   * <code>
   * osapi.rooms.icon.get( { roomId: :roomId } )
   * </code>
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "GET", name = "icon.get", path = "/{roomId}/icon")
  public StreamContent getIcon(SocialRequestItem request)
      throws ProtocolException {
    try {
      Set<UserId> userIds = request.getUsers();
      String roomId = request.getParameter("roomId");
      String size = request.getParameter("size");

      // Preconditions
      AipoPreconditions.validateScope(request.getToken(), AipoScope.R_ALL);
      AipoPreconditions.required("userId", userIds);
      AipoPreconditions.notMultiple("userId", userIds);
      AipoPreconditions.required("roomId", roomId);
      int roomIdInt = AipoPreconditions.isInteger("roomId", roomId);

      return new StreamContent("image/jpeg", service.getRoomIcon(userIds
        .iterator()
        .next(), roomIdInt, size, request.getToken()));
    } catch (ProtocolException e) {
      throw e;
    } catch (Throwable t) {
      throw new AipoProtocolException(AipoErrorCode.INTERNAL_ERROR);
    }
  }

  /**
   * ルームアイコン更新 <br>
   * <code>
   * POST/PUT /rooms/:roomId/icon
   * </code><br>
   * <code>
   * osapi.rooms.icon.get( { roomId: :roomId } )
   * </code>
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = { "PUT", "POST" }, name = "icon.update", path = "/{roomId}/icon")
  public Future<?> updateIcon(SocialRequestItem request)
      throws ProtocolException {
    try {
      Set<UserId> userIds = request.getUsers();
      String roomId = request.getParameter("roomId");
      FormDataItem roomIcon = request.getFormMimePart("roomIcon");

      // Preconditions
      AipoPreconditions.validateScope(request.getToken(), AipoScope.W_ALL);
      AipoPreconditions.required("userId", userIds);
      AipoPreconditions.notMultiple("userId", userIds);
      AipoPreconditions.required("roomId", roomId);
      AipoPreconditions.required("roomIcon", roomIcon);
      int roomIdInt = AipoPreconditions.isInteger("roomId", roomId);

      return service.putRoomIcon(
        userIds.iterator().next(),
        roomIdInt,
        roomIcon,
        request.getToken());

    } catch (ProtocolException e) {
      throw e;
    } catch (Throwable t) {
      throw new AipoProtocolException(AipoErrorCode.INTERNAL_ERROR);
    }
  }

  /**
   * ルームアイコン削除 <br>
   * <code>
   * DELETE /rooms/:roomId/icon
   * </code><br>
   * <code>
   * osapi.rooms.icon["delete"]( { roomId: :roomId } )
   * </code>
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "DELETE", name = "icon.delete", path = "/{roomId}/icon")
  public Future<?> deleteIcon(SocialRequestItem request)
      throws ProtocolException {
    try {
      Set<UserId> userIds = request.getUsers();
      String roomId = request.getParameter("roomId");

      // Preconditions
      AipoPreconditions.validateScope(request.getToken(), AipoScope.W_ALL);
      AipoPreconditions.required("userId", userIds);
      AipoPreconditions.notMultiple("userId", userIds);
      AipoPreconditions.required("roomId", roomId);
      int roomIdInt = AipoPreconditions.isInteger("roomId", roomId);

      return service.deleteRoomIcon(
        userIds.iterator().next(),
        roomIdInt,
        request.getToken());
    } catch (ProtocolException e) {
      throw e;
    } catch (Throwable t) {
      throw new AipoProtocolException(AipoErrorCode.INTERNAL_ERROR);
    }
  }

}
