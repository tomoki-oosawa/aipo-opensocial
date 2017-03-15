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
package com.aipo.orm.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.cayenne.DataRow;

import com.aipo.orm.Database;
import com.aipo.orm.model.portlet.EipTMessage;
import com.aipo.orm.model.portlet.EipTMessageFile;
import com.aipo.orm.model.portlet.EipTMessageRead;
import com.aipo.orm.model.portlet.EipTMessageRoom;
import com.aipo.orm.model.portlet.EipTMessageRoomMember;
import com.aipo.orm.model.security.TurbineUser;
import com.aipo.orm.query.Operations;
import com.aipo.orm.query.SQLTemplate;
import com.aipo.orm.query.SelectQuery;
import com.aipo.orm.service.request.SearchOptions;
import com.aipo.orm.service.request.SearchOptions.SortOrder;
import com.aipo.util.AipoToolkit;
import com.aipo.util.AipoToolkit.SystemUser;
import com.aipo.util.CommonUtils;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 *
 */
public class AipoMessageDbService implements MessageDbService {

  private final TurbineUserDbService turbineUserDbService;

  private final String alias;

  @Inject
  public AipoMessageDbService(TurbineUserDbService turbineUserDbService,
      @Named("aipo.alias") String alias) {
    this.turbineUserDbService = turbineUserDbService;
    this.alias = alias;
  }

  @Override
  public EipTMessageRoom findRoom(String username, String targetUsername) {
    List<EipTMessageRoom> rooms = findRoom(
      0,
      username,
      targetUsername,
      SearchOptions.build());
    if (rooms != null && rooms.size() > 0) {
      return rooms.get(0);
    }
    return null;
  }

  @Override
  public EipTMessageRoom findRoom(int roomId, String username) {
    List<EipTMessageRoom> rooms = findRoom(
      roomId,
      username,
      null,
      SearchOptions.build());
    if (rooms != null && rooms.size() > 0) {
      return rooms.get(0);
    }
    return null;
  }

  @Override
  public List<EipTMessageRoom> findRoom(String username, SearchOptions options) {
    return findRoom(0, username, null, options);
  }

