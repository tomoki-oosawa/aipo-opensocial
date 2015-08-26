package org.apache.shindig.auth;

import java.util.ArrayList;
import java.util.List;

import org.apache.shindig.config.ContainerConfig;

public class AipoOAuth2SecurityToken implements SecurityToken {

  private final String ownerId;

  private final String viewerId;

  private final String container;

  private final List<String> scope;

  public AipoOAuth2SecurityToken(String ownerId, String viewerId) {
    this.ownerId = ownerId;
    this.viewerId = viewerId;
    this.container = ContainerConfig.DEFAULT_CONTAINER;
    this.scope = new ArrayList<String>();
  }

  public AipoOAuth2SecurityToken(String ownerId, String viewerId,
      List<String> scope) {
    this.ownerId = ownerId;
    this.viewerId = viewerId;
    this.container = ContainerConfig.DEFAULT_CONTAINER;
    this.scope = scope;
  }

  /**
   * @return
   */
  @Override
  public String getOwnerId() {
    return ownerId;
  }

  /**
   * @return
   */
  @Override
  public String getViewerId() {
    return viewerId;
  }

  /**
   * @return
   */
  @Override
  public String getAppId() {
    throw new UnsupportedOperationException();
  }

  /**
   * @return
   */
  @Override
  public String getDomain() {
    throw new UnsupportedOperationException();
  }

  /**
   * @return
   */
  @Override
  public String getContainer() {
    return this.container;
  }

  /**
   * @return
   */
  @Override
  public String getAppUrl() {
    throw new UnsupportedOperationException();
  }

  /**
   * @return
   */
  @Override
  public long getModuleId() {
    return 0;
  }

  /**
   * @return
   */
  @Override
  public Long getExpiresAt() {
    throw new UnsupportedOperationException();
  }

  /**
   * @return
   */
  @Override
  public boolean isExpired() {
    throw new UnsupportedOperationException();
  }

  /**
   * @return
   */
  @Override
  public String getUpdatedToken() {
    throw new UnsupportedOperationException();
  }

  /**
   * @return
   */
  @Override
  public String getAuthenticationMode() {
    throw new UnsupportedOperationException();
  }

  /**
   * @return
   */
  @Override
  public String getTrustedJson() {
    throw new UnsupportedOperationException();
  }

  /**
   * @return
   */
  @Override
  public boolean isAnonymous() {
    return false;
  }

  /**
   * @return
   */
  @Override
  public String getActiveUrl() {
    throw new UnsupportedOperationException();
  }

  public List<String> getScope() {
    return scope;
  }

}