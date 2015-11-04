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
package com.aipo.social.core.model;

import java.util.List;

import com.aipo.social.opensocial.model.ALActivity;

/**
 * @see org.apache.shindig.social.core.model.ActivityImpl
 */
public class ALActivityImpl extends
    org.apache.shindig.social.core.model.ActivityImpl implements ALActivity {

  private List<String> recipients;

  private String displayName;

  private String portletParams;

  private String icon;

  /**
   * @return
   */
  @Override
  public List<String> getRecipients() {
    return recipients;
  }

  /**
   * @param userIds
   */
  @Override
  public void setRecipients(List<String> userIds) {
    this.recipients = userIds;
  }

  /**
   * @return
   */
  @Override
  public String getDisplayName() {
    return displayName;
  }

  /**
   * @param displayName
   */
  @Override
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * @return
   */
  @Override
  public String getPortletParams() {
    return portletParams;
  }

  /**
   * @param portletParams
   */
  @Override
  public void setPortletParams(String portletParams) {
    this.portletParams = portletParams;
  }

  /**
   * @return
   */
  @Override
  public String getIcon() {
    return icon;
  }

  /**
   * @param icon
   */
  @Override
  public void setIcon(String icon) {
    this.icon = icon;
  }

}
