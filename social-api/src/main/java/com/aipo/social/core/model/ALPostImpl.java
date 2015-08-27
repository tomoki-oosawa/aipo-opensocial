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

import com.aipo.social.opensocial.model.ALPost;

/**
 * @see org.apache.shindig. social.core.model.GroupImpl
 */
public class ALPostImpl implements ALPost {

  private String postId;

  private String postName;

  private String groupName;

  ALPostImpl() {
  }

  /**
   * @return
   */
  @Override
  public String getPostId() {
    return postId;
  }

  /**
   * @param postId
   */
  @Override
  public void setPostId(String postId) {
    this.postId = postId;
  }

  /**
   * @return
   */
  @Override
  public String getPostName() {
    return postName;
  }

  /**
   * @param postName
   */
  @Override
  public void setPostName(String postName) {
    this.postName = postName;
  }

  /**
   * @return
   */
  @Override
  public String getGroupName() {
    return groupName;
  }

  /**
   * @param groupName
   */
  @Override
  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

}
