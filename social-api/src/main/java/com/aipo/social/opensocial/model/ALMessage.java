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
package com.aipo.social.opensocial.model;

import java.util.List;

import org.apache.shindig.protocol.model.Exportablebean;

@Exportablebean
public interface ALMessage {
  public long getMessageId();

  public void setMessegeId(long messageId);

  public long getRoomId();

  public void setRoomId(long roomId);

  public String getUserId();

  public void setUserId(String paramString);

  public String getTargetUserId();

  public void setTargetUserId(String paramString);

  public int getUnreadCount();

  public void setUnreadCount(int count);

  public int getMemberCount();

  public void setMemberCount(int count);

  public String getMessage();

  public void setMessage(String message);

  public List<String> getReadMembers();

  public void setReadMembers(List<String> paramStrings);

  public String getCreateDate();

  public void setCreateDate(String paramString);

  public String getTransactionId();

  public void setTransactionId(String transactionId);

  public List<ALMessageFile> getFiles();

  public void setFiles(List<ALMessageFile> files);

  public static enum Field {
    MESSAGE_ID("messageId"), ROOM_ID("roomId"), USER_ID("userId"), TARGET_USER_ID(
        "targetUserId"), UNREAD_COUNT("unreadCount"), MEMBER_COUNT(
        "memberCount"), MESSAGE("message"), READ_MEMBERS("readMembers"), CREATEDATE(
        "createDate"), TRANSACTION_ID("transactionId"), FILES("files");

    private final String jsonString;

    private Field(String jsonString) {
      this.jsonString = jsonString;
    }

    @Override
    public String toString() {
      return this.jsonString;
    }
  }
}