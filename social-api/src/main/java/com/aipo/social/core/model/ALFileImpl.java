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

import com.aipo.social.opensocial.model.ALFile;

/**
 * @see org.apache.shindig. social.core.model.GroupImpl
 */
public class ALFileImpl implements ALFile {

  private long fileId;

  private String fileName;

  private String filePath;

  private Integer userId;

  private Long fileSize;

  private String contentType;

  private String categoryKey;

  /**
   * @return
   */
  @Override
  public long getFileId() {
    return fileId;
  }

  /**
   * @param fileId
   */
  @Override
  public void setFileId(long fileId) {
    this.fileId = fileId;
  }

  /**
   * @return
   */
  @Override
  public String getFileName() {
    return fileName;
  }

  /**
   * @param fileName
   */
  @Override
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  /**
   * @return
   */
  @Override
  public String getFilePath() {
    return filePath;
  }

  /**
   * @param filePath
   */
  @Override
  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  /**
   * @return
   */
  @Override
  public Long getFileSize() {
    return fileSize;
  }

  /**
   * @param fileSize
   */
  @Override
  public void setFileSize(Long fileSize) {
    this.fileSize = fileSize;
  }

  /**
   * @return
   */
  @Override
  public String getContentType() {
    return contentType;
  }

  /**
   * @param contentType
   */
  @Override
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  /**
   * @return
   */
  @Override
  public String getCategoryKey() {
    return categoryKey;
  }

  /**
   * @param categoryKey
   */
  @Override
  public void setCategoryKey(String categoryKey) {
    this.categoryKey = categoryKey;
  }

  /**
   * @return
   */
  @Override
  public Integer getUserId() {
    return userId;
  }

  /**
   * @param userId
   */
  @Override
  public void setUserId(Integer userId) {
    this.userId = userId;
  }
}
