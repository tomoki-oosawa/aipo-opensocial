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

import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.shindig.protocol.Operation;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.Service;
import org.apache.shindig.protocol.multipart.FormDataItem;
import org.apache.shindig.social.opensocial.service.SocialRequestItem;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.aipo.container.protocol.AipoErrorCode;
import com.aipo.container.protocol.AipoPreconditions;
import com.aipo.container.protocol.AipoProtocolException;
import com.aipo.container.protocol.StreamContent;
import com.aipo.social.opensocial.spi.PersonService;
import com.google.inject.Inject;

/**
 * RPC/REST handler for Icons API
 */
@Service(name = "icons")
public class AipoIconsHandler {

  private final PersonService personService;

  @Inject
  public AipoIconsHandler(PersonService personService) {
    this.personService = personService;
  }

  /**
   * アイコン <br>
   * <code>
   * GET /icons/:userId
   * </code><br>
   * <code>
   * osapi.icons.get( { userId: :userId })
   * </code>
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "GET", path = "/{userId}+")
  public StreamContent get(SocialRequestItem request) throws ProtocolException {
    try {
      Set<UserId> userIds = request.getUsers();

      // Preconditions
      AipoPreconditions.required("userId", userIds);
      AipoPreconditions.notMultiple("userId", userIds);

      InputStream userIcon =
        personService.getIcon(userIds.iterator().next(), request.getToken());
      if (userIcon == null) {
        return null;
      }
      return new StreamContent("image/jpeg", userIcon);
    } catch (ProtocolException e) {
      throw e;
    } catch (Throwable t) {
      throw new AipoProtocolException(AipoErrorCode.INTERNAL_ERROR);
    }
  }

  /**
   * アイコン更新 <br>
   * <code>
   * PUT /icons/:userId
   * </code><br>
   * <code>
   * osapi.icons.update( { userId: :userId })
   * </code>
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = { "PUT", "POST" }, path = "/{userId}+")
  public Future<?> update(SocialRequestItem request) throws ProtocolException {
    try {
      Set<UserId> userIds = request.getUsers();
      FormDataItem profileIcon = request.getFormMimePart("profileIcon");

      // Preconditions
      AipoPreconditions.required("userId", userIds);
      AipoPreconditions.notMultiple("userId", userIds);
      AipoPreconditions.required("profileIcon", profileIcon);

      return personService.putIcon(
        userIds.iterator().next(),
        profileIcon,
        request.getToken());
    } catch (ProtocolException e) {
      throw e;
    } catch (Throwable t) {
      throw new AipoProtocolException(AipoErrorCode.INTERNAL_ERROR);
    }
  }

  /**
   * アイコン削除 <br>
   * <code>
   * PUT /icons/:userId
   * </code><br>
   * <code>
   * osapi.icons.update( { userId: :userId })
   * </code>
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "DELETE", path = "/{userId}+")
  public Future<?> delete(SocialRequestItem request) throws ProtocolException {
    try {
      Set<UserId> userIds = request.getUsers();

      // Preconditions
      AipoPreconditions.required("userId", userIds);
      AipoPreconditions.notMultiple("userId", userIds);

      return personService.deleteIcon(userIds.iterator().next(), request
        .getToken());
    } catch (ProtocolException e) {
      throw e;
    } catch (Throwable t) {
      throw new AipoProtocolException(AipoErrorCode.INTERNAL_ERROR);
    }
  }
}
