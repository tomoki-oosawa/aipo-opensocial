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
package com.aipo.orm.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aipo.orm.Database;
import com.aipo.orm.model.security.TurbineUser;
import com.aipo.orm.model.social.Activity;
import com.aipo.orm.model.social.ActivityMap;
import com.aipo.orm.query.Operations;
import com.aipo.orm.service.ContainerConfigDbService.Property;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AipoActivityDbService implements ActivityDbService {

  private final TurbineUserDbService turbineUserDbService;

  private final ContainerConfigDbService containerConfigDbService;

  @Inject
  public AipoActivityDbService(TurbineUserDbService turbineUserDbService,
      ContainerConfigDbService containerConfigDbService) {
    this.turbineUserDbService = turbineUserDbService;
    this.containerConfigDbService = containerConfigDbService;
  }

  public void create(String username, String appId, Map<String, Object> values) {

    try {
      String externalId = (String) values.get("externalId");
      Activity activity = null;
      boolean replace = true;
      if (externalId != null && externalId.length() > 0) {
        activity =
          Database
            .query(Activity.class)
            .where(Operations.eq(Activity.APP_ID_PROPERTY, appId))
            .where(Operations.eq(Activity.EXTERNAL_ID_PROPERTY, externalId))
            .where(Operations.eq(Activity.LOGIN_NAME_PROPERTY, username))
            .fetchSingle();
      }
      if (activity == null) {
        activity = Database.create(Activity.class);
        replace = false;
      }
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

      Long moduleId = (Long) values.get("moduleId");
      activity.setModuleId(moduleId == null ? 0 : moduleId.intValue());

      @SuppressWarnings("unchecked")
      Set<String> recipients = (Set<String>) values.get("recipients");
      List<ActivityMap> activityMaps = null;
      if (replace) {
        activityMaps =
          Database.query(ActivityMap.class).where(
            Operations.eq(ActivityMap.ACTIVITY_PROPERTY, activity)).fetchList();
      } else {
        activityMaps = new ArrayList<ActivityMap>();
      }
      if (recipients != null && recipients.size() > 0) {
        List<TurbineUser> list =
          turbineUserDbService.findByUsername(recipients);

        for (TurbineUser recipient : list) {
          boolean exists = false;
          for (ActivityMap m : activityMaps) {
            String loginName = m.getLoginName();
            if (loginName.equals(recipient.getLoginName())) {
              m.setIsRead(0);
              exists = true;
              break;
            }
          }
          if (!exists) {
            ActivityMap activityMap = Database.create(ActivityMap.class);
            activityMap.setLoginName(recipient.getLoginName());
            activityMap.setActivity(activity);
            activityMap.setIsRead(0);
          }
        }
      } else {
        if (activityMaps.size() == 0) {
          // recipients が指定されなかった場合は、priority は　0 とする。
          activity.setPriority(0f);
          ActivityMap activityMap = Database.create(ActivityMap.class);
          activityMap.setLoginName("-1");
          activityMap.setActivity(activity);
          activityMap.setIsRead(1);
        }
      }

      String activitySaveLimit =
        containerConfigDbService.get(Property.ACTIVITY_SAVE_LIMIT);
      int limit = 30;
      try {
        limit = Integer.valueOf(activitySaveLimit).intValue();
      } catch (Throwable ignore) {

      }
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DAY_OF_MONTH, -limit);

      Database.query(ActivityMap.class).where(
        Operations.lt(ActivityMap.ACTIVITY_PROPERTY
          + "."
          + Activity.UPDATE_DATE_PROPERTY, cal.getTime())).deleteAll();

      String sql =
        "delete from activity where update_date < '"
          + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal.getTime())
          + "'";
      Database.sql(Activity.class, sql).execute();

      Database.commit();
    } catch (Throwable t) {
      Database.rollback();
      throw new RuntimeException(t);
    }
  }
}