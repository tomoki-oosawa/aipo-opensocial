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
package com.aipo.orm.service.bean;

import java.io.Serializable;
import java.util.Date;

/**
 *
 */
public class OAuth2Client implements Serializable {

  private static final long serialVersionUID = -2024524979016696568L;

  private String id;

  private String secret;

  private String redirectURI;

  private String title;

  private String iconUrl;

  private ClientType type;

  private Flow flow;

  private Date createDate;

  private Date updateDate;

  /**
   *
   * @return
   */
  public String getId() {
    return id;
  }

  /**
   *
   * @param id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   *
   * @return
   */
  public String getSecret() {
    return secret;
  }

  /**
   *
   * @param secret
   */
  public void setSecret(String secret) {
    this.secret = secret;
  }

  /**
   *
   * @return
   */
  public String getRedirectURI() {
    return redirectURI;
  }

  /**
   *
   * @param redirectUri
   */
  public void setRedirectURI(String redirectUri) {
    this.redirectURI = redirectUri;
  }

  /**
   * Gets the client's title.
   *
   * @return String represents the client's title
   */
  public String getTitle() {
    return title;
  }

  /**
   *
   * @param title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   *
   * @return
   */
  public String getIconUrl() {
    return iconUrl;
  }

  /**
   *
   * @param iconUrl
   */
  public void setIconUrl(String iconUrl) {
    this.iconUrl = iconUrl;
  }

  /**
   *
   * @return
   */
  public ClientType getType() {
    return type;
  }

  /**
   *
   * @param type
   */
  public void setType(ClientType type) {
    this.type = type;
  }

  /**
   *
   * @param flow
   */
  public void setFlow(String flow) {
    if (Flow.CLIENT_CREDENTIALS.toString().equals(flow)) {
      this.flow = Flow.CLIENT_CREDENTIALS;
    } else if (Flow.AUTHORIZATION_CODE.toString().equals(flow)) {
      this.flow = Flow.AUTHORIZATION_CODE;
    } else if (Flow.IMPLICIT.toString().equals(flow)) {
      this.flow = Flow.IMPLICIT;
    } else {
      this.flow = null;
    }
  }

  /**
   *
   * @param flow
   */
  public void setFlowEnum(Flow flow) {
    this.flow = flow;
  }

  /**
   *
   * @return
   */
  public Flow getFlow() {
    return flow;
  }

  /**
   * @return createDate
   */
  public Date getCreateDate() {
    return createDate;
  }

  /**
   * @param createDate
   *          セットする createDate
   */
  public void setCreateDate(Date createDate) {
    this.createDate = createDate;
  }

  /**
   * @return updateDate
   */
  public Date getUpdateDate() {
    return updateDate;
  }

  /**
   * @param updateDate
   *          セットする updateDate
   */
  public void setUpdateDate(Date updateDate) {
    this.updateDate = updateDate;
  }

  public static enum ClientType {
    PUBLIC("public"), CONFIDENTIAL("confidential");

    private final String name;

    private ClientType(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  public static enum Flow {
    CLIENT_CREDENTIALS("client_credentials"), AUTHORIZATION_CODE(
        "authorization_code"), IMPLICIT("implicit");

    private final String name;

    private Flow(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }
}