  /**
   *
   * @param roomId
   * @param username
   * @param targetUsername
   * @param options
   * @return
   */
  @Override
  public List<EipTMessageRoom> findRoom(int roomId, String username,
      String targetUsername, SearchOptions options) {
    Integer userId = null;
    TurbineUser turbineUser = turbineUserDbService.findByUsername(username);
    if (turbineUser == null) {
      SystemUser systemUser = AipoToolkit.getSystemUser(username);
      if (systemUser != null) {
        userId = systemUser.getUserId();
      }
    } else {
      userId = turbineUser.getUserId();
    }
    if (userId == null) {
      return new ArrayList<EipTMessageRoom>();
    }
    if (targetUsername != null) {
      Integer targetUserId = null;
      TurbineUser targetTurbineUser = turbineUserDbService
        .findByUsername(targetUsername);
      if (targetTurbineUser == null) {
        SystemUser systemUser = AipoToolkit.getSystemUser(targetUsername);
        if (systemUser != null) {
          targetUserId = systemUser.getUserId();
        }
      } else {
        targetUserId = targetTurbineUser.getUserId();
      }
      if (targetUserId == null) {
        return new ArrayList<EipTMessageRoom>();
      }
      EipTMessageRoom room = getRoom(userId, targetUserId);
      if (room != null) {
        roomId = room.getRoomId();
      } else {
        return new ArrayList<EipTMessageRoom>();
      }
    }

    // Filter
    String filter = options.getFilterBy();
    // FilterOperation filterOperation = options.getFilterOperation();
    String filterValue = options.getFilterValue();
    boolean isFilter = false;

    // MessageUtils.getRoomListと同等の処理（pageの制約はなし）に必要な変数を取得
    // int limit = options.getLimit();
    boolean isMySQL = !Database.isJdbcPostgreSQL();

    StringBuilder select = new StringBuilder();

    select.append("select");
    select.append(" t2.room_id, ");
    select.append(" t2.name, ");
    select.append(" t2.has_photo, ");
    select.append(" t2.photo_modified, ");
    select.append(" t4.user_id, ");
    select.append(" t4.login_name, ");
    select.append(" t4.last_name, ");
    select.append(" t4.first_name, ");
    select.append(" t4.has_photo as user_has_photo, ");
    select.append(" t4.photo_modified as user_photo_modified, ");
    select.append(" t2.auto_name, ");
    select.append(" t2.room_type, ");

    select.append(" t2.last_message, ");
    select.append(" last_update_date, ");
    select
      .append(" (select message_id from eip_t_message t4 where t4.room_id = t2.room_id order by t4.create_date desc limit 1) as last_message_id, ");
    select
      .append(" (select count(*) from eip_t_message_read t3 where t3.room_id = t2.room_id and t3.user_id = #bind($user_id) and t3.is_read ='F') as unread ");

    StringBuilder count = new StringBuilder();
    count.append("select count(t2.room_id) AS c ");

    StringBuilder body = new StringBuilder();
    body
      .append("  from eip_t_message_room_member t1, eip_t_message_room t2, turbine_user t4 where t1.user_id = #bind($user_id) and t1.room_id = t2.room_id and t1.target_user_id = t4.user_id ");

    if (roomId > 0) {
      body.append(" and t1.room_id=");
      body.append(roomId);
    }

    if ("keyword".equals(filter)) {
      if (isMySQL) {
        body
          .append(" and ((t2.room_type='G' and t2.name like #bind($keyword)) or (t2.room_type='O' and CONCAT(t4.last_name,t4.first_name) like #bind($keyword))) ");
      } else {
        body
          .append(" and ((t2.room_type='G' and t2.name like #bind($keyword)) or (t2.room_type='O' and (t4.last_name || t4.first_name) like #bind($keyword))) ");
      }
      isFilter = true;
    }

    StringBuilder last = new StringBuilder();

    last.append(" order by t2.last_update_date desc ");
    /*-
    if (limit > 0) {
      last.append(" LIMIT ");
      last.append(limit);
    }
     */

    SQLTemplate<EipTMessageRoom> sql = Database.sql(
      EipTMessageRoom.class,
      select.toString() + body.toString() + last.toString()).param(
      "user_id",
      Integer.valueOf(userId));
    if (isFilter) {
      sql.param("keyword", "%" + filterValue + "%");
    }
    List<DataRow> fetchList = sql.fetchListAsDataRow();

    List<EipTMessageRoomMember> roomMembers = null;
    if (roomId > 0) {
      roomMembers = Database
        .sql(
          EipTMessageRoomMember.class,
          "select login_name, authority, mobile_notification from eip_t_message_room_member where room_id=#bind($room_id)")
        .param("room_id", Integer.valueOf(roomId))
        .fetchList();
    }

    List<EipTMessageRoom> list = new ArrayList<EipTMessageRoom>();
    for (DataRow row : fetchList) {
      Long unread = (Long) row.get("unread");
      Integer tUserId = (Integer) row.get("user_id");
      String loginName = (String) row.get("login_name");
      String lastName = (String) row.get("last_name");
      String firstName = (String) row.get("first_name");
      String userHasPhoto = (String) row.get("user_has_photo");
      Date userPhotoModified = (Date) row.get("user_photo_modified");
      Long lastMessageId = null;
      try {
        lastMessageId = (Long) row.get("last_message_id");
      } catch (ClassCastException ignore) {
        Integer lastMessageIdInt = (Integer) row.get("last_message_id");
        if (lastMessageIdInt != null) {
          lastMessageId = lastMessageIdInt.longValue();
        }
      }

      EipTMessageRoom object = Database.objectFromRowData(
        row,
        EipTMessageRoom.class);
      object.setUnreadCount(unread.intValue());
      object.setUserId(tUserId);
      object.setLoginName(loginName);
      object.setFirstName(firstName);
      object.setLastName(lastName);
      if (roomId > 0 && roomMembers != null) {
        List<String> roomMembersStr = new ArrayList<String>();
        List<String> roomAdminMembersStr = new ArrayList<String>();
        List<String> roomMobileNotificationMembersStr = new ArrayList<String>();
        for (EipTMessageRoomMember roomMember : roomMembers) {
          roomMembersStr.add(roomMember.getLoginName());
          if ("A".equals(roomMember.getAuthority())) {
            roomAdminMembersStr.add(roomMember.getLoginName());
          }
          if ("A".equals(roomMember.getMobileNotification())) {
            roomMobileNotificationMembersStr.add(roomMember.getLoginName());
          }
        }
        object.setRoomMembers(roomMembersStr);
        object.setRoomAdminMembers(roomAdminMembersStr);
        object
          .setRoomMobileNotificationMembers(roomMobileNotificationMembersStr);
      }
      if (lastMessageId != null && lastMessageId.longValue() > 0) {
        object.setLastMessageId(lastMessageId.intValue());
      }
      object.setUserHasPhoto(userHasPhoto);
      if (userPhotoModified != null) {
        object.setUserPhotoModified(userPhotoModified);
      }
      list.add(object);
    }

    return list;

  }

