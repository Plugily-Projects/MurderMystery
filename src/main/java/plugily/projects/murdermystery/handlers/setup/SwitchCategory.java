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

import org.bukkit.plugin.java.JavaPlugin;
import plugily.projects.minigamesbox.classic.PluginMain;
import plugily.projects.minigamesbox.classic.handlers.setup.categories.PluginSwitchCategory;
import plugily.projects.minigamesbox.classic.handlers.setup.items.category.SwitchItem;
import plugily.projects.minigamesbox.classic.utils.helper.ItemBuilder;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XMaterial;
import plugily.projects.minigamesbox.inventory.normal.NormalFastInv;
import plugily.projects.murdermystery.Main;

import java.util.Arrays;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 01.07.2022
 */
public class SwitchCategory extends PluginSwitchCategory {
  @Override
  public void addItems(NormalFastInv gui) {
    super.addItems(gui);
    SwitchItem goldVisuals = new SwitchItem(getSetupInventory(), new ItemBuilder(XMaterial.REDSTONE.parseMaterial()), "Gold Visuals", "Enables gold visuals to spawn\nsome particle effects above gold locations", "goldvisuals", Arrays.asList("true", "false"), inventoryClickEvent -> {
      JavaPlugin.getPlugin(Main.class).getArenaRegistry().getArena(getSetupInventory().getArenaKey()).toggleGoldVisuals();
    });
    gui.setItem((getInventoryLine() * 9) + 1, goldVisuals);
    getItemList().add(goldVisuals);
  }

}
