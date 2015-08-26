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
package com.aipo.social.opensocial.model;

import java.util.Date;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.apache.shindig.protocol.model.Exportablebean;
import org.apache.shindig.social.opensocial.model.Name;

import com.aipo.social.core.model.ALPersonImpl;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.ImplementedBy;

/**
 * @see org.apache.shindig.social.opensocial.model.Person
 */
@ImplementedBy(ALPersonImpl.class)
@Exportablebean
public interface ALPerson extends
    org.apache.shindig.social.opensocial.model.Person {
  public static enum Field {
    /** the json field for aboutMe. */
    ABOUT_ME("aboutMe"),
    /** the json field for accounts. */
    ACCOUNTS("accounts"),
    /** the json field for activities. */
    ACTIVITIES("activities"),
    /** the json field for addresses. */
    ADDRESSES("addresses"),
    /** the json field for age. */
    AGE("age"),
    /** the json field for appData. */
    APP_DATA("appData"),
    /** the json field for bodyType. */
    BODY_TYPE("bodyType"),
    /** the json field for books. */
    BOOKS("books"),
    /** the json field for cars. */
    CARS("cars"),
    /** the json field for children. */
    CHILDREN("children"),
    /** the json field for currentLocation. */
    CURRENT_LOCATION("currentLocation"),
    /** the json field for birthday. */
    BIRTHDAY("birthday"),
    /** the json field for display name. */
    DISPLAY_NAME("displayName"), /** Needed to support the RESTful api. */
    /** the json field for drinker. */
    DRINKER("drinker"),
    /** the json field for emails. */
    EMAILS("emails"),
    /** the json field for ethnicity. */
    ETHNICITY("ethnicity"),
    /** the json field for fashion. */
    FASHION("fashion"),
    /** the json field for food. */
    FOOD("food"),
    /** the json field for gender. */
    GENDER("gender"),
    /** the json field for happiestWhen. */
    HAPPIEST_WHEN("happiestWhen"),
    /** the json field for hasApp. */
    HAS_APP("hasApp"),
    /** the json field for heroes. */
    HEROES("heroes"),
    /** the json field for humor. */
    HUMOR("humor"),
    /** the json field for id. */
    ID("id"),
    /** the json field for IM accounts. */
    IMS("ims"),
    /** the json field for interests. */
    INTERESTS("interests"),
    /** the json field for jobInterests. */
    JOB_INTERESTS("jobInterests"),
    /** the json field for languagesSpoken. */
    LANGUAGES_SPOKEN("languagesSpoken"),
    /** the json field for updated. */
    LAST_UPDATED("updated"), /** Needed to support the RESTful api. */
    /** the json field for livingArrangement. */
    LIVING_ARRANGEMENT("livingArrangement"),
    /** the json field for lookingFor. */
    LOOKING_FOR("lookingFor"),
    /** the json field for movies. */
    MOVIES("movies"),
    /** the json field for music. */
    MUSIC("music"),
    /** the json field for name. */
    NAME("name"),
    /** the json field for networkPresence. */
    NETWORKPRESENCE("networkPresence"),
    /** the json field for nickname. */
    NICKNAME("nickname"),
    /** the json field for organiztions. */
    ORGANIZATIONS("organizations"),
    /** the json field for pets. */
    PETS("pets"),
    /** the json field for phoneNumbers. */
    PHONE_NUMBERS("phoneNumbers"),
    /** the json field for photos. */
    PHOTOS("photos"),
    /** the json field for politicalViews. */
    POLITICAL_VIEWS("politicalViews"),
    /** the json field for preferredUsername */
    PREFERRED_USERNAME("preferredUsername"),
    /** the json field for profileSong. */
    PROFILE_SONG("profileSong"),
    /** the json field for profileUrl. */
    PROFILE_URL("profileUrl"),
    /** the json field for profileVideo. */
    PROFILE_VIDEO("profileVideo"),
    /** the json field for quotes. */
    QUOTES("quotes"),
    /** the json field for relationshipStatus. */
    RELATIONSHIP_STATUS("relationshipStatus"),
    /** the json field for religion. */
    RELIGION("religion"),
    /** the json field for romance. */
    ROMANCE("romance"),
    /** the json field for scaredOf. */
    SCARED_OF("scaredOf"),
    /** the json field for sexualOrientation. */
    SEXUAL_ORIENTATION("sexualOrientation"),
    /** the json field for smoker. */
    SMOKER("smoker"),
    /** the json field for sports. */
    SPORTS("sports"),
    /** the json field for status. */
    STATUS("status"),
    /** the json field for tags. */
    TAGS("tags"),
    /** the json field for thumbnailUrl. */
    THUMBNAIL_URL("thumbnailUrl"),
    /** the json field for utcOffset. */
    UTC_OFFSET("utcOffset"),
    /** the json field for turnOffs. */
    TURN_OFFS("turnOffs"),
    /** the json field for turnOns. */
    TURN_ONS("turnOns"),
    /** the json field for tvShows. */
    TV_SHOWS("tvShows"),
    /** the json field for urls. */
    URLS("urls"),
    // Ext/
    NAMEKANA("nameKana");

    /**
     * a Map to convert json string to Field representations.
     */

    private static final Map<String, Field> LOOKUP = Maps.uniqueIndex(EnumSet
      .allOf(Field.class), Functions.toStringFunction());

    /**
     * The json field that the instance represents.
     */
    private final String urlString;

    /**
     * The set of all fields.
     */
    public static final Set<String> ALL_FIELDS = LOOKUP.keySet();

    /**
     * The set of default fields returned fields.
     */
    public static final Set<String> DEFAULT_FIELDS = ImmutableSet.of(ID
      .toString(), NAME.toString(), THUMBNAIL_URL.toString());

    /**
     * create a field base on the a json element.
     *
     * @param urlString
     *          the name of the element
     */
    private Field(String urlString) {
      this.urlString = urlString;
    }

    /**
     * emit the field as a json element.
     *
     * @return the field name
     */
    @Override
    public String toString() {
      return this.urlString;
    }

    public static Field getField(String jsonString) {
      return LOOKUP.get(jsonString);
    }

    /**
     * Converts from a url string (usually passed in the fields= parameter) into
     * the corresponding field enum.
     *
     * @param urlString
     *          The string to translate.
     * @return The corresponding person field.
     */
    public static ALPerson.Field fromUrlString(String urlString) {
      return LOOKUP.get(urlString);
    }
  }

  Name getNameKana();

  void setNameKana(Name nameKana);

  /**
   * @return
   */
  Date getPhotoModified();

  /**
   * @param photoModified
   */
  void setPhotoModified(Date photoModified);

  /**
   * @return
   */
  boolean getHasPhoto();

  /**
   * @param hasPhoto
   */
  void setHasPhoto(boolean hasPhoto);
}
