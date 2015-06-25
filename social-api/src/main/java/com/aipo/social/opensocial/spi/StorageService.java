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
package com.aipo.social.opensocial.spi;

import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.protocol.ProtocolException;

import com.aipo.social.opensocial.model.ALFile;

/**
 *
 */
public interface StorageService {

  public InputStream getFile(ALFile file, SecurityToken paramSecurityToken)
      throws ProtocolException, FileNotFoundException;

  public InputStream getFile(String filePath, SecurityToken paramSecurityToken)
      throws ProtocolException, FileNotFoundException;

  public String separator() throws ProtocolException;

  public String getSaveDirPath(String rootPath, String categoryKey,
      String userId) throws ProtocolException;

  public String getDocumentPath(String rootPath, String categoryKey)
      throws ProtocolException;

  public long getFileSize(String rootPath, String dir, String filename)
      throws ProtocolException;

  public String getContentType(String filePath, SecurityToken paramSecurityToken)
      throws ProtocolException, FileNotFoundException;

}
