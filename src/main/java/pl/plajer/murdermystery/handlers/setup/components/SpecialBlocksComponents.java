/*
 * MurderMystery - Find the murderer, kill him and survive!
 * Copyright (C) 2019  Plajer's Lair - maintained by Plajer and contributors
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
 */

package pl.plajer.murdermystery.handlers.setup.components;

import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.arena.Arena;
import pl.plajer.murdermystery.arena.special.SpecialBlock;
import pl.plajer.murdermystery.handlers.ChatManager;
import pl.plajer.murdermystery.handlers.setup.SetupInventory;
import pl.plajerlair.commonsbox.minecraft.compat.XMaterial;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;
import pl.plajerlair.commonsbox.minecraft.item.ItemBuilder;
import pl.plajerlair.commonsbox.minecraft.serialization.LocationSerializer;

/**
 * @author Plajer
 * <p>
 * Created at 25.05.2019
 */
public class SpecialBlocksComponents implements SetupComponent {

  private SetupInventory setupInventory;

  @Override
  public void prepare(SetupInventory setupInventory) {
    this.setupInventory = setupInventory;
  }

  @Override
  public void injectComponents(StaticPane pane) {
    Player player = setupInventory.getPlayer();
    FileConfiguration config = setupInventory.getConfig();
    Arena arena = setupInventory.getArena();
    Main plugin = setupInventory.getPlugin();

    pane.addItem(new GuiItem(new ItemBuilder(XMaterial.PAPER.parseItem())
        .name(ChatManager.colorRawMessage("&6&lSpecial Blocks Section"))
        .lore(ChatColor.GRAY + "Items on the right will allow")
        .lore(ChatColor.GRAY + "you to add special game blocks!")
        .build()), 0, 3);

    pane.addItem(new GuiItem(new ItemBuilder(XMaterial.ENDER_CHEST.parseItem())
        .name(ChatManager.colorRawMessage("&e&lAdd Mystery Cauldron"))
        .lore(ChatColor.GRAY + "Target a cauldron and add it to the game")
        .lore(ChatColor.GRAY + "it will cost 1 gold per potion!")
        .lore(ChatColor.GRAY + "Configure cauldron potions in specialblocks.yml file!")
        .build(), e -> {
      e.getWhoClicked().closeInventory();
      if (e.getWhoClicked().getTargetBlock(null, 10).getType() != XMaterial.CAULDRON.parseMaterial()) {
        e.getWhoClicked().sendMessage(ChatColor.RED + "Please target cauldron to continue!");
        return;
      }

      arena.loadSpecialBlock(new SpecialBlock(e.getWhoClicked().getTargetBlock(null, 10).getLocation(),
          SpecialBlock.SpecialBlockType.MYSTERY_CAULDRON));
      List<String> cauldrons = new ArrayList<>(config.getStringList("instances." + arena.getId() + ".mystery-cauldrons"));
      cauldrons.add(LocationSerializer.locationToString(e.getWhoClicked().getTargetBlock(null, 10).getLocation()));
      config.set("instances." + arena.getId() + ".mystery-cauldrons", cauldrons);
      player.sendMessage("Murder Mystery: New mystery cauldron for arena/instance " + arena.getId() + " was added");
      ConfigUtils.saveConfig(plugin, config, "arenas");
    }), 1, 3);

    pane.addItem(new GuiItem(new ItemBuilder(XMaterial.ENCHANTING_TABLE.parseItem())
        .name(ChatManager.colorRawMessage("&e&lAdd Confessional"))
        .lore(ChatColor.GRAY + "Target enchanting table and add praise to the developer")
        .lore(ChatColor.GRAY + "confessional, gift for the developer costs 1 gold!")
        .lore(ChatColor.GOLD + "Add some levers in radius of 3 blocks near the enchant table")
        .lore(ChatColor.GOLD + "to allow users to pray there!")
        .lore(ChatColor.RED + "You can either get gifts or curses from prayer!")
        .build(), e -> {
      e.getWhoClicked().closeInventory();
      if (e.getWhoClicked().getTargetBlock(null, 10).getType() != XMaterial.ENCHANTING_TABLE.parseMaterial()) {
        e.getWhoClicked().sendMessage(ChatColor.RED + "Please target enchanting table to continue!");
        return;
      }

      arena.loadSpecialBlock(new SpecialBlock(e.getWhoClicked().getTargetBlock(null, 10).getLocation(),
          SpecialBlock.SpecialBlockType.PRAISE_DEVELOPER));
      List<String> confessionals = new ArrayList<>(config.getStringList("instances." + arena.getId() + ".confessionals"));
      confessionals.add(LocationSerializer.locationToString(e.getWhoClicked().getTargetBlock(null, 10).getLocation()));
      config.set("instances." + arena.getId() + ".confessionals", confessionals);
      player.sendMessage("Murder Mystery: New confessional for arena/instance " + arena.getId() + " was added");
      ConfigUtils.saveConfig(plugin, config, "arenas");
    }), 2, 3);
  }

}
