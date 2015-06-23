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
package com.aipo.social.core.model;

import java.util.List;

import com.aipo.social.opensocial.model.ALMessage;
import com.aipo.social.opensocial.model.ALMessageFile;

/**
 * @see org.apache.shindig. social.core.model.GroupImpl
 */
public class ALMessageImpl implements ALMessage {
  private long messageId;

  private long roomId;

  private String userId;

  private int unreadCount;

  private int memberCount;

  private String message;

  private List<String> readMembers;

  private String createDate;

  private String transactionId;

  private List<ALMessageFile> files;

  @Override
  public long getMessageId() {
    return messageId;
  }

  @Override
  public void setMessegeId(long messageId) {
    this.messageId = messageId;
  }

  @Override
  public long getRoomId() {
    return roomId;
  }

  @Override
  public void setRoomId(long roomId) {
    this.roomId = roomId;
  }

  @Override
  public String getUserId() {
    return userId;
  }

  @Override
  public void setUserId(String paramString) {
    this.userId = paramString;
  }

  @Override
  public int getUnreadCount() {
    return unreadCount;
  }

  @Override
  public void setUnreadCount(int count) {
    this.unreadCount = count;
  }

  @Override
  public int getMemberCount() {
    return memberCount;
  }

  @Override
  public void setMemberCount(int count) {
    this.memberCount = count;
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public List<String> getReadMembers() {
    return readMembers;
  }

  @Override
  public void setReadMembers(List<String> paramStrings) {
    this.readMembers = paramStrings;
  }

  @Override
  public String getCreateDate() {
    return createDate;
  }

  @Override
  public void setCreateDate(String paramString) {
    this.createDate = paramString;
  }

  /**
   * @return
   */
  @Override
  public String getTransactionId() {
    return this.transactionId;
  }

  /**
   * @param transactionId
   */
  @Override
  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  /**
   * @return
   */
  @Override
  public List<ALMessageFile> getFiles() {
    return files;
  }

  /**
   * @param files
   */
  @Override
  public void setFiles(List<ALMessageFile> files) {
    this.files = files;
  }

}
