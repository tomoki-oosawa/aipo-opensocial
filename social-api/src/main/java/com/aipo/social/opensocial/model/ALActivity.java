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

import java.util.List;

import org.apache.shindig.protocol.model.Exportablebean;

import com.aipo.social.core.model.ALActivityImpl;
import com.google.inject.ImplementedBy;

/**
 * @see org.apache.shindig.social.opensocial.model.Activity
 */
@ImplementedBy(ALActivityImpl.class)
@Exportablebean
public interface ALActivity extends
    org.apache.shindig.social.opensocial.model.Activity {

  public static enum Field {
    /** the json field for appId. */
    APP_ID("appId"),
    /** the json field for body. */
    BODY("body"),
    /** the json field for bodyId. */
    BODY_ID("bodyId"),
    /** the json field for externalId. */
    EXTERNAL_ID("externalId"),
    /** the json field for id. */
    ID("id"),
    /** the json field for updated. */
    LAST_UPDATED("updated"), /* Needed to support the RESTful api */
    /** the json field for mediaItems. */
    MEDIA_ITEMS("mediaItems"),
    /** the json field for postedTime. */
    POSTED_TIME("postedTime"),
    /** the json field for priority. */
    PRIORITY("priority"),
    /** the json field for streamFaviconUrl. */
    STREAM_FAVICON_URL("streamFaviconUrl"),
    /** the json field for streamSourceUrl. */
    STREAM_SOURCE_URL("streamSourceUrl"),
    /** the json field for streamTitle. */
    STREAM_TITLE("streamTitle"),
    /** the json field for streamUrl. */
    STREAM_URL("streamUrl"),
    /** the json field for templateParams. */
    TEMPLATE_PARAMS("templateParams"),
    /** the json field for title. */
    TITLE("title"),
    /** the json field for titleId. */
    TITLE_ID("titleId"),
    /** the json field for url. */
    URL("url"),
    /** the json field for userId. */
    USER_ID("userId"),

    // Ext.
    RECIPIENTS("recipients");

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

  List<String> getRecipients();

  void setRecipients(List<String> userIds);

}
