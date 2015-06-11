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
package com.aipo.orm.service;

import java.util.List;
import java.util.Set;

import com.aipo.orm.model.portlet.EipTMessage;
import com.aipo.orm.model.portlet.EipTMessageRoom;
import com.aipo.orm.service.request.SearchOptions;

public interface MessageDbService {

  public List<EipTMessageRoom> findMessageRoom(int roomId, String username,
      SearchOptions options);

  public List<EipTMessage> findMessage(int roomId, int messageId,
      SearchOptions options);

  public void createRoom(String username, String name,
      List<String> memberNameList, Set<String> fields);

  public EipTMessage createMessage(String username, Integer roomId,
      String targetUsername, String message, Set<String> fields);

  public EipTMessageRoom updateRoom(Integer roomId, String username,
      String name, List<String> memberNameList);

}