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

@Exportablebean
public interface ALFile {
  public long getFileId();

  public void setFileId(long fileId);

  public String getFileName();

  public void setFileName(String fileName);

  public String getFilePath();

  public void setFilePath(String filePath);

  public String getUserId();

  public void setUserId(String userId);

  public int getFileSize();

  public void setFileSize(int fileSize);

  public boolean getIsImage();

  public void setIsImage(boolean isImage);

  public String getContentType();

  public void setContentType(String contentType);

  public String getCategoryKey();

  public void setCategoryKey(String categoryKey);

  public static enum Field {
    FILE_ID("fileId"), FILE_NAME("fileName");

    private final String jsonString;

    private Field(String jsonString) {
      this.jsonString = jsonString;
    }

    @Override
    public String toString() {
      return this.jsonString;
    }
  }
}