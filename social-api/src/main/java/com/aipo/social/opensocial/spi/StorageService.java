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
import java.util.Calendar;
import java.util.List;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.protocol.ProtocolException;

import com.aipo.orm.model.portlet.IEipTFile;
import com.aipo.social.opensocial.model.ALFile;

/**
 *
 */
public interface StorageService {

  public void saveFile(InputStream is, String folderPath, String fileName,
      SecurityToken paramSecurityToken) throws ProtocolException;

  public void createNewFile(InputStream is, String folderPath, String fileName,
      SecurityToken paramSecurityToken) throws ProtocolException;

  public void createNewTmpFile(InputStream is, int uid, String dir,
      String fileName, String realFileName, SecurityToken paramSecurityToken)
      throws ProtocolException;

  public long getTmpFolderSize(int uid, String dir,
      SecurityToken paramSecurityToken) throws ProtocolException;

  public boolean copyTmpFile(int uid, String srcDir, String srcFileName,
      String destRootPath, String destDir, String destFileName,
      SecurityToken paramSecurityToken) throws ProtocolException;

  public boolean deleteTmpFolder(int uid, String dir,
      SecurityToken paramSecurityToken) throws ProtocolException;

  public InputStream getTmpFile(int uid, String folderName, String finename,
      SecurityToken paramSecurityToken) throws FileNotFoundException,
      ProtocolException;

  public long getFolderSize(String rootPath, String dir,
      SecurityToken paramSecurityToken) throws ProtocolException;

  public long getFileSize(String rootPath, String dir, String filename,
      SecurityToken paramSecurityToken) throws ProtocolException;

  public long getFileSize(String categoryKey, int userId, String filename,
      SecurityToken paramSecurityToken) throws ProtocolException;

  public boolean copyFile(String srcRootPath, String srcDir,
      String srcFileName, String destRootPath, String destDir,
      String destFileName, SecurityToken paramSecurityToken)
      throws ProtocolException;

  public boolean deleteFolder(String rootPath, String dir,
      SecurityToken paramSecurityToken) throws ProtocolException;

  public boolean deleteFile(String rootPath, String dir, String filename,
      SecurityToken paramSecurityToken) throws ProtocolException;

  public boolean deleteFile(String filePath, SecurityToken paramSecurityToken)
      throws ProtocolException;

  public InputStream getFile(String rootPath, String dir, String fineName,
      SecurityToken paramSecurityToken) throws FileNotFoundException,
      ProtocolException;

  public InputStream getFile(ALFile file, SecurityToken paramSecurityToken)
      throws ProtocolException, FileNotFoundException;

  public InputStream getFile(String filePath, SecurityToken paramSecurityToken)
      throws ProtocolException, FileNotFoundException;

  public String getDocumentPath(String rootPath, String categoryKey,
      SecurityToken paramSecurityToken) throws ProtocolException;

  public boolean deleteOldFolder(String folderPath, Calendar cal,
      SecurityToken paramSecurityToken) throws ProtocolException;

  public String separator() throws ProtocolException;

  public void createNewFile(InputStream inputStream, String filepath,
      SecurityToken paramSecurityToken) throws ProtocolException;

  public String getSaveDirPath(String rootPath, String categoryKey, int userId,
      SecurityToken paramSecurityToken) throws ProtocolException;

  public String getContentType(ALFile file, SecurityToken paramSecurityToken)
      throws ProtocolException, FileNotFoundException;

  public String getContentType(String filePath, SecurityToken paramSecurityToken)
      throws ProtocolException, FileNotFoundException;

  public String getFileTypeName(String fileName,
      SecurityToken paramSecurityToken) throws ProtocolException;

  public boolean isImage(String fileName, SecurityToken paramSecurityToken)
      throws ProtocolException;

  public void deleteFiles(String categoryKey, List<?> files,
      SecurityToken paramSecurityToken) throws ProtocolException;

  public void createNewFile(InputStream is, String categoryKey, IEipTFile file,
      SecurityToken paramSecurityToken) throws ProtocolException;
}
