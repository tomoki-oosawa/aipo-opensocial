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
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.shindig.protocol.HandlerPreconditions;
import org.apache.shindig.protocol.Operation;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.Service;
import org.apache.shindig.protocol.multipart.FormDataItem;
import org.apache.shindig.social.opensocial.service.SocialRequestItem;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.aipo.container.protocol.StreamContent;
import com.aipo.social.opensocial.spi.AipoCollectionOptions;
import com.aipo.social.opensocial.spi.MessageService;
import com.google.inject.Inject;

/**
 * RPC/REST handler for all /peopleIcon requests
 */
@Service(name = "roomIcon", path = "/{userId}+/{groupId}/{roomId}+")
public class AipoRoomIconHandler {

  private final MessageService service;

  @Inject
  public AipoRoomIconHandler(MessageService service) {
    this.service = service;
  }

  @Operation(httpMethods = "GET")
  public StreamContent get(SocialRequestItem request) throws ProtocolException {
    GroupId groupId = request.getGroup();
    String roomId = request.getParameter("roomId");

    CollectionOptions options = new CollectionOptions(request);

    InputStream roomIcon = service.getRoomIcon(roomId, request.getToken());

    return new StreamContent("image/jpeg", roomIcon);
  }

  /**
   * ルームアイコン作成 POST /roomIcon/@viewer/@self
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "POST")
  public Future<?> create(SocialRequestItem request) {

    Set<UserId> userIds = request.getUsers();
    GroupId groupId = request.getGroup();

    String name = request.getParameter("name");
    List<String> memberList = request.getListParameter("member_to");

    FormDataItem roomIcon = request.getFormMimePart("roomIcon");

    AipoCollectionOptions options = new AipoCollectionOptions(request);

    // Preconditions
    HandlerPreconditions.requireNotEmpty(userIds, "No userId specified");
    HandlerPreconditions.requireSingular(
      userIds,
      "Only one userId must be specified");
    HandlerPreconditions.requireNotEmpty(memberList, "No member_to specified");
    HandlerPreconditions.requirePlural(
      memberList,
      "More than one member_to must be specified");

    String body = request.getParameter("body");

    return service.postRoom(
      userIds.iterator().next(),
      request.getFields(),
      name,
      memberList,
      request.getToken());
  }
}
