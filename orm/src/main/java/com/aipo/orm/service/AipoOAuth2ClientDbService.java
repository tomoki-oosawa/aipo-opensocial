package com.aipo.orm.service;

import java.util.Date;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.access.DataContext;

import com.aipo.orm.Database;
import com.aipo.orm.query.Operations;
import com.aipo.orm.service.bean.OAuth2Client;
import com.aipo.orm.service.bean.OAuth2Client.ClientType;

public class AipoOAuth2ClientDbService implements OAuth2ClientDbService {

  /**
   *
   * @param clientId
   * @return
   */
  @Override
  public OAuth2Client get(String clientId) {
    selectDefaultDataDomain();

    if (clientId == null || clientId.length() == 0) {
      return null;
    }

    com.aipo.orm.model.social.OAuth2Client model =
      Database.query(com.aipo.orm.model.social.OAuth2Client.class).where(
        Operations.eq(
          com.aipo.orm.model.social.OAuth2Client.CLIENT_ID_PROPERTY,
          clientId)).fetchSingle();
    if (model != null) {
      OAuth2Client oauth2Client = new OAuth2Client();
      oauth2Client.setId(model.getClientId());
      oauth2Client.setSecret(model.getClientSecret());
      oauth2Client.setFlow(model.getFlow());
      oauth2Client.setType(ClientType.PUBLIC.toString().equals(
        model.getClientType()) ? ClientType.PUBLIC : ClientType.CONFIDENTIAL);
      oauth2Client.setTitle(model.getTitle());
      oauth2Client.setIconUrl(model.getIconUrl());
      oauth2Client.setRedirectURI(model.getRedirectUri());
      oauth2Client.setCreateDate(model.getCreateDate());
      oauth2Client.setUpdateDate(model.getUpdateDate());
      return oauth2Client;
    }
    return null;
  }

  /**
   * @param oauth2Cient
   */
  @Override
  public void put(OAuth2Client oauth2Cient) {
    try {
      selectDefaultDataDomain();
      com.aipo.orm.model.social.OAuth2Client model =
        Database.query(com.aipo.orm.model.social.OAuth2Client.class).where(
          Operations.eq(
            com.aipo.orm.model.social.OAuth2Client.CLIENT_ID_PROPERTY,
            oauth2Cient.getId())).fetchSingle();
      if (model == null) {
        model = Database.create(com.aipo.orm.model.social.OAuth2Client.class);
        model.setCreateDate(new Date());
        model.setClientId(oauth2Cient.getId());
      }
      model.setClientSecret(oauth2Cient.getSecret());
      if (oauth2Cient.getType() != null) {
        model.setClientType(oauth2Cient.getType().toString());
      }
      if (oauth2Cient.getFlow() != null) {
        model.setFlow(oauth2Cient.getFlow().toString());
      }
      model.setIconUrl(oauth2Cient.getIconUrl());
      model.setRedirectUri(oauth2Cient.getRedirectURI());
      model.setTitle(oauth2Cient.getTitle());
      model.setUpdateDate(new Date());
      Database.commit();
    } catch (Throwable t) {
      Database.rollback();
      throw new RuntimeException(t);
    }
  }

  /**
   * @param hashCode
   */
  @Override
  public void remove(String clientId) {
    try {
      selectDefaultDataDomain();
      com.aipo.orm.model.social.OAuth2Client model =
        Database.query(com.aipo.orm.model.social.OAuth2Client.class).where(
          Operations.eq(
            com.aipo.orm.model.social.OAuth2Client.CLIENT_ID_PROPERTY,
            clientId)).fetchSingle();
      if (model == null) {
        return;
      }
      Database.delete(model);
      Database.commit();
    } catch (Throwable t) {
      Database.rollback();
      throw new RuntimeException(t);
    }
  }

  private void selectDefaultDataDomain() {
    ObjectContext dataContext = null;
    try {
      dataContext = DataContext.getThreadObjectContext();
    } catch (IllegalStateException ignore) {
      // first
    }
    if (dataContext == null) {
      try {
        dataContext = Database.createDataContext("org001");
        DataContext.bindThreadObjectContext(dataContext);
      } catch (Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }
}