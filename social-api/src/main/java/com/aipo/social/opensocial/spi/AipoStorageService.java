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
package com.aipo.social.opensocial.spi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.protocol.ProtocolException;

import com.aipo.orm.Database;
import com.aipo.orm.model.portlet.IEipTFile;
import com.aipo.social.opensocial.model.ALFile;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 *
 */
public class AipoStorageService extends AbstractService implements
    StorageService {

  private final String fileDir;

  private final String tmpFileuploadAttachmentDir;

  private final String EXT_FILENAME = ".txt";

  /**
  *
  */
  @Inject
  public AipoStorageService(
      @Named("aipo.filedir") String fireDir,
      @Named("aipo.tmp.fileupload.attachment.directory") String tmpFileuploadAttachmentDir) {
    this.fileDir = fireDir;
    this.tmpFileuploadAttachmentDir = tmpFileuploadAttachmentDir;
  }

  @Override
  public void saveFile(InputStream is, String folderPath, String filename,
      SecurityToken paramSecurityToken) throws ProtocolException {
    File path = new File(getAbsolutePath(folderPath));

    if (!path.exists()) {
      try {
        path.mkdirs();
      } catch (Exception e) {
      }
    }

    String filepath = path + separator() + filename;
    File file = new File(filepath);
    FileOutputStream os = null;
    try {
      if (!file.exists()) {
        if (!file.createNewFile()) {
          throw new RuntimeException("createNewFile error");
        }
      }
      os = new FileOutputStream(filepath);
      int c;
      while ((c = is.read()) != -1) {
        os.write(c);
      }
    } catch (IOException e) {
    } finally {
      if (os != null) {
        try {
          os.flush();
          os.close();
        } catch (Throwable e) {
          // ignore
        }
      }
    }
  }

  /**
   * @param inputStream
   * @param filepath
   */
  @Override
  public void createNewFile(InputStream is, String filepath,
      SecurityToken paramSecurityToken) throws ProtocolException {
    File file = new File(getAbsolutePath(filepath));

    if (!file.exists()) {
      try {
        String parent = file.getParent();
        if (parent != null) {
          File dir = new File(parent);
          if (!dir.exists()) {
            if (!dir.mkdirs()) {
              throw new RuntimeException("mkdir error");
            }
          }
        }
        if (!file.createNewFile()) {
          throw new RuntimeException("createNewFile error");
        }
      } catch (IOException e) {
      }
    }

    FileOutputStream os = null;
    try {
      os = new FileOutputStream(getAbsolutePath(filepath));
      int c;
      while ((c = is.read()) != -1) {
        os.write(c);
      }
    } catch (IOException e) {
    } finally {
      if (os != null) {
        try {
          os.flush();
          os.close();
        } catch (Throwable e) {
          // ignore
        }
      }
    }
  }

  /**
   * @param is
   * @param folderPath
   * @param filename
   */
  @Override
  public void createNewFile(InputStream is, String folderPath, String filename,
      SecurityToken paramSecurityToken) throws ProtocolException {
    File path = new File(getAbsolutePath(folderPath));

    if (!path.exists()) {
      try {
        path.mkdirs();
      } catch (Exception e) {
      }
    }

    String filepath = path + separator() + filename;
    File file = new File(filepath);
    FileOutputStream os = null;
    try {
      if (!file.createNewFile()) {
        throw new RuntimeException("createNewFile error");
      }
      os = new FileOutputStream(filepath);
      int c;
      while ((c = is.read()) != -1) {
        os.write(c);
      }
    } catch (IOException e) {
    } finally {
      if (os != null) {
        try {
          os.flush();
          os.close();
        } catch (Throwable e) {
          // ignore
        }
      }
    }
  }

  /**
   * @param is
   * @param rootPath
   * @param fileName
   */
  @Override
  public void createNewTmpFile(InputStream is, int uid, String dir,
      String fileName, String realFileName, SecurityToken paramSecurityToken)
      throws ProtocolException {

    File path =
      new File(getAbsolutePath(tmpFileuploadAttachmentDir)
        + separator()
        + Database.getDomainName()
        + separator()
        + uid
        + separator()
        + dir);

    if (!path.exists()) {
      try {
        path.mkdirs();
      } catch (Exception e) {
      }
    }

    // バッファリング修正START
    try {
      String filepath = path + separator() + fileName;
      File file = new File(filepath);
      if (!file.createNewFile()) {
        throw new RuntimeException("createNewFile error");
      }
      int c;
      BufferedInputStream bis = null;
      BufferedOutputStream bos = null;
      try {
        bis = new BufferedInputStream(is, 1024 * 1024);
        bos =
          new BufferedOutputStream(new FileOutputStream(filepath), 1024 * 1024);

        while ((c = bis.read()) != -1) {
          bos.write(c);
        }
      } catch (IOException e) {
      } finally {

        IOUtils.closeQuietly(bis);
        IOUtils.closeQuietly(bos);
      }
      // バッファリング修正END

      PrintWriter w = null;
      try {
        w =
          new PrintWriter(new OutputStreamWriter(new FileOutputStream(filepath
            + EXT_FILENAME), "UTF-8"));
        w.println(realFileName);
      } catch (IOException e) {
      } finally {
        if (w != null) {
          try {
            w.flush();
            w.close();
          } catch (Throwable e) {
            // ignore
          }
        }
      }
    } catch (FileNotFoundException e) {
    } catch (IOException e) {
    }
  }

  @Override
  public boolean copyFile(String srcRootPath, String srcDir,
      String srcFileName, String destRootPath, String destDir,
      String destFileName, SecurityToken paramSecurityToken)
      throws ProtocolException {

    File srcPath =
      new File(getAbsolutePath(srcRootPath)
        + separator()
        + Database.getDomainName()
        + separator()
        + srcDir);

    if (!srcPath.exists()) {
      try {
        srcPath.mkdirs();
      } catch (Exception e) {
        return false;
      }
    }

    File destPath =
      new File(getAbsolutePath(destRootPath)
        + separator()
        + Database.getDomainName()
        + separator()
        + destDir);

    if (!destPath.exists()) {
      try {
        destPath.mkdirs();
      } catch (Exception e) {
        return false;
      }
    }

    File from = new File(srcPath + separator() + srcFileName);
    File to = new File(destPath + separator() + destFileName);

    boolean res = true;
    FileChannel srcChannel = null;
    FileChannel destChannel = null;

    try {
      srcChannel = new FileInputStream(from).getChannel();
      destChannel = new FileOutputStream(to).getChannel();
      destChannel.transferFrom(srcChannel, 0, srcChannel.size());
    } catch (RuntimeException ex) {
      throw ex;
    } catch (Exception ex) {
      res = false;
    } finally {
      if (destChannel != null) {
        try {
          destChannel.close();
        } catch (IOException ex) {
          res = false;
        }
      }
      if (srcChannel != null) {
        try {
          srcChannel.close();
        } catch (IOException ex) {
          res = false;
        }
      }
    }

    return res;
  }

  /**
   * @param rootPath
   * @param dir
   */
  @Override
  public long getFolderSize(String rootPath, String dir,
      SecurityToken paramSecurityToken) throws ProtocolException {
    return getFolderSize(rootPath
      + separator()
      + Database.getDomainName()
      + separator()
      + dir);
  }

  protected long getFolderSize(String folderPath) {
    if (folderPath == null || folderPath.equals("")) {
      return 0;
    }

    File folder = new File(getAbsolutePath(folderPath));
    if (!folder.exists()) {
      return 0;
    }
    if (folder.isFile()) {
      return getFileSize(folder);
    }
    int fileSizeSum = 0;
    File file = null;
    String[] files = folder.list();
    int length = files.length;
    for (int i = 0; i < length; i++) {
      file = new File(getAbsolutePath(folderPath) + separator() + files[i]);
      if (file.isFile()) {
        fileSizeSum += getFileSize(file);
      } else if (file.isDirectory()) {
        fileSizeSum += getFolderSize(file.getAbsolutePath());
      }
    }
    return fileSizeSum;
  }

  /**
   * @param rootPath
   * @param dir
   * @param filePath
   * @return
   * @throws ProtocolException
   */
  @Override
  public long getFileSize(String rootPath, String dir, String filePath,
      SecurityToken paramSecurityToken) throws ProtocolException {
    return getFileSize(new File(getAbsolutePath(rootPath)
      + separator()
      + Database.getDomainName()
      + separator()
      + dir
      + separator()
      + filePath));
  }

  @Override
  public long getFileSize(String categoryKey, int userId, String filePath,
      SecurityToken paramSecurityToken) throws ProtocolException {
    return getFileSize(new File(getAbsolutePath(fileDir)
      + separator()
      + Database.getDomainName()
      + separator()
      + categoryKey
      + separator()
      + userId
      + filePath));
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

  @Override
  public boolean deleteFolder(String rootPath, String dir,
      SecurityToken paramSecurityToken) throws ProtocolException {
    File file =
      new File(getAbsolutePath(rootPath)
        + separator()
        + Database.getDomainName()
        + separator()
        + dir);

    if (!file.exists()) {
      return true;
    }

    return deleteFolder(file);
  }

  protected boolean deleteFolder(File folder) {
    if (folder == null) {
      return true;
    }

    String[] files = folder.list();
    if (files == null) {
      if (!folder.delete()) {
        throw new RuntimeException("delete error");
      }
      return true;
    }

    int length = files.length;
    if (length <= 0) {
      if (!folder.delete()) {
        throw new RuntimeException("delete error");
      }
      return true;
    }

    String folderPath = folder.getAbsolutePath() + separator();
    File tmpfile = null;
    for (int i = 0; i < length; i++) {
      tmpfile = new File(folderPath + files[i]);
      if (tmpfile.exists()) {
        if (tmpfile.isFile()) {
          if (!tmpfile.delete()) {
            throw new RuntimeException("delete error");
          }
        } else if (tmpfile.isDirectory()) {
          deleteFolder(tmpfile);
        }
      }
    }

    if (!folder.delete()) {
      throw new RuntimeException("delete error");
    }
    return true;
  }

  @Override
  public InputStream getFile(ALFile file, SecurityToken paramSecurityToken)
      throws ProtocolException, FileNotFoundException {

    String documentPath =
      getSaveDirPath(
        fileDir,
        file.getCategoryKey(),
        file.getUserId(),
        paramSecurityToken);

    return getFile(documentPath + file.getFilePath(), paramSecurityToken);
  }

  /**
   * ユーザ毎のルート保存先（絶対パス）を取得します。
   *
   * @param uid
   * @return
   */
  @Override
  public String getSaveDirPath(String rootPath, String categoryKey, int userId,
      SecurityToken paramSecurityToken) {
    return getDocumentPath(
      rootPath,
      categoryKey + separator() + userId,
      paramSecurityToken);
  }

  @Override
  public InputStream getFile(String rootPath, String dir, String fileName,
      SecurityToken paramSecurityToken) throws FileNotFoundException {
    return getFile(rootPath
      + separator()
      + Database.getDomainName()
      + separator()
      + dir
      + separator()
      + fileName, paramSecurityToken);
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
   * @param rootPath
   * @param categoryKey
   * @return
   * @throws ProtocolException
   */
  @Override
  public String getDocumentPath(String rootPath, String categoryKey,
      SecurityToken paramSecurityToken) throws ProtocolException {
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
   * @return
   * @throws ProtocolException
   */
  @Override
  public String separator() throws ProtocolException {
    return File.separator;
  }

  /**
   * @param rootPath
   * @param dir
   * @param filename
   * @return
   */
  @Override
  public boolean deleteFile(String rootPath, String dir, String filename,
      SecurityToken paramSecurityToken) throws ProtocolException {

    File file =
      new File(getDocumentPath(rootPath, dir, paramSecurityToken)
        + separator()
        + filename);

    if (file != null && file.exists()) {
      if (!file.delete()) {
        throw new RuntimeException("delete error");
      }
    }

    return true;
  }

  @Override
  public boolean deleteFile(String filePath, SecurityToken paramSecurityToken)
      throws ProtocolException {

    File file = new File(getAbsolutePath(filePath));

    if (file != null && file.exists()) {
      if (!file.delete()) {
        throw new RuntimeException("delete error");
      }
    }

    return true;
  }

  @Override
  public boolean deleteOldFolder(String folderPath, Calendar cal,
      SecurityToken paramSecurityToken) throws ProtocolException {
    Calendar mod = Calendar.getInstance();
    boolean flag = true;
    File parent_folder = new File(getAbsolutePath(folderPath));
    try {
      if (!parent_folder.exists()) {
        return false;
      }
      if (parent_folder.isFile()) {
        return false;
      }
      String folders_path[] = parent_folder.list();
      if (folders_path.length == 0) {
        return true;
      }
      int length = folders_path.length;
      for (int i = 0; i < length; i++) {
        File folder =
          new File(parent_folder.getAbsolutePath()
            + File.separator
            + folders_path[i]);
        mod.setTimeInMillis(folder.lastModified());// ファイルの最終更新日時を格納
        if (folder.isDirectory()) {
          if (!deleteOldFolder(
            folder.getAbsolutePath(),
            cal,
            paramSecurityToken)) {// フォルダの中身が空もしくは全部削除された場合
            flag = false;
          } else if (mod.before(cal)) {// 空のフォルダが古い場合
            if (!folder.delete()) {
              flag = false;
            }
          }
        } else {
          if (mod.before(cal)) {
            // 一つでも消えないファイルがあればフラグを動かす
            if (!folder.delete()) {
              flag = false;
            }
          } else {
            flag = false;
          }
        }

      }
    } catch (Exception e) {
      return false;
    }
    return flag;
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
  public String getContentType(ALFile file, SecurityToken paramSecurityToken)
      throws ProtocolException, FileNotFoundException {

    String documentPath =
      getSaveDirPath(
        fileDir,
        file.getCategoryKey(),
        file.getUserId(),
        paramSecurityToken);

    return getContentType(documentPath + file.getFilePath(), paramSecurityToken);
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

  /**
   * Java1.5：BMP, bmp, jpeg, wbmp, gif, png, JPG, jpg, WBMP, JPEG
   *
   * @param fileType
   * @return
   */
  @Override
  public boolean isImage(String fileName, SecurityToken paramSecurityToken)
      throws ProtocolException {
    if (fileName == null || "".equals(fileName)) {
      return false;
    }

    int index = fileName.lastIndexOf(".");
    if (index < 1) {
      return false;
    }

    String fileType = getFileTypeName(fileName, paramSecurityToken);

    String[] format = ImageIO.getWriterFormatNames();
    int len = format.length;
    for (int i = 0; i < len; i++) {
      if (format[i].equals(fileType)) {
        return true;
      }
    }
    return false;
  }

  /**
   * ファイル名からファイルの拡張子を取得する。
   *
   * @param fileName
   * @return
   */
  @Override
  public String getFileTypeName(String fileName,
      SecurityToken paramSecurityToken) throws ProtocolException {
    if (fileName == null || "".equals(fileName)) {
      return null;
    }

    int index = fileName.lastIndexOf(".");
    if (index < 1) {
      return null;
    }

    return fileName.substring(index + 1, fileName.length());
  }

  /**
   * @param uid
   * @param dir
   * @return
   * @throws ProtocolException
   */
  @Override
  public long getTmpFolderSize(int uid, String dir,
      SecurityToken paramSecurityToken) throws ProtocolException {
    return getFolderSize(
      tmpFileuploadAttachmentDir,
      uid + separator() + dir,
      paramSecurityToken);
  }

  /**
   * @param uid
   * @param srcDir
   * @param srcFileName
   * @param destRootPath
   * @param destDir
   * @param destFileName
   * @return
   * @throws ProtocolException
   */
  @Override
  public boolean copyTmpFile(int uid, String srcDir, String srcFileName,
      String destRootPath, String destDir, String destFileName,
      SecurityToken paramSecurityToken) throws ProtocolException {
    return copyFile(
      tmpFileuploadAttachmentDir,
      uid + separator() + srcDir,
      srcFileName,
      destRootPath,
      destDir,
      destFileName,
      paramSecurityToken);
  }

  /**
   * @param uid
   * @param dir
   * @return
   * @throws ProtocolException
   */
  @Override
  public boolean deleteTmpFolder(int uid, String dir,
      SecurityToken paramSecurityToken) throws ProtocolException {
    return deleteFolder(
      tmpFileuploadAttachmentDir,
      uid + separator() + dir,
      paramSecurityToken);
  }

  /**
   * @param uid
   * @param folderName
   * @param finename
   * @return
   * @throws FileNotFoundException
   * @throws ProtocolException
   */
  @Override
  public InputStream getTmpFile(int uid, String folderName, String finename,
      SecurityToken paramSecurityToken) throws FileNotFoundException,
      ProtocolException {
    return getFile(
      tmpFileuploadAttachmentDir,
      uid + separator() + folderName,
      finename,
      paramSecurityToken);
  }

  @Override
  public void deleteFiles(String categoryKey, List<?> files,
      SecurityToken paramSecurityToken) throws ProtocolException {
    for (Object file : files) {
      if (file instanceof IEipTFile) {
        IEipTFile ifile = (IEipTFile) file;
        deleteFile(getDocumentPath(fileDir, categoryKey
          + separator()
          + ifile.getOwnerId().intValue(), paramSecurityToken)
          + ifile.getFilePath(), paramSecurityToken);
      }
    }
  }

  @Override
  public void createNewFile(InputStream is, String categoryKey, IEipTFile file,
      SecurityToken paramSecurityToken) throws ProtocolException {

    IEipTFile ifile = file;
    createNewFile(is, getDocumentPath(fileDir, categoryKey
      + separator()
      + ifile.getOwnerId().intValue(), paramSecurityToken)
      + ifile.getFilePath(), paramSecurityToken);

  }
}
