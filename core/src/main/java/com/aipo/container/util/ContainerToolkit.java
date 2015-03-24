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
package com.aipo.container.util;

import javax.servlet.http.HttpServletRequest;

import com.aipo.container.http.HttpServletRequestLocator;
import com.aipo.orm.service.ContainerConfigDbService;
import com.aipo.orm.service.ContainerConfigDbService.Property;

public class ContainerToolkit {

  public static String getScheme() {
    HttpServletRequest request = HttpServletRequestLocator.get();
    return request.getScheme();
  }

  public static String getHost(ContainerConfigDbService service) {
    HttpServletRequest request = HttpServletRequestLocator.get();

    if ("true".equalsIgnoreCase(service.get(Property.LOCKED_DOMAIN_REQUIRED))) {
      String unlockedDomain = service.get(Property.UNLOCKED_DOMAIN);
      return (unlockedDomain != null && unlockedDomain.length() > 0)
        ? unlockedDomain
        : getCurrentHost(request);
    } else {
      return getCurrentHost(request);
    }
  }

  protected static String getCurrentHost(HttpServletRequest request) {
    StringBuilder builder = new StringBuilder(request.getServerName());
    int serverPort = request.getServerPort();
    if (serverPort != 80 || serverPort != 443) {
      builder.append(":").append(serverPort);
    }
    return builder.toString();
  }

  private ContainerToolkit() {

  }
}
