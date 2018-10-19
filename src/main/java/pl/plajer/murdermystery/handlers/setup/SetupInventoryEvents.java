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
import org.bukkit.block.Block;
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
import pl.plajerlair.core.services.exception.ReportedException;
import pl.plajerlair.core.utils.ConfigUtils;
import pl.plajerlair.core.utils.LocationUtils;
import pl.plajerlair.core.utils.XMaterial;

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
  public void onClick(InventoryClickEvent event) {
    try {
      if (event.getWhoClicked().getType() != EntityType.PLAYER) {
        return;
      }
      Player player = (Player) event.getWhoClicked();
      if (!player.hasPermission("murdermystery.admin.create") || !event.getInventory().getName().contains("MM Arena:")
          || event.getInventory().getHolder() != null || event.getCurrentItem() == null
          || !event.getCurrentItem().hasItemMeta() || !event.getCurrentItem().getItemMeta().hasDisplayName()) {
        return;
      }

      String name = event.getCurrentItem().getItemMeta().getDisplayName();
      name = ChatColor.stripColor(name);

      Arena arena = ArenaRegistry.getArena(event.getInventory().getName().replace("MM Arena: ", ""));
      if (event.getCurrentItem().getType() == Material.NAME_TAG && event.getCursor().getType() == Material.NAME_TAG) {
        event.setCancelled(true);
        if (!event.getCursor().hasItemMeta()) {
          player.sendMessage(ChatColor.RED + "This item doesn't has a name!");
          return;
        }
        if (!event.getCursor().getItemMeta().hasDisplayName()) {
          player.sendMessage(ChatColor.RED + "This item doesn't has a name!");
          return;
        }

        player.performCommand("mm " + arena.getID() + " set MAPNAME " + event.getCursor().getItemMeta().getDisplayName());
        event.getCurrentItem().getItemMeta().setDisplayName(ChatColor.GOLD + "Set a mapname (currently: " + event.getCursor().getItemMeta().getDisplayName());
        return;
      }
      ClickType clickType = event.getClick();
      if (name.contains("ending location")) {
        event.setCancelled(true);
        player.closeInventory();
        player.performCommand("mm " + arena.getID() + " set ENDLOC");
        return;
      }
      if (name.contains("starting location")) {
        event.setCancelled(true);
        player.closeInventory();
        player.performCommand("mm " + arena.getID() + " add STARTLOC");
        return;
      }
      if (name.contains("lobby location")) {
        event.setCancelled(true);
        player.closeInventory();
        player.performCommand("mm " + arena.getID() + " set LOBBYLOC");
        return;
      }
      if (name.contains("maximum players")) {
        event.setCancelled(true);
        if (clickType.isRightClick()) {
          event.getCurrentItem().setAmount(event.getCurrentItem().getAmount() + 1);
          player.performCommand("mm " + arena.getID() + " set MAXPLAYERS " + event.getCurrentItem().getAmount());
        }
        if (clickType.isLeftClick()) {
          event.getCurrentItem().setAmount(event.getCurrentItem().getAmount() - 1);
          player.performCommand("mm " + arena.getID() + " set MAXPLAYERS " + event.getCurrentItem().getAmount());
        }
        player.closeInventory();
        player.openInventory(new SetupInventory(arena).getInventory());
      }

      if (name.contains("minimum players")) {
        event.setCancelled(true);
        if (clickType.isRightClick()) {
          event.getCurrentItem().setAmount(event.getCurrentItem().getAmount() + 1);
          player.performCommand("mm " + arena.getID() + " set MINPLAYERS " + event.getCurrentItem().getAmount());
        }
        if (clickType.isLeftClick()) {
          event.getCurrentItem().setAmount(event.getCurrentItem().getAmount() - 1);
          player.performCommand("mm " + arena.getID() + " set MINPLAYERS " + event.getCurrentItem().getAmount());
        }
        player.closeInventory();
        player.openInventory(new SetupInventory(arena).getInventory());
      }
      if (name.contains("Add game sign")) {
        event.setCancelled(true);
        plugin.getMainCommand().getAdminCommands().addSign(player, arena.getID());
        return;
      }
      if (event.getCurrentItem().getType() != Material.NAME_TAG) {
        event.setCancelled(true);
      }
      if (name.contains("Add gold spawn")) {
        event.setCancelled(true);
        player.performCommand("mm " + arena.getID() + " add gold");
        player.closeInventory();
        return;
      }
      if(name.contains("Add mystery cauldron")) {
        event.setCancelled(true);
        Block block = event.getWhoClicked().getTargetBlock(null, 10);
        if(block == null ||
            block.getType() != XMaterial.CAULDRON.parseMaterial()) {
          event.getWhoClicked().sendMessage(ChatColor.RED + "Please target cauldron to continue!");
          return;
        }
        FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
        String loc = LocationUtils.locationToString(block.getLocation());

        List<String> locs = new ArrayList<>(config.getStringList("instances." + arena.getID() + ".mystery-cauldrons"));
        locs.add(loc);
        config.set("instances." + arena.getID() + ".mystery-cauldrons", locs);
        ConfigUtils.saveConfig(plugin, config, "arenas");
        player.sendMessage("Murder Mystery: New mystery cauldron for arena/instance " + arena.getID() + " was added");
        return;
      }
      if(name.contains("Add confessional")) {
        event.setCancelled(true);
        Block block = event.getWhoClicked().getTargetBlock(null, 10);
        if(block == null ||
            block.getType() != XMaterial.ENCHANTING_TABLE.parseMaterial()) {
          event.getWhoClicked().sendMessage(ChatColor.RED + "Please target enchanting table to continue!");
          return;
        }
        FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
        String loc = LocationUtils.locationToString(block.getLocation());

        List<String> locs = new ArrayList<>(config.getStringList("instances." + arena.getID() + ".confessionals"));
        locs.add(loc);
        config.set("instances." + arena.getID() + ".confessionals", locs);
        ConfigUtils.saveConfig(plugin, config, "arenas");
        player.sendMessage("Murder Mystery: New confessional for arena/instance " + arena.getID() + " was added");
        return;
      }
      if (name.contains("Register arena")) {
        event.setCancelled(true);
        event.getWhoClicked().closeInventory();
        if (ArenaRegistry.getArena(arena.getID()).isReady()) {
          event.getWhoClicked().sendMessage(ChatColor.GREEN + "This arena was already validated and is ready to use!");
          return;
        }
        String[] locations = new String[] {"lobbylocation", "Endlocation"};
        String[] spawns = new String[] {"goldspawnpoints", "playerspawnpoints"};
        for (String s : locations) {
          if (!ConfigUtils.getConfig(plugin, "arenas").isSet("instances." + arena.getID() + "." + s) || ConfigUtils.getConfig(plugin, "arenas")
              .getString("instances." + arena.getID() + "." + s).equals(LocationUtils.locationToString(Bukkit.getWorlds().get(0).getSpawnLocation()))) {
            event.getWhoClicked().sendMessage(ChatColor.RED + "Arena validation failed! Please configure following spawn properly: " + s + " (cannot be world spawn location)");
            return;
          }
        }
        for (String s : spawns) {
          if (!ConfigUtils.getConfig(plugin, "arenas").isSet("instances." + arena.getID() + "." + s) || ConfigUtils.getConfig(plugin, "arenas")
              .getStringList("instances." + arena.getID() + "." + s).size() < 3) {
            event.getWhoClicked().sendMessage(ChatColor.RED + "Arena validation failed! Please configure following spawns properly: " + s + " (must be minimum 3 spawns)");
            return;
          }
        }
        event.getWhoClicked().sendMessage(ChatColor.GREEN + "Validation succeeded! Registering new arena instance: " + arena.getID());
        FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
        config.set("instances." + arena.getID() + ".isdone", true);
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
        arena = new Arena(arena.getID(), plugin);
        arena.setReady(true);
        List<Location> playerSpawnPoints = new ArrayList<>();
        for (String loc : config.getStringList("instances." + arena.getID() + ".playerspawnpoints")) {
          playerSpawnPoints.add(LocationUtils.getLocation(loc));
        }
        arena.setPlayerSpawnPoints(playerSpawnPoints);
        List<Location> goldSpawnPoints = new ArrayList<>();
        for (String loc : config.getStringList("instances." + arena.getID() + ".goldspawnpoints")) {
          goldSpawnPoints.add(LocationUtils.getLocation(loc));
        }
        arena.setGoldSpawnPoints(goldSpawnPoints);

        List<SpecialBlock> specialBlocks = new ArrayList<>();
        if(config.isSet("instances." + arena.getID() + ".mystery-cauldrons")) {
          for (String loc : config.getStringList("instances." + arena.getID() + ".mystery-cauldrons")) {
            specialBlocks.add(new SpecialBlock(LocationUtils.getLocation(loc), SpecialBlock.SpecialBlockType.MYSTERY_CAULDRON));
          }
        }
        if(config.isSet("instances." + arena.getID() + ".confessionals")) {
          for (String loc : config.getStringList("instances." + arena.getID() + ".confessionals")) {
            specialBlocks.add(new SpecialBlock(LocationUtils.getLocation(loc), SpecialBlock.SpecialBlockType.PRAISE_DEVELOPER));
          }
        }
        for(SpecialBlock block : specialBlocks) {
          arena.loadSpecialBlock(block);
        }
        arena.setMinimumPlayers(config.getInt("instances." + arena.getID() + ".minimumplayers"));
        arena.setMaximumPlayers(config.getInt("instances." + arena.getID() + ".maximumplayers"));
        arena.setMapName(config.getString("instances." + arena.getID() + ".mapname"));
        arena.setLobbyLocation(LocationUtils.getLocation(config.getString("instances." + arena.getID() + ".lobbylocation")));
        arena.setEndLocation(LocationUtils.getLocation(config.getString("instances." + arena.getID() + ".Endlocation")));
        ArenaRegistry.registerArena(arena);
        arena.start();
        for (Sign s : signsToUpdate) {
          plugin.getSignManager().getLoadedSigns().put(s, arena);
        }
      }
    } catch (Exception ex) {
      new ReportedException(plugin, ex);
    }
  }
}
