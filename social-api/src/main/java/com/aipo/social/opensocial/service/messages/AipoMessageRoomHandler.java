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
package com.aipo.social.opensocial.service.messages;

import java.util.concurrent.Future;

import org.apache.shindig.protocol.Operation;
import org.apache.shindig.protocol.Service;
import org.apache.shindig.social.opensocial.service.SocialRequestItem;

import com.aipo.social.opensocial.spi.MessageService;
import com.google.inject.Inject;

/**
 * Message API :rooms
 */
@Service(name = "messages", path = "/rooms/{userId}+/{groupId}/{roomId}+")
public class AipoMessageRoomHandler {

  private final MessageService service;

  @Inject
  public AipoMessageRoomHandler(MessageService service) {
    this.service = service;
  }

  /**
   * ルーム一覧 GET /messages/rooms/@viewer/@self
   *
   * ルーム詳細 GET /messages/rooms/@viewer/@self/1
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "GET")
  public Future<?> get(SocialRequestItem request) {
    /*-
    {
      "roomId" : 1,
      "name" : "木村 一郎",
      "userId" : "org001:sample1",
      "unreadCount" : 3,
      "isDirect" : true,
      "isAutoName" : true,
      "updateDate" : "2015-04-01T12:31:45+09:00"
    },
    {
      "roomId" : 2,
      "name" : "営業本部",
      "unreadCount" : 3,
      "isDirect" : false,
      "isAutoName" : false,
      "updateDate" : "2015-04-01T12:31:45+09:00"
    }
     */
    // throw new UnsupportedOperationException();
    return service.getRooms(null, null, null, null);
  }

  /**
   * ルーム更新 PUT /messages/rooms/@viewer/@self/1
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "PUT")
  public Future<?> update(SocialRequestItem request) {
    throw new UnsupportedOperationException();
  }

  /**
   * ルーム作成 POST /messages/rooms/@viewer/@self
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "POST")
  public Future<?> create(SocialRequestItem request) {
    throw new UnsupportedOperationException();
  }

  /**
   * ルーム削除 DELETE /messages/rooms/@viewer/@self/1
   *
   * @param request
   * @return
   */
  @Operation(httpMethods = "DELETE")
  public Future<?> delete(SocialRequestItem request) {
    throw new UnsupportedOperationException();
  }
}
