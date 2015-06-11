package com.aipo.orm.service;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.access.DataContext;

import com.aipo.orm.Database;
import com.aipo.orm.query.Operations;
import com.aipo.orm.service.bean.OAuth2Token;

// TODO:
public class AipoOAuth2TokenDbService implements OAuth2TokenDbService {

  /**
   * @param hashCode
   * @return
   */
  @Override
  public OAuth2Token get(String token, String codeType) {
    selectDefaultDataDomain();
    com.aipo.orm.model.social.OAuth2Token model =
      Database.query(com.aipo.orm.model.social.OAuth2Token.class).where(
        Operations.eq(
          com.aipo.orm.model.social.OAuth2Token.CODE_TYPE_PROPERTY,
          codeType)).where(
        Operations.eq(
          com.aipo.orm.model.social.OAuth2Token.TOKEN_PROPERTY,
          token)).fetchSingle();
    if (model != null) {
      OAuth2Token oAuth2Token = new OAuth2Token();
      oAuth2Token.setUserId(model.getUserId());
      oAuth2Token.setToken(model.getToken());
      oAuth2Token.setCreateDate(model.getCreateDate());
      oAuth2Token.setExpireTime(model.getExpireTime());
      oAuth2Token.setScope(model.getScope());
      oAuth2Token.setTokenType(model.getTokenType());
      oAuth2Token.setCodeType(model.getCodeType());
      return oAuth2Token;
    }
    return null;
  }

  /**
   * @param oAuthToken
   */
  @Override
  public void put(OAuth2Token oAuth2Token) {
    try {
      selectDefaultDataDomain();
      com.aipo.orm.model.social.OAuth2Token model =
        Database.create(com.aipo.orm.model.social.OAuth2Token.class);
      model.setToken(oAuth2Token.getToken());
      model.setUserId(oAuth2Token.getUserId());
      model.setCreateDate(oAuth2Token.getCreateDate());
      model.setExpireTime(oAuth2Token.getExpireTime());
      model.setScope(oAuth2Token.getScope());
      model.setTokenType(oAuth2Token.getTokenType());
      model.setCodeType(oAuth2Token.getCodeType());
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
  public void remove(String token) {
    try {
      selectDefaultDataDomain();
      com.aipo.orm.model.social.OAuth2Token model =
        Database.query(com.aipo.orm.model.social.OAuth2Token.class).where(
          Operations.eq(
            com.aipo.orm.model.social.OAuth2Token.TOKEN_PROPERTY,
            token)).fetchSingle();
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