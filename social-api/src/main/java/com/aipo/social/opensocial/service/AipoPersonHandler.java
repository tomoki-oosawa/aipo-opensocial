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

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.shindig.common.util.FutureUtil;
import org.apache.shindig.config.ContainerConfig;
import org.apache.shindig.protocol.HandlerPreconditions;
import org.apache.shindig.protocol.Operation;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RequestItem;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.protocol.Service;
import org.apache.shindig.social.opensocial.model.Person;
import org.apache.shindig.social.opensocial.service.SocialRequestItem;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.aipo.social.opensocial.model.ALPerson;
import com.aipo.social.opensocial.spi.PersonService;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

/**
 *
 */
@Service(name = "people", path = "/{userId}+/{groupId}/{personId}+")
public class AipoPersonHandler {

  private final PersonService personService;

  private final ContainerConfig config;

  @Inject
  public AipoPersonHandler(PersonService personService, ContainerConfig config) {
    this.personService = personService;
    this.config = config;
  }

  /**
   * Allowed end-points /people/{userId}+/{groupId}
   * /people/{userId}/{groupId}/{optionalPersonId}+
   *
   * examples: /people/john.doe/@all /people/john.doe/@friends /people/john.doe/@self
   */
  @Operation(httpMethods = "GET")
  public Future<?> get(SocialRequestItem request) throws ProtocolException {
    GroupId groupId = request.getGroup();
    Set<String> optionalPersonId =
      ImmutableSet.copyOf(request.getListParameter("personId"));
    Set<String> fields = request.getFields(Person.Field.DEFAULT_FIELDS);
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
          // If a filter is set then we have to call getPeople(), otherwise use
          // the simpler getPerson()
          if (options.getFilter() != null) {
            Future<RestfulCollection<ALPerson>> people =
              personService.getPeople(
                userIds,
                groupId,
                options,
                fields,
                request.getToken());
            return FutureUtil.getFirstFromCollection(people);
          } else {
            return personService.getPerson(
              userIds.iterator().next(),
              fields,
              request.getToken());
          }
        } else {
          return personService.getPeople(
            userIds,
            groupId,
            options,
            fields,
            request.getToken());
        }
      } else if (optionalPersonId.size() == 1) {
        // TODO: Add some crazy concept to handle the userId?
        Set<UserId> optionalUserIds =
          ImmutableSet.of(new UserId(UserId.Type.userId, optionalPersonId
            .iterator()
            .next()));

        Future<RestfulCollection<ALPerson>> people =
          personService.getPeople(optionalUserIds, new GroupId(
            GroupId.Type.self,
            null), options, fields, request.getToken());
        return FutureUtil.getFirstFromCollection(people);
      } else {
        ImmutableSet.Builder<UserId> personIds = ImmutableSet.builder();
        for (String pid : optionalPersonId) {
          personIds.add(new UserId(UserId.Type.userId, pid));
        }
        // Every other case is a collection response of optional person ids
        return personService.getPeople(personIds.build(), new GroupId(
          GroupId.Type.self,
          null), options, fields, request.getToken());
      }
    }

    // Every other case is a collection response.
    return personService.getPeople(userIds, groupId, options, fields, request
      .getToken());
  }

  @Operation(httpMethods = "GET", path = "/@supportedFields")
  public List<Object> supportedFields(RequestItem request) {
    // TODO: Would be nice if name in config matched name of service.
    String container =
      Objects.firstNonNull(request.getToken().getContainer(), "default");
    return config.getList(
      container,
      "${Cur['gadgets.features'].opensocial.supportedFields.person}");
  }
}
