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

import java.util.Set;
import java.util.concurrent.Future;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.aipo.social.opensocial.model.ALActivity;

/**
 *
 */
public interface ActivityService {

  Future<RestfulCollection<ALActivity>> getActivities(Set<UserId> userIds,
      GroupId groupId, String appId, Set<String> fields,
      CollectionOptions options, SecurityToken token) throws ProtocolException;

  Future<RestfulCollection<ALActivity>> getActivities(UserId userId,
      GroupId groupId, String appId, Set<String> fields,
      CollectionOptions options, Set<String> activityIds, SecurityToken token)
      throws ProtocolException;

  Future<ALActivity> getActivity(UserId userId, GroupId groupId, String appId,
      Set<String> fields, String activityId, SecurityToken token)
      throws ProtocolException;

  Future<Void> deleteActivities(UserId userId, GroupId groupId, String appId,
      Set<String> activityIds, SecurityToken token) throws ProtocolException;

  Future<Void> createActivity(UserId userId, GroupId groupId, String appId,
      Set<String> fields, ALActivity activity, SecurityToken token)
      throws ProtocolException;
}
