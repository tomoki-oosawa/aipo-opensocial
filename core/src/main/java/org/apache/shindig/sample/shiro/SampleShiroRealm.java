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
package org.apache.shindig.sample.shiro;

import org.apache.shindig.social.sample.spi.JsonDbOpensocialService;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

/**
 * A Sample Shiro Realm that uses the JSON DB to get passwords
 *
 */
public class SampleShiroRealm extends AuthorizingRealm {
  // HACK, apache.shiro relies upon no-arg constructors..
  @Inject
  private static JsonDbOpensocialService jsonDbService;

  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    UsernamePasswordToken upToken = (UsernamePasswordToken) token;
    String username = upToken.getUsername();

    // Null username is invalid
    if (username == null) {
        throw new AccountException("Null usernames are not allowed by this realm.");
    }
    String password = jsonDbService.getPassword(username);

    return  new SimpleAuthenticationInfo(username, password, this.getName());
  }

  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    //null usernames are invalid
    if (principals == null) {
      throw new AuthorizationException("PrincipalCollection method argument cannot be null.");
    }

    String username = (String) principals.fromRealm(getName()).iterator().next();


    Set<String> roleNames;

    if (username == null) {
      roleNames = ImmutableSet.of();
    } else {
      roleNames = ImmutableSet.of("foo", "goo");
    }

    return new SimpleAuthorizationInfo(roleNames);
  }

}
