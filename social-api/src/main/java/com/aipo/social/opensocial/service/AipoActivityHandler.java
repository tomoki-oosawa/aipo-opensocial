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
package com.aipo.social.opensocial.service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.shindig.config.ContainerConfig;
import org.apache.shindig.protocol.Operation;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RequestItem;
import org.apache.shindig.protocol.Service;
import org.apache.shindig.social.opensocial.service.SocialRequestItem;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.aipo.container.protocol.AipoErrorCode;
import com.aipo.container.protocol.AipoPreconditions;
import com.aipo.container.protocol.AipoProtocolException;
import com.aipo.container.protocol.AipoScope;
import com.aipo.social.opensocial.model.ALActivity;
import com.aipo.social.opensocial.spi.ActivityService;
import com.aipo.social.opensocial.spi.AipoCollectionOptions;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;

/**
 * RPC/REST handler for Activities API
 */
@Service(name = "activities", path = "/{userId}+/{groupId}/{appId}/{activityId}+")
public class AipoActivityHandler {

  private final ActivityService service;

  private final ContainerConfig config;

  @Inject
  public AipoActivityHandler(ActivityService service, ContainerConfig config) {
    this.service = service;
    this.config = config;
  }

  /**
   * Allowed end-points /activities/{userId}/@self/{actvityId}+
   *
   * examples: /activities/john.doe/@self/1
   */
  @Operation(httpMethods = "DELETE")
  public Future<?> delete(SocialRequestItem request) throws ProtocolException {
    try {
      Set<UserId> userIds = request.getUsers();
      Set<String> activityIds =
        ImmutableSet.copyOf(request.getListParameter("activityId"));

      // Preconditions
      AipoPreconditions.validateScope(request.getToken(), AipoScope.W_ALL);
      AipoPreconditions.required("userId", userIds);
      AipoPreconditions.notMultiple("userId", userIds);

      return service.deleteActivities(
        Iterables.getOnlyElement(userIds),
        request.getGroup(),
        request.getAppId(),
        activityIds,
        request.getToken());
    } catch (ProtocolException e) {
      throw e;
    } catch (Throwable t) {
      throw new AipoProtocolException(AipoErrorCode.INTERNAL_ERROR);
    }
  }

  /**
   * Allowed end-points /activities/{userId}/@self
   *
   * examples: /activities/john.doe/@self - postBody is an activity object
   */
  @Operation(httpMethods = "PUT", bodyParam = "activity")
  public Future<?> update(SocialRequestItem request) throws ProtocolException {
    return create(request);
  }

  /**
   * Allowed end-points /activities/{userId}/@self
   *
   * examples: /activities/john.doe/@self - postBody is an activity object
   */
  @Operation(httpMethods = "POST", bodyParam = "activity")
  public Future<?> create(SocialRequestItem request) throws ProtocolException {
    try {
      Set<UserId> userIds = request.getUsers();

      // Preconditions
      AipoPreconditions.validateScope(request.getToken(), AipoScope.W_ALL);
      AipoPreconditions.required("userId", userIds);
      AipoPreconditions.notMultiple("userId", userIds);

      return service.createActivity(Iterables.getOnlyElement(userIds), request
        .getGroup(), request.getAppId(), request.getFields(), request
        .getTypedParameter("activity", ALActivity.class), request.getToken());
    } catch (ProtocolException e) {
      throw e;
    } catch (Throwable t) {
      throw new AipoProtocolException(AipoErrorCode.INTERNAL_ERROR);
    }
  }

  /**
   * Allowed end-points /activities/{userId}/{groupId}/{optionalActvityId}+
   * /activities/{userId}+/{groupId}
   *
   * examples: /activities/john.doe/@self/1 /activities/john.doe/@self
   * /activities/john.doe,jane.doe/@friends
   */
  @Operation(httpMethods = "GET")
  public Future<?> get(SocialRequestItem request) throws ProtocolException {
    try {
      Set<UserId> userIds = request.getUsers();
      Set<String> optionalActivityIds =
        ImmutableSet.copyOf(request.getListParameter("activityId"));

      AipoCollectionOptions options = new AipoCollectionOptions(request);

      // Preconditions
      AipoPreconditions.validateScope(request.getToken(), AipoScope.R_ALL);
      AipoPreconditions.required("userId", userIds);
      AipoPreconditions.notMultiple("userId", userIds);

      if (!optionalActivityIds.isEmpty()) {
        if (optionalActivityIds.size() == 1) {
          return service.getActivity(
            userIds.iterator().next(),
            request.getGroup(),
            request.getAppId(),
            request.getFields(),
            optionalActivityIds.iterator().next(),
            request.getToken());
        } else {
          return service.getActivities(
            userIds.iterator().next(),
            request.getGroup(),
            request.getAppId(),
            request.getFields(),
            options,
            optionalActivityIds,
            request.getToken());
        }
      }

      return service.getActivities(
        userIds.iterator().next(),
        request.getGroup(),
        request.getAppId(),
        request.getFields(),
        options,
        null,
        request.getToken());
    } catch (ProtocolException e) {
      throw e;
    } catch (Throwable t) {
      throw new AipoProtocolException(AipoErrorCode.INTERNAL_ERROR);
    }
  }

  @Operation(httpMethods = "GET", path = "/@supportedFields")
  public List<Object> supportedFields(RequestItem request) {
    try {
      String container =
        Objects.firstNonNull(
          request.getToken().getContainer(),
          ContainerConfig.DEFAULT_CONTAINER);
      return config.getList(
        container,
        "${Cur['gadgets.features'].opensocial.supportedFields.activity}");
    } catch (ProtocolException e) {
      throw e;
    } catch (Throwable t) {
      throw new AipoProtocolException(AipoErrorCode.INTERNAL_ERROR);
    }
  }
}
