package com.aipo.orm.model.portlet;

import java.util.Date;
import java.util.List;

import org.apache.cayenne.ObjectId;

import com.aipo.orm.model.portlet.auto._EipTMessageRoom;

public class EipTMessageRoom extends _EipTMessageRoom {

  private int unreadCount = 0;

  private Integer userId = null;

  private String loginName = null;

  private String firstName = null;

  private String lastName = null;

  private String userHasPhoto = null;

  private Date userPhotoModified = null;

  private List<String> roomMembers = null;

  private Integer lastMessageId = null;

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
   * @param loginName
   *          セットする loginName
   */
  public void setLoginName(String loginName) {
    this.loginName = loginName;
  }

  /**
   * @return loginName
   */
  public String getLoginName() {
    return loginName;
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
    this.userHasPhoto = hasPhoto;
  }

  /**
   * @return hasPhoto
   */
  public String getUserHasPhoto() {
    return userHasPhoto;
  }

  /**
   * @param photoModified
   *          セットする photoModified
   */
  public void setUserPhotoModified(Date userPhotoModified) {
    this.userPhotoModified = userPhotoModified;
  }

  /**
   * @return photoModified
   */
  public Date getUserPhotoModified() {
    return userPhotoModified;
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

  /**
   * @param roomId
   *          セットする roomId
   */
  public void setRoomMembers(List<String> roomMembers) {
    this.roomMembers = roomMembers;
  }

  /**
   * @return roomId
   */
  public List<String> getRoomMembers() {
    return roomMembers;
  }

  public void setLastMessageId(int lastMessageId) {
    this.lastMessageId = lastMessageId;
  }

  /**
   * @return lastMessageId
   */
  public Integer getLastMessageId() {
    return lastMessageId;
  }
}
