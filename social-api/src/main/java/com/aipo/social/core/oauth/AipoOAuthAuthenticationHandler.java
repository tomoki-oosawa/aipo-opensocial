/*
 * Aipo is a groupware program developed by TOWN, Inc.
 * Copyright (C) 2004-2015 TOWN, Inc.
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
package com.aipo.social.core.oauth;

import net.oauth.OAuth;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;

import org.apache.shindig.auth.AipoSecurityTokenAuthenticationHandler;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.social.core.oauth.OAuthAuthenticationHandler;
import org.apache.shindig.social.core.oauth.OAuthSecurityToken;
import org.apache.shindig.social.opensocial.oauth.OAuthDataStore;
import org.apache.shindig.social.opensocial.oauth.OAuthEntry;

import com.google.inject.Inject;

/**
 * @see OAuthAuthenticationHandler
 */
public class AipoOAuthAuthenticationHandler extends OAuthAuthenticationHandler
    implements AipoSecurityTokenAuthenticationHandler {

  private final OAuthDataStore store;

  /**
   * @param store
   */
  @Inject
  public AipoOAuthAuthenticationHandler(OAuthDataStore store) {
    super(store);
    this.store = store;
  }

  @Override
  protected SecurityToken getTokenFromVerifiedRequest(OAuthMessage message,
      OAuthEntry entry, OAuthConsumer authConsumer)
      throws OAuthProblemException {

    if (entry != null) {
      verifyOrgScope(authConsumer.consumerKey, entry.getUserId());
      return new OAuthSecurityToken(
        entry.getUserId(),
        entry.getCallbackUrl(),
        entry.getAppId(),
        entry.getDomain(),
        entry.getContainer(),
        entry.expiresAt().getTime());
    } else {
      String userId = getParameter(message, REQUESTOR_ID_PARAM);
      verifyOrgScope(authConsumer.consumerKey, userId);
      return store.getSecurityTokenForConsumerRequest(
        authConsumer.consumerKey,
        userId);
    }
  }

  protected void verifyOrgScope(String consumerKey, String userId)
      throws OAuthProblemException {
    String[] split1 = consumerKey.split(":");
    String[] split2 = userId.split(":");
    if (split1.length > 1) {
      if (split2.length > 1) {
        if (split1[0].equalsIgnoreCase(split2[0])) {
          return;
        }
      }
      throw new OAuthProblemException(OAuth.Problems.CONSUMER_KEY_REJECTED);
    }
  }
}
