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
import java.util.concurrent.Future;

import org.apache.shindig.protocol.Operation;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.Service;
import org.apache.shindig.social.opensocial.service.SocialRequestItem;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.aipo.container.protocol.AipoErrorCode;
import com.aipo.container.protocol.AipoPreconditions;
import com.aipo.container.protocol.AipoProtocolException;
import com.aipo.container.protocol.AipoScope;
import com.aipo.social.opensocial.spi.AipoCollectionOptions;
import com.aipo.social.opensocial.spi.GroupService;
import com.google.inject.Inject;

/**
 * RPC/REST handler for Groups API
 */
@Service(name = "groups", path = "/{userId}")
public class AipoGroupHandler {

  private final GroupService service;

  @Inject
  public AipoGroupHandler(GroupService service) {
    this.service = service;
  }

  @Operation(httpMethods = "GET")
  public Future<?> get(SocialRequestItem request) throws ProtocolException {
    try {
      Set<UserId> userIds = request.getUsers();
      AipoCollectionOptions options = new AipoCollectionOptions(request);

      // Preconditions
      AipoPreconditions.validateScope(request.getToken(), AipoScope.R_ALL);
      AipoPreconditions.required("userId", userIds);
      AipoPreconditions.notMultiple("userId", userIds);

      return service.getGroups(userIds.iterator().next(), options, request
        .getFields(), request.getToken());
    } catch (ProtocolException e) {
      throw e;
    } catch (Throwable t) {
      throw new AipoProtocolException(AipoErrorCode.INTERNAL_ERROR);
    }
  }
}
