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
package com.aipo.social.core.model;

import java.util.Date;
import java.util.List;

import org.apache.shindig.social.opensocial.model.Name;

import com.aipo.social.opensocial.model.ALPerson;
import com.aipo.social.opensocial.model.ALPosition;
import com.aipo.social.opensocial.model.ALPost;

/**
 * @see org.apache.shindig. social.core.model.PersonImpl
 */
public class ALPersonImpl extends
    org.apache.shindig.social.core.model.PersonImpl implements ALPerson {

  private Name nameKana;

  private String email;

  private String telephone;

  private String extension;

  private String telephoneMobile;

  private String emailMobile;

  private Date photoModified;

  private boolean hasPhoto;

  private ALPosition position;

  private List<ALPost> post;

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

  /**
   * @return
   */
  @Override
  public String getEmail() {
    return email;
  }

  /**
   * @param email
   */
  @Override
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * @return
   */
  @Override
  public String getTelephone() {
    return telephone;
  }

  /**
   * @param telephone
   */
  @Override
  public void setTelephone(String telephone) {
    this.telephone = telephone;
  }

  /**
   * @return
   */
  @Override
  public String getExtension() {
    return extension;
  }

  /**
   * @param extension
   */
  @Override
  public void setExtension(String extension) {
    this.extension = extension;
  }

  /**
   * @return
   */
  @Override
  public String getTelephoneMobile() {
    return telephoneMobile;
  }

  /**
   * @param telephoneMobile
   */
  @Override
  public void setTelephoneMobile(String telephoneMobile) {
    this.telephoneMobile = telephoneMobile;
  }

  /**
   * @return
   */
  @Override
  public String getEmailMobile() {
    return emailMobile;
  }

  /**
   * @param emailMobile
   */
  @Override
  public void setEmailMobile(String emailMobile) {
    this.emailMobile = emailMobile;
  }

  /**
   * @return
   */
  @Override
  public ALPosition getPosition() {
    return position;
  }

  /**
   * @param position
   */
  @Override
  public void setPosition(ALPosition position) {
    this.position = position;
  }

  /**
   * @return
   */
  @Override
  public List<ALPost> getPost() {
    return post;
  }

  /**
   * @param post
   */
  @Override
  public void setPost(List<ALPost> post) {
    this.post = post;
  }

}
