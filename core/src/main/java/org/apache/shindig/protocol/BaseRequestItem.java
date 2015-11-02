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
package org.apache.shindig.protocol;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.protocol.conversion.BeanConverter;
import org.apache.shindig.protocol.conversion.BeanJsonConverter;
import org.apache.shindig.protocol.model.FilterOperation;
import org.apache.shindig.protocol.model.SortOrder;
import org.apache.shindig.protocol.multipart.FormDataItem;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aipo.container.protocol.AipoErrorCode;
import com.aipo.container.protocol.AipoProtocolException;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @see BaseRequestItem
 */
public class BaseRequestItem implements RequestItem {

  protected final SecurityToken token;

  final BeanConverter converter;

  final Map<String, Object> parameters;

  final Map<String, FormDataItem> formItems;

  final BeanJsonConverter jsonConverter;

  Map<String, Object> attributes;

  public BaseRequestItem(Map<String, String[]> parameters,
      Map<String, FormDataItem> formItems, SecurityToken token,
      BeanConverter converter, BeanJsonConverter jsonConverter) {
    this.token = token;
    this.converter = converter;
    this.parameters = Maps.newHashMap();

    for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
      if (entry.getValue() == null) {
        setParameter(entry.getKey(), null);
      } else if (entry.getValue().length == 1) {
        setParameter(entry.getKey(), entry.getValue()[0]);
      } else {
        setParameter(entry.getKey(), Lists.newArrayList(entry.getValue()));
      }
    }
    this.jsonConverter = jsonConverter;
    this.formItems = formItems;
  }

  public BaseRequestItem(Map<String, String[]> parameters, SecurityToken token,
      BeanConverter converter, BeanJsonConverter jsonConverter) {
    this.token = token;
    this.converter = converter;
    this.parameters = Maps.newHashMap();

    for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
      if (entry.getValue() == null) {
        setParameter(entry.getKey(), null);
      } else if (entry.getValue().length == 1) {
        setParameter(entry.getKey(), entry.getValue()[0]);
      } else {
        setParameter(entry.getKey(), Lists.newArrayList(entry.getValue()));
      }
    }
    this.jsonConverter = jsonConverter;
    this.formItems = null;
  }

  public BaseRequestItem(JSONObject parameters,
      Map<String, FormDataItem> formItems, SecurityToken token,
      BeanConverter converter, BeanJsonConverter jsonConverter) {
    try {
      this.parameters = Maps.newHashMap();
      @SuppressWarnings("unchecked")
      // JSONObject keys are always strings
      Iterator<String> keys = parameters.keys();
      while (keys.hasNext()) {
        String key = keys.next();
        this.parameters.put(key, parameters.get(key));
      }
      this.token = token;
      this.converter = converter;
      this.formItems = formItems;
    } catch (JSONException je) {
      throw new AipoProtocolException(AipoErrorCode.INTERNAL_ERROR);
    }
    this.jsonConverter = jsonConverter;
  }

  @Override
  public String getAppId() {
    String appId = getParameter(APP_ID);
    if (appId != null && appId.equals(APP_SUBSTITUTION_TOKEN)) {
      return token.getAppId();
    } else {
      return appId;
    }
  }

  @Override
  public Date getUpdatedSince() {
    String updatedSince = getParameter("updatedSince");
    if (updatedSince == null) {
      return null;
    }

    DateTime date = new DateTime(updatedSince);

    return date.toDate();
  }

  @Override
  public String getSortBy() {
    return getParameter(SORT_BY);
  }

  @Override
  public SortOrder getSortOrder() {
    String sortOrder = getParameter(SORT_ORDER);
    try {
      return sortOrder == null ? SortOrder.ascending : SortOrder
        .valueOf(sortOrder);
    } catch (IllegalArgumentException iae) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ERROR
        .customMessage("Parameter "
          + SORT_ORDER
          + " ("
          + sortOrder
          + ") is not valid."));
    }
  }

  @Override
  public String getFilterBy() {
    return getParameter(FILTER_BY);
  }

  @Override
  public int getStartIndex() {
    String startIndex = getParameter(START_INDEX);
    try {
      return startIndex == null ? DEFAULT_START_INDEX : Integer
        .valueOf(startIndex);
    } catch (NumberFormatException nfe) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ERROR
        .customMessage("Parameter "
          + START_INDEX
          + " ("
          + startIndex
          + ") is not a number."));
    }
  }

  @Override
  public int getCount() {
    String count = getParameter(COUNT);
    try {
      return count == null ? DEFAULT_COUNT : Integer.valueOf(count);
    } catch (NumberFormatException nfe) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ERROR
        .customMessage("Parameter "
          + COUNT
          + " ("
          + count
          + ") is not a number."));
    }
  }

  @Override
  public FilterOperation getFilterOperation() {
    String filterOp = getParameter(FILTER_OPERATION);
    try {
      return filterOp == null ? FilterOperation.contains : FilterOperation
        .valueOf(filterOp);
    } catch (IllegalArgumentException iae) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ERROR
        .customMessage("Parameter "
          + FILTER_OPERATION
          + " ("
          + filterOp
          + ") is not valid."));
    }
  }

  @Override
  public String getFilterValue() {
    String filterValue = getParameter(FILTER_VALUE);
    return Objects.firstNonNull(filterValue, "");
  }

  @Override
  public Set<String> getFields() {
    return getFields(Collections.<String> emptySet());
  }

  @Override
  public Set<String> getFields(Set<String> defaultValue) {
    Set<String> result = ImmutableSet.copyOf(getListParameter(FIELDS));
    if (result.isEmpty()) {
      return defaultValue;
    }
    return result;
  }

  @Override
  public SecurityToken getToken() {
    return token;
  }

  @Override
  public <T> T getTypedParameter(String parameterName, Class<T> dataTypeClass) {
    try {
      return converter.convertToObject(
        getParameter(parameterName),
        dataTypeClass);
    } catch (RuntimeException e) {
      if (e.getCause() instanceof JSONException) {
        throw new AipoProtocolException(AipoErrorCode.VALIDATE_ERROR
          .customMessage(e.getMessage()));
      }
      throw e;
    }
  }

  @Override
  public <T> T getTypedRequest(Class<T> dataTypeClass) {
    try {
      return jsonConverter.convertToObject(new JSONObject(this.parameters)
        .toString(), dataTypeClass);
    } catch (RuntimeException e) {
      if (e.getCause() instanceof JSONException) {
        throw new AipoProtocolException(AipoErrorCode.VALIDATE_ERROR
          .customMessage(e.getMessage()));
      }
      throw e;
    }
  }

  @Override
  public String getParameter(String paramName) {
    Object param = this.parameters.get(paramName);
    if (param instanceof List<?>) {
      if (((List<?>) param).isEmpty()) {
        return null;
      } else {
        param = ((List<?>) param).get(0);
      }
    }
    if (param == null) {
      return null;
    }
    return param.toString();
  }

  @Override
  public String getParameter(String paramName, String defaultValue) {
    String param = getParameter(paramName);
    if (param == null) {
      return defaultValue;
    }
    return param;
  }

  @Override
  public List<String> getListParameter(String paramName) {
    Object param = this.parameters.get(paramName);
    if (param == null) {
      return Collections.emptyList();
    }
    if (param instanceof String && ((String) param).indexOf(',') != -1) {
      List<String> listParam =
        Arrays.asList(StringUtils.split((String) param, ','));
      this.parameters.put(paramName, listParam);
      return listParam;
    } else if (param instanceof List<?>) {
      // Assume it's a list of strings. This is not type-safe.
      return (List<String>) param;
    } else if (param instanceof JSONArray) {
      try {
        JSONArray jsonArray = (JSONArray) param;
        List<String> returnVal =
          Lists.newArrayListWithCapacity(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
          returnVal.add(jsonArray.getString(i));
        }
        return returnVal;
      } catch (JSONException je) {
        throw new AipoProtocolException(AipoErrorCode.VALIDATE_ERROR
          .customMessage(je.getMessage()));
      }
    } else {
      // Allow up-conversion of non-array to array params.
      return Lists.newArrayList(param.toString());
    }
  }

  // Exposed for testing only
  public void setParameter(String paramName, Object paramValue) {
    if (paramValue instanceof String[]) {
      String[] arr = (String[]) paramValue;
      if (arr.length == 1) {
        this.parameters.put(paramName, arr[0]);
      } else {
        this.parameters.put(paramName, Lists.newArrayList(arr));
      }
    } else if (paramValue instanceof String) {
      String stringValue = (String) paramValue;
      if (stringValue.length() > 0) {
        this.parameters.put(paramName, stringValue);
      }
    } else {
      this.parameters.put(paramName, paramValue);
    }
  }

  @Override
  public FormDataItem getFormMimePart(String partName) {
    if (formItems != null) {
      return formItems.get(partName);
    } else {
      return null;
    }
  }

  private Map<String, Object> getAttributeMap() {
    if (this.attributes == null) {
      this.attributes = Maps.newHashMap();
    }
    return attributes;
  }

  @Override
  public Object getAttribute(String val) {
    Preconditions.checkNotNull(val);
    return getAttributeMap().get(val);
  }

  @Override
  public void setAttribute(String val, Object obj) {
    Preconditions.checkNotNull(val);
    if (obj == null) {
      getAttributeMap().remove(val);
    } else {
      getAttributeMap().put(val, obj);
    }
  }
}
