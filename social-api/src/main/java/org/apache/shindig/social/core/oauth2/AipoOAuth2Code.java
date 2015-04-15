package org.apache.shindig.social.core.oauth2;

// TODO:
public class AipoOAuth2Code extends OAuth2Code {
  private String userId;

  /**
   * @return userId
   */
  public String getUserId() {
    return userId;
  }

  /**
   * @param userId
   *          セットする userId
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }
}