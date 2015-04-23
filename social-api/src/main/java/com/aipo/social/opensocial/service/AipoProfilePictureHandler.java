/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
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
 * RPC/REST handler for all /people requests
 */
@Service(name = "pic", path = "/{userId}+/{groupId}/{personId}+")
public class AipoProfilePictureHandler {

  private final PersonService personService;

  // private final ContainerConfig config;

  @Inject
  public AipoProfilePictureHandler(PersonService personService,
      ContainerConfig config) {
    this.personService = personService;
    // this.config = config;
  }

  /**
   * Allowed end-points /people/{userId}+/{groupId}
   * /people/{userId}/{groupId}/{optionalPersonId}+
   *
   * examples: /people/john.doe/@all /people/john.doe/@friends /people/john.doe/@self
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
          // If a filter is set then we have to call getPeople(), otherwise use
          // the simpler getPerson()
          if (options.getFilter() != null) {
            throw new ProtocolException(
              501,
              "filtering is not supported.",
              new UnsupportedOperationException());
          } else {
            // TODO: content typeを確認する方法を考える
            return new StreamContent("image/jpeg", personService
              .getProfilePicture(userIds.iterator().next(), request.getToken()));
          }
        } else {
          throw new ProtocolException(
            501,
            "list is not supported.",
            new UnsupportedOperationException());
        }
      } else if (optionalPersonId.size() == 1) {
        // TODO: Add some crazy concept to handle the userId?
        throw new ProtocolException(
          501,
          null,
          new UnsupportedOperationException());
      } else {
        throw new ProtocolException(
          501,
          null,
          new UnsupportedOperationException());
      }
    }

    // Every other case is a collection response.
    throw new ProtocolException(501, null, new UnsupportedOperationException());
  }
}
