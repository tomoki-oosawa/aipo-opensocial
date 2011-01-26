/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2011 Aimluck,Inc.
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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.ImmediateFuture;
import org.apache.shindig.protocol.DataCollection;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.social.opensocial.spi.AppDataService;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.aipo.orm.service.AppDataDbService;
import com.google.inject.Inject;

/**
 * 
 */
public class AipoAppDataService extends AbstractService implements
    AppDataService {

  private final AppDataDbService appDataDbService;

  /**
   * 
   */
  @Inject
  public AipoAppDataService(AppDataDbService activityDbService) {
    this.appDataDbService = activityDbService;
  }

  /**
   * @param userId
   * @param groupId
   * @param fields
   * @param values
   * @param token
   * @return
   * @throws ProtocolException
   */
  public Future<Void> updatePersonData(UserId userId, GroupId groupId,
      String appId, Set<String> fields, Map<String, String> values,
      SecurityToken token) throws ProtocolException {
    return ImmediateFuture.newInstance(null);
  }

  /**
   * @param userId
   * @param groupId
   * @param appId
   * @param fields
   * @param token
   * @return
   * @throws ProtocolException
   */
  public Future<Void> deletePersonData(UserId userId, GroupId groupId,
      String appId, Set<String> fields, SecurityToken token)
      throws ProtocolException {
    return ImmediateFuture.newInstance(null);
  }

  /**
   * @param userIds
   * @param groupId
   * @param appId
   * @param fields
   * @param token
   * @return
   * @throws ProtocolException
   */
  public Future<DataCollection> getPersonData(Set<UserId> userIds,
      GroupId groupId, String appId, Set<String> fields, SecurityToken token)
      throws ProtocolException {
    return null;
  }

}
