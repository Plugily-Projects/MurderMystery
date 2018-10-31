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

  //todo test
  @EventHandler
  public void onClick(InventoryClickEvent e) {
    try {
      if (!(e.getWhoClicked() instanceof Player) || e.getInventory() == null || e.getCurrentItem() == null || e.getInventory().getHolder() == null ||
          !e.getCurrentItem().hasItemMeta() || !e.getCurrentItem().getItemMeta().hasDisplayName() || !e.getWhoClicked().hasPermission("murdermystery.admin.create") ||
          !e.getInventory().getName().contains("MM Arena:")) {
        return;
      }
      e.setCancelled(true);
      Player player = (Player) e.getWhoClicked();
      player.closeInventory();
      Arena arena = ArenaRegistry.getArena(e.getInventory().getName().replace("MM Arena: ", ""));
      String itemName = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());

      FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
      Block block = e.getWhoClicked().getTargetBlock(null, 10);
      String targetBlock = LocationUtils.locationToString(Utils.fixLocation(block.getLocation()));
      switch (itemName) {
        case "► Set ending location":
          player.performCommand("mm " + arena.getID() + " set ENDLOC");
          return;
        case "► Set lobby location":
          player.performCommand("mm " + arena.getID() + " set LOBBYLOC");
          return;
        case "► Add starting location":
          player.performCommand("mm " + arena.getID() + " add STARTLOC");
          return;
        case "► Set minimum players size":
          if (e.getClick().isRightClick()) {
            e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() + 1);
            player.performCommand("mm " + arena.getID() + " set MINPLAYERS " + e.getCurrentItem().getAmount());
          }
          if (e.getClick().isLeftClick()) {
            e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() - 1);
            player.performCommand("mm " + arena.getID() + " set MINPLAYERS " + e.getCurrentItem().getAmount());
          }
          player.openInventory(new SetupInventory(arena).getInventory());
          break;
        case "► Set maximum players size":
          if (e.getClick().isRightClick()) {
            e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() + 1);
            player.performCommand("mm " + arena.getID() + " set MAXPLAYERS " + e.getCurrentItem().getAmount());
          }
          if (e.getClick().isLeftClick()) {
            e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() - 1);
            player.performCommand("mm " + arena.getID() + " set MAXPLAYERS " + e.getCurrentItem().getAmount());
          }
          player.openInventory(new SetupInventory(arena).getInventory());
          return;
        case "► Add game sign":
          plugin.getMainCommand().getAdminCommands().addSign(player, arena.getID());
          return;
        case "► Add gold spawn":
          player.performCommand("mm " + arena.getID() + " add gold");
          return;
        case "► Register arena":
          if (ArenaRegistry.getArena(arena.getID()).isReady()) {
            e.getWhoClicked().sendMessage(ChatColor.GREEN + "This arena was already validated and is ready to use!");
            return;
          }
          String[] locations = new String[] {"lobbylocation", "Endlocation"};
          String[] spawns = new String[] {"goldspawnpoints", "playerspawnpoints"};
          for (String s : locations) {
            if (!ConfigUtils.getConfig(plugin, "arenas").isSet("instances." + arena.getID() + "." + s) || ConfigUtils.getConfig(plugin, "arenas")
                .getString("instances." + arena.getID() + "." + s).equals(LocationUtils.locationToString(Bukkit.getWorlds().get(0).getSpawnLocation()))) {
              e.getWhoClicked().sendMessage(ChatColor.RED + "Arena validation failed! Please configure following spawn properly: " + s + " (cannot be world spawn location)");
              return;
            }
          }
          for (String s : spawns) {
            if (!ConfigUtils.getConfig(plugin, "arenas").isSet("instances." + arena.getID() + "." + s) || ConfigUtils.getConfig(plugin, "arenas")
                .getStringList("instances." + arena.getID() + "." + s).size() < 3) {
              e.getWhoClicked().sendMessage(ChatColor.RED + "Arena validation failed! Please configure following spawns properly: " + s + " (must be minimum 3 spawns)");
              return;
            }
          }
          e.getWhoClicked().sendMessage(ChatColor.GREEN + "Validation succeeded! Registering new arena instance: " + arena.getID());
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
          if (config.isSet("instances." + arena.getID() + ".mystery-cauldrons")) {
            for (String loc : config.getStringList("instances." + arena.getID() + ".mystery-cauldrons")) {
              specialBlocks.add(new SpecialBlock(LocationUtils.getLocation(loc), SpecialBlock.SpecialBlockType.MYSTERY_CAULDRON));
            }
          }
          if (config.isSet("instances." + arena.getID() + ".confessionals")) {
            for (String loc : config.getStringList("instances." + arena.getID() + ".confessionals")) {
              specialBlocks.add(new SpecialBlock(LocationUtils.getLocation(loc), SpecialBlock.SpecialBlockType.PRAISE_DEVELOPER));
            }
          }
          for (SpecialBlock specialBlock : specialBlocks) {
            if (arena.getSpecialBlocks().contains(specialBlock)) {
              continue;
            }
            arena.loadSpecialBlock(specialBlock);
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
          return;
        case "► View setup video":
          player.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorRawMessage("&6Check out this video: " + SetupInventory.VIDEO_LINK));
          return;

        //special blocks
        case "Special blocks section":
          return;
        case "► Add mystery cauldron":
          if (block.getType() != XMaterial.CAULDRON.parseMaterial()) {
            e.getWhoClicked().sendMessage(ChatColor.RED + "Please target cauldron to continue!");
            return;
          }

          arena.loadSpecialBlock(new SpecialBlock(Utils.fixLocation(block.getLocation()), SpecialBlock.SpecialBlockType.MYSTERY_CAULDRON));
          List<String> cauldrons = new ArrayList<>(config.getStringList("instances." + arena.getID() + ".mystery-cauldrons"));
          cauldrons.add(targetBlock);
          config.set("instances." + arena.getID() + ".mystery-cauldrons", cauldrons);
          ConfigUtils.saveConfig(plugin, config, "arenas");
          player.sendMessage("Murder Mystery: New mystery cauldron for arena/instance " + arena.getID() + " was added");
          return;
        case "► Add confessional":
          if (block.getType() != XMaterial.ENCHANTING_TABLE.parseMaterial()) {
            e.getWhoClicked().sendMessage(ChatColor.RED + "Please target enchanting table to continue!");
            return;
          }

          arena.loadSpecialBlock(new SpecialBlock(Utils.fixLocation(block.getLocation()), SpecialBlock.SpecialBlockType.PRAISE_DEVELOPER));
          List<String> confessionals = new ArrayList<>(config.getStringList("instances." + arena.getID() + ".confessionals"));
          confessionals.add(targetBlock);
          config.set("instances." + arena.getID() + ".confessionals", confessionals);
          ConfigUtils.saveConfig(plugin, config, "arenas");
          player.sendMessage("Murder Mystery: New confessional for arena/instance " + arena.getID() + " was added");
          return;
      }

      if (e.getCurrentItem().getType() == Material.NAME_TAG && e.getCursor().getType() == Material.NAME_TAG) {
        if (!e.getCursor().hasItemMeta()) {
          player.sendMessage(ChatColor.RED + "This item doesn't has a name!");
          return;
        }
        if (!e.getCursor().getItemMeta().hasDisplayName()) {
          player.sendMessage(ChatColor.RED + "This item doesn't has a name!");
          return;
        }

        player.performCommand("mm " + arena.getID() + " set MAPNAME " + e.getCursor().getItemMeta().getDisplayName());
        e.getCurrentItem().getItemMeta().setDisplayName(ChatColor.GOLD + "Set a mapname (currently: " + e.getCursor().getItemMeta().getDisplayName());
      }
    } catch (Exception ex) {
      new ReportedException(plugin, ex);
    }
  }
}
