/*
 * MurderMystery - Find the murderer, kill him and survive!
 * Copyright (C) 2020  Plugily Projects - maintained by Tigerpanzer_02, 2Wild4You and contributors
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

package plugily.projects.murdermystery.handlers.setup.components;

import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import pl.plajerlair.commonsbox.minecraft.compat.XMaterial;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;
import pl.plajerlair.commonsbox.minecraft.item.ItemBuilder;
import pl.plajerlair.commonsbox.minecraft.serialization.LocationSerializer;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.arena.special.SpecialBlock;
import plugily.projects.murdermystery.handlers.ChatManager;
import plugily.projects.murdermystery.handlers.setup.SetupInventory;
import plugily.projects.murdermystery.utils.Debugger;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

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
    ChatManager chatManager = plugin.getChatManager();

    pane.addItem(new GuiItem(new ItemBuilder(XMaterial.PAPER.parseItem())
      .name(chatManager.colorRawMessage("&6&lSpecial Blocks Section"))
      .lore(ChatColor.GRAY + "Items on the right will allow")
      .lore(ChatColor.GRAY + "you to add special game blocks!")
      .build()), 0, 3);

    pane.addItem(new GuiItem(new ItemBuilder(XMaterial.ENDER_CHEST.parseItem())
      .name(chatManager.colorRawMessage("&e&lAdd Mystery Cauldron"))
      .lore(ChatColor.GRAY + "Target a cauldron and add it to the game")
      .lore(ChatColor.GRAY + "it will cost 1 gold per potion!")
      .lore(ChatColor.GRAY + "Configure cauldron potions in specialblocks.yml file!")
      .build(), e -> {
      e.getWhoClicked().closeInventory();
      Debugger.debug(Level.INFO, "" + e.getWhoClicked().getTargetBlock(null, 10).getType() + e.getWhoClicked().getTargetBlock(null, 10).getLocation());
      if (e.getWhoClicked().getTargetBlock(null, 15).getType() != Material.CAULDRON) {
        e.getWhoClicked().sendMessage(ChatColor.RED + "Please target cauldron to continue!");
        return;
      }

      arena.loadSpecialBlock(new SpecialBlock(e.getWhoClicked().getTargetBlock(null, 10).getLocation(),
        SpecialBlock.SpecialBlockType.MYSTERY_CAULDRON));
      List<String> cauldrons = new ArrayList<>(config.getStringList("instances." + arena.getId() + ".mystery-cauldrons"));
      cauldrons.add(LocationSerializer.locationToString(e.getWhoClicked().getTargetBlock(null, 10).getLocation()));
      config.set("instances." + arena.getId() + ".mystery-cauldrons", cauldrons);
      player.sendMessage(chatManager.colorRawMessage("&e✔ Completed | &aAdded Cauldron special block!"));
      ConfigUtils.saveConfig(plugin, config, "arenas");
    }), 1, 3);

    pane.addItem(new GuiItem(new ItemBuilder(XMaterial.ENCHANTING_TABLE.parseItem())
      .name(chatManager.colorRawMessage("&e&lAdd Confessional"))
      .lore(ChatColor.GRAY + "Target enchanting table and")
      .lore(ChatColor.GRAY + "add praise to the developer")
      .lore(ChatColor.GRAY + "confessional, gift for")
      .lore(ChatColor.GRAY + "the developer costs 1 gold!")
      .lore(ChatColor.GOLD + "Add some levers in radius")
      .lore(ChatColor.GOLD + "of 3 blocks near the enchant table")
      .lore(ChatColor.GOLD + "to allow users to pray there!")
      .lore(ChatColor.RED + "You can either get gifts")
      .lore(ChatColor.RED + "or curses from prayer!")
      .build(), e -> {
      e.getWhoClicked().closeInventory();
      Debugger.debug(Level.INFO, "" + e.getWhoClicked().getTargetBlock(null, 10).getType() + e.getWhoClicked().getTargetBlock(null, 10).getLocation());
      if (e.getWhoClicked().getTargetBlock(null, 15).getType() != XMaterial.ENCHANTING_TABLE.parseMaterial()) {
        e.getWhoClicked().sendMessage(ChatColor.RED + "Please target enchanting table to continue!");
        return;
      }

      arena.loadSpecialBlock(new SpecialBlock(e.getWhoClicked().getTargetBlock(null, 10).getLocation(),
        SpecialBlock.SpecialBlockType.PRAISE_DEVELOPER));
      List<String> confessionals = new ArrayList<>(config.getStringList("instances." + arena.getId() + ".confessionals"));
      confessionals.add(LocationSerializer.locationToString(e.getWhoClicked().getTargetBlock(null, 10).getLocation()));
      config.set("instances." + arena.getId() + ".confessionals", confessionals);
      player.sendMessage(chatManager.colorRawMessage("&e✔ Completed | &aAdded Confessional special block!"));
      player.sendMessage(chatManager.colorRawMessage("&eInfo | &aRemember to place any lever in radius of 3 near enchant table!"));
      ConfigUtils.saveConfig(plugin, config, "arenas");
    }), 2, 3);
  }

}
