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
package com.aipo.container.gadgets;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.shindig.common.logging.i18n.MessageKeys;
import org.apache.shindig.common.servlet.Authority;
import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.common.uri.Uri.UriException;
import org.apache.shindig.config.ContainerConfig;
import org.apache.shindig.gadgets.AbstractLockedDomainService;
import org.apache.shindig.gadgets.Gadget;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.spec.Feature;
import org.apache.shindig.gadgets.uri.LockedDomainPrefixGenerator;

import com.aipo.orm.service.ContainerConfigDbService;
import com.aipo.orm.service.ContainerConfigDbService.Property;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * @HashLockedDomainService
 */
@Singleton
public class AipoHashLockedDomainService extends AbstractLockedDomainService {

  /**
   * Used to observer locked domain suffixes for this class
   */
  private class HashLockedDomainObserver implements
      ContainerConfig.ConfigObserver {

    @Override
    public void containersChanged(ContainerConfig config,
        Collection<String> changed, Collection<String> removed) {
      for (String container : changed) {
        String suffix = config.getString(container, LOCKED_DOMAIN_SUFFIX_KEY);
        if (suffix == null) {
          if (LOG.isLoggable(Level.WARNING)) {
            LOG.logp(
              Level.WARNING,
              classname,
              "containersChanged",
              MessageKeys.NO_LOCKED_DOMAIN_CONFIG,
              new Object[] { container });
          }
        } else {
          AipoHashLockedDomainService.this.lockedSuffixes.put(
            container,
            checkSuffix(suffix));
        }
      }
      for (String container : removed) {
        AipoHashLockedDomainService.this.lockedSuffixes.remove(container);
      }
    }
  }

  // class name for logging purpose
  private static final String classname = AipoHashLockedDomainService.class
    .getName();

  private static final Logger LOG = Logger.getLogger(
    classname,
    MessageKeys.MESSAGES);

  private final Map<String, String> lockedSuffixes;

  private Authority authority;

  private final LockedDomainPrefixGenerator ldGen;

  private final Pattern authpattern = Pattern.compile("%authority%");

  private HashLockedDomainObserver ldObserver;

  public static final String LOCKED_DOMAIN_SUFFIX_KEY =
    "gadgets.uri.iframe.lockedDomainSuffix";

  private final ContainerConfigDbService containerConfigDbService;

  /*
   * Injected methods
   */

  /**
   * Create a LockedDomainService
   *
   * @param config
   *          per-container configuration
   * @param enabled
   *          whether this service should do anything at all.
   */
  @Inject
  public AipoHashLockedDomainService(ContainerConfig config,
      @Named("shindig.locked-domain.enabled") boolean enabled,
      LockedDomainPrefixGenerator ldGen,
      ContainerConfigDbService containerConfigDbService) {
    super(config, enabled);
    this.lockedSuffixes = Maps.newHashMap();
    this.ldGen = ldGen;
    if (enabled) {
      this.ldObserver = new HashLockedDomainObserver();
      config.addConfigObserver(this.ldObserver, true);
    }
    this.containerConfigDbService = containerConfigDbService;
  }

  @Override
  public String getLockedDomainForGadget(Gadget gadget, String container)
      throws GadgetException {
    container = getContainer(container);
    if (isEnabled() && !isExcludedFromLockedDomain(gadget, container)) {
      if (isGadgetReqestingLocking(gadget)
        || containerRequiresLockedDomain(container)) {
        return getLockedDomain(gadget, container);
      }
    }
    return null;
  }

  /**
   * Generates a locked domain prefix given a gadget Uri.
   *
   * @param gadget
   *          The uri of the gadget.
   * @return A locked domain prefix for the gadgetUri. Returns empty string if
   *         locked domains are not enabled on the server.
   */
  private String getLockedDomainPrefix(Gadget gadget) throws GadgetException {
    String ret = "";
    if (isEnabled()) {
      ret =
        this.ldGen.getLockedDomainPrefix(getLockedDomainParticipants(gadget));
    }
    // Lower-case to prevent casing from being relevant.
    return ret.toLowerCase();
  }

