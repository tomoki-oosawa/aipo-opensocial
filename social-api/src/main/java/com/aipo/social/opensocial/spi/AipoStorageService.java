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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.protocol.ProtocolException;

import com.aipo.orm.Database;
import com.aipo.social.opensocial.model.ALFile;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 *
 */
public class AipoStorageService extends AbstractService implements
    StorageService {

  @Inject
  @Named("aipo.filedir")
  private String FILE_DIR;

  @Override
  public InputStream getFile(ALFile file, SecurityToken paramSecurityToken)
      throws ProtocolException, FileNotFoundException {

    String documentPath =
      getSaveDirPath(FILE_DIR, file.getCategoryKey(), file.getUserId());

    return getFile(
      documentPath + separator() + file.getFilePath(),
      paramSecurityToken);
  }

  /**
   * ユーザ毎のルート保存先（絶対パス）を取得します。
   *
   * @param uid
   * @return
   */
  @Override
  public String getSaveDirPath(String rootPath, String categoryKey,
      String userId) {
    return getDocumentPath(rootPath, categoryKey + separator() + userId);
  }

  /**
   * @param filePath
   * @param paramSecurityToken
   * @return
   * @throws ProtocolException
   * @throws FileNotFoundException
   */
  @Override
  public InputStream getFile(String filePath, SecurityToken paramSecurityToken)
      throws ProtocolException, FileNotFoundException {
    return new FileInputStream(getAbsolutePath(filePath));
  }

  /**
   * @return
   * @throws ProtocolException
   */
  @Override
  public String separator() throws ProtocolException {
    return File.separator;
  }

  /**
   * @param rootPath
   * @param categoryKey
   * @return
   * @throws ProtocolException
   */
  @Override
  public String getDocumentPath(String rootPath, String categoryKey)
      throws ProtocolException {
    File rootDir = new File(getAbsolutePath(rootPath));
    String org_name = Database.getDomainName();
    if (!rootDir.exists()) {
      try {
        rootDir.mkdirs();
      } catch (Exception e) {
        return rootDir.getAbsolutePath();
      }
    }

    if (org_name == null) {
      return rootDir.getAbsolutePath();
    }

    File base = null;

    // パスを作成
    base =
      new File(rootDir.getAbsolutePath()
        + separator()
        + org_name
        + separator()
        + categoryKey);

    if (!base.exists()) {
      try {
        base.mkdirs();
      } catch (Exception e) {
        return base.getAbsolutePath();
      }
    }
    return base.getAbsolutePath();

  }

  /**
   * @param rootPath
   * @param dir
   * @param filename
   * @return
   * @throws ProtocolException
   */
  @Override
  public long getFileSize(String rootPath, String dir, String filename)
      throws ProtocolException {
    return getFileSize(new File(getAbsolutePath(rootPath)
      + separator()
      + Database.getDomainName()
      + separator()
      + dir
      + separator()
      + filename));
  }

  protected int getFileSize(File file) {
    if (file == null) {
      return -1;
    }

    FileInputStream fileInputStream = null;
    int size = -1;
    try {
      fileInputStream = new FileInputStream(file);
      BufferedInputStream input = new BufferedInputStream(fileInputStream);
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      byte[] b = new byte[512];
      int len = -1;
      while ((len = input.read(b)) != -1) {
        output.write(b, 0, len);
        output.flush();
      }
      input.close();
      fileInputStream.close();

      byte[] fileArray = output.toByteArray();
      size = fileArray.length;
      output.close();
    } catch (FileNotFoundException e) {
      return -1;
    } catch (IOException ioe) {
      return -1;
    }
    return size;
  }

  protected String getAbsolutePath(String folderPath) {
    try {
      Path path = Paths.get(folderPath);
      if (path == null) {
        return folderPath;
      }
      if (path.isAbsolute()) {
        return folderPath;
      }
      String root = System.getProperty("catalina.home");
      if (root == null) {
        return folderPath;
      }
      return root + separator() + folderPath;
    } catch (Throwable ignore) {
      //
    }
    return folderPath;
  }

  @Override
  public String getContentType(String filePath, SecurityToken paramSecurityToken)
      throws ProtocolException, FileNotFoundException {
    String contenType = null;
    File file = new File(filePath);
    DataHandler hData = new DataHandler(new FileDataSource(file));

    if (hData != null) {
      contenType = hData.getContentType();
    }
    return contenType;
  }
}
