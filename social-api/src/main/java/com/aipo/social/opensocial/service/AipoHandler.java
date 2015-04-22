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
package com.aipo.social.opensocial.service;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.apache.shindig.protocol.Operation;
import org.apache.shindig.protocol.RequestItem;
import org.apache.shindig.protocol.Service;

import com.google.inject.Inject;
import com.google.inject.name.Named;

@Service(name = "aipo")
public class AipoHandler {

  private final String version;

  @Inject
  public AipoHandler(@Named("aipo.version") String version) {
    this.version = version;
  }

  @Operation(httpMethods = "GET", path = "/version")
  public String version(RequestItem request) {
    return version;
  }

  @Operation(httpMethods = "GET", path = "/mytest")
  public byte[] mytest(RequestItem request) {
    try {
      return "this is test".getBytes();
    } catch (Exception e) {
      e.printStackTrace();
      return "failed".getBytes();
    }
    // int[] results = { 0, 1 };
    // return results;
  }

  @Operation(httpMethods = "GET", path = "/mypicture.png")
  public FileInputStream mytest2(RequestItem request) {
    try {
      return new FileInputStream("./aipo_logo_l.png");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * ファイルを読み込み、その中身をバイト配列で取得する
   *
   * @param filePath
   *          対象ファイルパス
   * @return 読み込んだバイト配列
   * @throws Exception
   *           ファイルが見つからない、アクセスできないときなど
   */
  private byte[] readFileToByte(String filePath) throws Exception {
    byte[] b = new byte[1];
    FileInputStream fis = new FileInputStream(filePath);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    while (fis.read(b) > 0) {
      baos.write(b);
    }
    baos.close();
    fis.close();
    b = baos.toByteArray();

    return b;
  }

  public static class Container {
    private final byte[] data;

    public Container(byte[] data) {
      this.data = data;
    }
  }
}