  @Override
  public boolean isGadgetValidForHost(String host, Gadget gadget,
      String container) {
    container = getContainer(container);
    if (isEnabled()) {
      if (isGadgetReqestingLocking(gadget)
        || isHostUsingLockedDomain(host)
        || containerRequiresLockedDomain(container)) {
        if (isRefererCheckEnabled() && !isValidReferer(gadget, container)) {
          return false;
        }
        String neededHost;
        try {
          neededHost = getLockedDomain(gadget, container);
        } catch (GadgetException e) {
          if (LOG.isLoggable(Level.WARNING)) {
            LOG.log(Level.WARNING, "Invalid host for call.", e);
          }
          return false;
        }
        return host.equalsIgnoreCase(neededHost);
      }
    }
    return true;
  }

  @Override
  public boolean isHostUsingLockedDomain(String host) {
    if (isEnabled()) {
      if (host.endsWith(getLockedDomainSuffix())) {
        return true;
      }
    }
    return false;
  }

  @Inject(optional = true)
  public void setAuthority(Authority authority) {
    this.authority = authority;
  }

  private String checkSuffix(String suffix) {
    if (suffix != null) {
      Matcher m = this.authpattern.matcher(suffix);
      if (m.matches()) {
        if (LOG.isLoggable(Level.WARNING)) {
          LOG
            .warning("You should not be using %authority% replacement in a running environment!");
          LOG
            .warning("Check your config and specify an explicit locked domain suffix.");
          LOG.warning("Found suffix: " + suffix);
        }
        if (this.authority != null) {
          suffix = m.replaceAll(this.authority.getAuthority());
        }
      }
    }
    return suffix;
  }

  private String getContainer(String container) {
    if (this.required.containsKey(container)) {
      return container;
    }
    return ContainerConfig.DEFAULT_CONTAINER;
  }

  private String getLockedDomain(Gadget gadget, String container)
      throws GadgetException {
    String suffix = getLockedDomainSuffix();
    if (suffix == null) {
      return null;
    }
    return getLockedDomainPrefix(gadget) + suffix;
  }

  private String getLockedDomainParticipants(Gadget gadget)
      throws GadgetException {
    Map<String, Feature> features =
      gadget.getSpec().getModulePrefs().getFeatures();
    Feature ldFeature = features.get("locked-domain");

    // This gadget is always a participant.
    Set<String> filtered = new TreeSet<String>();
    filtered.add(gadget.getSpec().getUrl().toString().toLowerCase());

    if (ldFeature != null) {
      Collection<String> participants =
        ldFeature.getParamCollection("participant");
      for (String participant : participants) {
        // be picky, this should be a valid uri
        try {
          Uri.parse(participant);
        } catch (UriException e) {
          throw new GadgetException(
            GadgetException.Code.INVALID_PARAMETER,
            "Participant param must be a valid uri",
            e);
        }
        filtered.add(participant.toLowerCase());
      }
    }

    StringBuilder buffer = new StringBuilder();
    for (String participant : filtered) {
      buffer.append(participant);
    }
    return buffer.toString();
  }

  @VisibleForTesting
  ContainerConfig.ConfigObserver getConfigObserver() {
    return this.ldObserver;
  }

  private boolean containerRequiresLockedDomain(String container) {
    return isLockedDomainRequired();
  }

  private boolean isLockedDomainRequired() {
    String value =
      containerConfigDbService.get(Property.LOCKED_DOMAIN_REQUIRED);
    return "true".equalsIgnoreCase(value);
  }

  private String getLockedDomainSuffix() {
    String value = containerConfigDbService.get(Property.LOCKED_DOMAIN_SUFFIX);
    return value;
  }
}