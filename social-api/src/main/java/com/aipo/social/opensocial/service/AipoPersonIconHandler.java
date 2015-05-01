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
package com.aipo.social.opensocial.service;

import java.util.Set;

import org.apache.shindig.config.ContainerConfig;
import org.apache.shindig.protocol.HandlerPreconditions;
import org.apache.shindig.protocol.Operation;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.Service;
import org.apache.shindig.social.opensocial.service.SocialRequestItem;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.aipo.container.protocol.StreamContent;
import com.aipo.social.opensocial.spi.PersonService;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

/**
 * RPC/REST handler for all /peopleIcon requests
 */
@Service(name = "peopleIcon", path = "/{userId}+/{groupId}/{personId}+")
public class AipoPersonIconHandler {

  private final PersonService personService;

  @Inject
  public AipoPersonIconHandler(PersonService personService,
      ContainerConfig config) {
    this.personService = personService;
  }

  /**
   *
   * @param request
   * @return
   * @throws ProtocolException
   */
  @Operation(httpMethods = "GET")
  public StreamContent get(SocialRequestItem request) throws ProtocolException {
    GroupId groupId = request.getGroup();
    Set<String> optionalPersonId =
      ImmutableSet.copyOf(request.getListParameter("personId"));
    // Set<String> fields = request.getFields(Person.Field.DEFAULT_FIELDS);
    Set<UserId> userIds = request.getUsers();

    // Preconditions
    HandlerPreconditions.requireNotEmpty(userIds, "No userId specified");
    if (userIds.size() > 1 && !optionalPersonId.isEmpty()) {
      throw new IllegalArgumentException(
        "Cannot fetch personIds for multiple userIds");
    }

    CollectionOptions options = new CollectionOptions(request);

    if (userIds.size() == 1) {
      if (optionalPersonId.isEmpty()) {
        if (groupId.getType() == GroupId.Type.self) {
          if (options.getFilter() != null) {
            throw new ProtocolException(
              501,
              "filtering is not supported.",
              new UnsupportedOperationException());
          } else {
            // TODO: content typeを確認する方法を考える
            return new StreamContent("image/jpeg", personService.getIcon(
              userIds.iterator().next(),
              request.getToken()));
          }
        } else {
          throw new ProtocolException(
            501,
            "list is not supported.",
            new UnsupportedOperationException());
        }
      } else if (optionalPersonId.size() == 1) {
        UserId userId =
          new UserId(UserId.Type.userId, optionalPersonId.iterator().next());
        // TODO: content typeを確認する方法を考える
        return new StreamContent("image/jpeg", personService.getIcon(
          userId,
          request.getToken()));
      } else {
        throw new ProtocolException(
          501,
          "list is not supported.",
          new UnsupportedOperationException());
      }
    }

    throw new ProtocolException(501, null, new UnsupportedOperationException());
  }
}
