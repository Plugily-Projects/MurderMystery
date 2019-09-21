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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.arena.Arena;
import pl.plajer.murdermystery.arena.ArenaRegistry;
import pl.plajer.murdermystery.arena.special.SpecialBlock;
import pl.plajer.murdermystery.handlers.ChatManager;
import pl.plajer.murdermystery.handlers.setup.SetupInventory;
import pl.plajer.murdermystery.handlers.sign.ArenaSign;
import pl.plajerlair.commonsbox.minecraft.compat.XMaterial;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;
import pl.plajerlair.commonsbox.minecraft.item.ItemBuilder;
import pl.plajerlair.commonsbox.minecraft.serialization.LocationSerializer;

/**
 * @author Plajer
 * <p>
 * Created at 25.05.2019
 */
public class ArenaRegisterComponent implements SetupComponent {

  private SetupInventory setupInventory;

  @Override
  public void prepare(SetupInventory setupInventory) {
    this.setupInventory = setupInventory;
  }

  @Override
  public void injectComponents(StaticPane pane) {
    FileConfiguration config = setupInventory.getConfig();
    Main plugin = setupInventory.getPlugin();
    ItemStack registeredItem;
    if (!setupInventory.getArena().isReady()) {
      registeredItem = new ItemBuilder(XMaterial.FIREWORK_ROCKET.parseItem())
        .name(ChatManager.colorRawMessage("&e&lRegister Arena - Finish Setup"))
        .lore(ChatColor.GRAY + "Click this when you're done with configuration.")
        .lore(ChatColor.GRAY + "It will validate and register arena.")
        .build();
    } else {
      registeredItem = new ItemBuilder(Material.BARRIER)
        .name(ChatManager.colorRawMessage("&a&lArena Registered - Congratulations"))
        .lore(ChatColor.GRAY + "This arena is already registered!")
        .lore(ChatColor.GRAY + "Good job, you went through whole setup!")
        .lore(ChatColor.GRAY + "You can play on this arena now!")
        .build();
    }
    pane.addItem(new GuiItem(registeredItem, e -> {
      Arena arena = setupInventory.getArena();
      if (arena.isReady()) {
        return;
      }
      e.getWhoClicked().closeInventory();
      if (ArenaRegistry.getArena(setupInventory.getArena().getId()).isReady()) {
        e.getWhoClicked().sendMessage(ChatManager.colorRawMessage("&a&l✔ &aThis arena was already validated and is ready to use!"));
        return;
      }
      String[] locations = new String[] {"lobbylocation", "Endlocation"};
      String[] spawns = new String[] {"goldspawnpoints", "playerspawnpoints"};
      FileConfiguration arenasConfig = ConfigUtils.getConfig(plugin, "arenas");
      for (String s : locations) {
        if (!arenasConfig.isSet("instances." + arena.getId() + "." + s) || arenasConfig.getString("instances." + arena.getId() + "." + s).equals(LocationSerializer.locationToString(Bukkit.getWorlds().get(0).getSpawnLocation()))) {
          e.getWhoClicked().sendMessage(ChatManager.colorRawMessage("&c&l✘ &cArena validation failed! Please configure following spawn properly: " + s + " (cannot be world spawn location)"));
          return;
        }
      }
      for (String s : spawns) {
        if (!arenasConfig.isSet("instances." + arena.getId() + "." + s) || arenasConfig.getStringList("instances." + arena.getId() + "." + s).size() < 4) {
          e.getWhoClicked().sendMessage(ChatManager.colorRawMessage("&c&l✘ &cArena validation failed! Please configure following spawns properly: " + s + " (must be minimum 4 spawns)"));
          return;
        }
      }
      e.getWhoClicked().sendMessage(ChatManager.colorRawMessage("&a&l✔ &aValidation succeeded! Registering new arena instance: " + arena.getId()));
      config.set("instances." + arena.getId() + ".isdone", true);
      ConfigUtils.saveConfig(plugin, config, "arenas");
      List<Sign> signsToUpdate = new ArrayList<>();
      ArenaRegistry.unregisterArena(setupInventory.getArena());

      for (ArenaSign arenaSign : plugin.getSignManager().getArenaSigns()) {
        if (arenaSign.getArena().equals(setupInventory.getArena())) {
          signsToUpdate.add(arenaSign.getSign());
        }
      }
      arena = new Arena(setupInventory.getArena().getId());
      arena.setReady(true);
      List<Location> playerSpawnPoints = new ArrayList<>();
      for (String loc : config.getStringList("instances." + arena.getId() + ".playerspawnpoints")) {
        playerSpawnPoints.add(LocationSerializer.getLocation(loc));
      }
      arena.setPlayerSpawnPoints(playerSpawnPoints);
      List<Location> goldSpawnPoints = new ArrayList<>();
      for (String loc : config.getStringList("instances." + arena.getId() + ".goldspawnpoints")) {
        goldSpawnPoints.add(LocationSerializer.getLocation(loc));
      }
      arena.setGoldSpawnPoints(goldSpawnPoints);

      List<SpecialBlock> specialBlocks = new ArrayList<>();
      if (config.isSet("instances." + arena.getId() + ".mystery-cauldrons")) {
        for (String loc : config.getStringList("instances." + arena.getId() + ".mystery-cauldrons")) {
          specialBlocks.add(new SpecialBlock(LocationSerializer.getLocation(loc), SpecialBlock.SpecialBlockType.MYSTERY_CAULDRON));
        }
      }
      if (config.isSet("instances." + arena.getId() + ".confessionals")) {
        for (String loc : config.getStringList("instances." + arena.getId() + ".confessionals")) {
          specialBlocks.add(new SpecialBlock(LocationSerializer.getLocation(loc), SpecialBlock.SpecialBlockType.PRAISE_DEVELOPER));
        }
      }
      for (SpecialBlock specialBlock : specialBlocks) {
        if (arena.getSpecialBlocks().contains(specialBlock)) {
          continue;
        }
        arena.loadSpecialBlock(specialBlock);
      }
      arena.setMinimumPlayers(config.getInt("instances." + arena.getId() + ".minimumplayers"));
      arena.setMaximumPlayers(config.getInt("instances." + arena.getId() + ".maximumplayers"));
      arena.setMapName(config.getString("instances." + arena.getId() + ".mapname"));
      arena.setLobbyLocation(LocationSerializer.getLocation(config.getString("instances." + arena.getId() + ".lobbylocation")));
      arena.setEndLocation(LocationSerializer.getLocation(config.getString("instances." + arena.getId() + ".Endlocation")));
      ArenaRegistry.registerArena(arena);
      arena.start();
      for (Sign s : signsToUpdate) {
        plugin.getSignManager().getArenaSigns().add(new ArenaSign(s, arena));
      }
      ConfigUtils.saveConfig(plugin, config, "arenas");
    }), 8, 0);
  }

}
