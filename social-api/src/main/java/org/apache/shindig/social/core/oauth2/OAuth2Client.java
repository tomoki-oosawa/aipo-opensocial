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
package org.apache.shindig.social.core.oauth2;

/**
 * Represents an OAuth 2.0 client.
 */
public class OAuth2Client {

  protected String id;

  protected String secret;

  protected String redirectURI;

  protected String title;

  protected String iconUrl;

  protected ClientType type;

  private Flow flow;

  /**
   * Gets the client's ID.
   *
   * @return String represents the client's ID.
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the client's ID.
   *
   * @param id
   *          represents the client's ID.
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Gets the client's secret.
   *
   * @return String represents the client's secret
   */
  public String getSecret() {
    return secret;
  }

  /**
   * Sets the client's secret.
   *
   * @param secret
   *          represents the client's secret
   */
  public void setSecret(String secret) {
    this.secret = secret;
  }

  /**
   * Gets the client's redirect URI.
   *
   * @return String represents the client's redirect URI
   */
  public String getRedirectURI() {
    return redirectURI;
  }

  /**
   * Sets the client's redirect URI.
   *
   * @param redirectUri
   *          represents the client's redirect URI
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
   * Sets the client's title.
   *
   * @param title
   *          represents the client's title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Gets the client's icon URL.
   *
   * @return String represents the client's icon URL
   */
  public String getIconUrl() {
    return iconUrl;
  }

  /**
   * Sets the client's icon URL.
   *
   * @param iconUrl
   *          represents the client's icon URL
   */
  public void setIconUrl(String iconUrl) {
    this.iconUrl = iconUrl;
  }

  /**
   * Gets the client's type.
   *
   * @return ClientType represents the client's type
   */
  public ClientType getType() {
    return type;
  }

  /**
   * Sets the client's type.
   *
   * @param clientType
   *          represents the client's type
   */
  public void setType(ClientType type) {
    this.type = type;
  }

  /**
   * Sets the client's OAuth2 flow (via a String flow identifier)
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
   * Sets the client's OAuth2 flow
   *
   * @param flow
   */
  public void setFlowEnum(Flow flow) {
    this.flow = flow;
  }

  /**
   * Gets the client's OAuth2 flow
   *
   * @return
   */
  public Flow getFlow() {
    return flow;
  }

  /**
   * Enumerated client types in the OAuth 2.0 specification.
   */
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
