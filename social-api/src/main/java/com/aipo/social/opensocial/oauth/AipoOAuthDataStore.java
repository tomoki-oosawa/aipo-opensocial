/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2011 Aimluck,Inc.
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

package com.aipo.social.opensocial.oauth;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import net.oauth.OAuthConsumer;
import net.oauth.OAuthServiceProvider;

import org.apache.shindig.auth.AuthenticationMode;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.crypto.Crypto;
import org.apache.shindig.social.core.oauth.OAuthSecurityToken;
import org.apache.shindig.social.opensocial.oauth.OAuthDataStore;
import org.apache.shindig.social.opensocial.oauth.OAuthEntry;
import org.apache.shindig.social.sample.oauth.SampleOAuthDataStore;

import com.aipo.orm.service.ApplicationDbService;
import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * @see SampleOAuthDataStore
 */
public class AipoOAuthDataStore implements OAuthDataStore {

  private static final int CALLBACK_TOKEN_LENGTH = 6;

  private static final int CALLBACK_TOKEN_ATTEMPTS = 5;

  private final OAuthServiceProvider SERVICE_PROVIDER;

  private final ApplicationDbService applicationDbService;

  private final String domain;

  @Inject
  public AipoOAuthDataStore(ApplicationDbService applicationDbService,
      @Named("shindig.oauth.base-url") String baseUrl,
      @Named("shindig.oauth.domain") String domain) {
    this.domain = domain;
    this.applicationDbService = applicationDbService;
    this.SERVICE_PROVIDER =
      new OAuthServiceProvider(
        baseUrl + "requestToken",
        baseUrl + "authorize",
        baseUrl + "accessToken");
  }

  // All valid OAuth tokens
  private static ConcurrentMap<String, OAuthEntry> oauthEntries =
    new MapMaker().makeMap();

  // Get the OAuthEntry that corresponds to the oauthToken
  public OAuthEntry getEntry(String oauthToken) {
    Preconditions.checkNotNull(oauthToken);
    return oauthEntries.get(oauthToken);
  }

  public OAuthConsumer getConsumer(String consumerKey) {
    try {
      String consumerSecret =
        applicationDbService.getConsumerSecret(consumerKey);

      if (consumerSecret == null) {
        return null;
      }

      OAuthConsumer consumer =
        new OAuthConsumer(null, consumerKey, consumerSecret, SERVICE_PROVIDER);

      // Set some properties loosely based on the ModulePrefs of a gadget
      /*-
      for (String key : ImmutableList.of(
        "title",
        "summary",
        "description",
        "thumbnail",
        "icon")) {
        if (app.has(key)) {
          consumer.setProperty(key, app.getString(key));
        }
      }
       */

      return consumer;

    } catch (Throwable t) {
      return null;
    }
  }

  public OAuthEntry generateRequestToken(String consumerKey,
      String oauthVersion, String signedCallbackUrl) {
    OAuthEntry entry = new OAuthEntry();
    entry.setAppId(consumerKey);
    entry.setConsumerKey(consumerKey);
    entry.setDomain(domain);
    entry.setContainer("default");

    entry.setToken(UUID.randomUUID().toString());
    entry.setTokenSecret(UUID.randomUUID().toString());

    entry.setType(OAuthEntry.Type.REQUEST);
    entry.setIssueTime(new Date());
    entry.setOauthVersion(oauthVersion);
    if (signedCallbackUrl != null) {
      entry.setCallbackUrlSigned(true);
      entry.setCallbackUrl(signedCallbackUrl);
    }

    oauthEntries.put(entry.getToken(), entry);
    return entry;
  }

  /**
   * 
   * @param entry
   * @return
   */
  public OAuthEntry convertToAccessToken(OAuthEntry entry) {
    Preconditions.checkNotNull(entry);
    Preconditions.checkState(
      entry.getType() == OAuthEntry.Type.REQUEST,
      "Token must be a request token");

    OAuthEntry accessEntry = new OAuthEntry(entry);

    accessEntry.setToken(UUID.randomUUID().toString());
    accessEntry.setTokenSecret(UUID.randomUUID().toString());

    accessEntry.setType(OAuthEntry.Type.ACCESS);
    accessEntry.setIssueTime(new Date());

    oauthEntries.remove(entry.getToken());
    oauthEntries.put(accessEntry.getToken(), accessEntry);

    return accessEntry;
  }

  /**
   * 
   * @param entry
   * @param userId
   */
  public void authorizeToken(OAuthEntry entry, String userId) {
    Preconditions.checkNotNull(entry);
    entry.setAuthorized(true);
    entry.setUserId(Preconditions.checkNotNull(userId));
    if (entry.isCallbackUrlSigned()) {
      entry.setCallbackToken(Crypto.getRandomDigits(CALLBACK_TOKEN_LENGTH));
    }
  }

  /**
   * 
   * @param entry
   */
  public void disableToken(OAuthEntry entry) {
    Preconditions.checkNotNull(entry);
    entry.setCallbackTokenAttempts(entry.getCallbackTokenAttempts() + 1);
    if (!entry.isCallbackUrlSigned()
      || entry.getCallbackTokenAttempts() >= CALLBACK_TOKEN_ATTEMPTS) {
      entry.setType(OAuthEntry.Type.DISABLED);
    }

    oauthEntries.put(entry.getToken(), entry);
  }

  /**
   * 
   * @param entry
   */
  public void removeToken(OAuthEntry entry) {
    Preconditions.checkNotNull(entry);

    oauthEntries.remove(entry.getToken());
  }

  /**
   * 
   * @param consumerKey
   * @param userId
   * @return
   */
  public SecurityToken getSecurityTokenForConsumerRequest(String consumerKey,
      String userId) {
    String container = "default";

    return new OAuthSecurityToken(
      userId,
      null,
      consumerKey,
      domain,
      container,
      null,
      AuthenticationMode.OAUTH_CONSUMER_REQUEST.name());

  }
}
