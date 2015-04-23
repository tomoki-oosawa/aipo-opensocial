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
package com.aipo.social.opensocial.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.ImmediateFuture;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.aipo.orm.model.portlet.EipTMessage;
import com.aipo.orm.model.portlet.EipTMessageRoom;
import com.aipo.orm.model.portlet.EipTMessageRoomMember;
import com.aipo.orm.service.MessageDbService;
import com.aipo.orm.service.request.SearchOptions;
import com.aipo.orm.service.request.SearchOptions.FilterOperation;
import com.aipo.orm.service.request.SearchOptions.SortOrder;
import com.aipo.social.core.model.ALMessageImpl;
import com.aipo.social.core.model.ALMessageRoomImpl;
import com.aipo.social.opensocial.model.ALMessage;
import com.aipo.social.opensocial.model.ALMessageRoom;
import com.google.inject.Inject;

public class AipoMessageService extends AbstractService implements
    MessageService {

  private final MessageDbService messageDbService;

  /**
   *
   */
  @Inject
  public AipoMessageService(MessageDbService turbineUserSercice) {
    this.messageDbService = turbineUserSercice;
  }

  /**
   * @param userId
   * @param collectionOptions
   * @param fields
   * @param token
   * @return
   */
  @Override
  public Future<RestfulCollection<ALMessageRoom>> getRooms(UserId userId,
      CollectionOptions collectionOptions, Set<String> fields,
      SecurityToken token) {

    // TODO: FIELDS

    setUp(token);

    String username = getUserId(userId, token);

    // Search
    SearchOptions options =
      SearchOptions.build().withRange(
        collectionOptions.getMax(),
        collectionOptions.getFirst()).withFilter(
        collectionOptions.getFilter(),
        collectionOptions.getFilterOperation() == null
          ? FilterOperation.equals
          : FilterOperation.valueOf(collectionOptions
            .getFilterOperation()
            .toString()),
        collectionOptions.getFilterValue()).withSort(
        collectionOptions.getSortBy(),
        collectionOptions.getSortOrder() == null
          ? SortOrder.ascending
          : SortOrder.valueOf(collectionOptions.getSortOrder().toString()));

    List<EipTMessageRoom> list = null;
    // /messages/rooms/\(userId)/\(groupId)
    // {userId} が所属しているルームを取得
    list = messageDbService.findMessageRoom(username, options);
    List<ALMessageRoom> result = new ArrayList<ALMessageRoom>(list.size());
    for (EipTMessageRoom room : list) {
      result.add(assignMessageRoom(room, fields, token));
    }

    int totalResults = result.size();

    RestfulCollection<ALMessageRoom> restCollection =
      new RestfulCollection<ALMessageRoom>(
        result,
        collectionOptions.getFirst(),
        totalResults,
        collectionOptions.getMax());
    return ImmediateFuture.newInstance(restCollection);
  }

  protected ALMessageRoom assignMessageRoom(EipTMessageRoom model,
      Set<String> fields, SecurityToken token) {
    ALMessageRoom room = new ALMessageRoomImpl();

    room.setId(model.getRoomId());
    room.setName(model.getName());
    room.setUserId(model.getUserId().toString());
    room.setUnreadCount(model.getUnreadCount());
    room.setIsDirect("O".equals(model.getRoomType()));

    return room;
  }

  /**
   * @param userId
   * @param collectionOptions
   * @param fields
   * @param token
   * @return
   */
  @Override
  public Future<RestfulCollection<ALMessage>> getPosts(UserId userId,
      CollectionOptions collectionOptions, Set<String> fields,
      SecurityToken token) {

    // TODO: FIELDS

    setUp(token);

    String username = getUserId(userId, token);

    // Search
    SearchOptions options =
      SearchOptions.build().withRange(
        collectionOptions.getMax(),
        collectionOptions.getFirst()).withFilter(
        collectionOptions.getFilter(),
        collectionOptions.getFilterOperation() == null
          ? FilterOperation.equals
          : FilterOperation.valueOf(collectionOptions
            .getFilterOperation()
            .toString()),
        collectionOptions.getFilterValue()).withSort(
        collectionOptions.getSortBy(),
        collectionOptions.getSortOrder() == null
          ? SortOrder.ascending
          : SortOrder.valueOf(collectionOptions.getSortOrder().toString()));

    List<EipTMessage> list = null;
    // /messages/rooms/\(userId)/\(groupId)
    // {userId} が所属しているルームを取得
    list = messageDbService.findMessage(username, options);
    List<ALMessage> result = new ArrayList<ALMessage>(list.size());
    for (EipTMessage room : list) {
      result.add(assignMessage(room, fields, token));
    }

    int totalResults = result.size();

    RestfulCollection<ALMessage> restCollection =
      new RestfulCollection<ALMessage>(
        result,
        collectionOptions.getFirst(),
        totalResults,
        collectionOptions.getMax());
    return ImmediateFuture.newInstance(restCollection);
  }

  /**
   * @param room
   * @param fields
   * @param token
   * @return
   */
  private ALMessage assignMessage(EipTMessage model, Set<String> fields,
      SecurityToken token) {
    ALMessage message = new ALMessageImpl();

    message.setId(model.getMessageId());
    message.setRoomId(model.getEipTMessageRoom().getRoomId());
    message.setUserId(model.getUserId().toString());
    message.setUnreadCount(model.getUnreadCount());
    message.setMemberCount(model.getMemberCount());
    message.setMessage(model.getMessage());
    List<String> members = new ArrayList<String>();
    for (EipTMessageRoomMember member : model
      .getEipTMessageRoom()
      .getEipTMessageRoomMember()) {
      // fix
      members.add(member.getLoginName());
    }
    message.setReadMembers(members);

    message.setCreateDate(model.getCreateDate().toString());

    return message;
  }

}