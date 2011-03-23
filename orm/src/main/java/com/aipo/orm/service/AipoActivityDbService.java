/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2011 Aimluck,Inc.
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

package com.aipo.orm.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aipo.orm.Database;
import com.aipo.orm.model.security.TurbineUser;
import com.aipo.orm.model.social.Activity;
import com.aipo.orm.model.social.ActivityMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AipoActivityDbService implements ActivityDbService {

  private final TurbineUserDbService turbineUserDbService;

  @Inject
  public AipoActivityDbService(TurbineUserDbService turbineUserDbService) {
    this.turbineUserDbService = turbineUserDbService;
  }

  public void create(String username, String appId, Map<String, Object> values) {

    try {
      Activity activity = Database.create(Activity.class);
      activity.setAppId(appId);
      activity.setLoginName(username);
      activity.setBody((String) values.get("body"));
      activity.setExternalId((String) values.get("externalId"));
      // priority は 0 <= 1 の間
      Float priority = (Float) values.get("priority");
      if (priority == null) {
        priority = 0f;
      }
      if (priority < 0) {
        priority = 0f;
      }
      if (priority > 1) {
        priority = 1f;
      }
      activity.setPriority(priority);
      activity.setTitle((String) values.get("title"));
      activity.setUpdateDate(new Date());

      activity.setIcon((String) values.get("icon"));

      @SuppressWarnings("unchecked")
      Set<String> recipients = (Set<String>) values.get("recipients");
      if (recipients != null && recipients.size() > 0) {
        List<TurbineUser> list =
          turbineUserDbService.findByUsername(recipients);
        for (TurbineUser recipient : list) {
          ActivityMap activityMap = Database.create(ActivityMap.class);
          activityMap.setLoginName(recipient.getLoginName());
          activityMap.setActivity(activity);
          activityMap.setIsRead(0);
        }
      } else {
        ActivityMap activityMap = Database.create(ActivityMap.class);
        activityMap.setLoginName("-1");
        activityMap.setActivity(activity);
        activityMap.setIsRead(1);
      }
      Database.commit();
    } catch (Throwable t) {
      Database.rollback();
      t.printStackTrace();
      throw new RuntimeException(t);
    }
  }
}