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
package org.apache.shindig.common.util;

import java.util.Date;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public final class DateUtil {
  private static final DateTimeFormatter rfc1123DateFormat = DateTimeFormat
    .forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
    .withLocale(Locale.JAPAN)
    .withZone(DateTimeZone.forID("Asia/Tokyo"));

  private static final DateTimeFormatter iso8601DateFormat = ISODateTimeFormat
    .dateTime()
    .withZone(DateTimeZone.forID("Asia/Tokyo"));

  private DateUtil() {
  }

  public static Date parseRfc1123Date(String dateStr) {
    try {
      return rfc1123DateFormat.parseDateTime(dateStr).toDate();
    } catch (Exception e) {
    }
    return null;
  }

  public static Date parseIso8601DateTime(String dateStr) {
    try {
      return new DateTime(dateStr).toDate();
    } catch (Exception e) {
    }
    return null;
  }

  public static String formatIso8601Date(Date date) {
    return formatIso8601Date(date.getTime());
  }

  public static String formatIso8601Date(long time) {
    return iso8601DateFormat.print(time);
  }

  public static String formatRfc1123Date(Date date) {
    return formatRfc1123Date(date.getTime());
  }

  public static String formatRfc1123Date(long timeStamp) {
    return rfc1123DateFormat.print(timeStamp);
  }
}