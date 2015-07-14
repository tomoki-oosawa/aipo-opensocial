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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.DateUtil;
import org.apache.shindig.common.util.ImmediateFuture;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.protocol.multipart.FormDataItem;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.aipo.container.protocol.AipoErrorCode;
import com.aipo.container.protocol.AipoProtocolException;
import com.aipo.orm.model.portlet.EipTMessage;
import com.aipo.orm.model.portlet.EipTMessageFile;
import com.aipo.orm.model.portlet.EipTMessageRoom;
import com.aipo.orm.model.portlet.EipTMessageRoomMember;
import com.aipo.orm.service.MessageDbService;
import com.aipo.orm.service.request.SearchOptions;
import com.aipo.orm.service.request.SearchOptions.FilterOperation;
import com.aipo.orm.service.request.SearchOptions.SortOrder;
import com.aipo.social.core.model.ALFileImpl;
import com.aipo.social.core.model.ALMessageFileImpl;
import com.aipo.social.core.model.ALMessageImpl;
import com.aipo.social.core.model.ALMessageRoomImpl;
import com.aipo.social.opensocial.model.ALFile;
import com.aipo.social.opensocial.model.ALMessage;
import com.aipo.social.opensocial.model.ALMessageFile;
import com.aipo.social.opensocial.model.ALMessageRoom;
import com.google.common.util.concurrent.Futures;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class AipoMessageService extends AbstractService implements
    MessageService {

  @Inject
  @Named("aipo.message.categorykey")
  private String MESSAGE_CATEGORY_KEY;

  private final MessageDbService messageDbService;

  private final PushService pushService;

  /**
   *
   */
  @Inject
  public AipoMessageService(MessageDbService turbineUserSercice,
      PushService pushService) {
    this.messageDbService = turbineUserSercice;
    this.pushService = pushService;
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

    list = messageDbService.findRoom(roomIdInt, username, null, options);
    if (roomIdInt > 0 && list.size() == 0) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ACCESS_NOT_DENIED);
    }

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
    room.setLastMessage(model.getLastMessage());
    if (room.getIsDirect()) {
      if (model.getUserPhotoModified() != null) {
        room.setPhotoModified(model.getUserPhotoModified());
      }
    } else {
      if (model.getPhotoModified() != null) {
        room.setPhotoModified(model.getPhotoModified());
      }
    }
    if (model.getLastMessageId() != null) {
      room.setLastMessageId(model.getLastMessageId());
    }
    room.setIsAutoName("T".equals(model.getAutoName()));
    room.setUpdateDate(model.getLastUpdateDate());
    if (roomIdInt == 0) {
      // ルーム一覧の場合
      return room;
    }

    List<String> members = new ArrayList<String>();
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
  public Future<ALMessageRoom> postRoom(UserId userId, Set<String> fields,
      String name, List<String> memberList, SecurityToken token) {
    // TODO: FIELDS

    setUp(token);

    List<String> memberNameList = new ArrayList<String>();
    for (String memberId : memberList) {
      if (!"".equals(memberId)) {
        String memberName = getUserId(memberId, token);
        memberNameList.add(memberName);
      }
    }

    // 自分(Viewer)を含むルームのみ作成可能
    checkSameViewer(userId, token);
    String username = getUserId(userId, token);
    if (!memberNameList.contains(username)) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ERROR
        .customMessage("Parameter member_to should contain userId."));
    }

    EipTMessageRoom model = null;
    if (memberNameList.size() != 0) {
      // ルーム
      model =
        messageDbService.createRoom(username, name, memberNameList, fields);
    } else {
      // ダイレクトメッセージ
    }

    ALMessageRoom result = new ALMessageRoomImpl();
    Set<String> dummy = new HashSet<String>();
    result = assignMessageRoom(model, dummy, token, model.getRoomId());

    return ImmediateFuture.newInstance(result);

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
    String targetUsername = null;
    Integer messageIdInt = 0;

    // Room
    try {
      roomIdInt = Integer.valueOf(roomId);
    } catch (Throwable ignore) {
      // ダイレクト
      targetUsername = getUserId(roomId, token);
    }
    try {
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

    // 自分(Viewer)のルームのみ取得可能
    checkSameViewer(userId, token);
    String username = getUserId(userId, token);

    EipTMessageRoom room = null;
    if (roomIdInt != null) {
      room = messageDbService.findRoom(roomIdInt, username);
    } else {
      room = messageDbService.findRoom(username, targetUsername);
    }
    if (room == null) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ACCESS_NOT_DENIED);
    }

    // /messages/rooms/\(userId)/\(groupId)
    // {userId} が所属しているルームを取得

    // ルーム
    List<EipTMessage> list =
      messageDbService.findMessage(room.getRoomId(), messageIdInt, options);

    List<ALMessage> result = new ArrayList<ALMessage>(list.size());
    for (EipTMessage message : list) {
      result.add(assignMessage(message, room, fields, token, messageIdInt));
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
  private ALMessage assignMessage(EipTMessage model, EipTMessageRoom room,
      Set<String> fields, SecurityToken token, Integer messageIdInt) {
    ALMessage message = new ALMessageImpl();
    String orgId = getOrgId(token);

    message.setMessegeId(model.getMessageId());
    message.setRoomId(model.getRoomId());
    message.setUserId(orgId + ":" + model.getLoginName());
    message.setUnreadCount(model.getUnreadCount());
    message.setMemberCount(model.getMemberCount());
    message.setMessage(model.getMessage());
    message.setCreateDate(DateUtil.formatIso8601Date(model.getCreateDate()));

    if ("O".equals(room.getRoomType())) {
      message.setTargetUserId(orgId + ":" + room.getLoginName());
    }

    if (model.getMessageFiles() != null) {
      List<ALMessageFile> files = new ArrayList<ALMessageFile>();
      for (EipTMessageFile file : model.getMessageFiles()) {
        ALMessageFile messageFile = assignMessageFile(file);
        files.add(messageFile);
      }
      message.setFiles(files);
    }

    if (messageIdInt == 0) {
      // メッセージ一覧の場合
      return message;
    }

    if (model.getReadMembers() != null) {
      List<String> members = new ArrayList<String>();
      for (String member : model.getReadMembers()) {
        members.add(orgId + ":" + member);
      }
      message.setReadMembers(members);
    }

    // メッセージ詳細の場合
    return message;
  }

  /**
   * @param room
   * @param fields
   * @param token
   * @return
   */
  private ALMessageFile assignMessageFile(EipTMessageFile model) {
    ALMessageFile file = new ALMessageFileImpl();

    file.setFileId(model.getFileId());
    file.setFileName(model.getFileName());

    return file;
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
      String roomId, String message, SecurityToken token, String transactionId) {
    // TODO: FIELDS

    setUp(token);

    Integer roomIdInt = null;
    String targetUsername = null;
    Integer messageIdInt = 0;

    // Room
    try {
      roomIdInt = Integer.valueOf(roomId);
    } catch (Throwable ignore) {
      // ダイレクト
      targetUsername = getUserId(roomId, token);
    }

    // 自分(Viewer)のルームのみ取得可能
    checkSameViewer(userId, token);
    String username = getUserId(userId, token);

    EipTMessageRoom room = null;
    if (roomIdInt != null) {
      room = messageDbService.findRoom(roomIdInt, username);
      if (room == null) {
        throw new AipoProtocolException(
          AipoErrorCode.VALIDATE_ACCESS_NOT_DENIED);
      }
    } else {
      room = messageDbService.findRoom(username, targetUsername);
    }

    EipTMessage model =
      messageDbService.createMessage(
        username,
        roomIdInt,
        targetUsername,
        message,
        fields);
    push(username, model);
    messageIdInt = model.getMessageId();

    if (room == null) {
      room = messageDbService.findRoom(username, targetUsername);
    }

    ALMessage result = new ALMessageImpl();
    result = assignMessage(model, room, fields, token, messageIdInt);
    result.setTransactionId(transactionId);

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

    // 自分(Viewer)を含むルームのみ設定可能
    checkSameViewer(userId, token);
    String username = getUserId(userId, token);

    if (!memberNameList.contains(username)) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ERROR
        .customMessage("Parameter member_to should contain userId."));
    }
    EipTMessageRoom room = null;
    if (roomIdInt != null) {
      room = messageDbService.findRoom(roomIdInt, username);
    }
    if (room == null) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ACCESS_NOT_DENIED);
    }

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

  /**
   * @param roomId
   * @param token
   * @return
   * @throws ProtocolException
   */
  @Override
  public InputStream getRoomIcon(UserId userId, String roomId,
      SecurityToken token) throws ProtocolException {

    setUp(token);

    Integer roomIdInt = null;
    try {
      roomIdInt = Integer.valueOf(roomId);
    } catch (Throwable t) {

    }
    if (roomIdInt == null) {
      throw new AipoProtocolException(AipoErrorCode.ICON_NOT_FOUND);
    }

    // 自分(Viewer)を含むルームのみ設定可能
    checkSameViewer(userId, token);
    String username = getUserId(userId, token);
    EipTMessageRoom room = null;
    if (roomIdInt != null) {
      room = messageDbService.findRoom(roomIdInt, username);
    }
    if (room == null) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ACCESS_NOT_DENIED);
    }

    InputStream roomIcon = messageDbService.getPhoto(roomIdInt.intValue());
    if (roomIcon == null) {
      throw new AipoProtocolException(AipoErrorCode.ICON_NOT_FOUND);
    }
    return roomIcon;
  }

  /**
   * @param roomId
   * @param roomIcon
   * @param token
   * @return
   */
  @Override
  public Future<Void> putRoomIcon(UserId userId, String roomId,
      FormDataItem roomIconItem, SecurityToken token) {
    setUp(token);

    Integer roomIdInt = null;
    try {
      roomIdInt = Integer.valueOf(roomId);
    } catch (Throwable t) {

    }
    if (roomIdInt == null) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ACCESS_NOT_DENIED);
    }
    byte[] roomIcon =
      getBytesShrink(
        roomIconItem,
        DEF_LARGE_THUMBNAIL_WIDTH,
        DEF_LARGE_THUMBNAIL_HEIGHT,
        false,
        DEF_VALIDATE_WIDTH,
        DEF_VALIDATE_HEIGHT).getShrinkImage();

    byte[] roomIconSmartPhone =
      getBytesShrink(
        roomIconItem,
        DEF_NORMAL_THUMBNAIL_WIDTH,
        DEF_NORMAL_THUMBNAIL_HEIGHT,
        false,
        DEF_VALIDATE_WIDTH,
        DEF_VALIDATE_HEIGHT).getShrinkImage();

    checkSameViewer(userId, token);
    String username = getUserId(userId, token);
    EipTMessageRoom room = null;
    if (roomIdInt != null) {
      room = messageDbService.findRoom(roomIdInt, username);
    }
    if (room == null) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ACCESS_NOT_DENIED);
    }

    messageDbService.setPhoto(
      roomIdInt.intValue(),
      roomIcon,
      roomIconSmartPhone);

    return Futures.immediateFuture(null);
  }

  /**
   * @param userId
   * @param options
   * @param fields
   * @param roomId
   * @param messageId
   * @param token
   * @return
   */
  @Override
  public Future<ALFile> getMessageFiles(UserId userId,
      CollectionOptions options, Set<String> fields, String fileId,
      SecurityToken token) {

    // TODO: FIELDS

    setUp(token);

    Integer fileIdInt = null;

    // File
    try {
      fileIdInt = Integer.valueOf(fileId);
    } catch (Throwable ignore) {
      //
    }

    // /messages/rooms/\(userId)/\(groupId)
    // {userId} が所属しているルームを取得
    checkSameViewer(userId, token);
    EipTMessageFile model = null;
    if (fileIdInt != null) {
      // ファイル
      model = messageDbService.findMessageFile(fileIdInt);
    } else {
      // ダイレクトメッセージ
    }
    if (model != null) {
      // roomメンバーチェック
      checkSameRoomMember(userId, token, model.getRoomId());
    } else {
      // ダイレクトメッセージ
    }

    ALFile result = new ALFileImpl();
    result = assignFile(model);

    return ImmediateFuture.newInstance(result);
  }

  /**
   * @param room
   * @param fields
   * @param token
   * @return
   */
  private ALFile assignFile(EipTMessageFile model) {
    ALFile file = new ALFileImpl();

    file.setFileId(model.getFileId());
    file.setFileName(model.getFileName());
    file.setFilePath(model.getFilePath());
    file.setCategoryKey(MESSAGE_CATEGORY_KEY);
    file.setUserId(String.valueOf(model.getOwnerId()));

    return file;
  }

  /**
   *
   * @param userId
   * @param token
   * @throws ProtocolException
   */
  protected void checkSameRoomMember(UserId userId, SecurityToken token,
      String roomId) throws ProtocolException {

    Integer roomIdInt = null;
    try {
      roomIdInt = Integer.valueOf(roomId);
      checkSameRoomMember(userId, token, roomIdInt);
    } catch (Throwable ignore) {
      //
    }
  }

  /**
   *
   * @param userId
   * @param token
   * @throws ProtocolException
   */
  protected void checkSameRoomMember(UserId userId, SecurityToken token,
      Integer roomId) throws ProtocolException {

    String username = getUserId(userId, token);
    if (roomId != null && username != null && !"".equals(username)) {
      boolean isJoinRoom = messageDbService.isJoinRoom(roomId, username);
      if (!isJoinRoom) {
        throw new AipoProtocolException(
          AipoErrorCode.VALIDATE_ACCESS_NOT_DENIED);
      }
    }
  }

  /**
   *
   * @param userId
   * @param token
   * @throws ProtocolException
   */
  protected void push(String username, EipTMessage message)
      throws ProtocolException {

    List<EipTMessageRoomMember> members =
      messageDbService.getOtherRoomMember(message.getRoomId(), username);

    List<String> recipients = new ArrayList<String>();
    for (EipTMessageRoomMember member : members) {
      recipients.add(member.getLoginName());
    }

    Map<String, String> params = new HashMap<String, String>();
    params.put("roomId", String.valueOf(message.getRoomId()));
    params.put("messageId", String.valueOf(message.getMessageId()));

    if (recipients.size() > 0) {
      pushService.pushAsync("messagev2", params, recipients);
    }
  }

}
