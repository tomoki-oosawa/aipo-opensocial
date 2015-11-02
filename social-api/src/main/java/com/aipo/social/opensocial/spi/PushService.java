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

import java.util.List;
import java.util.Map;

import org.apache.shindig.protocol.ProtocolException;

/**
 *
 */
public interface PushService {

  public void pushAsync(PushType type, Map<String, String> params,
      List<String> recipients) throws ProtocolException;

  enum PushType {
    MESSAGE("messagev2"), MESSAGE_READ("messagev2_read"), MESSAGE_DELETE(
        "messagev2_delete"), MESSAGE_ROOM("messagev2_room"), MESSAGE_ROOM_DELETE(
        "messagev2_room_delete");

    private final String value;

    private PushType(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }
  }
}
