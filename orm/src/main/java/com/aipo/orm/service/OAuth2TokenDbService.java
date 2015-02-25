package com.aipo.orm.service;

import com.aipo.orm.service.bean.OAuth2Token;

// TODO:
public interface OAuth2TokenDbService {

  public OAuth2Token get(int hashCode);

  public void remove(int hashCode);

  public void put(OAuth2Token oAuthToken);

}