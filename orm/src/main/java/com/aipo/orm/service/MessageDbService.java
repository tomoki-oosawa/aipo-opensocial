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
package com.aipo.orm.service;

import java.io.InputStream;
import java.util.List;

import com.aipo.orm.model.portlet.EipTMessage;
import com.aipo.orm.model.portlet.EipTMessageFile;
import com.aipo.orm.model.portlet.EipTMessageRoom;
import com.aipo.orm.model.portlet.EipTMessageRoomMember;
import com.aipo.orm.service.request.SearchOptions;

public interface MessageDbService {

  public EipTMessageRoom findRoom(String username, String targetUsername);

  public EipTMessageRoom findRoom(int roomId, String username);

  public List<EipTMessageRoom> findRoom(String username, SearchOptions options);

  public List<EipTMessageRoom> findRoom(int roomId, String username,
      String targetUsername, SearchOptions options);

  public void deleteRoom(int roomId);

  public List<EipTMessage> findMessage(int roomId, int messageId,
      SearchOptions options);

  public void deleteMessage(int messageId);

  public EipTMessageRoom createRoom(String username, String name,
      List<String> memberNameList);

  public EipTMessage createMessage(String username, Integer roomId,
      String targetUsername, String message);

  public EipTMessageRoom updateRoom(Integer roomId, String username,
      String name, List<String> memberNameList);

  public InputStream getPhoto(int roomId);

  public InputStream getPhoto(int roomId, IconSize size);

  public void setPhoto(int roomId, byte[] roomIcon, byte[] roomIconSmartPhone);

  public EipTMessageFile findMessageFile(int fileId);

  public boolean isJoinRoom(int roomId, String username);

  public List<EipTMessageRoomMember> getOtherRoomMember(int roomId,
      String username);

  public boolean read(String username, String targetUsername, int lastMessageId);

  public boolean read(String username, int roomId, int lastMessageId);

  public boolean isOwnMessage(int messageId, String username);

  public List<EipTMessageFile> getMessageFiles(List<Integer> messageIds);

  public List<EipTMessageFile> getMessageFilesByRoom(int roomId);

  public enum IconSize {
    NORMAL, LARGE;
  }
}
