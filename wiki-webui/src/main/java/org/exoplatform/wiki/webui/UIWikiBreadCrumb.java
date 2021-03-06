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
package org.exoplatform.wiki.webui;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.commons.EventUIComponent;
import org.exoplatform.webui.commons.EventUIComponent.EVENTTYPE;
import org.exoplatform.webui.commons.UISpacesSwitcher;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.service.BreadcrumbData;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.utils.Utils;

import java.util.ArrayList;
import java.util.List;

@ComponentConfig(
  lifecycle = Lifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiBreadCrumb.gtmpl",
  events = {@EventConfig(listeners = UIWikiBreadCrumb.SwitchSpaceActionListener.class)}
)
public class UIWikiBreadCrumb extends UIContainer {

  public static final String  SPACE_SWITCHER = "uiSpaceSwitcher_BreadCrumb";
  
  private static final String  SWITCH_SPACE_ACTION = "SwitchSpace";
  
  private static final String  BREAD_CRUMB_CONTAINER = "UIWikiBreadCrumb";

  private List<BreadcrumbData> breadCumbs = new ArrayList<BreadcrumbData>();

  private String               actionLabel;

  private boolean              isLink     = true;
  
  private boolean              isAllowChooseSpace = false;
  
  private boolean              isShowWikiName = true;
  
  private static final Log     log               = ExoLogger.getLogger(UIWikiBreadCrumb.class);
  
  public UIWikiBreadCrumb() throws Exception {
    UISpacesSwitcher uiWikiSpaceSwitcher = addChild(UISpacesSwitcher.class, null, SPACE_SWITCHER);
    EventUIComponent eventComponent = new EventUIComponent(BREAD_CRUMB_CONTAINER, SWITCH_SPACE_ACTION, EVENTTYPE.EVENT);
    uiWikiSpaceSwitcher.init(eventComponent);
  }

  private boolean              isShowWikiType = true;
  
  private boolean              isDisplayFullSpaceName = true;

  public List<BreadcrumbData> getBreadCumbs() {
    return breadCumbs;
  }

  public void setBreadCumbs(List<BreadcrumbData> breadCumbs) {
    this.breadCumbs = breadCumbs;
  }
  
  public String getActionLabel() {
    return actionLabel;
  }

  public void setActionLabel(String actionLabel) {
    this.actionLabel = actionLabel;
  }  

  public String getParentURL() throws Exception {
    if(breadCumbs.size() > 1) {
      return createActionLink(breadCumbs.get(breadCumbs.size() - 2)) ;
    }else {
      return createActionLink(breadCumbs.get(0)) ;
    }     
  }
  
  public boolean isLink() {
    return isLink;
  }

  public UIWikiBreadCrumb setLink(boolean isLink) {
    this.isLink = isLink;
    return this;
  }
  
  public boolean isAllowChooseSpace() {
    return isAllowChooseSpace;
  }
  
  public void setAllowChooseSpace(boolean isAlowChooseSpace) {
    this.isAllowChooseSpace = isAlowChooseSpace;
  }
  
  public boolean isShowWikiName() {
    return isShowWikiName;
  }
  
  public void setShowWikiName(boolean isShowWikiName) {
    this.isShowWikiName = isShowWikiName;
  }

  public WikiPageParams getPageParam() throws Exception {
    if (this.breadCumbs != null && this.breadCumbs.size() > 0) {
      WikiService wservice = (WikiService) PortalContainer.getComponent(WikiService.class);
      return wservice.getWikiPageParams(breadCumbs.get(breadCumbs.size() - 1));
    }
    return null;
  }

  public String getWikiType() throws Exception {
    WikiPageParams params = getPageParam();
    if (params != null) {
      return params.getType();
    }
    return null;
  }

  public String getWikiName() throws Exception {
    WikiPageParams params = getPageParam();
    if (params != null) {
      String wikiType = params.getType();
      String wikiOwner = params.getOwner();
      WikiService wikiService = (WikiService) PortalContainer.getComponent(WikiService.class);
      return org.exoplatform.wiki.commons.Utils.getSpaceName(wikiService.getWiki(wikiType, wikiOwner));
    }
    return null;
  }

  public boolean isShowWikiType() {
    return isShowWikiType;
  }

  public void setShowWikiType(boolean isShowWikiType) {
    this.isShowWikiType = isShowWikiType;
  }
  
  public boolean isDisplayFullSpaceName() {
    return isDisplayFullSpaceName;
  }

  public void setDisplayFullSpaceName(boolean isDisplayFullSpaceName) {
    this.isDisplayFullSpaceName = isDisplayFullSpaceName;
  }

  public String createActionLink(BreadcrumbData breadCumbData) throws Exception {  
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    StringBuilder sb = new StringBuilder(portalRequestContext.getPortalURI());
    UIPortal uiPortal = Util.getUIPortal();
    String pageNodeSelected = uiPortal.getSelectedUserNode().getURI();
    sb.append(pageNodeSelected);
    sb.append("/");
    if (!PortalConfig.PORTAL_TYPE.equalsIgnoreCase(breadCumbData.getWikiType())) {
      sb.append(breadCumbData.getWikiType());
      sb.append("/");
      sb.append(Utils.validateWikiOwner(breadCumbData.getWikiType(), breadCumbData.getWikiOwner()));
      sb.append("/");
    }
    sb.append(breadCumbData.getId());
    return sb.toString();
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.webui.core.UIComponent#processRender(org.exoplatform.webui.application.WebuiRequestContext)
   */
  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
	  if(IdentityConstants.ANONIM.equals(ConversationState.getCurrent().getIdentity().getUserId())) {
		  this.setAllowChooseSpace(false);
	  }
    super.processRender(context);
  }
  
  public static class SwitchSpaceActionListener extends EventListener<UIWikiBreadCrumb> {
    public void execute(Event<UIWikiBreadCrumb> event) throws Exception {
      String wikiId = event.getRequestContext().getRequestParameter(UISpacesSwitcher.SPACE_ID_PARAMETER);
      WikiService wikiService = (WikiService) PortalContainer.getComponent(WikiService.class);
      Wiki wiki = wikiService.getWikiById(wikiId);
      if (wiki != null) {
        PageImpl wikiHome = (PageImpl) wiki.getWikiHome();
        String link = org.exoplatform.wiki.commons.Utils.getURLFromParams(new WikiPageParams(wiki.getType(), wiki.getOwner(), wikiHome.getName()));
        org.exoplatform.wiki.commons.Utils.ajaxRedirect(event, link);
      } else {
        log.warn(String.format("Wrong wiki id: [%s], can not change space", wikiId));
      }
    }
  }
}
