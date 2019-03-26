/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2014 Kai Reinhard (k.reinhard@micromata.de)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.memo;

import org.projectforge.continuousdb.UpdateEntry;
import org.projectforge.menu.builder.MenuItemDef;
import org.projectforge.menu.builder.MenuItemDefId;
import org.projectforge.plugins.core.AbstractPlugin;
import org.projectforge.web.plugin.PluginWicketRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Your plugin initialization. Register all your components such as i18n files, data-access object etc.
 *
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
@Component
public class MemoPlugin extends AbstractPlugin {
  public static final String ID = "memo";

  public static final String RESOURCE_BUNDLE_NAME = "MemoI18nResources";

  // The order of the entities is important for xml dump and imports as well as for test cases (order for deleting objects at the end of
  // each test).
  // The entities are inserted in ascending order and deleted in descending order.
  private static final Class<?>[] PERSISTENT_ENTITIES = new Class<?>[]{MemoDO.class};

  @Autowired
  private MemoDao memoDao;

  @Autowired
  private PluginWicketRegistrationService pluginWicketRegistrationService;

  @Override
  protected void initialize() {
    // DatabaseUpdateDao is needed by the updater:
    MemoPluginUpdates.dao = myDatabaseUpdater;
    memoDao = (MemoDao) applicationContext.getBean("memoDao");
    // Register it:
    register(ID, MemoDao.class, memoDao, "plugins.memo");

    // Register the web part:
    pluginWicketRegistrationService.registerWeb(ID, MemoListPage.class, MemoEditPage.class);

    // Register the menu entry as sub menu entry of the misc menu:
    pluginWicketRegistrationService.registerMenuItem(MenuItemDefId.MISC, new MenuItemDef(ID, "plugins.memo.menu"), MemoListPage.class);

    // Define the access management:
    registerRight(new MemoRight(accessChecker));

    // All the i18n stuff:
    addResourceBundle(RESOURCE_BUNDLE_NAME);
  }

  public void setMemoDao(final MemoDao memoDao) {
    this.memoDao = memoDao;
  }

  @Override
  public UpdateEntry getInitializationUpdateEntry() {
    return MemoPluginUpdates.getInitializationUpdateEntry();
  }
}
