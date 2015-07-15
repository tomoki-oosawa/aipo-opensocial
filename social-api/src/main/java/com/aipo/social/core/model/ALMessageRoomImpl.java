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

import java.util.Date;
import java.util.List;

import com.aipo.social.opensocial.model.ALMessageRoom;

/**
 *
 */
public class ALMessageRoomImpl implements ALMessageRoom {

  private long roomId;

  private String name;

  private String userId;

  private int unreadCount;

  private boolean isDirect;

  private boolean isAutoName;

  private Date photoModified;

  private String lastMessage;

  private Integer lastMessageId;

  private List<String> members;

  private Date updateDate;

  /**
   * @return
   */
  @Override
  public long getRoomId() {
    return roomId;
  }

  /**
   * @param roomId
   */
  @Override
  public void setRoomId(long roomId) {
    this.roomId = roomId;
  }

  /**
   * @return
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * @param name
   */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return
   */
  @Override
  public String getUserId() {
    return userId;
  }

  /**
   * @param paramString
   */
  @Override
  public void setUserId(String paramString) {
    this.userId = paramString;
  }

  /**
   * @return
   */
  @Override
  public int getUnreadCount() {
    return unreadCount;
  }

  /**
   * @param count
   */
  @Override
  public void setUnreadCount(int count) {
    this.unreadCount = count;
  }

  /**
   * @return
   */
  @Override
  public boolean getIsDirect() {
    return isDirect;
  }

  /**
   * @param isDirect
   */
  @Override
  public void setIsDirect(boolean isDirect) {
    this.isDirect = isDirect;
  }

  /**
   * @return
   */
  @Override
  public boolean getIsAutoName() {
    return isAutoName;
  }

  /**
   * @param isAutoName
   */
  @Override
  public void setIsAutoName(boolean isAutoName) {
    this.isAutoName = isAutoName;
  }

  /**
   * @return members
   */
  @Override
  public List<String> getMembers() {
    return members;
  }

  /**
   * @param members
   */
  @Override
  public void setMembers(List<String> members) {
    this.members = members;
  }

  /**
   * @return
   */
  @Override
  public Date getUpdateDate() {
    return updateDate;
  }

  /**
   * @param date
   */
  @Override
  public void setUpdateDate(Date date) {
    this.updateDate = date;
  }

  /**
   * @return
   */
  @Override
  public String getLastMessage() {
    return lastMessage;
  }

  /**
   * @param lastMessage
   */
  @Override
  public void setLastMessage(String lastMessage) {
    this.lastMessage = lastMessage;
  }

  /**
   * @return
   */
  @Override
  public Date getPhotoModified() {
    return photoModified;
  }

  /**
   * @param photoModified
   */
  @Override
  public void setPhotoModified(Date photoModified) {
    this.photoModified = photoModified;
  }

  /**
   * @param messageId
   */
  @Override
  public void setLastMessageId(Integer messageId) {
    this.lastMessageId = messageId;
  }

  /**
   * @return
   */
  @Override
  public Integer getLastMessageId() {
    return lastMessageId;
  }

}
