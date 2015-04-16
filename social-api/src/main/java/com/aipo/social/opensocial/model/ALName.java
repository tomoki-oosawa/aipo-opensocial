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

import org.apache.shindig.protocol.model.Exportablebean;

import com.aipo.social.core.model.ALNameImpl;
import com.google.inject.ImplementedBy;

/**
 * @see org.apache.shindig.social.opensocial.model.Name
 */
@ImplementedBy(ALNameImpl.class)
@Exportablebean
public interface ALName extends org.apache.shindig.social.opensocial.model.Name {

  public static enum Field {
    /**
     * The additional name.
     */
    ADDITIONAL_NAME("additionalName"),
    /**
     * The family name.
     */
    FAMILY_NAME("familyName"),
    /**
     * The given name.
     */
    GIVEN_NAME("givenName"),
    /**
     * The honorific prefix.
     */
    HONORIFIC_PREFIX("honorificPrefix"),
    /**
     * The honorific suffix.
     */
    HONORIFIC_SUFFIX("honorificSuffix"),
    /**
     * The formatted name.
     */
    FORMATTED("formatted"),

    // Ext.
    FAMILY_NAME_KANA("familyNameKana"), GIVEN_NAME_KANA("givenNameKana");

    /**
     * the json key for this field.
     */
    private final String jsonString;

    /**
     * Construct the a field enum.
     *
     * @param jsonString
     *          the json key for the field.
     */
    private Field(String jsonString) {
      this.jsonString = jsonString;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
      return this.jsonString;
    }
  }

  String getFamilyNameKana();

  void setFamilyNameKana(String familyNameKana);

  String getGivenNameKana();

  void setGivenNameKana(String givenNameKana);

}
