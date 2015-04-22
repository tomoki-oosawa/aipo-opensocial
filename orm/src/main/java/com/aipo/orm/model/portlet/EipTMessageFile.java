package com.aipo.orm.model.portlet;

import org.apache.cayenne.ObjectId;

import com.aipo.orm.model.portlet.auto._EipTMessageFile;

public class EipTMessageFile extends _EipTMessageFile implements IEipTFile {

  private static final long serialVersionUID = -6108923614938110317L;

  public Integer getFileId() {
    if (getObjectId() != null && !getObjectId().isTemporary()) {
      Object obj = getObjectId().getIdSnapshot().get(FILE_ID_PK_COLUMN);
      if (obj instanceof Long) {
        Long value = (Long) obj;
        return Integer.valueOf(value.intValue());
      } else {
        return (Integer) obj;
      }
    } else {
      return null;
    }
  }

  public void setFileId(String id) {
    setObjectId(new ObjectId("EipTMessageFile", FILE_ID_PK_COLUMN, Integer
      .valueOf(id)));
  }
}
