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

import java.util.Date;

import org.apache.shindig.social.opensocial.model.Name;

import com.aipo.social.opensocial.model.ALPerson;

/**
 * @see org.apache.shindig. social.core.model.PersonImpl
 */
public class ALPersonImpl extends
    org.apache.shindig.social.core.model.PersonImpl implements ALPerson {

  private Name nameKana;

  private Date photoModified;

  private boolean hasPhoto;

  /**
   * @param userId
   * @param displayName
   * @param name
   * @param nameKana2
   */
  public ALPersonImpl(String id, String displayName, Name name, Name nameKana) {
    super(id, displayName, name);
    this.nameKana = nameKana;
  }

  /**
   *
   */
  public ALPersonImpl() {
  }

  /**
   * @return
   */
  @Override
  public Name getNameKana() {
    return nameKana;
  }

  /**
   * @param nameKana
   */
  @Override
  public void setNameKana(Name nameKana) {
    this.nameKana = nameKana;
  }

  /**
   * @return
   */
  @Override
  public Date getPhotoModified() {
    return photoModified;
  }

  /**
   * @param photoModified
   */
  @Override
  public void setPhotoModified(Date photoModified) {
    this.photoModified = photoModified;
  }

  /**
   * @return
   */
  @Override
  public boolean getHasPhoto() {
    return hasPhoto;
  }

  /**
   * @param hasPhoto
   */
  @Override
  public void setHasPhoto(boolean hasPhoto) {
    this.hasPhoto = hasPhoto;
  }

}
