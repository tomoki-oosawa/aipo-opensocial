package com.aipo.orm.service;

import com.aipo.orm.service.bean.OAuth2Client;

public interface OAuth2ClientDbService {

  public OAuth2Client get(String clientId);

  public void remove(String clientId);

  public void put(OAuth2Client client);

}