/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2010 Aimluck,Inc.
 * http://aipostyle.com/
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
package com.aipo.social.opensocial.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletResponse;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.ImmediateFuture;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.social.core.model.NameImpl;
import org.apache.shindig.social.core.model.PersonImpl;
import org.apache.shindig.social.opensocial.model.Name;
import org.apache.shindig.social.opensocial.model.Person;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.PersonService;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.aipo.orm.model.security.TurbineUser;
import com.aipo.orm.service.TurbineUserService;
import com.google.common.collect.Lists;

/**
 * 
 */
public class PersonServiceDb extends AbstractService implements PersonService {

  /**
   * 
   */
  public PersonServiceDb() {
  }

  /**
   * 
   * @param userIds
   * @param groupId
   * @param collectionOptions
   * @param fields
   * @param token
   * @return
   * @throws ProtocolException
   */
  public Future<RestfulCollection<Person>> getPeople(Set<UserId> userIds,
      GroupId groupId, CollectionOptions collectionOptions, Set<String> fields,
      SecurityToken token) throws ProtocolException {

    setUp(token);

    TurbineUserService service = new TurbineUserService();
    List<TurbineUser> list = null;
    int totalResults = 0;
    switch (groupId.getType()) {
      case all:
      case friends:
        // /people/{guid}/@all
        // /people/{guid}/@friends
        // {guid} が閲覧できるすべてのユーザーを取得
        // @all = @friends
        list =
          service.findAll(collectionOptions.getMax(), collectionOptions
            .getFirst());
        totalResults = service.getCountAll();
        break;
      case groupId:
        // /people/{guid}/{groupId}
        // /people/{guid}/{groupId}
        // {guid} が閲覧できるすべてのユーザーで {groupId} グループに所属しているものを取得
        list =
          service.findByGroupname(groupId.getGroupId(), collectionOptions
            .getMax(), collectionOptions.getFirst());
        totalResults = service.getCountByGroupname(groupId.getGroupId());
        break;
      case deleted:
        // /people/{guid}/@deleted
        // {guid} が閲覧できる無効なユーザーを取得
        list = Lists.newArrayList();
        break;
      case self:
        // {guid} 自身のユーザー情報を取得
        list = Lists.newArrayList();
        totalResults = 1;
        break;
      default:
        throw new ProtocolException(
          HttpServletResponse.SC_BAD_REQUEST,
          "Group ID not recognized");
    }

    List<Person> result = new ArrayList<Person>(list.size());
    for (TurbineUser user : list) {
      String displayName = user.getLastName() + " " + user.getFirstName();
      Name name = new NameImpl();
      name.setFamilyName(user.getLastName());
      name.setGivenName(user.getFirstName());
      name.setFormatted(user.getLastName() + " " + user.getFirstName());
      Person person =
        new PersonImpl(getOrgId(token) + ":" + user.getLoginName(), user
          .getLastName()
          + " "
          + user.getFirstName(), name);
      result.add(person);
      person.setNickname(displayName);
    }

    RestfulCollection<Person> restCollection =
      new RestfulCollection<Person>(
        result,
        collectionOptions.getFirst(),
        totalResults,
        collectionOptions.getMax());
    return ImmediateFuture.newInstance(restCollection);

  }

  /**
   * 
   * @param id
   * @param fields
   * @param token
   * @return
   * @throws ProtocolException
   */
  public Future<Person> getPerson(UserId id, Set<String> fields,
      SecurityToken token) throws ProtocolException {

    setUp(token);

    String userId = getUserId(id, token);

    TurbineUserService service = new TurbineUserService();
    TurbineUser user = service.findByUsername(userId);

    Person person = null;
    if (user != null) {
      String displayName = user.getLastName() + " " + user.getFirstName();
      Name name = new NameImpl();
      name.setFamilyName(user.getLastName());
      name.setGivenName(user.getFirstName());
      name.setFormatted(displayName);
      person =
        new PersonImpl(getOrgId(token) + ":" + user.getLoginName(), user
          .getLastName()
          + " "
          + user.getFirstName(), name);
      person.setNickname(displayName);
    }

    return ImmediateFuture.newInstance(person);
  }
}
