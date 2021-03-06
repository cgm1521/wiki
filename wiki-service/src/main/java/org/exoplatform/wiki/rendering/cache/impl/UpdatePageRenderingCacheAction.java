/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.wiki.rendering.cache.impl;

import javax.jcr.Node;
import javax.jcr.Property;

import org.apache.commons.chain.Context;
import org.chromattic.api.ChromatticSession;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.command.action.Action;
import org.exoplatform.services.ext.action.InvocationContext;
import org.exoplatform.services.jcr.observation.ExtendedEvent;
import org.exoplatform.wiki.chromattic.ext.ntdef.UncachedMixin;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.rendering.cache.PageRenderingCacheService;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.utils.Utils;

public class UpdatePageRenderingCacheAction implements Action {

  @Override
  public boolean execute(Context context) throws Exception {
    Object item = context.get("currentItem");
    Object eventObj = context.get(InvocationContext.EVENT);
    int eventCode = Integer.parseInt(eventObj.toString());
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    PageRenderingCacheService pRenderingCacheService = (PageRenderingCacheService)
                        container.getComponentInstanceOfType(PageRenderingCacheService.class);
    
    switch (eventCode) {
    case ExtendedEvent.NODE_ADDED:
      if (item instanceof Node) {
          Node node = (Node) item;
          Node parent = ((Node) item).getParent();
          if (parent.isNodeType(WikiNodeType.WIKI_PAGE) &&  node.isNodeType(WikiNodeType.WIKI_PAGE)) {
            PageImpl parentPage = (PageImpl) Utils.getObject(parent.getPath(), WikiNodeType.WIKI_PAGE);
            Wiki wiki = parentPage.getWiki();
          if (wiki != null) {
            pRenderingCacheService.invalidateCache(new WikiPageParams(wiki.getType(), wiki.getOwner(), parentPage.getName()));
            pRenderingCacheService.invalidateCache(new WikiPageParams(wiki.getType(), wiki.getOwner(), node.getName()));
          }
        }
      }
      break;
    case ExtendedEvent.ADD_MIXIN:
      if (item instanceof Node) {
        Node node = ((Node) item);
        if (node.isNodeType(WikiNodeType.WIKI_PAGE)) {
          PageImpl page = (PageImpl) Utils.getObject(node.getPath(), WikiNodeType.WIKI_PAGE);
          Wiki wiki = page.getWiki();
          if (wiki != null) {
            pRenderingCacheService.invalidateCache(new WikiPageParams(wiki.getType(), wiki.getOwner(), page.getName()));
            pRenderingCacheService.getPageLinksMap().remove(new WikiPageParams(wiki.getType(), wiki.getOwner(), page.getName()));
          }
        }
      }
      
    case ExtendedEvent.PROPERTY_CHANGED:
      Object previousItem = context.get("previousItem");
      if (item instanceof Property && previousItem instanceof Property){
        Property currentProperty = (Property) item;
        Node node = ((Property) item).getParent();
        if (WikiNodeType.Definition.OLD_PAGE_IDS.equals(currentProperty.getName())) {          
          if (node.isNodeType(WikiNodeType.WIKI_PAGE)) {
            PageImpl page = (PageImpl) Utils.getObject(node.getPath(), WikiNodeType.WIKI_PAGE);
            Wiki wiki = page.getWiki();
            if (wiki != null) {
              pRenderingCacheService.invalidateCache(new WikiPageParams(wiki.getType(), wiki.getOwner(), page.getName()));
            }
          }
        } else if (WikiNodeType.Definition.TARGET_PAGE.equals(currentProperty.getName())) {
          if (node.isNodeType(WikiNodeType.WIKI_PAGE)) {
            PageImpl page = (PageImpl) Utils.getObject(node.getPath(), WikiNodeType.WIKI_PAGE);
            Wiki wiki = page.getWiki();
            if (wiki != null) {
              pRenderingCacheService.invalidateCache(new WikiPageParams(wiki.getType(), wiki.getOwner(), page.getName()));
              PageImpl desPage = page.getMovedMixin().getTargetPage();
              pRenderingCacheService.invalidateCache(new WikiPageParams(wiki.getType(), wiki.getOwner(), desPage.getName()));
              pRenderingCacheService.getPageLinksMap().remove(new WikiPageParams(wiki.getType(), wiki.getOwner(), page.getName()));
            }            
          }
        }
      }
      break;
    case ExtendedEvent.CHECKIN:
      Node node = (Node) item;
      if (node.isNodeType(WikiNodeType.WIKI_PAGE)) {
        PageImpl page = (PageImpl) Utils.getObject(node.getPath(), WikiNodeType.WIKI_PAGE);
        Wiki wiki = page.getWiki();
        if (wiki != null) {
          //invalidate cache
          pRenderingCacheService.invalidateCache(new WikiPageParams(wiki.getType(), wiki.getOwner(), page.getName()));
        }
      }
      break;
    case ExtendedEvent.CHECKOUT:
      node = (Node) item;
      if (node.isNodeType(WikiNodeType.WIKI_PAGE)) {
        PageImpl page = (PageImpl) Utils.getObject(node.getPath(), WikiNodeType.WIKI_PAGE);
        Wiki wiki = page.getWiki();
        if (wiki != null) {
          checkUncachedMacroesInPage(page);
        }
      }
      break;
      
    default:
      break;
    }
    return CONTINUE_PROCESSING;
  }
  
  /**
   * checks if page contains uncached macroes
   * @param page
   */
  private void checkUncachedMacroesInPage(PageImpl page) {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    PageRenderingCacheService pRenderingCacheService = (PageRenderingCacheService)
                        container.getComponentInstanceOfType(PageRenderingCacheService.class);
    
    String content = page.getContent().getText();
    boolean found = false;
    for (String macro : pRenderingCacheService.getUncachedMacroes()) {
      String m1 = new StringBuilder().append("{{").append(macro).append("}}").toString();
      String m2 = new StringBuilder().append("{{").append(macro).append("/}}").toString();
      String m3 = new StringBuilder().append("{{").append(macro).append(" ").toString();
      if (content.contains(m1) || content.contains(m2) || content.contains(m3)) {
        found = true;
        break;
      }
    }
    
    ChromatticSession session = page.getMOWService().getSession();
    if (found) {
      if (page.getUncachedMixin() == null) {
        UncachedMixin uncachedMix = session.create(UncachedMixin.class);
        session.setEmbedded(page, UncachedMixin.class, uncachedMix);
      }
    } else {
      if (page.getUncachedMixin() != null) {
        session.setEmbedded(page, UncachedMixin.class, null);
      }
    }
  }

}