  /**
   * @param roomId
   * @param messageId
   * @param options
   * @return
   */
  @Override
  public List<EipTMessage> findMessage(int roomId, int messageId,
      SearchOptions options) {
    int limit = options.getLimit();
    Integer untilId = options.getParameterInt("until_id");
    Integer sinceId = options.getParameterInt("since_id");
    boolean isReverse = false;
    if (sinceId != null && sinceId > 0) {
      isReverse = true;
    }

    StringBuilder select = new StringBuilder();

    select.append("select");
    select.append(" t1.message_id, ");
    select.append(" t1.room_id,  ");
    select.append(" t1.user_id, ");
    select.append(" t1.message, ");
    select.append(" t1.create_date, ");
    select.append(" t1.member_count, ");
    select.append(" t2.login_name, ");
    select.append(" t2.last_name, ");
    select.append(" t2.first_name, ");
    select.append(" t2.has_photo, ");
    select.append(" t2.photo_modified, ");

    select
      .append(" (select count(*) from eip_t_message_read t3 where t3.message_id = t1.message_id and t3.room_id = t1.room_id and t3.is_read = 'F') as unread ");

    StringBuilder count = new StringBuilder();
    count.append("select count(t1.message_id) AS c ");

    StringBuilder body = new StringBuilder();
    body
      .append("  from eip_t_message t1, turbine_user t2 where t1.user_id = t2.user_id and t1.room_id = #bind($room_id) ");
    if (untilId != null && untilId > 0) {
      body.append(" and t1.message_id<");
      body.append(untilId);
    }
    if (sinceId != null && sinceId > 0) {
      body.append(" and t1.message_id>");
      body.append(sinceId);
    }
    if (messageId > 0) {
      body.append(" and t1.message_id=");
      body.append(messageId);
    }

    StringBuilder last = new StringBuilder();

    if (isReverse) {
      last.append(" order by t1.create_date asc ");
    } else {
      last.append(" order by t1.create_date desc ");
    }

    if (limit > 0) {
      last.append(" limit ");
      last.append(limit);
    }

    List<DataRow> fetchList = Database.sql(
      EipTMessage.class,
      select.toString() + body.toString() + last.toString()).param(
      "room_id",
      Integer.valueOf(roomId)).fetchListAsDataRow();

    List<Integer> messageIds = new ArrayList<Integer>();
    List<EipTMessage> list = new ArrayList<EipTMessage>();
    for (DataRow row : fetchList) {
      Long unread = (Long) row.get("unread");
      String loginName = (String) row.get("login_name");
      String lastName = (String) row.get("last_name");
      String firstName = (String) row.get("first_name");
      String hasPhoto = (String) row.get("has_photo");
      Date photoModified = (Date) row.get("photo_modified");

      EipTMessage object = Database.objectFromRowData(row, EipTMessage.class);
      object.setUnreadCount(unread.intValue());
      object.setLoginName(loginName);
      object.setFirstName(firstName);
      object.setLastName(lastName);
      object.setHasPhoto(hasPhoto);
      object.setRoomId(roomId);

      if (messageId > 0) {
        List<String> readMembers = new ArrayList<String>();
        List<TurbineUser> tusers = getReadUserList(messageId);
        for (TurbineUser tuser : tusers) {
          readMembers.add(tuser.getLoginName());
        }
        object.setReadMembers(readMembers);
      }

      if (photoModified != null) {
        object.setPhotoModified(photoModified.getTime());
      }
      list.add(object);
      messageIds.add(object.getMessageId());
    }

    List<EipTMessageFile> files = getMessageFiles(messageIds);
    if (files != null && files.size() > 0) {
      for (EipTMessage message : list) {
        ArrayList<EipTMessageFile> arrayList = new ArrayList<EipTMessageFile>();
        for (EipTMessageFile file : files) {
          if (message.getMessageId().equals(file.getMessageId())) {
            arrayList.add(file);
          }
        }
        if (arrayList != null && arrayList.size() > 0) {
          message.setMessageFiles(arrayList);
        }
      }
    }

    if (isReverse) {
      Collections.reverse(list);
    }
    return list;
  }

  /**
   * @param username
   * @param name
   * @param memberNameList
   * @param fields
   */
  @Override
  public EipTMessageRoom createRoom(String username, String name,
      List<String> memberNameList, Map<String, String> memberAuthorityMap) {
    try {
      TurbineUser turbineUser = turbineUserDbService.findByUsername(username);
      List<TurbineUser> memberList = turbineUserDbService
        .findByUsername(new HashSet<String>(memberNameList));
      Date now = new Date();

      EipTMessageRoom model = Database.create(EipTMessageRoom.class);

      boolean isFirst = true;
      StringBuilder autoName = new StringBuilder();
      for (TurbineUser user : memberList) {
        EipTMessageRoomMember map = Database
          .create(EipTMessageRoomMember.class);
        int userid = user.getUserId();
        map.setEipTMessageRoom(model);
        map.setTargetUserId(1);
        map.setUserId(Integer.valueOf(userid));
        map.setLoginName(user.getLoginName());
        map.setAuthority(memberAuthorityMap.get(user.getLoginName()));
        map.setDesktopNotification("A");
        map.setMobileNotification("A");
        if (!isFirst) {
          autoName.append(",");
        }
        autoName.append(new StringBuffer().append(user.getLastName()).append(
          " ").append(user.getFirstName()).toString());
        isFirst = false;
      }

      if (name == null || "".equals(name)) {
        model.setAutoName("T");
        model.setName(autoName.toString());
      } else {
        model.setAutoName("F");
        model.setName(CommonUtils.removeSpace(name));
      }

      model.setRoomType("G");
      model.setLastUpdateDate(now);
      model.setCreateDate(now);
      model.setCreateUserId((int) turbineUser.getUserId());
      model.setUpdateDate(now);

      // TODO:顔写真添付機能の追加
      // if (filebean != null && filebean.getFileId() != 0) {
      // model.setPhotoSmartphone(facePhoto_smartphone);
      // model.setPhoto(facePhoto);
      // model.setPhotoModified(new Date());
      // model.setHasPhoto("T");
      // } else {
      // model.setHasPhoto("F");
      // }

      Database.commit();

      List<String> roomMembersStr = new ArrayList<String>();
      List<String> roomAdminMembersStr = new ArrayList<String>();
      for (EipTMessageRoomMember roomMember : model.getEipTMessageRoomMember()) {
        if (!roomMembersStr.contains(roomMember.getLoginName())) {
          roomMembersStr.add(roomMember.getLoginName());
          if ("A".equals(roomMember.getAuthority())) {
            roomAdminMembersStr.add(roomMember.getLoginName());
          }
        }
      }
      model.setRoomMembers(roomMembersStr);
      model.setRoomAdminMembers(roomAdminMembersStr);

      return model;

    } catch (Throwable t) {
      Database.rollback();
      throw new RuntimeException(t);
    }
  }

