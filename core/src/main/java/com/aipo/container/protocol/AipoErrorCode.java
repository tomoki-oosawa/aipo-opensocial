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

import org.json.JSONException;
import org.json.JSONObject;

public enum AipoErrorCode {

  BAD_REQUEST {
    @Override
    int getCode() {
      return getStatus() + 1000;
    }

    @Override
    protected String getDefaultMessage() {
      return "Bad Request";
    }

    @Override
    int getStatus() {
      return 403;
    }
  },

  NOT_FOUND {
    @Override
    int getCode() {
      return getStatus() + 1000;
    }

    @Override
    protected String getDefaultMessage() {
      return "Not Found";
    }

    @Override
    int getStatus() {
      return 404;
    }
  },

  INTERNAL_ERROR {
    @Override
    int getCode() {
      return getStatus() + 1000;
    }

    @Override
    protected String getDefaultMessage() {
      return "Internal Server Error";
    }

    @Override
    int getStatus() {
      return 500;
    }
  },

  NOT_IMPLEMENTED {
    @Override
    int getCode() {
      return getStatus() + 1000;
    }

    @Override
    protected String getDefaultMessage() {
      return "Not Implemented";
    }

    @Override
    int getStatus() {
      return 501;
    }
  },

  BAD_GATEWAY {
    @Override
    int getCode() {
      return getStatus() + 1000;
    }

    @Override
    protected String getDefaultMessage() {
      return "Bad Gateway";
    }

    @Override
    int getStatus() {
      return 502;
    }
  },

  SERVICE_UNAVAILABLE {
    @Override
    int getCode() {
      return getStatus() + 1000;
    }

    @Override
    protected String getDefaultMessage() {
      return "Service Unavailable";
    }

    @Override
    int getStatus() {
      return 503;
    }
  },

  GATEWAY_TIMEOUT {
    @Override
    int getCode() {
      return getStatus() + 1000;
    }

    @Override
    protected String getDefaultMessage() {
      return "Gateway Timeout";
    }

    @Override
    int getStatus() {
      return 504;
    }
  },

  TOKEN_EXPIRED {
    @Override
    int getCode() {
      return 1001;
    }

    @Override
    protected String getDefaultMessage() {
      return "Invalid or expired token.";
    }

    @Override
    int getStatus() {
      return 401;
    }
  },

  VALIDATE_ERROR {
    @Override
    int getCode() {
      return 1002;
    }

    @Override
    protected String getDefaultMessage() {
      return "Parameter is not valid.";
    }

    @Override
    int getStatus() {
      return 403;
    }
  },

  UNSUPPORTED_OPERATION {
    @Override
    int getCode() {
      return 1003;
    }

    @Override
    protected String getDefaultMessage() {
      return "Unsupported operation.";
    }

    @Override
    int getStatus() {
      return 501;
    }
  },

  NO_SERIVCE_DEFINED {
    @Override
    int getCode() {
      return 1004;
    }

    @Override
    protected String getDefaultMessage() {
      return "No service defined for path.";
    }

    @Override
    int getStatus() {
      return 404;
    }
  };

  private String message = null;

  abstract int getStatus();

  abstract int getCode();

  protected abstract String getDefaultMessage();

  public String getMessage() {
    if (message != null) {
      return message;
    } else {
      return getDefaultMessage();
    }
  }

  public AipoErrorCode customMessage(String message) {
    this.message = message;
    return this;
  }

  public JSONObject responseJSON() {
    JSONObject object = new JSONObject();
    JSONObject error = new JSONObject();
    String errorMessage = getMessage();

    try {
      error.put("message", errorMessage);
      error.put("code", getCode());
      object.put("error", error);
    } catch (JSONException e) {
      // ignore
    }

    return object;
  }

  public String responseHTML() {
    return "<!DOCTYPE html><html><title>"
      + getStatus()
      + " Error</title><body><p>"
      + getCode()
      + ": "
      + getMessage()
      + "</p></body></html>";
  }

}