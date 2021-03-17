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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import pl.plajerlair.commonsbox.minecraft.compat.xseries.XMaterial;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;
import pl.plajerlair.commonsbox.minecraft.item.ItemBuilder;
import pl.plajerlair.commonsbox.minecraft.serialization.LocationSerializer;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.arena.ArenaRegistry;
import plugily.projects.murdermystery.arena.special.SpecialBlock;
import plugily.projects.murdermystery.handlers.ChatManager;
import plugily.projects.murdermystery.handlers.setup.SetupInventory;
import plugily.projects.murdermystery.handlers.sign.ArenaSign;

import java.util.ArrayList;
import java.util.List;

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
    Main plugin = setupInventory.getPlugin();
    ChatManager chatManager = plugin.getChatManager();
    ItemStack registeredItem;
    if(!setupInventory.getArena().isReady()) {
      registeredItem = new ItemBuilder(XMaterial.FIREWORK_ROCKET.parseItem())
        .name(chatManager.colorRawMessage("&e&lRegister Arena - Finish Setup"))
        .lore(ChatColor.GRAY + "Click this when you're done with configuration.")
        .lore(ChatColor.GRAY + "It will validate and register arena.")
        .build();
    } else {
      registeredItem = new ItemBuilder(Material.BARRIER)
        .name(chatManager.colorRawMessage("&a&lArena Registered - Congratulations"))
        .lore(ChatColor.GRAY + "This arena is already registered!")
        .lore(ChatColor.GRAY + "Good job, you went through whole setup!")
        .lore(ChatColor.GRAY + "You can play on this arena now!")
        .build();
    }
    pane.addItem(new GuiItem(registeredItem, e -> {
      Arena arena = setupInventory.getArena();
      e.getWhoClicked().closeInventory();
      if(arena.isReady()) {
        e.getWhoClicked().sendMessage(chatManager.colorRawMessage("&a&l✔ &aThis arena was already validated and is ready to use!"));
        return;
      }
      String path = "instances." + arena.getId() + ".";
      String[] locations = {"lobbylocation", "Endlocation"};
      String[] spawns = {"goldspawnpoints", "playerspawnpoints"};
      FileConfiguration arenasConfig = ConfigUtils.getConfig(plugin, "arenas");
      for(String s : locations) {
        if(!arenasConfig.isSet(path + s) || arenasConfig.getString(path + s).equals(LocationSerializer.locationToString(Bukkit.getWorlds().get(0).getSpawnLocation()))) {
          e.getWhoClicked().sendMessage(chatManager.colorRawMessage("&c&l✘ &cArena validation failed! Please configure following spawn properly: " + s + " (cannot be world spawn location)"));
          return;
        }
      }
      for(String s : spawns) {
        if(!arenasConfig.isSet(path + s) || arenasConfig.getStringList(path + s).size() < 4) {
          e.getWhoClicked().sendMessage(chatManager.colorRawMessage("&c&l✘ &cArena validation failed! Please configure following spawns properly: " + s + " (must be minimum 4 spawns)"));
          return;
        }
      }
      e.getWhoClicked().sendMessage(chatManager.colorRawMessage("&a&l✔ &aValidation succeeded! Registering new arena instance: " + arena.getId()));

      arenasConfig.set(path + "isdone", true);
      ConfigUtils.saveConfig(plugin, arenasConfig, "arenas");

      List<Sign> signsToUpdate = new ArrayList<>();
      ArenaRegistry.unregisterArena(setupInventory.getArena());

      plugin.getSignManager().getArenaSigns().stream().filter(arenaSign -> arenaSign.getArena().equals(setupInventory.getArena()))
        .forEach(arenaSign -> signsToUpdate.add(arenaSign.getSign()));

      arena = new Arena(setupInventory.getArena().getId());
      arena.setReady(true);
      List<Location> playerSpawnPoints = new ArrayList<>();
      for(String loc : arenasConfig.getStringList(path + "playerspawnpoints")) {
        playerSpawnPoints.add(LocationSerializer.getLocation(loc));
      }
      arena.setPlayerSpawnPoints(playerSpawnPoints);
      List<Location> goldSpawnPoints = new ArrayList<>();
      for(String loc : arenasConfig.getStringList(path + "goldspawnpoints")) {
        goldSpawnPoints.add(LocationSerializer.getLocation(loc));
      }
      arena.setGoldSpawnPoints(goldSpawnPoints);

      List<SpecialBlock> specialBlocks = new ArrayList<>();
      if(arenasConfig.isSet(path + "mystery-cauldrons")) {
        for(String loc : arenasConfig.getStringList(path + "mystery-cauldrons")) {
          specialBlocks.add(new SpecialBlock(LocationSerializer.getLocation(loc), SpecialBlock.SpecialBlockType.MYSTERY_CAULDRON));
        }
      }
      if(arenasConfig.isSet(path + "confessionals")) {
        for(String loc : arenasConfig.getStringList(path + "confessionals")) {
          specialBlocks.add(new SpecialBlock(LocationSerializer.getLocation(loc), SpecialBlock.SpecialBlockType.PRAISE_DEVELOPER));
        }
      }
      for(SpecialBlock specialBlock : specialBlocks) {
        if(!arena.getSpecialBlocks().contains(specialBlock)) {
          arena.loadSpecialBlock(specialBlock);
        }
      }
      arena.setMinimumPlayers(arenasConfig.getInt(path + "minimumplayers"));
      arena.setMaximumPlayers(arenasConfig.getInt(path + "maximumplayers"));
      arena.setMapName(arenasConfig.getString(path + "mapname"));
      arena.setSpawnGoldTime(arenasConfig.getInt(path + "spawngoldtime", 5));
      arena.setHideChances(arenasConfig.getBoolean(path + "hidechances", false));
      arena.setLobbyLocation(LocationSerializer.getLocation(arenasConfig.getString(path + "lobbylocation")));
      arena.setEndLocation(LocationSerializer.getLocation(arenasConfig.getString(path + "Endlocation")));
      arena.setMurderers(arenasConfig.getInt(path + "playerpermurderer", 5));
      arena.setDetectives(arenasConfig.getInt(path + "playerperdetective", 7));
      ArenaRegistry.registerArena(arena);
      arena.start();
      plugin.getSignManager().getArenaSigns().clear();
      for(Sign s : signsToUpdate) {
        plugin.getSignManager().getArenaSigns().add(new ArenaSign(s, arena));
        plugin.getSignManager().updateSigns();
      }
    }), 8, 0);
  }

}