  /**
   * @param roomId
   * @param targetUserId
   * @param message
   * @param fields
   * @return
   */
  @Override
  public EipTMessage createMessage(String username, Integer roomId,
      String targetUsername, String message) {
    try {
      TurbineUser turbineUser = turbineUserDbService.findByUsername(username);
      TurbineUser targetUser = turbineUserDbService
        .findByUsername(targetUsername);
      Date now = new Date();

      if (roomId == null && targetUsername != null) {
        int userId = turbineUser.getUserId().intValue();
        Integer targetUserId = null;
        if (targetUser != null) {
          targetUserId = targetUser.getUserId().intValue();
        } else {
          SystemUser systemUser = AipoToolkit.getSystemUser(targetUsername);
          if (systemUser != null) {
            targetUserId = systemUser.getUserId();
          }
        }
        if (targetUserId == null) {
          throw new Exception();
        }

        EipTMessageRoom room = getRoom(userId, targetUserId);
        if (room == null) {
          room = Database.create(EipTMessageRoom.class);

          EipTMessageRoomMember map1 = Database
            .create(EipTMessageRoomMember.class);
          map1.setEipTMessageRoom(room);
          map1.setUserId(userId);
          map1.setTargetUserId(targetUserId);
          map1.setLoginName(turbineUser.getLoginName());
          map1.setAuthority("A");
          map1.setDesktopNotification("A");
          map1.setMobileNotification("A");

          EipTMessageRoomMember map2 = Database
            .create(EipTMessageRoomMember.class);
          map2.setEipTMessageRoom(room);
          map2.setTargetUserId(userId);
          map2.setUserId(targetUserId);
          map2.setLoginName(targetUsername);
          map2.setAuthority("A");
          map2.setDesktopNotification("A");
          map2.setMobileNotification("A");

          room.setAutoName("T");
          room.setRoomType("O");
          room.setLastUpdateDate(now);
          room.setCreateDate(now);
          room.setCreateUserId(userId);
          room.setUpdateDate(now);

          Database.commit();

        }
        roomId = room.getRoomId();
      }

      if (roomId == null) {
        throw new Exception();
      }

      EipTMessageRoom room = Database.get(EipTMessageRoom.class, roomId
        .longValue());
      List<EipTMessageRoomMember> members = room.getEipTMessageRoomMember();

      EipTMessage model = Database.create(EipTMessage.class);
      model.setEipTMessageRoom(room);
      model.setMessage(CommonUtils.removeSpace(message));
      model.setCreateDate(now);
      model.setUpdateDate(now);
      model.setRoomId(roomId);

      model.setMemberCount(members.size());
      model.setUnreadCount(members.size() - 1);
      model.setUserId((int) turbineUser.getUserId());
      model.setLoginName(turbineUser.getLoginName());

      List<String> recipients = new ArrayList<String>();
      for (EipTMessageRoomMember member : members) {
        if (member.getUserId().intValue() != turbineUser.getUserId()) {
          EipTMessageRead record = Database.create(EipTMessageRead.class);
          record.setEipTMessage(model);
          record.setIsRead("F");
          record.setUserId(member.getUserId());
          record.setRoomId(room.getRoomId());
          recipients.add(member.getLoginName());
        }
      }

      room.setLastMessage(CommonUtils.compressString(message, 100));
      room.setLastUpdateDate(now);

      // TODO:ファイル添付機能の追加
      // insertAttachmentFiles(fileuploadList, folderName, (int) login_user
      // .getUserId()
      // .getValue(), model, msgList);

      Database.commit();

      return model;

    } catch (Throwable t) {
      Database.rollback();
      throw new RuntimeException(t);
    }
  }

