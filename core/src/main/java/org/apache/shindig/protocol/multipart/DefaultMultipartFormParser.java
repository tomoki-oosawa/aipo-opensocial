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
package org.apache.shindig.protocol.multipart;

import java.io.IOException;
import java.net.UnknownServiceException;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.common.collect.Lists;

public class DefaultMultipartFormParser implements MultipartFormParser {
  private static final String MULTIPART = "multipart/";

  public DefaultMultipartFormParser() {
  }

  @Override
  public Collection<FormDataItem> parse(HttpServletRequest servletRequest)
      throws IOException {
    FileItemFactory factory = new DiskFileItemFactory();
    ServletFileUpload upload = new ServletFileUpload(factory);
    try {
      List<FileItem> fileItems = upload.parseRequest(servletRequest);
      return convertToFormData(fileItems);
    } catch (FileUploadException e) {
      UnknownServiceException use =
        new UnknownServiceException("File upload error.");
      use.initCause(e);
      throw use;
    }
  }

  private Collection<FormDataItem> convertToFormData(List<FileItem> fileItems) {
    List<FormDataItem> formDataItems =
      Lists.newArrayListWithCapacity(fileItems.size());
    for (FileItem item : fileItems) {
      formDataItems.add(new CommonsFormDataItem(item));
    }
    return formDataItems;
  }

  @Override
  public boolean isMultipartContent(HttpServletRequest request) {
    if (!"POST".equals(request.getMethod())
      && !"PUT".equals(request.getMethod())) {
      return false;
    }
    String contentType = request.getContentType();
    if (contentType == null) {
      return false;
    }
    return contentType.toLowerCase().startsWith("multipart/");
  }
}
