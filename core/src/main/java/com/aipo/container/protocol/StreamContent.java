package com.aipo.container.protocol;

import java.io.InputStream;

public class StreamContent {
  private InputStream inputStream;

  private String contentType;

  public StreamContent() {
  }

  public StreamContent(String contentType, InputStream is) {
    this.contentType = contentType;
    this.inputStream = is;
  }

  public InputStream getInputStream() {
    return this.inputStream;
  }

  public String getContentType() {
    return this.contentType;
  }

  public void setInputStream(InputStream is) {
    this.inputStream = is;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }
}