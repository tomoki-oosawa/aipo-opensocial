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
package com.aipo.container.protocol;

import org.apache.shindig.protocol.ProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

public class AipoProtocolException extends ProtocolException {

  private static final long serialVersionUID = 2778407272109027809L;

  private final AipoErrorCode errorCode;

  private final JSONObject response;

  public AipoProtocolException(AipoErrorCode errorCode) {
    super(errorCode.getStatus(), errorCode.getMessage());
    this.errorCode = errorCode;
    this.response = new JSONObject();
    JSONObject error = new JSONObject();
    String errorMessage = errorCode.getMessage();

    try {
      error.put("message", errorMessage);
      error.put("code", errorCode.getCode());
      response.put("error", error);
    } catch (JSONException e) {
      // ignore
    }
  }

  @Override
  public int getCode() {
    return errorCode.getStatus();
  }

  @Override
  public Object getResponse() {
    return response;
  }

  @Override
  public String getMessage() {
    return errorCode.getMessage();
  }
}