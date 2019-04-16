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

/*
 * Murder Mystery is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Murder Mystery is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Murder Mystery.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Murder Mystery is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Murder Mystery is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Murder Mystery.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.plajer.murdermystery.handlers.setup;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.arena.Arena;
import pl.plajer.murdermystery.arena.ArenaRegistry;
import pl.plajer.murdermystery.arena.special.SpecialBlock;
import pl.plajer.murdermystery.handlers.ChatManager;
import pl.plajer.murdermystery.utils.Utils;
import pl.plajerlair.commonsbox.minecraft.compat.XMaterial;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;
import pl.plajerlair.commonsbox.minecraft.serialization.LocationSerializer;

/**
 * @author Plajer
 * <p>
 * Created at 05.08.2018
 */
public class SetupInventoryEvents implements Listener {

  private Main plugin;

  public SetupInventoryEvents(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void onClick(InventoryClickEvent e) {
    if (e.getWhoClicked().getType() != EntityType.PLAYER) {
      return;
    }
    Player player = (Player) e.getWhoClicked();
    if (!(player.hasPermission("murdermystery.admin.create") && e.getInventory().getName().contains("MM Arena:")
        && Utils.isNamed(e.getCurrentItem()))) {
      return;
    }

    SetupInventory.ClickPosition slot = SetupInventory.ClickPosition.getByPosition(e.getSlot());
    //do not close inventory nor cancel event when setting arena name via name tag
    if (e.getCurrentItem().getType() != Material.NAME_TAG) {
      if (!(slot == SetupInventory.ClickPosition.SET_MINIMUM_PLAYERS || slot == SetupInventory.ClickPosition.SET_MAXIMUM_PLAYERS)) {
        player.closeInventory();
      }
      e.setCancelled(true);
    }

    Arena arena = ArenaRegistry.getArena(e.getInventory().getName().replace("MM Arena: ", ""));
    ClickType clickType = e.getClick();
    String locationString = player.getLocation().getWorld().getName() + "," + player.getLocation().getX() + "," + player.getLocation().getY() + ","
        + player.getLocation().getZ() + "," + player.getLocation().getYaw() + ",0.0";
    FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
    String targetBlock = LocationSerializer.locationToString(e.getWhoClicked().getTargetBlock(null, 10).getLocation());
    switch (slot) {
      case SET_ENDING:
        config.set("instances." + arena.getId() + ".Endlocation", locationString);
        player.sendMessage(ChatManager.colorRawMessage("&e✔ Completed | &aEnding location for arena " + arena.getId() + " set at your location!"));
        break;
      case SET_LOBBY:
        config.set("instances." + arena.getId() + ".lobbylocation", locationString);
        player.sendMessage(ChatManager.colorRawMessage("&e✔ Completed | &aLobby location for arena " + arena.getId() + " set at your location!"));
        break;
      case ADD_STARTING:
        List<String> startingSpawns = config.getStringList("instances." + arena.getId() + ".playerspawnpoints");
        startingSpawns.add(LocationSerializer.locationToString(player.getLocation()));
        config.set("instances." + arena.getId() + ".playerspawnpoints", startingSpawns);
        String startingProgress = startingSpawns.size() >= 4 ? "&e✔ Completed | " : "&c✘ Not completed | ";
        player.sendMessage(ChatManager.colorRawMessage(startingProgress + "&aPlayer spawn added! &8(&7" + startingSpawns.size() + "/4&8)"));
        break;
      case SET_MINIMUM_PLAYERS:
        if (clickType.isRightClick()) {
          e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() + 1);
        }
        if (clickType.isLeftClick()) {
          e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() - 1);
        }
        config.set("instances." + arena.getId() + ".minimumplayers", e.getCurrentItem().getAmount());
        player.updateInventory();
        break;
      case SET_MAXIMUM_PLAYERS:
        if (clickType.isRightClick()) {
          e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() + 1);
        }
        if (clickType.isLeftClick()) {
          e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() - 1);
        }
        config.set("instances." + arena.getId() + ".maximumplayers", e.getCurrentItem().getAmount());
        player.updateInventory();
        break;
      case ADD_SIGN:
        Location location = player.getTargetBlock(null, 10).getLocation();
        if (!(location.getBlock().getState() instanceof Sign)) {
          player.sendMessage(ChatManager.colorMessage("Commands.Look-Sign"));
          break;
        }
        plugin.getSignManager().getLoadedSigns().put((Sign) location.getBlock().getState(), arena);
        player.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("Signs.Sign-Created"));
        String signLoc = location.getBlock().getWorld().getName() + "," + location.getBlock().getX() + "," + location.getBlock().getY() + "," + location.getBlock().getZ() + ",0.0,0.0";
        List<String> locs = config.getStringList("instances." + arena.getId() + ".signs");
        locs.add(signLoc);
        config.set("instances." + arena.getId() + ".signs", locs);
        break;
      case SET_MAP_NAME:
        if (e.getCurrentItem().getType() == Material.NAME_TAG && e.getCursor().getType() == Material.NAME_TAG) {
          if (!Utils.isNamed(e.getCursor())) {
            player.sendMessage(ChatColor.RED + "This item doesn't has a name!");
            return;
          }
          String newName = e.getCursor().getItemMeta().getDisplayName();
          config.set("instances." + arena.getId() + ".mapname", newName);
          player.sendMessage(ChatManager.colorRawMessage("&e✔ Completed | &aName of arena " + arena.getId() + " set to " + newName));
          e.getCurrentItem().getItemMeta().setDisplayName(ChatColor.GOLD + "Set a mapname (currently: " + newName);
        }
        break;
      case ADD_GOLD_SPAWN:
        List<String> goldSpawns = config.getStringList("instances." + arena.getId() + ".goldspawnpoints");
        goldSpawns.add(LocationSerializer.locationToString(player.getLocation()));
        config.set("instances." + arena.getId() + ".goldspawnpoints", goldSpawns);
        String goldProgress = goldSpawns.size() >= 4 ? "&e✔ Completed | " : "&c✘ Not completed | ";
        player.sendMessage(ChatManager.colorRawMessage(goldProgress + "&aGold spawn added! &8(&7" + goldSpawns.size() + "/4&8)"));
        break;
      case REGISTER_ARENA:
        if (ArenaRegistry.getArena(arena.getId()).isReady()) {
          e.getWhoClicked().sendMessage(ChatColor.GREEN + "This arena was already validated and is ready to use!");
          return;
        }
        String[] locations = new String[] {"lobbylocation", "Endlocation"};
        String[] spawns = new String[] {"goldspawnpoints", "playerspawnpoints"};
        FileConfiguration arenasConfig = ConfigUtils.getConfig(plugin, "arenas");
        for (String s : locations) {
          if (!arenasConfig.isSet("instances." + arena.getId() + "." + s) || arenasConfig.getString("instances." + arena.getId() + "." + s).equals(LocationSerializer.locationToString(Bukkit.getWorlds().get(0).getSpawnLocation()))) {
            e.getWhoClicked().sendMessage(ChatColor.RED + "Arena validation failed! Please configure following spawn properly: " + s + " (cannot be world spawn location)");
            return;
          }
        }
        for (String s : spawns) {
          if (!arenasConfig.isSet("instances." + arena.getId() + "." + s) || arenasConfig.getStringList("instances." + arena.getId() + "." + s).size() < 3) {
            e.getWhoClicked().sendMessage(ChatColor.RED + "Arena validation failed! Please configure following spawns properly: " + s + " (must be minimum 3 spawns)");
            return;
          }
        }
        e.getWhoClicked().sendMessage(ChatColor.GREEN + "Validation succeeded! Registering new arena instance: " + arena.getId());
        config.set("instances." + arena.getId() + ".isdone", true);
        ConfigUtils.saveConfig(plugin, config, "arenas");
        List<Sign> signsToUpdate = new ArrayList<>();
        ArenaRegistry.unregisterArena(arena);
        if (plugin.getSignManager().getLoadedSigns().containsValue(arena)) {
          for (Sign s : plugin.getSignManager().getLoadedSigns().keySet()) {
            if (plugin.getSignManager().getLoadedSigns().get(s).equals(arena)) {
              signsToUpdate.add(s);
            }
          }
        }
        arena = new Arena(arena.getId(), plugin);
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
          plugin.getSignManager().getLoadedSigns().put(s, arena);
        }
        break;
      case VIEW_SETUP_VIDEO:
        player.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorRawMessage("&6Check out this video: " + SetupInventory.VIDEO_LINK));
        break;
      case SPECIAL_BLOCKS:
        //do nothing
        break;
      case ADD_MYSTERY_CAULDRON:
        if (e.getWhoClicked().getTargetBlock(null, 10).getType() != XMaterial.CAULDRON.parseMaterial()) {
          e.getWhoClicked().sendMessage(ChatColor.RED + "Please target cauldron to continue!");
          return;
        }

        arena.loadSpecialBlock(new SpecialBlock(e.getWhoClicked().getTargetBlock(null, 10).getLocation(),
            SpecialBlock.SpecialBlockType.MYSTERY_CAULDRON));
        List<String> cauldrons = new ArrayList<>(config.getStringList("instances." + arena.getId() + ".mystery-cauldrons"));
        cauldrons.add(targetBlock);
        config.set("instances." + arena.getId() + ".mystery-cauldrons", cauldrons);
        player.sendMessage("Murder Mystery: New mystery cauldron for arena/instance " + arena.getId() + " was added");
        break;
      case ADD_CONFESSIONAL:
        if (e.getWhoClicked().getTargetBlock(null, 10).getType() != XMaterial.ENCHANTING_TABLE.parseMaterial()) {
          e.getWhoClicked().sendMessage(ChatColor.RED + "Please target enchanting table to continue!");
          return;
        }

        arena.loadSpecialBlock(new SpecialBlock(e.getWhoClicked().getTargetBlock(null, 10).getLocation(),
            SpecialBlock.SpecialBlockType.PRAISE_DEVELOPER));
        List<String> confessionals = new ArrayList<>(config.getStringList("instances." + arena.getId() + ".confessionals"));
        confessionals.add(targetBlock);
        config.set("instances." + arena.getId() + ".confessionals", confessionals);
        player.sendMessage("Murder Mystery: New confessional for arena/instance " + arena.getId() + " was added");
        break;
      /*case SPECIAL_BLOCK_REMOVER:
        player.getInventory().addItem(new ItemBuilder(Material.BLAZE_ROD).name(ChatManager.colorRawMessage("&e&lSpecial Blocks Remover")).build());
        player.sendMessage(ChatManager.colorRawMessage("&6You received special blocks remover wand! Click special block to remove it from any arena configuration!\n" +
            "&cIt will require plugin/server restart to apply changes!"));
        break;*/
    }
    ConfigUtils.saveConfig(plugin, config, "arenas");
  }
}
