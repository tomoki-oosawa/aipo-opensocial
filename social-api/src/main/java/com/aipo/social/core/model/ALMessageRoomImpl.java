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

import org.apache.shindig.social.opensocial.spi.GroupId;

import com.aipo.social.opensocial.model.ALMessageRoom;

/**
 * @see org.apache.shindig. social.core.model.GroupImpl
 */
public class ALMessageRoomImpl implements ALMessageRoom {

  /**
   * @return
   */
  @Override
  public long roomId() {
    return 0;
  }

  /**
   * @param roomId
   */
  @Override
  public void setId(GroupId roomId) {
  }

  /**
   * @return
   */
  @Override
  public String getName() {
    return "TEST_ROOM";
  }

  /**
   * @param name
   */
  @Override
  public void setName(String name) {
  }

  /**
   * @return
   */
  @Override
  public String getUserId() {
    return "org001:user1";
  }

  /**
   * @param paramString
   */
  @Override
  public void setUserId(String paramString) {
  }

  /**
   * @return
   */
  @Override
  public int getUnreadCount() {
    return 0;
  }

  /**
   * @param count
   */
  @Override
  public void setUnreadCount(int count) {
  }

  /**
   * @return
   */
  @Override
  public boolean getIsDirect() {
    return false;
  }

  /**
   * @param isDirect
   */
  @Override
  public void setIsDirect(boolean isDirect) {
  }

}
