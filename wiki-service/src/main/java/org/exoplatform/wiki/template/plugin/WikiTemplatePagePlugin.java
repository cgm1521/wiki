/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.wiki.template.plugin;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Jul 4, 2011  
 */
public class WikiTemplatePagePlugin extends BaseComponentPlugin {
  List<String> sourcePaths = new ArrayList<String>();

  @SuppressWarnings("unchecked")
  public WikiTemplatePagePlugin(InitParams params) throws Exception {
    ValuesParam vlsParam = params.getValuesParam("sourcePaths") ;
    sourcePaths.addAll(vlsParam.getValues());
  }

  public List<String> getSourcePaths() {
    return this.sourcePaths;
  }
}
