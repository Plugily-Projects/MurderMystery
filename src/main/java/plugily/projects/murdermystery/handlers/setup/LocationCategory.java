/*
 *
 * MurderMystery
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

import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.handlers.setup.categories.PluginLocationCategory;
import plugily.projects.minigamesbox.classic.handlers.setup.categories.PluginSpecificCategory;
import plugily.projects.minigamesbox.classic.handlers.setup.items.category.CountItem;
import plugily.projects.minigamesbox.classic.handlers.setup.items.category.LocationItem;
import plugily.projects.minigamesbox.classic.handlers.setup.items.category.MultiLocationItem;
import plugily.projects.minigamesbox.classic.utils.helper.ItemBuilder;
import plugily.projects.minigamesbox.classic.utils.serialization.LocationSerializer;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XMaterial;
import plugily.projects.minigamesbox.inventory.normal.NormalFastInv;


/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 01.07.2022
 */
public class LocationCategory extends PluginLocationCategory {
  @Override
  public void addItems(NormalFastInv gui) {
    super.addItems(gui);

    MultiLocationItem starting = new MultiLocationItem(getSetupInventory(), new ItemBuilder(XMaterial.EMERALD_BLOCK.parseMaterial()), "Player Spawn Points", "Location where players will be randomly teleported when the game starts", "playerspawnpoints", 4, inventoryClickEvent -> {
      LocationSerializer.saveLoc(getSetupInventory().getPlugin(), getSetupInventory().getConfig(), "arenas", "instances." + getSetupInventory().getArenaKey() + "." + "startlocation", inventoryClickEvent.getWhoClicked().getLocation());
    }, (emptyConsumer) -> {
    }, true, true, true);
    getItemList().add(starting);
    gui.setItem((getInventoryLine() * 9) + 2, starting);
  }

}