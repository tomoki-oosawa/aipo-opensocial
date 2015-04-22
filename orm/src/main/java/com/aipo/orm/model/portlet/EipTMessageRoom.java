package com.aipo.orm.model.portlet;

import org.apache.cayenne.ObjectId;

import com.aipo.orm.model.portlet.auto._EipTMessageRoom;

public class EipTMessageRoom extends _EipTMessageRoom {

  private int unreadCount = 0;

  private Integer userId = null;

  private String firstName = null;

  private String lastName = null;

  private String hasPhoto = null;

  private Long photoModified = null;

  public Integer getRoomId() {
    if (getObjectId() != null && !getObjectId().isTemporary()) {
      Object obj = getObjectId().getIdSnapshot().get(ROOM_ID_PK_COLUMN);
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

  public void setRoomId(String id) {
    setObjectId(new ObjectId("EipTMessageRoom", ROOM_ID_PK_COLUMN, Integer
      .valueOf(id)));
  }

  public void setUnreadCount(int unreadCount) {
    this.unreadCount = unreadCount;
  }

  /**
   * @return unreadCount
   */
  public Integer getUnreadCount() {
    return unreadCount;
  }

  /**
   * @param firstName
   *          セットする firstName
   */
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  /**
   * @return firstName
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * @param lastName
   *          セットする lastName
   */
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  /**
   * @return lastName
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * @param hasPhoto
   *          セットする hasPhoto
   */
  public void setUserHasPhoto(String hasPhoto) {
    this.hasPhoto = hasPhoto;
  }

  /**
   * @return hasPhoto
   */
  public String getUserHasPhoto() {
    return hasPhoto;
  }

  /**
   * @param photoModified
   *          セットする photoModified
   */
  public void setUserPhotoModified(Long photoModified) {
    this.photoModified = photoModified;
  }

  /**
   * @return photoModified
   */
  public Long getUserPhotoModified() {
    return photoModified;
  }

  /**
   * @param userId
   *          セットする userId
   */
  public void setUserId(Integer userId) {
    this.userId = userId;
  }

  /**
   * @return userId
   */
  public Integer getUserId() {
    return userId;
  }

}
