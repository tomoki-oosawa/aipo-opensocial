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
package com.aipo.util;

public class CommonUtils {
  /**
   * 入力フィールド値の左右の全角スペースを削除します。
   *
   * @param str
   * @return
   */
  public static String removeSpace(String str) {
    int len = str.length();
    int st = 0;
    char[] val = str.toCharArray();

    while ((st < len) && (val[st] <= ' ' || val[st] == 0x3000)) {
      st++;
    }
    while ((st < len) && (val[len - 1] <= ' ' || val[len - 1] == 0x3000)) {
      len--;
    }
    return ((st > 0) || (len < str.length())) ? str.substring(st, len) : str;
  }

  /**
   * 第二引数で指定した長さで、第一引数の文字列を丸める。
   *
   * @param src
   *          元の文字列
   * @param length
   *          丸めの長さ
   * @return ●処理後の文字列
   */
  public static String compressString(String src, int length) {
    if (src == null || src.length() == 0 || length <= 0) {
      return src;
    }

    String subject;
    if (src.length() > length) {
      subject = src.substring(0, length);
      subject += "・・・";
    } else {
      subject = src;
    }
    return subject;
  }
}
