package com.aipo.orm.service;

import com.aipo.orm.service.bean.OAuth2Token;

// TODO:
public interface OAuth2TokenDbService {

  public OAuth2Token get(String tokenString, String codeType);

  public void remove(String tokenString);

  public void put(OAuth2Token oAuthToken);

}