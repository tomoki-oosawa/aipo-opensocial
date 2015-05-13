/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2015 Aimluck,Inc.
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
package com.aipo.container.gadgets.oauth;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.oauth.OAuth;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthServiceProvider;
import net.oauth.signature.RSA_SHA1;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.ResourceLoader;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.oauth.BasicOAuthStore;
import org.apache.shindig.gadgets.oauth.BasicOAuthStoreConsumerKeyAndSecret;
import org.apache.shindig.gadgets.oauth.BasicOAuthStoreConsumerKeyAndSecret.KeyType;
import org.apache.shindig.gadgets.oauth.BasicOAuthStoreTokenIndex;
import org.apache.shindig.gadgets.oauth.OAuthStore;

import com.aipo.container.util.ContainerToolkit;
import com.aipo.orm.service.ContainerConfigDbService;
import com.aipo.orm.service.OAuthConsumerDbService;
import com.aipo.orm.service.OAuthTokenDbService;
import com.aipo.orm.service.bean.OAuthToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * @BasicOAuthStore
 */
@Singleton
public class AipoOAuthStore implements OAuthStore {

  private static final Logger logger = Logger.getLogger(AipoOAuthStore.class
    .getName());

  private static final String OAUTH_CONSUMER_KEY =
    "shindig.signing.consumerKey";

  private static final String OAUTH_SIGNING_KEY_FILE =
    "shindig.signing.key-file";

  private static final String OAUTH_SIGNING_KEY_NAME =
    "shindig.signing.key-name";

  private static final String OAUTH_CALLBACK_URL =
    "shindig.signing.global-callback-url";

  private BasicOAuthStoreConsumerKeyAndSecret defaultKey;

  private final String defaultCallbackUrl;

  private final OAuthConsumerDbService oAuthConsumerDbService;

  private final OAuthTokenDbService oAuthTokenDbService;

  private final ContainerConfigDbService containerConfigDbService;

  @Inject
  public AipoOAuthStore(@Named(OAUTH_SIGNING_KEY_FILE) String signingKeyFile,
      @Named(OAUTH_SIGNING_KEY_NAME) String signingKeyName,
      @Named(OAUTH_CALLBACK_URL) String defaultCallbackUrl,
      @Named(OAUTH_CONSUMER_KEY) String consumerKey,
      OAuthConsumerDbService oAuthConsumerDbService,
      OAuthTokenDbService oAuthTokenDbService,
      ContainerConfigDbService containerConfigDbService) {
    this.oAuthConsumerDbService = oAuthConsumerDbService;
    this.oAuthTokenDbService = oAuthTokenDbService;
    this.defaultCallbackUrl = defaultCallbackUrl;
    this.containerConfigDbService = containerConfigDbService;
    loadDefaultKey(consumerKey, signingKeyFile, signingKeyName);
  }

  public static String convertFromOpenSsl(String privateKey) {
    return privateKey.replaceAll("-----[A-Z ]*-----", "").replace("\n", "");
  }

  @Override
  public ConsumerInfo getConsumerKeyAndSecret(SecurityToken securityToken,
      String serviceName, OAuthServiceProvider provider) throws GadgetException {
    String appId = securityToken.getAppId();

    com.aipo.orm.service.bean.OAuthConsumer oAuthConsumer =
      oAuthConsumerDbService.get(appId, serviceName);

    BasicOAuthStoreConsumerKeyAndSecret cks;
    if (oAuthConsumer == null) {
      cks = defaultKey;
    } else {

      KeyType keyType = KeyType.HMAC_SYMMETRIC;

      String consumerKey = oAuthConsumer.getConsumerKey();
      String consumerSecret = oAuthConsumer.getConsumerSecret();
      if ("RSA-SHA1".equals(oAuthConsumer.getType())) {
        keyType = KeyType.RSA_PRIVATE;
        consumerSecret = convertFromOpenSsl(consumerSecret);
      }

      cks =
        new BasicOAuthStoreConsumerKeyAndSecret(
          consumerKey,
          consumerSecret,
          keyType,
          null,
          null);
    }
    OAuthConsumer consumer = null;
    if (cks == null) {
      return null;
    }
    if (cks.getKeyType() == KeyType.RSA_PRIVATE) {
      consumer = new OAuthConsumer(null, cks.getConsumerKey(), null, provider);
      consumer.setProperty(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.RSA_SHA1);
      consumer.setProperty(RSA_SHA1.PRIVATE_KEY, cks.getConsumerSecret());
    } else {
      consumer =
        new OAuthConsumer(
          null,
          cks.getConsumerKey(),
          cks.getConsumerSecret(),
          provider);
      consumer.setProperty(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.HMAC_SHA1);
    }
    String callback =
      (cks.getCallbackUrl() != null ? cks.getCallbackUrl() : new StringBuilder(
        ContainerToolkit.getScheme()).append("://").append(
        ContainerToolkit.getHost(containerConfigDbService)).append(
        defaultCallbackUrl).toString());

    return new ConsumerInfo(consumer, cks.getKeyName(), callback);
  }

