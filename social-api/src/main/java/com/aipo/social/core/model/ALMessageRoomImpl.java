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

import com.aipo.social.opensocial.model.ALMessageRoom;

/**
 * @see org.apache.shindig. social.core.model.GroupImpl
 */
public class ALMessageRoomImpl implements ALMessageRoom {

  private long roomId;

  private String name;

  private String userId;

  private int unreadCount;

  private boolean isDirect;

  private boolean isAutoName;

  private String updateDate;

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
   * @return
   */
  @Override
  public String getUpdateDate() {
    return updateDate;
  }

  /**
   * @param paramString
   */
  @Override
  public void setUpdateDate(String paramString) {
    this.updateDate = paramString;
  }

}
