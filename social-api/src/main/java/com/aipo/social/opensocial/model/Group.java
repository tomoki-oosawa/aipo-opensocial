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

package com.aipo.social.opensocial.model;

import org.apache.shindig.protocol.model.Exportablebean;

import com.aipo.social.core.model.ALGroupImpl;
import com.google.inject.ImplementedBy;

/**
 * @see org.apache.shindig.social.opensocial.model.Group
 */
@ImplementedBy(ALGroupImpl.class)
@Exportablebean
public interface Group extends org.apache.shindig.social.opensocial.model.Group {

  public static enum Field {
    /**
     * Unique ID for this group Required.
     */
    ID("Id"),
    /**
     * Title of group Required.
     */
    TITLE("title"),
    /**
     * Description of group Optional.
     */
    DESCRIPTION("description"),

    // Ext.
    TYPE("type");

    /**
     * The json field that the instance represents.
     */
    private final String jsonString;

    /**
     * create a field base on the a json element.
     * 
     * @param jsonString
     *          the name of the element
     */
    private Field(String jsonString) {
      this.jsonString = jsonString;
    }

    /**
     * emit the field as a json element.
     * 
     * @return the field name
     */
    @Override
    public String toString() {
      return jsonString;
    }
  }

  String getType();

  void setType(String type);
}