  /**
   * @param roomId
   * @param username
   * @param name
   * @param memberNameList
   */
  @Override
  public EipTMessageRoom updateRoom(Integer roomId, String username,
      String name, List<String> memberNameList,
      Map<String, String> memberAuthorityMap) {
    try {
      TurbineUser turbineUser = turbineUserDbService.findByUsername(username);
      List<TurbineUser> memberList = turbineUserDbService
        .findByUsername(new HashSet<String>(memberNameList));
      Date now = new Date();

      EipTMessageRoom model = Database.get(EipTMessageRoom.class, roomId);

      if (model == null) {
        return null;
      }

      if (!isJoinRoom(model, turbineUser.getUserId())) {
        return null;
      }

      Map<String, String> memberDesktopNotificationMap = new HashMap<String, String>();
      Map<String, String> memberMobileNotificationMap = new HashMap<String, String>();
      List<EipTMessageRoomMember> tmpMemberList = model
        .getEipTMessageRoomMember();
      for (EipTMessageRoomMember tmpMember : tmpMemberList) {
        memberDesktopNotificationMap.put(tmpMember.getLoginName(), tmpMember
          .getDesktopNotification());
        memberMobileNotificationMap.put(tmpMember.getLoginName(), tmpMember
          .getMobileNotification());
      }

      Database.deleteAll(model.getEipTMessageRoomMember());

      boolean isFirst = true;
      StringBuilder autoName = new StringBuilder();
      for (TurbineUser user : memberList) {
        EipTMessageRoomMember map = Database
          .create(EipTMessageRoomMember.class);
        int userid = user.getUserId();
        map.setEipTMessageRoom(model);
        map.setTargetUserId(1);
        map.setUserId(Integer.valueOf(userid));
        map.setLoginName(user.getLoginName());
        map.setAuthority(memberAuthorityMap.get(user.getLoginName()));
        map.setDesktopNotification(memberDesktopNotificationMap.get(user
          .getLoginName()));
        map.setMobileNotification(memberMobileNotificationMap.get(user
          .getLoginName()));

        if (!isFirst) {
          autoName.append(",");
        }
        autoName.append(new StringBuffer().append(user.getLastName()).append(
          " ").append(user.getFirstName()).toString());
        isFirst = false;
      }

      if (name == null || "".equals(name)) {
        model.setAutoName("T");
        model.setName(autoName.toString());
      } else {
        model.setAutoName("F");
        model.setName(CommonUtils.removeSpace(name));
      }

      model.setRoomType("G");
      model.setLastUpdateDate(now);
      model.setCreateDate(now);
      model.setCreateUserId((int) turbineUser.getUserId());
      model.setUpdateDate(now);

      // if (filebean != null && filebean.getFileId() != 0) {
      // model.setPhotoSmartphone(facePhoto_smartphone);
      // model.setPhoto(facePhoto);
      // model.setPhotoModified(new Date());
      // model.setHasPhoto("T");
      // }
      //
      // if (filebean != null) {
      // if (filebean.getFileId() != 0) {
      // model.setPhoto(facePhoto);
      // model.setPhotoSmartphone(facePhoto_smartphone);
      // model.setPhotoModified(new Date());
      // model.setHasPhoto("T");
      // }
      // } else {
      // model.setPhoto(null);
      // model.setPhotoSmartphone(null);
      // model.setPhotoModified(null);
      // model.setHasPhoto("F");
      // }

      Database.commit();

      List<String> roomMembersStr = new ArrayList<String>();
      List<String> roomAdminMembersStr = new ArrayList<String>();
      for (EipTMessageRoomMember roomMember : model.getEipTMessageRoomMember()) {
        if (!roomMembersStr.contains(roomMember.getLoginName())) {
          roomMembersStr.add(roomMember.getLoginName());
          if ("A".equals(roomMember.getAuthority())) {
            roomAdminMembersStr.add(roomMember.getLoginName());
          }
        }
      }
      model.setRoomMembers(roomMembersStr);
      model.setRoomAdminMembers(roomAdminMembersStr);
      return model;

    } catch (Throwable t) {
      Database.rollback();
      throw new RuntimeException(t);
    }
  }

  /**
   * @param roomId
   * @param deleteMessageId
   * @return
   */
  @Override
  public EipTMessageRoom updateRoomLastMessage(Integer roomId,
      Integer deleteMessageId) {
    SearchOptions options = SearchOptions.build().withSort(
      EipTMessage.MESSAGE_ID_PK_COLUMN,
      SortOrder.descending).withLimit(2);
    List<EipTMessage> messages = findMessage(roomId, 0, options);

    if (messages != null && messages.size() > 0) {
      int lastMessageId = messages.get(0).getMessageId();
      Date now = new Date();
      EipTMessageRoom updateRoom = Database.get(EipTMessageRoom.class, roomId);
      if (deleteMessageId == lastMessageId) {
        if (updateRoom == null) {
          return null;
        }
        if (messages.size() > 1) {
          EipTMessage secondLastMessage = messages.get(1);
          updateRoom.setLastMessage(CommonUtils.compressString(
            secondLastMessage.getMessage(),
            100));
          updateRoom.setLastUpdateDate(now);
        } else {
          // メッセージが一つの場合lastMessageにnull
          updateRoom.setLastMessage(CommonUtils.compressString(null, 100));
          updateRoom.setLastUpdateDate(now);
        }
        Database.commit();
      }
      return updateRoom;
    }
    return null;
  }

