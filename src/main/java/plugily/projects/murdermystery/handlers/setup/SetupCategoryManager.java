/*
 *
 * BuildBattle - Ultimate building competition minigame
 * Copyright (C) 2021 Plugily Projects - maintained by Tigerpanzer_02, 2Wild4You and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package plugily.projects.murdermystery.handlers.setup;

import plugily.projects.minigamesbox.classic.handlers.setup.SetupInventory;
import plugily.projects.minigamesbox.classic.handlers.setup.categories.PluginSetupCategoryManager;
import plugily.projects.minigamesbox.classic.handlers.setup.categories.SetupCategory;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 01.07.2022
 */
public class SetupCategoryManager extends PluginSetupCategoryManager {

  public SetupCategoryManager(SetupInventory setupInventory) {
    super(setupInventory);
    getCategoryHandler().put(SetupCategory.LOCATIONS, new LocationCategory());
    getCategoryHandler().put(SetupCategory.SPECIFIC, new SpecificCategory());
    getCategoryHandler().put(SetupCategory.SWITCH, new SwitchCategory());
    super.init();
  }

}
