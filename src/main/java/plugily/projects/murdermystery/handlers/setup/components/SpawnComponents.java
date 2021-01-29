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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;
import pl.plajerlair.commonsbox.minecraft.item.ItemBuilder;
import pl.plajerlair.commonsbox.minecraft.serialization.LocationSerializer;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.handlers.ChatManager;
import plugily.projects.murdermystery.handlers.setup.SetupInventory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Plajer
 * <p>
 * Created at 25.05.2019
 */
public class SpawnComponents implements SetupComponent {

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
    String serializedLocation = player.getLocation().getWorld().getName() + "," + player.getLocation().getX() + "," + player.getLocation().getY() + ","
      + player.getLocation().getZ() + "," + player.getLocation().getYaw() + ",0.0";
    pane.addItem(new GuiItem(new ItemBuilder(Material.REDSTONE_BLOCK)
      .name(chatManager.colorRawMessage("&e&lSet Ending Location"))
      .lore(ChatColor.GRAY + "Click to set the ending location")
      .lore(ChatColor.GRAY + "on the place where you are standing.")
      .lore(ChatColor.DARK_GRAY + "(location where players will be")
      .lore(ChatColor.DARK_GRAY + "teleported after the game)")
      .lore("", setupInventory.getSetupUtilities().isOptionDoneBool("instances." + arena.getId() + ".Endlocation"))
      .build(), e -> {
      e.getWhoClicked().closeInventory();
      config.set("instances." + arena.getId() + ".Endlocation", serializedLocation);
      arena.setEndLocation(player.getLocation());
      player.sendMessage(chatManager.colorRawMessage("&e✔ Completed | &aEnding location for arena " + arena.getId() + " set at your location!"));
      ConfigUtils.saveConfig(plugin, config, "arenas");
    }), 0, 0);

    pane.addItem(new GuiItem(new ItemBuilder(Material.LAPIS_BLOCK)
      .name(chatManager.colorRawMessage("&e&lSet Lobby Location"))
      .lore(ChatColor.GRAY + "Click to set the lobby location")
      .lore(ChatColor.GRAY + "on the place where you are standing")
      .lore("", setupInventory.getSetupUtilities().isOptionDoneBool("instances." + arena.getId() + ".lobbylocation"))
      .build(), e -> {
      e.getWhoClicked().closeInventory();
      config.set("instances." + arena.getId() + ".lobbylocation", serializedLocation);
      arena.setLobbyLocation(player.getLocation());
      player.sendMessage(chatManager.colorRawMessage("&e✔ Completed | &aLobby location for arena " + arena.getId() + " set at your location!"));
      ConfigUtils.saveConfig(plugin, config, "arenas");
    }), 1, 0);

    pane.addItem(new GuiItem(new ItemBuilder(Material.EMERALD_BLOCK)
      .name(chatManager.colorRawMessage("&e&lAdd Starting Location"))
      .lore(ChatColor.GRAY + "Click to add the starting location")
      .lore(ChatColor.GRAY + "on the place where you are standing.")
      .lore(ChatColor.DARK_GRAY + "(locations where players will be")
      .lore(ChatColor.DARK_GRAY + "teleported when game starts)")
      .lore("", setupInventory.getSetupUtilities().isOptionDoneList("instances." + arena.getId() + ".playerspawnpoints", 4))
      .lore("", chatManager.colorRawMessage("&8Shift + Right Click to remove all spawns"))
      .build(), e -> {
      e.getWhoClicked().closeInventory();
      if(e.getClick() == ClickType.SHIFT_RIGHT) {
        config.set("instances." + arena.getId() + ".playerspawnpoints", new ArrayList<>());
        arena.setPlayerSpawnPoints(new ArrayList<>());
        player.sendMessage(chatManager.colorRawMessage("&eDone | &aPlayer spawn points deleted, you can add them again now!"));
        arena.setReady(false);
        ConfigUtils.saveConfig(plugin, config, "arenas");
        return;
      }
      List<String> startingSpawns = config.getStringList("instances." + arena.getId() + ".playerspawnpoints");
      startingSpawns.add(LocationSerializer.locationToString(player.getLocation()));
      config.set("instances." + arena.getId() + ".playerspawnpoints", startingSpawns);
      String startingProgress = startingSpawns.size() >= 4 ? "&e✔ Completed | " : "&c✘ Not completed | ";
      player.sendMessage(chatManager.colorRawMessage(startingProgress + "&aPlayer spawn added! &8(&7" + startingSpawns.size() + "/4&8)"));
      if(startingSpawns.size() == 4) {
        player.sendMessage(chatManager.colorRawMessage("&eInfo | &aYou can add more than 4 player spawns! Four is just a minimum!"));
      }
      List<Location> spawns = new ArrayList<>(arena.getPlayerSpawnPoints());
      spawns.add(player.getLocation());
      arena.setPlayerSpawnPoints(spawns);
      ConfigUtils.saveConfig(plugin, config, "arenas");
    }), 2, 0);
  }

}
