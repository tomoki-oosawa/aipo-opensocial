/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2010 Aimluck,Inc.
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

package com.aipo.social.opensocial.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.ImmediateFuture;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.social.core.model.GroupImpl;
import org.apache.shindig.social.opensocial.model.Group;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.GroupId.Type;
import org.apache.shindig.social.opensocial.spi.GroupService;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.aipo.orm.model.account.EipMPost;
import com.aipo.orm.service.EipMPostService;
import com.google.inject.Inject;

/**
 * 
 */
public class AipoGroupService extends AbstractService implements GroupService {

  private final EipMPostService eipMPostService;

  /**
   * 
   */
  @Inject
  public AipoGroupService(EipMPostService eipMPostService) {
    this.eipMPostService = eipMPostService;
  }

  /**
   * @param userId
   * @param options
   * @param fields
   * @param token
   * @return
   */
  public Future<RestfulCollection<Group>> getGroups(UserId userId,
      CollectionOptions collectionOptions, Set<String> fields,
      SecurityToken token) {

    setUp(token);

    List<EipMPost> list =
      eipMPostService.findAll(collectionOptions.getMax(), collectionOptions
        .getFirst());
    List<Group> result = new ArrayList<Group>();
    for (EipMPost post : list) {
      Group group = new GroupImpl();
      GroupId groupId = new GroupId(Type.groupId, post.getGroupName());
      group.setId(groupId);
      group.setTitle(post.getPostName());
      result.add(group);
    }
    int totalResults = eipMPostService.getCountAll();

    RestfulCollection<Group> restCollection =
      new RestfulCollection<Group>(
        result,
        collectionOptions.getFirst(),
        totalResults,
        collectionOptions.getMax());
    return ImmediateFuture.newInstance(restCollection);
  }
}
