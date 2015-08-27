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

import com.aipo.social.opensocial.model.ALPosition;

/**
 * @see org.apache.shindig. social.core.model.GroupImpl
 */
public class ALPositionImpl implements ALPosition {

  private int positionId;

  private String positionName;

  public ALPositionImpl() {
  }

  /**
   * @return
   */
  @Override
  public int getPositionId() {
    return positionId;
  }

  /**
   * @param positionId
   */
  @Override
  public void setPositionId(int positionId) {
    this.positionId = positionId;
  }

  /**
   * @return
   */
  @Override
  public String getPositionName() {
    return positionName;
  }

  /**
   * @param postName
   */
  @Override
  public void setPositionName(String positionName) {
    this.positionName = positionName;
  }

}