  private BasicOAuthStoreTokenIndex makeBasicOAuthStoreTokenIndex(
      SecurityToken securityToken, String serviceName, String tokenName) {
    BasicOAuthStoreTokenIndex tokenKey = new BasicOAuthStoreTokenIndex();
    tokenKey.setGadgetUri(securityToken.getAppUrl());
    tokenKey.setModuleId(securityToken.getModuleId());
    tokenKey.setServiceName(serviceName);
    tokenKey.setTokenName(tokenName);
    tokenKey.setUserId(securityToken.getViewerId());
    return tokenKey;
  }

  @Override
  public TokenInfo getTokenInfo(SecurityToken securityToken,
      ConsumerInfo consumerInfo, String serviceName, String tokenName) {

    BasicOAuthStoreTokenIndex tokenKey =
      makeBasicOAuthStoreTokenIndex(securityToken, serviceName, tokenName);
    OAuthToken oAuthToken = oAuthTokenDbService.get(tokenKey.hashCode());

    if (oAuthToken == null) {
      return null;
    }

    return new TokenInfo(oAuthToken.getAccessToken(), oAuthToken
      .getTokenSecret(), oAuthToken.getSessionHandle(), oAuthToken
      .getTokenExpireMilis());
  }

  @Override
  public void setTokenInfo(SecurityToken securityToken,
      ConsumerInfo consumerInfo, String serviceName, String tokenName,
      TokenInfo tokenInfo) {
    BasicOAuthStoreTokenIndex tokenKey =
      makeBasicOAuthStoreTokenIndex(securityToken, serviceName, tokenName);

    OAuthToken oAuthToken = new OAuthToken();
    oAuthToken.setKey(tokenKey.hashCode());
    oAuthToken.setAccessToken(tokenInfo.getAccessToken());
    oAuthToken.setTokenSecret(tokenInfo.getTokenSecret());
    oAuthToken.setSessionHandle(tokenInfo.getSessionHandle());
    oAuthToken.setTokenExpireMilis(tokenInfo.getTokenExpireMillis());

    oAuthTokenDbService.put(oAuthToken);
  }

  @Override
  public void removeToken(SecurityToken securityToken,
      ConsumerInfo consumerInfo, String serviceName, String tokenName) {

    BasicOAuthStoreTokenIndex tokenKey =
      makeBasicOAuthStoreTokenIndex(securityToken, serviceName, tokenName);

    oAuthTokenDbService.remove(tokenKey.hashCode());
  }

  private void loadDefaultKey(String consumerKey, String signingKeyFile,
      String signingKeyName) {
    BasicOAuthStoreConsumerKeyAndSecret key = null;
    if (!StringUtils.isBlank(signingKeyFile)) {
      try {
        logger.info("Loading OAuth signing key from " + signingKeyFile);
        String privateKey =
          IOUtils.toString(ResourceLoader.open(signingKeyFile), "UTF-8");
        privateKey = BasicOAuthStore.convertFromOpenSsl(privateKey);
        key =
          new BasicOAuthStoreConsumerKeyAndSecret(
            consumerKey,
            privateKey,
            KeyType.RSA_PRIVATE,
            signingKeyName,
            null);
      } catch (Throwable t) {
        logger
          .log(Level.WARNING, "Couldn't load key file " + signingKeyFile, t);
      }
    }
    if (key != null) {
      defaultKey = key;
    } else {
      logger
        .log(
          Level.WARNING,
          "Couldn't load OAuth signing key.  To create a key, run:\n"
            + "  openssl req -newkey rsa:1024 -days 365 -nodes -x509 -keyout testkey.pem \\\n"
            + "     -out testkey.pem -subj '/CN=mytestkey'\n"
            + "  openssl pkcs8 -in testkey.pem -out oauthkey.pem -topk8 -nocrypt -outform PEM\n"
            + '\n'
            + "Then edit shindig.properties and add these lines:\n"
            + OAUTH_SIGNING_KEY_FILE
            + "=<path-to-oauthkey.pem>\n"
            + OAUTH_SIGNING_KEY_NAME
            + "=mykey\n");
    }
  }
}
