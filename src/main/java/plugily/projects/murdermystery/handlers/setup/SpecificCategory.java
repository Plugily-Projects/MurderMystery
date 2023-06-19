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

import org.bukkit.Material;
import plugily.projects.minigamesbox.classic.handlers.setup.categories.PluginSpecificCategory;
import plugily.projects.minigamesbox.classic.handlers.setup.items.category.CountItem;
import plugily.projects.minigamesbox.classic.handlers.setup.items.category.MaterialLocationItem;
import plugily.projects.minigamesbox.classic.handlers.setup.items.category.MaterialMultiLocationItem;
import plugily.projects.minigamesbox.classic.handlers.setup.items.category.MultiLocationItem;
import plugily.projects.minigamesbox.classic.utils.helper.ItemBuilder;
import plugily.projects.minigamesbox.classic.utils.helper.MaterialUtils;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XMaterial;
import plugily.projects.minigamesbox.inventory.normal.NormalFastInv;

import java.util.Collections;


/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 01.07.2022
 */
public class SpecificCategory extends PluginSpecificCategory {
  @Override
  public void addItems(NormalFastInv gui) {
    super.addItems(gui);

    CountItem spawnGoldTime = new CountItem(getSetupInventory(), new ItemBuilder(XMaterial.REDSTONE_TORCH.parseMaterial()), "Gold Spawn Time (Seconds)", "How much gold should be spawned? \nThat means 1 gold spawned every ... seconds\nDefault: 5\nEvery 5 seconds it will spawn 1 gold", "spawngoldtime");
    gui.setItem((getInventoryLine() * 9) + 1, spawnGoldTime);
    getItemList().add(spawnGoldTime);

    CountItem playerMurderer = new CountItem(getSetupInventory(), new ItemBuilder(XMaterial.IRON_SWORD.parseMaterial()), "Player Per Murderer", "How many murderer should be ingame? This means \none murderer for that amount of players. Default: \n5 players are 1 murderer, that means if we have \n14 Players it will calculate 2 murderer! \nSet it to 1 if you want only one murderer ", "playerpermurderer");
    gui.setItem((getInventoryLine() * 9) + 2, playerMurderer);
    getItemList().add(playerMurderer);

    CountItem playerDetective = new CountItem(getSetupInventory(), new ItemBuilder(XMaterial.IRON_SWORD.parseMaterial()), "Player Per Murderer", "How many detectives should be ingame? This means \none detective for that amount of players. Default: \n7 players are 1 detective, that means if we have \n18 Players it will calculate 2 detectives! \nSet it to 1 if you want only one detective ", "playerperdetective");
    gui.setItem((getInventoryLine() * 9) + 3, playerDetective);
    getItemList().add(playerDetective);

    MultiLocationItem goldSpawn = new MultiLocationItem(getSetupInventory(), new ItemBuilder(XMaterial.GOLD_INGOT.parseMaterial()), "Gold Spawn", "Add new gold spawn \n on the place you're standing at.", "goldspawnpoints", 4);
    gui.setItem((getInventoryLine() * 9) + 4, goldSpawn);
    getItemList().add(goldSpawn);

    MaterialMultiLocationItem mysteryCauldron = new MaterialMultiLocationItem(getSetupInventory(), new ItemBuilder(XMaterial.CAULDRON.parseMaterial()), "Mystery Cauldron", "Target a cauldron and add it to the game\nit will cost 1 gold per potion!\nConfigure cauldron potions \nin special_blocks.yml file!", "mystery-cauldrons", Collections.singleton(Material.CAULDRON), false, 0);
    gui.setItem((getInventoryLine() * 9) + 5, mysteryCauldron);
    getItemList().add(mysteryCauldron);

    MaterialMultiLocationItem confessional = new MaterialMultiLocationItem(getSetupInventory(), new ItemBuilder(XMaterial.ENCHANTING_TABLE.parseMaterial()), "Confessional", "Target enchanting table and\nadd praise to the developer\nconfessional, gift for\nthe developer costs 1 gold!\nAdd some levers in radius\nof 3 blocks near the enchant table\nto allow users to pray there!\nYou can either get gifts\nor curses from prayer!", "confessionals", Collections.singleton(XMaterial.ENCHANTING_TABLE.parseMaterial()), false, 0);
    gui.setItem((getInventoryLine() * 9) + 6, confessional);
    getItemList().add(confessional);

  }

}