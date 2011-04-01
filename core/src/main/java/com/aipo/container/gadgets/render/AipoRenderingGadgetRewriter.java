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

package com.aipo.container.gadgets.render;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.shindig.common.util.ResourceLoader;
import org.apache.shindig.common.xml.DomUtil;
import org.apache.shindig.config.ContainerConfig;
import org.apache.shindig.gadgets.Gadget;
import org.apache.shindig.gadgets.GadgetContext;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.MessageBundleFactory;
import org.apache.shindig.gadgets.config.ConfigContributor;
import org.apache.shindig.gadgets.features.FeatureRegistry;
import org.apache.shindig.gadgets.render.RenderingGadgetRewriter;
import org.apache.shindig.gadgets.rewrite.MutableContent;
import org.apache.shindig.gadgets.rewrite.RewritingException;
import org.apache.shindig.gadgets.spec.MessageBundle;
import org.apache.shindig.gadgets.uri.JsUriManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * @see RenderingGadgetRewriter
 */
public class AipoRenderingGadgetRewriter extends RenderingGadgetRewriter {

  private static final Logger LOG = Logger
    .getLogger(AipoRenderingGadgetRewriter.class.getName());

  protected String aipoStyleCss;

  /**
   * @param messageBundleFactory
   * @param containerConfig
   * @param featureRegistry
   * @param jsUriManager
   * @param configContributors
   */
  @Inject
  public AipoRenderingGadgetRewriter(MessageBundleFactory messageBundleFactory,
      ContainerConfig containerConfig, FeatureRegistry featureRegistry,
      JsUriManager jsUriManager,
      Map<String, ConfigContributor> configContributors,
      @Named("aipo.aipostyle.css") String path) {
    super(
      messageBundleFactory,
      containerConfig,
      featureRegistry,
      jsUriManager,
      configContributors);
    loadAipoStyle(path);
  }

  protected void loadAipoStyle(String path) {
    try {
      aipoStyleCss = IOUtils.toString(ResourceLoader.open(path), "UTF-8");
    } catch (IOException e) {
      LOG.log(Level.WARNING, e.getMessage(), e);
    }
  }

  @Override
  public void rewrite(Gadget gadget, MutableContent mutableContent)
      throws RewritingException {
    // Don't touch sanitized gadgets.
    if (gadget.sanitizeOutput()) {
      return;
    }
    try {
      Document document = mutableContent.getDocument();

      Element head =
        (Element) DomUtil.getFirstNamedChildNode(
          document.getDocumentElement(),
          "head");

      // Insert new content before any of the existing children of the head
      // element
      Node firstHeadChild = head.getFirstChild();

      // Only inject default styles if no doctype was specified.
      // if (document.getDoctype() == null) {
      boolean isAipoStyle =
        gadget.getAllFeatures().contains("aipostyle")
          && gadget.getSpec().getModulePrefs().getFeatures().keySet().contains(
            "aipostyle");
      Element defaultStyle = document.createElement("style");
      defaultStyle.setAttribute("type", "text/css");
      head.insertBefore(defaultStyle, firstHeadChild);
      defaultStyle.appendChild(defaultStyle.getOwnerDocument().createTextNode(
        isAipoStyle ? aipoStyleCss : DEFAULT_CSS));
      // }

      injectBaseTag(gadget, head);
      injectGadgetBeacon(gadget, head, firstHeadChild);
      injectFeatureLibraries(gadget, head, firstHeadChild);

      // This can be one script block.
      Element mainScriptTag = document.createElement("script");
      GadgetContext context = gadget.getContext();
      MessageBundle bundle =
        messageBundleFactory.getBundle(
          gadget.getSpec(),
          context.getLocale(),
          context.getIgnoreCache(),
          context.getContainer());
      injectMessageBundles(bundle, mainScriptTag);
      injectDefaultPrefs(gadget, mainScriptTag);
      injectPreloads(gadget, mainScriptTag);

      // We need to inject our script before any developer scripts.
      head.insertBefore(mainScriptTag, firstHeadChild);

      Element body =
        (Element) DomUtil.getFirstNamedChildNode(
          document.getDocumentElement(),
          "body");

      body.setAttribute("dir", bundle.getLanguageDirection());

      injectOnLoadHandlers(body);

      mutableContent.documentChanged();
    } catch (GadgetException e) {
      throw new RewritingException(e.getLocalizedMessage(), e, e
        .getHttpStatusCode());
    }
  }
}
