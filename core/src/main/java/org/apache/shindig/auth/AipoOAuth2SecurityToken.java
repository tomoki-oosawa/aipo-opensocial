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
package org.apache.shindig.auth;

import java.util.ArrayList;
import java.util.List;

import org.apache.shindig.config.ContainerConfig;

public class AipoOAuth2SecurityToken implements SecurityToken {

  private final String ownerId;

  private final String viewerId;

  private final String container;

  private final List<String> scope;

  public AipoOAuth2SecurityToken(String ownerId, String viewerId) {
    this.ownerId = ownerId;
    this.viewerId = viewerId;
    this.container = ContainerConfig.DEFAULT_CONTAINER;
    this.scope = new ArrayList<String>();
  }

  public AipoOAuth2SecurityToken(String ownerId, String viewerId,
      List<String> scope) {
    this.ownerId = ownerId;
    this.viewerId = viewerId;
    this.container = ContainerConfig.DEFAULT_CONTAINER;
    this.scope = scope;
  }

  /**
   * @return
   */
  @Override
  public String getOwnerId() {
    return ownerId;
  }

  /**
   * @return
   */
  @Override
  public String getViewerId() {
    return viewerId;
  }

  /**
   * @return
   */
  @Override
  public String getAppId() {
    throw new UnsupportedOperationException();
  }

  /**
   * @return
   */
  @Override
  public String getDomain() {
    throw new UnsupportedOperationException();
  }

  /**
   * @return
   */
  @Override
  public String getContainer() {
    return this.container;
  }

  /**
   * @return
   */
  @Override
  public String getAppUrl() {
    throw new UnsupportedOperationException();
  }

  /**
   * @return
   */
  @Override
  public long getModuleId() {
    return 0;
  }

  /**
   * @return
   */
  @Override
  public Long getExpiresAt() {
    throw new UnsupportedOperationException();
  }

  /**
   * @return
   */
  @Override
  public boolean isExpired() {
    throw new UnsupportedOperationException();
  }

  /**
   * @return
   */
  @Override
  public String getUpdatedToken() {
    throw new UnsupportedOperationException();
  }

  /**
   * @return
   */
  @Override
  public String getAuthenticationMode() {
    throw new UnsupportedOperationException();
  }

  /**
   * @return
   */
  @Override
  public String getTrustedJson() {
    throw new UnsupportedOperationException();
  }

  /**
   * @return
   */
  @Override
  public boolean isAnonymous() {
    return false;
  }

  /**
   * @return
   */
  @Override
  public String getActiveUrl() {
    throw new UnsupportedOperationException();
  }

  public List<String> getScope() {
    return scope;
  }

}