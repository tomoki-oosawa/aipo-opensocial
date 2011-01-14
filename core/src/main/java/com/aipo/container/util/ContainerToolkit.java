/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2011 Aimluck,Inc.
 * http://aipostyle.com/
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

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.access.DataContext;

import com.aipo.container.http.HttpServletRequestLocator;
import com.aipo.orm.Database;
import com.aipo.orm.service.ContainerConfigService;
import com.aipo.orm.service.ContainerConfigService.Property;

public class ContainerToolkit {

  public static String getScheme() {
    HttpServletRequest request = HttpServletRequestLocator.get();
    return request.getScheme();
  }

  public static String getHost() {
    selectDefaultDataDomain();
    HttpServletRequest request = HttpServletRequestLocator.get();
    ContainerConfigService service = new ContainerConfigService();
    if ("true".equalsIgnoreCase(service.get(Property.LOCKED_DOMAIN_REQUIRED))) {
      return service.get(Property.UNLOCKED_DOMAIN);
    } else {
      StringBuilder builder = new StringBuilder(request.getServerName());
      int serverPort = request.getServerPort();
      if (serverPort != 80 || serverPort != 443) {
        builder.append(":").append(serverPort);
      }
      return builder.toString();
    }
  }

  public static void selectDefaultDataDomain() {
    ObjectContext dataContext = null;
    try {
      dataContext = DataContext.getThreadObjectContext();
    } catch (IllegalStateException ignore) {
      // first
    }
    if (dataContext == null) {
      try {
        dataContext = Database.createDataContext("org001");
        DataContext.bindThreadObjectContext(dataContext);
      } catch (Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private ContainerToolkit() {

  }
}