  /**
   * @param roomId
   */
  @Override
  public void deleteRoom(int roomId) {
    try {
      EipTMessageRoom room = Database.get(EipTMessageRoom.class, roomId);
      if (room == null) {
        return;
      }

      List<EipTMessageFile> files = getMessageFilesByRoom(roomId);

      // messageを削除
      Database.delete(room);
      if (files.size() > 0) {
        // messageの添付ファイルを削除
        Database.deleteAll(files);
      }

      Database.commit();
    } catch (Throwable t) {
      Database.rollback();
      throw new RuntimeException(t);
    }
  }

  /**
   * @param messageId
   */
  @Override
  public void deleteMessage(int messageId) {
    try {
      EipTMessage message = Database.get(EipTMessage.class, messageId);
      if (message == null) {
        return;
      }

      List<EipTMessageFile> files = getMessageFiles(Arrays.asList(messageId));

      // messageを削除
      Database.delete(message);
      if (files.size() > 0) {
        // messageの添付ファイルを削除
        Database.deleteAll(files);
      }

      Database.commit();
    } catch (Throwable t) {
      Database.rollback();
      throw new RuntimeException(t);
    }
  }

  protected List<TurbineUser> getReadUserList(int messageId) {
    StringBuilder sql = new StringBuilder();
    sql
      .append("select t1.user_id, t1.login_name, t1.last_name, t1.first_name, t1.has_photo, t1.photo_modified from turbine_user t1, eip_t_message_read t2 where t1.user_id = t2.user_id and t2.message_id = #bind($message_id) and t2.is_read = 'T';");

    SQLTemplate<TurbineUser> query = Database.sql(
      TurbineUser.class,
      sql.toString()).param("message_id", messageId);

    return query.fetchList();
  }

  @Override
  public List<EipTMessageFile> getMessageFiles(List<Integer> messageIds) {
    SelectQuery<EipTMessageFile> query = Database.query(EipTMessageFile.class);
    query.where(Operations.in(EipTMessageFile.MESSAGE_ID_PROPERTY, messageIds));

    query.orderAscending(EipTMessageFile.MESSAGE_ID_PROPERTY);
    query.orderAscending(EipTMessageFile.UPDATE_DATE_PROPERTY);
    query.orderAscending(EipTMessageFile.FILE_PATH_PROPERTY);

    return query.fetchList();
  }

  @Override
  public List<EipTMessageFile> getMessageFilesByRoom(int roomId) {
    return Database.query(EipTMessageFile.class).where(
      Operations.eq(EipTMessageFile.ROOM_ID_PROPERTY, roomId)).fetchList();
  }

  @Override
  public String getRoomNotification(String username, int roomId) {
    Integer userId = null;
    TurbineUser turbineUser = turbineUserDbService.findByUsername(username);
    if (turbineUser == null) {
      SystemUser systemUser = AipoToolkit.getSystemUser(username);
      if (systemUser != null) {
        userId = systemUser.getUserId();
      }
    } else {
      userId = turbineUser.getUserId();
    }
    if (userId == null) {
      return null;
    }
    EipTMessageRoom room = Database.get(EipTMessageRoom.class, roomId);
    if (room != null) {
      return getRoomMobileNotification(room, userId);
    } else {
      return null;
    }

  }

  @Override
  public void setRoomNotification(String username, int roomId,
      String mobileNotification) {
    try {
      EipTMessageRoom room = Database.get(EipTMessageRoom.class, roomId);
      List<EipTMessageRoomMember> members = room.getEipTMessageRoomMember();

      for (EipTMessageRoomMember member : members) {
        if (member.getLoginName().equals(username)) {
          member.setMobileNotification(mobileNotification);
        }
      }

      Database.commit();
    } catch (Throwable t) {
      Database.rollback();
      throw new RuntimeException(t);
    }
  }

  public boolean isJoinRoom(EipTMessageRoom room, int userId) {
    List<EipTMessageRoomMember> list = room.getEipTMessageRoomMember();
    for (EipTMessageRoomMember member : list) {
      if (member.getUserId().intValue() == userId) {
        return true;
      }
    }
    return false;
  }

  public boolean hasAuthorityRoom(EipTMessageRoom room, int userId) {
    List<EipTMessageRoomMember> list = room.getEipTMessageRoomMember();
    for (EipTMessageRoomMember member : list) {
      if (member.getUserId().intValue() == userId) {
        if (AUTHORITY_TYPE_ADMIN.equals(member.getAuthority())) {
          return true;
        }
      }
    }
    return false;
  }

