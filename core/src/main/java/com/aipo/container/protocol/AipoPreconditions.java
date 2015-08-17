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

package com.aipo.container.protocol;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.shindig.protocol.multipart.FormDataItem;

/**
 *
 */
public class AipoPreconditions {

  public static void required(String name, String value)
      throws AipoProtocolException {
    if (value == null || value.length() == 0) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ERROR
        .customMessage("Parameter " + name + " is required."));
    }
  }

  public static void required(String name, Collection<?> value)
      throws AipoProtocolException {
    if (value == null || value.size() == 0) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ERROR
        .customMessage("Parameter " + name + " is required."));
    }
  }

  public static void required(String name, FormDataItem value)
      throws AipoProtocolException {
    if (value == null || value.getSize() == 0) {
      new AipoProtocolException(AipoErrorCode.VALIDATE_ERROR
        .customMessage("Parameter " + name + " is required."));
    }
  }

  public static void maxLength(String name, String value, int maxLength)
      throws AipoProtocolException {
    if (value == null || value.length() == 0 || value.length() > maxLength) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ERROR
        .customMessage("Parameter "
          + name
          + "'s maximum length is "
          + maxLength
          + "."));
    }
  }

  public static int isIntegerOrNull(String name, String value)
      throws AipoProtocolException {
    if (value == null) {
      return 0;
    }
    try {
      return Integer.valueOf(value).intValue();
    } catch (Throwable t) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ERROR
        .customMessage("Parameter " + name + " is not integer."));
    }
  }

  public static int isInteger(String name, String value)
      throws AipoProtocolException {
    try {
      return Integer.valueOf(value).intValue();
    } catch (Throwable t) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ERROR
        .customMessage("Parameter " + name + " is not integer."));
    }
  }

  public static void isUTF8(String name, String value)
      throws AipoProtocolException {
    Pattern PATTERN = Pattern.compile("[\\u0000-\\uFFFF]*");
    Matcher m = PATTERN.matcher(value);
    if (!m.matches()) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ERROR
        .customMessage("Parameter " + name + " is not proper UTF-8."));
    }
  }

  public static void multiple(String name, Collection<?> value)
      throws AipoProtocolException {
    if (value == null || value.size() <= 1) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ERROR
        .customMessage("Parameter " + name + " must specify multiple values."));
    }
  }

  public static void notMultiple(String name, Collection<?> value)
      throws AipoProtocolException {
    if (value != null && value.size() > 1) {
      throw new AipoProtocolException(
        AipoErrorCode.VALIDATE_ERROR.customMessage("Parameter "
          + name
          + " cannot specify multiple values."));
    }
  }
}
