/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2011 Aimluck,Inc.
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

public class OAuthConsumer implements Serializable {

  private static final long serialVersionUID = 6257610766757157040L;

  private String consumerKey;

  private String consumerSecret;

  private String type;

  private String name;

  /**
   * @return consumerKey
   */
  public String getConsumerKey() {
    return consumerKey;
  }

  /**
   * @param consumerKey
   *          セットする consumerKey
   */
  public void setConsumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
  }

  /**
   * @return consumerSecret
   */
  public String getConsumerSecret() {
    return consumerSecret;
  }

  /**
   * @param consumerSecret
   *          セットする consumerSecret
   */
  public void setConsumerSecret(String consumerSecret) {
    this.consumerSecret = consumerSecret;
  }

  /**
   * @return type
   */
  public String getType() {
    return type;
  }

  /**
   * @param type
   *          セットする type
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * @return name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          セットする name
   */
  public void setName(String name) {
    this.name = name;
  }
}