  public String getRoomMobileNotification(EipTMessageRoom room, int userId) {
    List<EipTMessageRoomMember> list = room.getEipTMessageRoomMember();
    for (EipTMessageRoomMember member : list) {
      if (member.getUserId().intValue() == userId) {
        return member.getMobileNotification();
      }
    }
    return null;
  }

  /**
   * @param roomId
   * @return
   */
  @Override
  public void setPhoto(int roomId, byte[] roomIcon, byte[] roomIconSmartPhone) {
    try {
      EipTMessageRoom model = Database.get(EipTMessageRoom.class, roomId);

      if (model == null) {
        return;
      }

      if (roomIcon != null && roomIconSmartPhone != null) {
        model.setPhoto(roomIcon);
        model.setPhotoSmartphone(roomIconSmartPhone);
        model.setPhotoModified(new Date());
        // 新仕様:N、旧仕様:F
        model.setHasPhoto("N");
        Database.commit();
      } else {
        model.setPhoto(null);
        model.setPhotoSmartphone(null);
        model.setPhotoModified(new Date());
        model.setHasPhoto("F");
        Database.commit();
      }
    } catch (Throwable t) {
      Database.rollback();
      throw new RuntimeException(t);
    }

  }

  /**
   *
   * @param roomId
   * @return
   */
  @Override
  public InputStream getPhoto(int roomId) {
    return getPhoto(roomId, IconSize.NORMAL);
  }

  /**
   *
   * @param roomId
   * @param size
   * @return
   */
  @Override
  public InputStream getPhoto(int roomId, IconSize size) {

    StringBuilder select = new StringBuilder();
    select.append(" select t1.room_id, t1.photo, t1.photo_smartphone ");
    select.append(" from eip_t_message_room as t1 ");
    select.append(" where t1.room_id = #bind($roomId) ");

    String query = select.toString();

    EipTMessageRoom room = Database.sql(EipTMessageRoom.class, query).param(
      "roomId",
      roomId).fetchSingle();

    if (room == null) {
      return null;
    }

    String hasPhoto = room.getHasPhoto();
    byte[] photo = null;
    if ("N".equals(hasPhoto)) {
      if (IconSize.LARGE.equals(size)) {
        // getPhoto() で 200x200 をダウンロード
        photo = room.getPhoto();
        if (photo == null) {
          return null;
        }
      } else {
        // getPhotoSmartphone() で 100x100 をダウンロード
        photo = room.getPhotoSmartphone();
        if (photo == null) {
          return null;
        }
      }
      // 旧仕様（HAS_PHOTO=T）の場合
    } else {
      // すべて getPhoto() で 86x86 をダウンロード
      photo = room.getPhoto();
      if (photo == null) {
        return null;
      }
    }

    return new ByteArrayInputStream(photo);
  }

  @Override
  public boolean isJoinRoom(int roomId, String username) {
    TurbineUser turbineUser = turbineUserDbService.findByUsername(username);
    if (turbineUser == null) {
      return false;
    }
    Integer userId = turbineUser.getUserId();
    EipTMessageRoom room = Database.get(EipTMessageRoom.class, roomId);
    if (room != null) {
      return isJoinRoom(room, userId);
    }
    return false;
  }

  @Override
  public boolean hasAuthorityRoom(int roomId, String username) {
    TurbineUser turbineUser = turbineUserDbService.findByUsername(username);
    if (turbineUser == null) {
      return false;
    }
    Integer userId = turbineUser.getUserId();
    EipTMessageRoom room = Database.get(EipTMessageRoom.class, roomId);
    if (room != null) {
      return hasAuthorityRoom(room, userId);
    }
    return false;
  }

  @Override
  public boolean isOwnMessage(int messageId, String username) {
    TurbineUser turbineUser = turbineUserDbService.findByUsername(username);
    if (turbineUser == null) {
      return false;
    }
    EipTMessage message = Database.get(EipTMessage.class, messageId);
    if (message == null) {
      return false;
    }
    return isOwnMessage(message, turbineUser.getUserId().intValue());
  }

  protected boolean isOwnMessage(EipTMessage message, int userId) {
    if (message == null) {
      return false;
    }
    return userId == message.getUserId().intValue();
  }

