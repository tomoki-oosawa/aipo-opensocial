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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.DateUtil;
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
      CollectionOptions collectionOptions, Set<String> fields, String roomId,
      SecurityToken token) {

    // TODO: FIELDS

    setUp(token);

    Integer roomIdInt = 0;

    // Room
    try {
      if (roomId != null && !"".equals(roomId)) {
        roomIdInt = Integer.valueOf(roomId);
      }
    } catch (Throwable ignore) {
      //
    }

    // 自分(Viewer)のルームのみ取得可能
    checkSameViewer(userId, token);

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

    list = messageDbService.findMessageRoom(roomIdInt, username, options);

    List<ALMessageRoom> result = new ArrayList<ALMessageRoom>(list.size());
    for (EipTMessageRoom room : list) {
      result.add(assignMessageRoom(room, fields, token, roomIdInt));
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
      Set<String> fields, SecurityToken token, Integer roomIdInt) {
    ALMessageRoom room = new ALMessageRoomImpl();
    String orgId = getOrgId(token);

    room.setRoomId(model.getRoomId());
    if ("O".equals(model.getRoomType())) {
      room.setUserId(orgId + ":" + model.getLoginName());
      room.setName(model.getLastName() + " " + model.getFirstName());
    } else {
      room.setName(model.getName());
    }
    room.setUnreadCount(model.getUnreadCount());
    room.setIsDirect("O".equals(model.getRoomType()));
    room.setIsAutoName("T".equals(model.getAutoName()));
    room.setUpdateDate(DateUtil.formatIso8601Date(model.getLastUpdateDate()));
    if (roomIdInt == 0) {
      // ルーム一覧の場合
      return room;
    }

    List<String> members = new ArrayList<String>();
//    for (EipTMessageRoomMember member : model.getEipTMessageRoomMember()) {
//      members.add(orgId + ":" + member.getLoginName());
//    }
    for (String member : model.getRoomMembers()) {
        members.add(orgId + ":" + member);
      }
    room.setMembers(members);

    // ルーム詳細の場合
    return room;
  }

  /**
   * @param userId
   * @param fields
   * @param name
   * @param memberList
   * @param token
   */
  @Override
  public void postRoom(UserId userId, Set<String> fields, String name,
      List<String> memberList, SecurityToken token) {
    // TODO: FIELDS

    setUp(token);

    List<String> memberNameList = new ArrayList<String>();
    for (String memberId : memberList) {
      if (!"".equals(memberId)) {
        String memberName = getUserId(memberId, token);
        memberNameList.add(memberName);
      }
    }

    // TODO: 権限をチェック
    // 自分(Viewer)を含むルームのみ作成可能
    checkSameViewer(userId, token);
    String username = getUserId(userId, token);

    if (memberNameList.size() != 0) {
      // ルーム
      messageDbService.createRoom(username, name, memberNameList, fields);
    } else {
      // ダイレクトメッセージ
    }

  }

  /**
   *
   * @param userId
   * @param collectionOptions
   * @param fields
   * @param roomId
   * @param messageId
   * @param token
   * @return
   */
  @Override
  public Future<RestfulCollection<ALMessage>> getMessages(UserId userId,
      AipoCollectionOptions collectionOptions, Set<String> fields,
      String roomId, String messageId, SecurityToken token) {

    // TODO: FIELDS

    setUp(token);

    Integer roomIdInt = null;
    Integer messageIdInt = 0;

    // Room
    try {
      roomIdInt = Integer.valueOf(roomId);
      if (messageId != null && !"".equals(messageId)) {
        messageIdInt = Integer.valueOf(messageId);
      }
    } catch (Throwable ignore) {
      //
    }

    // Search
    SearchOptions options =
      SearchOptions
        .build()
        .withRange(collectionOptions.getMax(), collectionOptions.getFirst())
        .withFilter(
          collectionOptions.getFilter(),
          collectionOptions.getFilterOperation() == null
            ? FilterOperation.equals
            : FilterOperation.valueOf(collectionOptions
              .getFilterOperation()
              .toString()),
          collectionOptions.getFilterValue())
        .withSort(
          collectionOptions.getSortBy(),
          collectionOptions.getSortOrder() == null
            ? SortOrder.ascending
            : SortOrder.valueOf(collectionOptions.getSortOrder().toString()))
        .withParameters(collectionOptions.getParameters());

    List<EipTMessage> list = null;
    // /messages/rooms/\(userId)/\(groupId)
    // {userId} が所属しているルームを取得
    // TODO: 閲覧権限をチェック
    if (roomIdInt != null) {
      // ルーム
      list = messageDbService.findMessage(roomIdInt, messageIdInt, options);
    } else {
      // ダイレクトメッセージ
    }
    List<ALMessage> result = new ArrayList<ALMessage>(list.size());
    for (EipTMessage message : list) {
      result.add(assignMessage(message, fields, token, messageIdInt));
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
      SecurityToken token, Integer messageIdInt) {
    ALMessage message = new ALMessageImpl();
    String orgId = getOrgId(token);

    message.setMessegeId(model.getMessageId());
    message.setRoomId(model.getRoomId());
    message.setUserId(orgId + ":" + model.getLoginName());
    message.setUnreadCount(model.getUnreadCount());
    message.setMemberCount(model.getMemberCount());
    message.setMessage(model.getMessage());
    message.setCreateDate(DateUtil.formatIso8601Date(model.getCreateDate()));
    if (messageIdInt == 0) {
      // メッセージ一覧の場合
      return message;
    }

    List<String> members = new ArrayList<String>();
    for (String member : model.getReadMembers()) {
      members.add(orgId + ":" + member);
    }
    message.setReadMembers(members);
    // メッセージ詳細の場合
    return message;
  }

  /**
   * @param next
   * @param options
   * @param fields
   * @param roomId
   * @param message
   * @param token
   * @return
   */
  @Override
  public Future<ALMessage> postMessage(UserId userId, Set<String> fields,
      String roomId, String targetUserId, String message, SecurityToken token) {
    // TODO: FIELDS

    setUp(token);

    Integer roomIdInt = null;
    Integer messageIdInt = 0;

    // Room
    try {
      if (roomId != null && !"".equals(roomId)) {
        roomIdInt = Integer.valueOf(roomId);
      }
    } catch (Throwable ignore) {
    }

    String targetUsername = null;
    if (targetUserId != null && !"".equals(targetUserId)) {
      targetUsername = getUserId(targetUserId, token);
    }

    // TODO: 権限をチェック
    // 自分(Viewer)のルームのみ取得可能
    checkSameViewer(userId, token);
    String username = getUserId(userId, token);

    EipTMessage model = null;
    if (roomIdInt != null
      || !("".equals(targetUsername) || targetUsername == null)) {
      // ルーム
      model =
        messageDbService.createMessage(
          username,
          roomIdInt,
          targetUsername,
          message,
          fields);
    } else {
      // ダイレクトメッセージ
    }

    messageIdInt = model.getMessageId();

    ALMessage result = new ALMessageImpl();
    result = assignMessage(model, fields, token, messageIdInt);

    return ImmediateFuture.newInstance(result);
  }

  /**
   * @param next
   * @param fields
   * @param name
   * @param memberList
   * @param token
   * @return
   */
  @Override
  public Future<ALMessageRoom> putRoom(UserId userId, String name,
      List<String> memberList, String roomId, SecurityToken token) {
    // TODO: FIELDS

    setUp(token);

    Integer roomIdInt = null;

    // Room
    try {
      roomIdInt = Integer.valueOf(roomId);
    } catch (Throwable ignore) {
      //
    }

    List<String> memberNameList = new ArrayList<String>();
    for (String memberId : memberList) {
      if (!"".equals(memberId)) {
        String memberName = getUserId(memberId, token);
        memberNameList.add(memberName);
      }
    }

    // TODO: 権限をチェック
    // 自分(Viewer)を含むルームのみ設定可能
    checkSameViewer(userId, token);
    String username = getUserId(userId, token);

    EipTMessageRoom model = null;
    if (roomIdInt != null && memberNameList.size() != 0) {
      // ルーム
      model =
        messageDbService.updateRoom(roomIdInt, username, name, memberNameList);
    } else {
      // ダイレクトメッセージ
    }

    ALMessageRoom result = new ALMessageRoomImpl();
    Set<String> dummy = new HashSet<String>();
    result = assignMessageRoom(model, dummy, token, roomIdInt);

    return ImmediateFuture.newInstance(result);
  }
}
