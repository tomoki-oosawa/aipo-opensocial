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
package org.apache.shindig.social.opensocial.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.protocol.BaseRequestItem;
import org.apache.shindig.protocol.conversion.BeanConverter;
import org.apache.shindig.protocol.conversion.BeanJsonConverter;
import org.apache.shindig.protocol.multipart.FormDataItem;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.PersonService;
import org.apache.shindig.social.opensocial.spi.UserId;
import org.json.JSONObject;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

/**
 * @see SocialRequestItem
 */
public class SocialRequestItem extends BaseRequestItem {

  private static final String USER_ID = "userId";

  private static final String GROUP_ID = "groupId";

  public SocialRequestItem(Map<String, String[]> parameters,
      Map<String, FormDataItem> formItems, SecurityToken token,
      BeanConverter converter, BeanJsonConverter jsonConverter) {
    super(parameters, formItems, token, converter, jsonConverter);
  }

  public SocialRequestItem(Map<String, String[]> parameters,
      SecurityToken token, BeanConverter converter,
      BeanJsonConverter jsonConverter) {
    super(parameters, token, converter, jsonConverter);
  }

  public SocialRequestItem(JSONObject parameters,
      Map<String, FormDataItem> formItems, SecurityToken token,
      BeanConverter converter, BeanJsonConverter jsonConverter) {
    super(parameters, formItems, token, converter, jsonConverter);
  }

  public Set<UserId> getUsers() {
    List<String> ids = getListParameter(USER_ID);
    if (ids.isEmpty()) {
      Preconditions.checkArgument(
        token.getViewerId() != null,
        "No userId provided and viewer not available");
      // Assume @me
      return ImmutableSet.of(UserId.fromJson("@me"));
    }
    ImmutableSet.Builder<UserId> userIds = ImmutableSet.builder();
    for (String id : ids) {
      userIds.add(UserId.fromJson(id));
    }
    return userIds.build();
  }

  public GroupId getGroup() {
    return GroupId.fromJson(getParameter(GROUP_ID, "@self"));
  }

  @Override
  public String getSortBy() {
    String sortBy = super.getSortBy();
    return sortBy == null ? PersonService.TOP_FRIENDS_SORT : sortBy;
  }

}