  /**
   * @param username
   * @param messageIdInt
   * @param file
   * @param fileThumbnail
   */
  @Override
  public EipTMessageFile insertMessageFiles(String username, int messageIdInt,
      String fileName, byte[] shrinkImage) {
    try {
      EipTMessage message = Database.get(EipTMessage.class, messageIdInt);
      if (message == null) {
        return null;
      }

      TurbineUser turbineUser = turbineUserDbService.findByUsername(username);
      if (turbineUser == null) {
        return null;
      }
      Integer userId = turbineUser.getUserId();

      // messageの投稿者だけが添付ファイルを追加できるようにValidate
      if (!message.getUserId().equals(userId)) {
        return null;
      }

      String filename = "0_" + String.valueOf(System.nanoTime());

      EipTMessageFile model = Database.create(EipTMessageFile.class);
      model.setOwnerId(userId);
      model.setFileName(fileName);
      model.setFilePath(getRelativePath(filename));
      if (shrinkImage != null) {
        model.setFileThumbnail(shrinkImage);
      }
      model.setEipTMessage(message);
      model.setRoomId(message.getEipTMessageRoom().getRoomId());
      model.setCreateDate(Calendar.getInstance().getTime());
      model.setUpdateDate(Calendar.getInstance().getTime());

      Database.commit();

      return model;

    } catch (Throwable t) {
      Database.rollback();
      throw new RuntimeException(t);
    }
  }

  /**
   * @param filename
   * @return
   */
  private String getRelativePath(String fileName) {
    return new StringBuffer().append("/").append(fileName).toString();
  }

  /**
   *
   * @param fileId
   * @return
   */
  @Override
  public EipTMessageFile findMessageFile(int fileId) {
    return Database.get(EipTMessageFile.class, fileId);
  }

  @Override
  public List<EipTMessageRoomMember> getRoomMember(int roomId, String username) {
    List<EipTMessageRoomMember> members = new ArrayList<EipTMessageRoomMember>();
    TurbineUser turbineUser = turbineUserDbService.findByUsername(username);
    if (turbineUser == null) {
      return members;
    }

    EipTMessageRoom room = Database.get(EipTMessageRoom.class, roomId);
    if (room != null) {
      List<EipTMessageRoomMember> list = room.getEipTMessageRoomMember();
      for (EipTMessageRoomMember member : list) {

        members.add(member);

      }
    }
    return members;
  }

  @Override
  public boolean read(String username, int roomId, int lastMessageId) {
    return read(username, roomId, null, lastMessageId);
  }

  @Override
  public boolean read(String username, String targetUserName, int lastMessageId) {
    return read(username, null, targetUserName, lastMessageId);
  }

  protected boolean read(String username, Integer roomId,
      String targetUsername, int lastMessageId) {
    try {
      TurbineUser turbineUser = turbineUserDbService.findByUsername(username);
      TurbineUser targetUser = turbineUserDbService
        .findByUsername(targetUsername);

      EipTMessageRoom room = null;
      int userId = turbineUser.getUserId().intValue();
      if (roomId == null && targetUsername != null) {
        Integer targetUserId = null;
        if (targetUser != null) {
          targetUserId = targetUser.getUserId();
        } else {
          SystemUser systemUser = AipoToolkit.getSystemUser(targetUsername);
          if (systemUser != null) {
            targetUserId = systemUser.getUserId();
          }
        }
        if (targetUserId == null) {
          return false;
        }
        room = getRoom(userId, targetUserId);
      } else {
        if (roomId != null) {
          room = Database.get(EipTMessageRoom.class, roomId.longValue());
        }
      }
      if (room == null) {
        return false;
      }

      SQLTemplate<EipTMessageRead> countQuery = Database
        .sql(
          EipTMessageRead.class,
          "select count(*) as c from eip_t_message_read where room_id = #bind($room_id) and user_id = #bind($user_id) and is_read = 'F' and message_id <= #bind($message_id)")
        .param("room_id", Integer.valueOf(room.getRoomId()))
        .param("user_id", Integer.valueOf(userId))
        .param("message_id", Integer.valueOf(lastMessageId));

      int countValue = 0;
      List<DataRow> fetchCount = countQuery.fetchListAsDataRow();

      for (DataRow row : fetchCount) {
        countValue = ((Long) row.get("c")).intValue();
      }
      if (countValue > 0) {
        String sql = "update eip_t_message_read set is_read = 'T' where room_id = #bind($room_id) and user_id = #bind($user_id) and is_read = 'F' and message_id <= #bind($message_id)";
        Database.sql(EipTMessageRead.class, sql).param(
          "room_id",
          Integer.valueOf(room.getRoomId())).param(
          "user_id",
          Integer.valueOf(userId)).param(
          "message_id",
          Integer.valueOf(lastMessageId)).execute();
        return true;
      } else {
        return false;
      }
    } catch (Throwable t) {
      Database.rollback();
      throw new RuntimeException(t);
    }

  }

  private EipTMessageRoom getRoom(int userId, int targetUserId) {
    int user1 = userId;
    int user2 = targetUserId;
    if (userId > targetUserId) {
      user1 = targetUserId;
      user2 = userId;
    }
    EipTMessageRoomMember model = Database
      .query(EipTMessageRoomMember.class)
      .where(Operations.eq(EipTMessageRoomMember.USER_ID_PROPERTY, user1))
      .where(
        Operations.eq(EipTMessageRoomMember.TARGET_USER_ID_PROPERTY, user2))
      .fetchSingle();
    if (model != null) {
      return model.getEipTMessageRoom();
    } else {
      return null;
    }
  }

}
