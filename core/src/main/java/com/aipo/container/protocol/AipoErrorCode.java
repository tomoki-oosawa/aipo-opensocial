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
package com.aipo.container.protocol;

import javax.servlet.http.HttpServletResponse;

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
      return HttpServletResponse.SC_BAD_REQUEST;
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
      return HttpServletResponse.SC_NOT_FOUND;
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
      return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
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
      return HttpServletResponse.SC_NOT_IMPLEMENTED;
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
      return HttpServletResponse.SC_BAD_GATEWAY;
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
      return HttpServletResponse.SC_SERVICE_UNAVAILABLE;
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
      return HttpServletResponse.SC_GATEWAY_TIMEOUT;
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
      return HttpServletResponse.SC_UNAUTHORIZED;
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
      return HttpServletResponse.SC_BAD_REQUEST;
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
      return HttpServletResponse.SC_NOT_IMPLEMENTED;
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
      return HttpServletResponse.SC_NOT_FOUND;
    }
  },

  ICON_NOT_FOUND {
    @Override
    int getCode() {
      return 1005;
    }

    @Override
    protected String getDefaultMessage() {
      return "Icon not found.";
    }

    @Override
    int getStatus() {
      return HttpServletResponse.SC_NOT_FOUND;
    }
  },

  VALIDATE_IMAGE_SIZE_200 {
    @Override
    int getCode() {
      return 1006;
    }

    @Override
    protected String getDefaultMessage() {
      return "Upload image must be more than 200x200.";
    }

    @Override
    int getStatus() {
      return HttpServletResponse.SC_BAD_REQUEST;
    }
  },

  VALIDATE_IMAGE_FORMAT {
    @Override
    int getCode() {
      return 1007;
    }

    @Override
    protected String getDefaultMessage() {
      return "Upload image is not valid.";
    }

    @Override
    int getStatus() {
      return HttpServletResponse.SC_BAD_REQUEST;
    }
  },

  VALIDATE_ACCESS_DENIED {
    @Override
    int getCode() {
      return 1008;
    }

    @Override
    protected String getDefaultMessage() {
      return "Access denied.";
    }

    @Override
    int getStatus() {
      return HttpServletResponse.SC_BAD_REQUEST;
    }
  },

  FILE_NOT_FOUND {
    @Override
    int getCode() {
      return 1009;
    }

    @Override
    protected String getDefaultMessage() {
      return "File not found.";
    }

    @Override
    int getStatus() {
      return HttpServletResponse.SC_NOT_FOUND;
    }
  },

  INVALID_UER {
    @Override
    int getCode() {
      return 1010;
    }

    @Override
    protected String getDefaultMessage() {
      return "Invalid user.";
    }

    @Override
    int getStatus() {
      return HttpServletResponse.SC_FORBIDDEN;
    }
  },

  OVER_QUOTA {
    @Override
    int getCode() {
      return 1011;
    }

    @Override
    protected String getDefaultMessage() {
      return "Over quota.";
    }

    @Override
    int getStatus() {
      return HttpServletResponse.SC_FORBIDDEN;
    }
  },

  IP_ACCESS_DENIED {
    @Override
    int getCode() {
      return 1012;
    }

    @Override
    protected String getDefaultMessage() {
      return "Your ip address is denied.";
    }

    @Override
    int getStatus() {
      return HttpServletResponse.SC_FORBIDDEN;
    }
  },

  DEVICE_ACCESS_DENIED {
    @Override
    int getCode() {
      return 1013;
    }

    @Override
    protected String getDefaultMessage() {
      return "Your device is denied.";
    }

    @Override
    int getStatus() {
      return HttpServletResponse.SC_FORBIDDEN;
    }
  },

  NOTIFICATION_NOT_FOUND {
    @Override
    int getCode() {
      return 1014;
    }

    @Override
    protected String getDefaultMessage() {
      return "Notification not found.";
    }

    @Override
    int getStatus() {
      return HttpServletResponse.SC_NOT_FOUND;
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