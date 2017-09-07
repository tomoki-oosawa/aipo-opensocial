package com.aipo.orm.model.portlet.auto;

import java.util.Date;

import org.apache.cayenne.CayenneDataObject;

import com.aipo.orm.model.portlet.EipTMessage;

/**
 * Class _EipTMessageFile was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _EipTMessageFile extends CayenneDataObject {

    public static final String CREATE_DATE_PROPERTY = "createDate";
    public static final String FILE_NAME_PROPERTY = "fileName";
    public static final String FILE_PATH_PROPERTY = "filePath";
    public static final String FILE_THUMBNAIL_PROPERTY = "fileThumbnail";
    public static final String MESSAGE_ID_PROPERTY = "messageId";
    public static final String OWNER_ID_PROPERTY = "ownerId";
    public static final String ROOM_ID_PROPERTY = "roomId";
    public static final String UPDATE_DATE_PROPERTY = "updateDate";
    public static final String EIP_TMESSAGE_PROPERTY = "eipTMessage";

    public static final String FILE_ID_PK_COLUMN = "FILE_ID";

    public void setCreateDate(Date createDate) {
        writeProperty("createDate", createDate);
    }
    public Date getCreateDate() {
        return (Date)readProperty("createDate");
    }

    public void setFileName(String fileName) {
        writeProperty("fileName", fileName);
    }
    public String getFileName() {
        return (String)readProperty("fileName");
    }

    public void setFilePath(String filePath) {
        writeProperty("filePath", filePath);
    }
    public String getFilePath() {
        return (String)readProperty("filePath");
    }

    public void setFileThumbnail(byte[] fileThumbnail) {
        writeProperty("fileThumbnail", fileThumbnail);
    }
    public byte[] getFileThumbnail() {
        return (byte[])readProperty("fileThumbnail");
    }

    public void setMessageId(Integer messageId) {
        writeProperty("messageId", messageId);
    }
    public Integer getMessageId() {
        return (Integer)readProperty("messageId");
    }

    public void setOwnerId(Integer ownerId) {
        writeProperty("ownerId", ownerId);
    }
    public Integer getOwnerId() {
        return (Integer)readProperty("ownerId");
    }

    public void setRoomId(Integer roomId) {
        writeProperty("roomId", roomId);
    }
    public Integer getRoomId() {
        return (Integer)readProperty("roomId");
    }

    public void setUpdateDate(Date updateDate) {
        writeProperty("updateDate", updateDate);
    }
    public Date getUpdateDate() {
        return (Date)readProperty("updateDate");
    }

    public void setEipTMessage(EipTMessage eipTMessage) {
        setToOneTarget("eipTMessage", eipTMessage, true);
    }

    public EipTMessage getEipTMessage() {
        return (EipTMessage)readProperty("eipTMessage");
    }


}