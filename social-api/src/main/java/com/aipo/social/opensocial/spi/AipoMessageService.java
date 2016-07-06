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
package com.aipo.social.opensocial.spi;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.aipo.orm.service.MessageDbService.IconSize;
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
import com.aipo.social.opensocial.spi.PushService.PushType;
import com.aipo.util.AipoToolkit;
import com.aipo.util.AipoToolkit.SystemUser;
import com.google.common.util.concurrent.Futures;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class AipoMessageService extends AbstractService implements
    MessageService {

  private final String messageCategoryKey;

  private final MessageDbService messageDbService;

  private final StorageService storageService;

  private final PushService pushService;

  private final String alias;

  /**
   *
   */
  @Inject
  public AipoMessageService(MessageDbService messageDbService,
      StorageService storageService, PushService pushService,
      @Named("aipo.message.categorykey") String messageCategoryKey,
      @Named("aipo.alias") String alias) {
    this.messageDbService = messageDbService;
    this.storageService = storageService;
    this.pushService = pushService;
    this.messageCategoryKey = messageCategoryKey;
    this.alias = alias;
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
      SecurityToken token) throws ProtocolException {

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
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ACCESS_DENIED);
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
      SystemUser systemUser = AipoToolkit.getSystemUser(model.getLoginName());
      if (systemUser != null) {
        room.setName(alias);
      } else {
        room.setName(model.getLastName() + " " + model.getFirstName());
      }
      room.setUserId(orgId + ":" + model.getLoginName());
    } else {
      room.setName(model.getName());
    }
    room.setUnreadCount(model.getUnreadCount());
    room.setIsDirect("O".equals(model.getRoomType()));
    room.setHasPhoto(room.getIsDirect()
      ? ("T".equals(model.getUserHasPhoto()) || "N".equals(model
        .getUserHasPhoto()))
      : ("T".equals(model.getHasPhoto()) || "N".equals(model.getHasPhoto())));
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

    List<String> adminMembers = new ArrayList<String>();
    for (String adminMember : model.getRoomAdminMembers()) {
      adminMembers.add(orgId + ":" + adminMember);
    }
    room.setAdminMembers(adminMembers);

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
  public Future<ALMessageRoom> postRoom(UserId userId, String name,
      List<String> memberList, List<String> memberAdminsList,
      SecurityToken token) throws ProtocolException {

    setUp(token);

    List<String> memberNameList = new ArrayList<String>();
    Map<String, String> memberAuthorityMap = new HashMap<String, String>();
    for (int i = 0; i < memberList.size(); i++) {
      String memberId = memberList.get(i);
      if (!"".equals(memberId)) {
        String memberName = getUserId(memberId, token);
        memberNameList.add(memberName);
        memberAuthorityMap.put(
          memberName,
          MessageDbService.AUTHORITY_TYPE_MEMBER);
      }
    }
    for (int i = 0; i < memberAdminsList.size(); i++) {
      String memberId = memberAdminsList.get(i);
      if (!"".equals(memberId)) {
        String memberName = getUserId(memberId, token);
        memberAuthorityMap.put(
          memberName,
          MessageDbService.AUTHORITY_TYPE_ADMIN);
      }
    }

    checkSameViewer(userId, token);
    String username = getUserId(userId, token);

    // 管理者のユーザーIDの一覧(member_admins)を省略した場合は作成者を管理者に設定
    if (memberAdminsList.size() == 0) {
      memberAuthorityMap.put(username, MessageDbService.AUTHORITY_TYPE_ADMIN);
    }

    // 管理者のユーザーIDはルームメンバーに指定したユーザーIDの中から指定
    for (String admin : memberAdminsList) {
      if (!memberList.contains(admin)) {
        throw new AipoProtocolException(AipoErrorCode.VALIDATE_ERROR
          .customMessage("Parameter member_to should contain member_admins."));
      }
    }

    // 自分(Viewer)を含むルームのみ作成可能
    if (!memberNameList.contains(username)) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ERROR
        .customMessage("Parameter member_to should contain userId."));
    } else {
      // 作成者は当初は「管理者」である必要がある
      if (memberAuthorityMap.get(username) != "A") {
        throw new AipoProtocolException(
          AipoErrorCode.VALIDATE_ERROR
            .customMessage("People who create a room must be Administrator of the room at first."));
      }
    }

    EipTMessageRoom model = null;
    if (memberNameList.size() != 0) {
      // ルーム
      model =
        messageDbService.createRoom(
          username,
          name,
          memberNameList,
          memberAuthorityMap);
    } else {
      // ダイレクトメッセージ
    }

    ALMessageRoom result = new ALMessageRoomImpl();
    Set<String> dummy = new HashSet<String>();
    if (model != null && model.getRoomId() != null) {
      result = assignMessageRoom(model, dummy, token, model.getRoomId());
    }

    return ImmediateFuture.newInstance(result);

  }

  /**
   * @param userId
   * @param roomId
   * @param token
   * @return
   * @throws ProtocolException
   */
  @Override
  public Future<Void> deleteRoom(UserId userId, int roomId, SecurityToken token)
      throws ProtocolException {

    setUp(token);

    checkSameViewer(userId, token);
    checkSameRoomAdmin(userId, token, roomId);

    List<EipTMessageFile> files =
      messageDbService.getMessageFilesByRoom(roomId);
    if (files != null && files.size() > 0) {
      storageService.deleteFiles(messageCategoryKey, files, token);
    }
    messageDbService.deleteRoom(roomId);

    return ImmediateFuture.newInstance(null);
  }

  /**
   *
   * @param userId
   * @param collectionOptions
   * @param fields
   * @param roomIdOrUsername
   * @param messageId
   * @param token
   * @return
   */
  @Override
  public Future<RestfulCollection<ALMessage>> getMessages(UserId userId,
      AipoCollectionOptions collectionOptions, Set<String> fields,
      String roomIdOrUsername, int messageId, SecurityToken token)
      throws ProtocolException {

    // TODO: FIELDS

    setUp(token);

    Integer roomId = null;
    String targetUsername = null;

    try {
      // Room
      roomId = Integer.valueOf(roomIdOrUsername);
    } catch (Throwable ignore) {
      // ダイレクト
      targetUsername = getUserId(roomIdOrUsername, token);
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
    if (roomId != null) {
      room = messageDbService.findRoom(roomId, username);
    } else {
      room = messageDbService.findRoom(username, targetUsername);
    }
    if (room == null) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ACCESS_DENIED);
    }

    // /messages/rooms/\(userId)/\(groupId)
    // {userId} が所属しているルームを取得

    // ルーム
    List<EipTMessage> list =
      messageDbService.findMessage(room.getRoomId(), messageId, options);

    List<ALMessage> result = new ArrayList<ALMessage>(list.size());
    for (EipTMessage message : list) {
      result.add(assignMessage(message, room, token, messageId));
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
      SecurityToken token, int messageId) {
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

    if (messageId == 0) {
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
   * @param roomIdOrUsername
   * @param message
   * @param token
   * @return
   */
  @Override
  public Future<ALMessage> postMessage(UserId userId, String roomIdOrUsername,
      String message, String transactionId, SecurityToken token,
      FormDataItem file) throws ProtocolException {

    setUp(token);

    Integer roomId = null;
    String targetUsername = null;

    try {
      // Room
      roomId = Integer.valueOf(roomIdOrUsername);
    } catch (Throwable ignore) {
      // ダイレクト
      targetUsername = getUserId(roomIdOrUsername, token);
    }

    // 自分(Viewer)のルームのみ取得可能
    checkSameViewer(userId, token);
    String username = getUserId(userId, token);

    EipTMessageRoom room = null;
    if (roomId != null) {
      room = messageDbService.findRoom(roomId, username);
      if (room == null) {
        throw new AipoProtocolException(AipoErrorCode.VALIDATE_ACCESS_DENIED);
      }
    } else {
      room = messageDbService.findRoom(username, targetUsername);
    }

    EipTMessage model =
      messageDbService.createMessage(username, roomId, targetUsername, message);

    if (file != null) {
      byte[] shrinkImage =
        getBytesShrink(
          file,
          DEF_THUMBNAIL_WIDTH,
          DEF_THUMBNAIL_HEIGHT,
          true,
          0,
          0).getShrinkImage();

      EipTMessageFile messageFile =
        messageDbService.insertMessageFiles(
          username,
          model.getMessageId(),
          file.getName(),
          shrinkImage);

      storageService.createNewFile(
        new ByteArrayInputStream(file.get()),
        messageCategoryKey,
        messageFile,
        token);
    }

    push(
      PushType.MESSAGE,
      username,
      model.getRoomId(),
      model.getMessageId(),
      transactionId);

    if (room == null) {
      room = messageDbService.findRoom(username, targetUsername);
    }

    ALMessage result = new ALMessageImpl();
    result = assignMessage(model, room, token, model.getMessageId());
    result.setTransactionId(transactionId);

    return ImmediateFuture.newInstance(result);
  }

  /**
   * @param userId
   * @param roomId
   * @param messageId
   * @param token
   * @return
   * @throws ProtocolException
   */
  @Override
  public Future<Void> deleteMessage(UserId userId, String roomIdOrUsername,
      int messageId, SecurityToken token) throws ProtocolException {

    setUp(token);

    checkSameViewer(userId, token);
    String username = getUserId(userId, token);

    Integer roomId = null;
    String targetUsername = null;

    try {
      // Room
      roomId = Integer.valueOf(roomIdOrUsername);
    } catch (Throwable ignore) {
      // ダイレクト
      targetUsername = getUserId(roomIdOrUsername, token);
    }

    EipTMessageRoom room = null;
    if (roomId != null) {
      room = messageDbService.findRoom(roomId, username);
    } else {
      room = messageDbService.findRoom(username, targetUsername);
    }
    if (room == null) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ACCESS_DENIED);
    }

    List<EipTMessage> list =
      messageDbService.findMessage(room.getRoomId(), messageId, SearchOptions
        .build());

    if (messageDbService.isOwnMessage(messageId, username)) {
      // 自分自身のメッセージは削除可能
      List<EipTMessageFile> files =
        messageDbService.getMessageFiles(Arrays.asList(messageId));
      storageService.deleteFiles(messageCategoryKey, files, token);
      messageDbService.deleteMessage(messageId);
    } else {
      // 管理者権限を持つユーザーは削除可能（ダイレクトメッセージ以外）
      // 管理者権限があればシステム投稿を削除可能
      if ("O".equals(room.getRoomType())
        && AipoToolkit.getSystemUser(targetUsername) == null) {
        throw new AipoProtocolException(AipoErrorCode.VALIDATE_ACCESS_DENIED);
      } else {
        checkSameRoomAdmin(userId, token, room.getRoomId());
        List<EipTMessageFile> files =
          messageDbService.getMessageFiles(Arrays.asList(messageId));
        storageService.deleteFiles(messageCategoryKey, files, token);
        messageDbService.deleteMessage(messageId);
      }
    }

    if (room != null) {
      push(PushType.MESSAGE_DELETE, username, room.getRoomId(), messageId);
    }

    return Futures.immediateFuture(null);
  }

  /**
   * @param userId
   * @param roomIdOrUsername
   * @param messageId
   * @param token
   * @return
   * @throws ProtocolException
   */
  @Override
  public Future<Void> read(UserId userId, String roomIdOrUsername,
      int messageId, SecurityToken token) throws ProtocolException {
    setUp(token);

    Integer roomId = null;
    String targetUsername = null;

    try {
      // Room
      roomId = Integer.valueOf(roomIdOrUsername);
    } catch (Throwable ignore) {
      // ダイレクト
      targetUsername = getUserId(roomIdOrUsername, token);
    }

    // 自分(Viewer)のルームのみ取得可能
    checkSameViewer(userId, token);
    String username = getUserId(userId, token);

    EipTMessageRoom room = null;
    boolean updated = false;
    if (roomId != null) {
      room = messageDbService.findRoom(roomId, username);
      if (room == null) {
        throw new AipoProtocolException(AipoErrorCode.VALIDATE_ACCESS_DENIED);
      }
      updated = messageDbService.read(username, roomId, messageId);
    } else {
      room = messageDbService.findRoom(username, targetUsername);
      if (room != null) {
        updated = messageDbService.read(username, targetUsername, messageId);
      }
    }

    if (updated && room != null) {
      push(PushType.MESSAGE_READ, username, room.getRoomId());
    }

    return Futures.immediateFuture(null);
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
      List<String> memberList, List<String> memberAdminsList, String roomId,
      String desktopNotification, String mobileNotification, SecurityToken token)
      throws ProtocolException {

    setUp(token);

    Integer roomIdInt = null;

    // Room
    try {
      roomIdInt = Integer.valueOf(roomId);
    } catch (Throwable ignore) {
      //
    }

    if (roomIdInt == null) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ERROR
        .customMessage("Parameter roomId required."));
    }

    List<String> memberNameList = new ArrayList<String>();
    Map<String, String> memberAuthorityMap = new HashMap<String, String>();

    for (int i = 0; i < memberList.size(); i++) {
      String memberId = memberList.get(i);
      if (!"".equals(memberId)) {
        String memberName = getUserId(memberId, token);
        memberNameList.add(memberName);
        memberAuthorityMap.put(
          memberName,
          MessageDbService.AUTHORITY_TYPE_MEMBER);
      }
    }
    for (int i = 0; i < memberAdminsList.size(); i++) {
      String memberId = memberAdminsList.get(i);
      if (!"".equals(memberId)) {
        String memberName = getUserId(memberId, token);
        memberAuthorityMap.put(
          memberName,
          MessageDbService.AUTHORITY_TYPE_ADMIN);
      }
    }

    checkSameViewer(userId, token);
    checkSameRoomAdmin(userId, token, roomIdInt);
    String username = getUserId(userId, token);

    // 管理者のユーザーIDはルームメンバーに指定したユーザーIDの中から指定
    for (String admin : memberAdminsList) {
      if (!memberList.contains(admin)) {
        throw new AipoProtocolException(AipoErrorCode.VALIDATE_ERROR
          .customMessage("Parameter member_to should contain member_admins."));
      }
    }

    // 管理者のユーザーIDの一覧(member_admins)を省略した場合は、元々いるユーザーの管理者権限を変更しない
    if (memberAdminsList.size() == 0) {
      List<EipTMessageRoomMember> members =
        messageDbService.getRoomMember(roomIdInt, username);
      for (EipTMessageRoomMember member : members) {
        memberAuthorityMap.put(member.getLoginName(), member.getAuthority());
      }
    }
    // 自分(Viewer)を含むルームのみ作成可能
    if (!memberNameList.contains(username)) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ERROR
        .customMessage("Parameter member_to should contain userId."));
    }

    EipTMessageRoom model = null;
    if (roomIdInt != null && memberNameList.size() != 0) {
      // ルーム
      model =
        messageDbService.updateRoom(
          roomIdInt,
          username,
          name,
          desktopNotification,
          mobileNotification,
          memberNameList,
          memberAuthorityMap);
    } else {
      // ダイレクトメッセージ
    }

    ALMessageRoom result = new ALMessageRoomImpl();
    Set<String> dummy = new HashSet<String>();
    if (roomIdInt != null) {
      result = assignMessageRoom(model, dummy, token, roomIdInt);
    }

    return ImmediateFuture.newInstance(result);
  }

  /**
   * @param roomId
   * @param token
   * @return
   * @throws ProtocolException
   */
  @Override
  public InputStream getRoomIcon(UserId userId, int roomId, String size,
      SecurityToken token) throws ProtocolException {

    setUp(token);

    checkSameViewer(userId, token);
    checkSameRoomMember(userId, token, roomId);

    IconSize iconSize = IconSize.NORMAL;
    if ("large".equals(size)) {
      iconSize = IconSize.LARGE;
    }

    InputStream roomIcon = messageDbService.getPhoto(roomId, iconSize);
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
  public Future<Void> putRoomIcon(UserId userId, int roomId,
      FormDataItem roomIconItem, SecurityToken token) throws ProtocolException {

    setUp(token);

    checkSameViewer(userId, token);
    checkSameRoomAdmin(userId, token, roomId);

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

    messageDbService.setPhoto(roomId, roomIcon, roomIconSmartPhone);

    return Futures.immediateFuture(null);
  }

  /**
   * @param userId
   * @param roomId
   * @param token
   * @return
   */
  @Override
  public Future<Void> deleteRoomIcon(UserId userId, int roomId,
      SecurityToken token) throws ProtocolException {

    Integer roomIdInt = null;
    try {
      roomIdInt = Integer.valueOf(roomId);
    } catch (Throwable t) {

    }
    if (roomIdInt == null) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ACCESS_DENIED);
    }

    setUp(token);
    checkSameViewer(userId, token);
    checkSameRoomAdmin(userId, token, roomId);

    messageDbService.setPhoto(roomIdInt.intValue(), null, null);

    return Futures.immediateFuture(null);
  }

  /**
   * @param userId
   * @param messageIdInt
   * @param token
   * @return
   */
  @Override
  public Future<?> putMessageFiles(UserId userId, int messageIdInt,
      FormDataItem file, SecurityToken token) {
    setUp(token);

    checkSameViewer(userId, token);

    String username = getUserId(userId, token);

    byte[] shrinkImage =
      getBytesShrink(
        file,
        DEF_THUMBNAIL_WIDTH,
        DEF_THUMBNAIL_HEIGHT,
        true,
        0,
        0).getShrinkImage();

    EipTMessageFile messageFile =
      messageDbService.insertMessageFiles(username, messageIdInt, file
        .getName(), shrinkImage);

    storageService.createNewFile(
      new ByteArrayInputStream(file.get()),
      messageCategoryKey,
      messageFile,
      token);

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
  public Future<ALFile> getMessageFiles(UserId userId, int fileId,
      SecurityToken token) {

    setUp(token);

    checkSameViewer(userId, token);
    EipTMessageFile model = messageDbService.findMessageFile(fileId);
    if (model == null) {
      throw new AipoProtocolException(AipoErrorCode.FILE_NOT_FOUND);
    }
    checkSameRoomMember(userId, token, model.getRoomId());

    ALFile result = assignFile(model);

    return ImmediateFuture.newInstance(result);
  }

  @Override
  public Future<ALFile> getMessageFilesInfo(UserId userId, int fileId,
      SecurityToken token) {

    setUp(token);

    checkSameViewer(userId, token);
    EipTMessageFile model = messageDbService.findMessageFile(fileId);
    if (model == null) {
      throw new AipoProtocolException(AipoErrorCode.FILE_NOT_FOUND);
    }
    checkSameRoomMember(userId, token, model.getRoomId());

    long fileSize =
      storageService.getFileSize(messageCategoryKey, model
        .getOwnerId()
        .intValue(), model.getFilePath(), token);

    ALFile result = assignFileInfo(model, fileSize);

    return ImmediateFuture.newInstance(result);
  }

  /**
   * @param userId
   * @param fileId
   * @param token
   * @return
   */
  @Override
  public InputStream getMessageFilesThumbnail(UserId userId, int fileId,
      SecurityToken token) {
    setUp(token);

    setUp(token);

    checkSameViewer(userId, token);
    EipTMessageFile model = messageDbService.findMessageFile(fileId);
    if (model == null) {
      throw new AipoProtocolException(AipoErrorCode.FILE_NOT_FOUND);
    }
    checkSameRoomMember(userId, token, model.getRoomId());

    byte[] thumbnail = null;
    thumbnail = model.getFileThumbnail();
    if (thumbnail == null) {
      throw new AipoProtocolException(AipoErrorCode.FILE_NOT_FOUND);
    }

    return new ByteArrayInputStream(thumbnail);
  }

  /**
   *
   * @param model
   * @return
   */
  private ALFile assignFile(EipTMessageFile model) {
    ALFile file = new ALFileImpl();

    file.setFileId(model.getFileId());
    file.setFileName(model.getFileName());
    file.setFilePath(model.getFilePath());
    file.setCategoryKey(messageCategoryKey);
    file.setUserId(model.getOwnerId());

    return file;
  }

  /**
   *
   * @param model
   * @param fileSize
   * @return
   */
  private ALFile assignFileInfo(EipTMessageFile model, long fileSize) {
    ALFile file = new ALFileImpl();

    file.setFileId(model.getFileId());
    file.setFileName(model.getFileName());
    file.setFileSize(fileSize);

    return file;
  }

  /**
   *
   * @param userId
   * @param token
   * @throws ProtocolException
   */
  protected void checkSameRoomMember(UserId userId, SecurityToken token,
      int roomId) throws ProtocolException {

    String username = getUserId(userId, token);
    if (username != null && !"".equals(username)) {
      boolean isJoinRoom = messageDbService.isJoinRoom(roomId, username);
      if (!isJoinRoom) {
        throw new AipoProtocolException(AipoErrorCode.VALIDATE_ACCESS_DENIED);
      }
    }
  }

  /**
   *
   * @param userId
   * @param token
   * @throws ProtocolException
   */
  protected void checkSameRoomAdmin(UserId userId, SecurityToken token,
      int roomId) throws ProtocolException {

    String username = getUserId(userId, token);
    if (username != null && !"".equals(username)) {
      boolean hasAuthority =
        messageDbService.hasAuthorityRoom(roomId, username);
      if (!hasAuthority) {
        throw new AipoProtocolException(AipoErrorCode.VALIDATE_ACCESS_DENIED);
      }
    }
  }

  protected void push(PushType type, String username, int roomId)
      throws ProtocolException {
    push(type, username, roomId, 0);
  }

  protected void push(PushType type, String username, int roomId, int messageId)
      throws ProtocolException {
    push(type, username, roomId, messageId, "");
  }

  /**
   *
   * @param type
   * @param username
   * @param roomId
   * @param messageId
   * @throws ProtocolException
   */
  protected void push(PushType type, String username, int roomId,
      int messageId, String transactionId) throws ProtocolException {

    List<EipTMessageRoomMember> members =
      messageDbService.getRoomMember(roomId, username);

    List<String> recipients = new ArrayList<String>();
    for (EipTMessageRoomMember member : members) {
      recipients.add(member.getLoginName());
    }

    Map<String, String> params = new HashMap<String, String>();
    params.put("roomId", String.valueOf(roomId));
    if (messageId > 0) {
      params.put("messageId", String.valueOf(messageId));
    }
    if (!"".equals(transactionId)) {
      params.put("transactionId", transactionId);
    }
    params.put("userId", username);

    if (recipients.size() > 0) {
      pushService.pushAsync(type, params, recipients);
    }
  }

}
