/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2011 Aimluck,Inc.
 * http://www.aipo.com/
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

import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shindig.common.util.Base32;
import org.apache.shindig.config.ContainerConfig;
import org.apache.shindig.gadgets.Gadget;
import org.apache.shindig.gadgets.LockedDomainService;

import com.aipo.orm.service.ContainerConfigService;
import com.aipo.orm.service.ContainerConfigService.Property;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * @HashLockedDomainService
 */
@Singleton
public class AipoHashLockedDomainService implements LockedDomainService {

  @SuppressWarnings("unused")
  private static final Logger logger = Logger
    .getLogger(AipoHashLockedDomainService.class.getName());

  private final ContainerConfigService containerConfigService;

  private final boolean enabled;

  private boolean lockSecurityTokens = false;

  @Inject
  public AipoHashLockedDomainService(ContainerConfig config,
      @Named("shindig.locked-domain.enabled") boolean enabled,
      ContainerConfigService containerConfigService) {
    this.enabled = enabled;
    this.containerConfigService = containerConfigService;

  }

  @Inject(optional = true)
  public void setLockSecurityTokens(
      @Named("shindig.locked-domain.lock-security-tokens") Boolean lockSecurityTokens) {
    this.lockSecurityTokens = lockSecurityTokens;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public boolean isSafeForOpenProxy(String host) {
    if (enabled) {
      return !hostRequiresLockedDomain(host);
    }
    return true;
  }

  public boolean gadgetCanRender(String host, Gadget gadget, String container) {
    container = normalizeContainer(container);
    if (enabled) {
      if (gadgetWantsLockedDomain(gadget)
        || hostRequiresLockedDomain(host)
        || containerRequiresLockedDomain(container)) {
        String neededHost = getLockedDomain(gadget, container);
        return host.equals(neededHost);
      }
    }
    return true;
  }

  public String getLockedDomainForGadget(Gadget gadget, String container) {
    container = normalizeContainer(container);
    if (enabled) {
      if (gadgetWantsLockedDomain(gadget)
        || containerRequiresLockedDomain(container)) {
        return getLockedDomain(gadget, container);
      }
    }
    return null;
  }

  private String getLockedDomain(Gadget gadget, String container) {
    String suffix = getLockedDomainSuffix();
    if (suffix == null) {
      return null;
    }
    byte[] sha1 = DigestUtils.sha(gadget.getSpec().getUrl().toString());
    String hash = new String(Base32.encodeBase32(sha1));
    return hash + suffix;
  }

  private boolean gadgetWantsLockedDomain(Gadget gadget) {
    if (lockSecurityTokens) {
      return gadget.getAllFeatures().contains("locked-domain");
    }
    return gadget.getSpec().getModulePrefs().getFeatures().keySet().contains(
      "locked-domain");
  }

  private boolean hostRequiresLockedDomain(String host) {
    if (host.endsWith(getLockedDomainSuffix())) {
      return true;
    }
    return false;
  }

  private boolean containerRequiresLockedDomain(String container) {
    return isLockedDomainRequired();
  }

  private String normalizeContainer(String container) {
    return ContainerConfig.DEFAULT_CONTAINER;
  }

  private boolean isLockedDomainRequired() {
    String value = containerConfigService.get(Property.LOCKED_DOMAIN_REQUIRED);
    return "true".equalsIgnoreCase(value);
  }

  private String getLockedDomainSuffix() {
    String value = containerConfigService.get(Property.LOCKED_DOMAIN_SUFFIX);
    return value;
  }

}
