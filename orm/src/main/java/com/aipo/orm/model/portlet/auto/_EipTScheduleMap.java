package com.aipo.orm.model.portlet.auto;

import org.apache.cayenne.CayenneDataObject;

import com.aipo.orm.model.portlet.EipTCommonCategory;
import com.aipo.orm.model.portlet.EipTSchedule;
import com.aipo.orm.model.security.TurbineUserGroupRole;

/**
 * Class _EipTScheduleMap was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _EipTScheduleMap extends CayenneDataObject {

    public static final String COMMON_CATEGORY_ID_PROPERTY = "commonCategoryId";
    public static final String SCHEDULE_ID_PROPERTY = "scheduleId";
    public static final String STATUS_PROPERTY = "status";
    public static final String TYPE_PROPERTY = "type";
    public static final String USER_ID_PROPERTY = "userId";
    public static final String EIP_TCOMMON_CATEGORY_PROPERTY = "eipTCommonCategory";
    public static final String EIP_TSCHEDULE_PROPERTY = "eipTSchedule";
    public static final String TURBINE_USER_GROUP_ROLE_PROPERTY = "turbineUserGroupRole";

    public static final String ID_PK_COLUMN = "ID";

    public void setCommonCategoryId(Integer commonCategoryId) {
        writeProperty("commonCategoryId", commonCategoryId);
    }
    public Integer getCommonCategoryId() {
        return (Integer)readProperty("commonCategoryId");
    }

    public void setScheduleId(Integer scheduleId) {
        writeProperty("scheduleId", scheduleId);
    }
    public Integer getScheduleId() {
        return (Integer)readProperty("scheduleId");
    }

    public void setStatus(String status) {
        writeProperty("status", status);
    }
    public String getStatus() {
        return (String)readProperty("status");
    }

    public void setType(String type) {
        writeProperty("type", type);
    }
    public String getType() {
        return (String)readProperty("type");
    }

    public void setUserId(Integer userId) {
        writeProperty("userId", userId);
    }
    public Integer getUserId() {
        return (Integer)readProperty("userId");
    }

    public void setEipTCommonCategory(EipTCommonCategory eipTCommonCategory) {
        setToOneTarget("eipTCommonCategory", eipTCommonCategory, true);
    }

    public EipTCommonCategory getEipTCommonCategory() {
        return (EipTCommonCategory)readProperty("eipTCommonCategory");
    }


    public void setEipTSchedule(EipTSchedule eipTSchedule) {
        setToOneTarget("eipTSchedule", eipTSchedule, true);
    }

    public EipTSchedule getEipTSchedule() {
        return (EipTSchedule)readProperty("eipTSchedule");
    }


    public void setTurbineUserGroupRole(TurbineUserGroupRole turbineUserGroupRole) {
        setToOneTarget("turbineUserGroupRole", turbineUserGroupRole, true);
    }

    public TurbineUserGroupRole getTurbineUserGroupRole() {
        return (TurbineUserGroupRole)readProperty("turbineUserGroupRole");
    }


}
