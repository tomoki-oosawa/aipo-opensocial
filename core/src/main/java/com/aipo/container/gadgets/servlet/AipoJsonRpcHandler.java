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
package com.aipo.container.gadgets.servlet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import org.apache.shindig.gadgets.Gadget;
import org.apache.shindig.gadgets.GadgetContext;
import org.apache.shindig.gadgets.process.Processor;
import org.apache.shindig.gadgets.servlet.JsonRpcHandler;
import org.apache.shindig.gadgets.spec.GadgetSpec;
import org.apache.shindig.gadgets.spec.Icon;
import org.apache.shindig.gadgets.spec.ModulePrefs;
import org.apache.shindig.gadgets.spec.OAuthService;
import org.apache.shindig.gadgets.spec.OAuthSpec;
import org.apache.shindig.gadgets.uri.IframeUriManager;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;

/**
 * @see JsonRpcHandler
 */
public class AipoJsonRpcHandler extends JsonRpcHandler {

  /**
   * @param executor
   * @param processor
   * @param iframeUriManager
   */
  @Inject
  public AipoJsonRpcHandler(ExecutorService executor, Processor processor,
      IframeUriManager iframeUriManager) {
    super(executor, processor, iframeUriManager);
  }

  @Override
  protected Job createNewJob(GadgetContext context) {
    return new JobWrapper(context);
  }

  protected class JobWrapper extends Job {

    protected final GadgetContext context;

    public JobWrapper(GadgetContext context) {
      super(context);
      this.context = context;
    }

    @Override
    protected JSONObject getGadgetJson(Gadget gadget, GadgetSpec spec)
        throws JSONException {
      try {
        JSONObject gadgetJson = super.getGadgetJson(gadget, spec);
        GadgetContext gadgetContext = gadget.getContext();
        ModulePrefs modulePrefs = spec.getModulePrefs();
        String withDescription = gadgetContext.getParameter("withDescription");
        if (withDescription != null) {
          gadgetJson.put("description", modulePrefs.getDescription());
        }
        List<Icon> icons = modulePrefs.getIcons();
        String iconUrl = "";
        for (Icon icon : icons) {
          String mode = icon.getMode();
          if (mode == null || mode.isEmpty()) {
            iconUrl = icon.getContent();
            break;
          }
        }
        gadgetJson.put("icon", iconUrl);
        String withOAuthService =
          gadgetContext.getParameter("withOAuthService");
        if (withOAuthService != null) {
          OAuthSpec oAuthSpec = modulePrefs.getOAuthSpec();
          List<JSONObject> jsonEnums = new ArrayList<JSONObject>();
          if (oAuthSpec != null) {
            Map<String, OAuthService> services =
              modulePrefs.getOAuthSpec().getServices();
            Iterator<Entry<String, OAuthService>> iterator =
              services.entrySet().iterator();
            while (iterator.hasNext()) {
              JSONObject service = new JSONObject();
              Entry<String, OAuthService> next = iterator.next();
              OAuthService value = next.getValue();
              service.put("name", value.getName());
              service.put("accessUrl", value.getAccessUrl().url.toString());
              service.put("requestUrl", value.getRequestUrl().url.toString());
              service.put("authorizationUrl", value
                .getAuthorizationUrl()
                .toString());
              jsonEnums.add(service);
            }
          }
          gadgetJson.put("oauthService", jsonEnums);
        }
        return gadgetJson;
      } catch (Throwable t) {
        t.printStackTrace();
        return null;
      }
    }

  }
}
