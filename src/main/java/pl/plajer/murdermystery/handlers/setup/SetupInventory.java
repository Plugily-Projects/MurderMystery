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

package pl.plajer.murdermystery.handlers.setup;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import pl.plajer.murdermystery.ConfigPreferences;
import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.arena.Arena;
import pl.plajerlair.commonsbox.minecraft.compat.XMaterial;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;
import pl.plajerlair.commonsbox.minecraft.item.ItemBuilder;
import pl.plajerlair.commonsbox.minecraft.serialization.LocationSerializer;

/**
 * @author Plajer
 * <p>
 * Created at 05.08.2018
 */
public class SetupInventory {

  public static final String VIDEO_LINK = "https://tutorial.plajer.xyz";
  private static Main plugin = JavaPlugin.getPlugin(Main.class);
  private Inventory inventory;

  public SetupInventory(Arena arena) {
    this.inventory = Bukkit.createInventory(null, 9 * 4, "MM Arena: " + arena.getId());

    inventory.setItem(ClickPosition.SET_ENDING.getPosition(), new ItemBuilder(Material.REDSTONE_BLOCK)
        .name(ChatColor.GOLD + "► Set" + ChatColor.RED + " ending " + ChatColor.GOLD + "location")
        .lore(ChatColor.GRAY + "Click to set the ending location")
        .lore(ChatColor.GRAY + "on the place where you are standing.")
        .lore(ChatColor.DARK_GRAY + "(location where players will be teleported")
        .lore(ChatColor.DARK_GRAY + "after the game)")
        .lore(isOptionDoneBool("instances." + arena.getId() + ".Endlocation"))
        .build());
    inventory.setItem(ClickPosition.SET_LOBBY.getPosition(), new ItemBuilder(Material.LAPIS_BLOCK)
        .name(ChatColor.GOLD + "► Set" + ChatColor.WHITE + " lobby " + ChatColor.GOLD + "location")
        .lore(ChatColor.GRAY + "Click to set the lobby location")
        .lore(ChatColor.GRAY + "on the place where you are standing")
        .lore(isOptionDoneBool("instances." + arena.getId() + ".lobbylocation"))
        .build());
    inventory.setItem(ClickPosition.ADD_STARTING.getPosition(), new ItemBuilder(Material.EMERALD_BLOCK)
        .name(ChatColor.GOLD + "► Add" + ChatColor.YELLOW + " starting " + ChatColor.GOLD + "location")
        .lore(ChatColor.GRAY + "Click to add the starting location")
        .lore(ChatColor.GRAY + "on the place where you are standing.")
        .lore(ChatColor.DARK_GRAY + "(locations where players will be teleported")
        .lore(ChatColor.DARK_GRAY + "when game starts)")
        .lore(isOptionDoneList("instances." + arena.getId() + ".playerspawnpoints", 4))
        .build());

    FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
    int min = config.getInt("instances." + arena.getId() + ".minimumplayers");
    if (min == 0) {
      min = 1;
    }
    inventory.setItem(ClickPosition.SET_MINIMUM_PLAYERS.getPosition(), new ItemBuilder(Material.COAL).amount(min)
        .name(ChatColor.GOLD + "► Set" + ChatColor.DARK_GREEN + " minimum players " + ChatColor.GOLD + "size")
        .lore(ChatColor.GRAY + "LEFT click to decrease")
        .lore(ChatColor.GRAY + "RIGHT click to increase")
        .lore(ChatColor.DARK_GRAY + "(how many players are needed")
        .lore(ChatColor.DARK_GRAY + "for game to start lobby countdown)")
        .lore(isOptionDone("instances." + arena.getId() + ".minimumplayers"))
        .build());
    inventory.setItem(ClickPosition.SET_MAXIMUM_PLAYERS.getPosition(), new ItemBuilder(Material.REDSTONE)
        .amount(config.getInt("instances." + arena.getId() + ".maximumplayers"))
        .name(ChatColor.GOLD + "► Set" + ChatColor.GREEN + " maximum players " + ChatColor.GOLD + "size")
        .lore(ChatColor.GRAY + "LEFT click to decrease")
        .lore(ChatColor.GRAY + "RIGHT click to increase")
        .lore(ChatColor.DARK_GRAY + "(how many players arena can hold)")
        .lore(isOptionDone("instances." + arena.getId() + ".maximumplayers"))
        .build());

    if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
      inventory.setItem(ClickPosition.ADD_SIGN.getPosition(), new ItemBuilder(Material.SIGN)
          .name(ChatColor.GOLD + "► Add game" + ChatColor.AQUA + " sign")
          .lore(ChatColor.GRAY + "Target a sign and click this.")
          .lore(ChatColor.DARK_GRAY + "(this will set target sign as game sign)")
          .build());
    }
    inventory.setItem(ClickPosition.SET_MAP_NAME.getPosition(), new ItemBuilder(Material.NAME_TAG)
        .name(ChatColor.GOLD + "► Set" + ChatColor.RED + " map name " + ChatColor.GOLD + "(currently: " + arena.getMapName() + ")")
        .lore(ChatColor.GRAY + "Replace this name tag with named name tag.")
        .lore(ChatColor.GRAY + "It will be set as arena name.")
        .lore(ChatColor.RED + "" + ChatColor.BOLD + "Drop name tag here don't move")
        .lore(ChatColor.RED + "" + ChatColor.BOLD + "it and replace with new!!!")
        .build());
    inventory.setItem(ClickPosition.ADD_GOLD_SPAWN.getPosition(), new ItemBuilder(Material.GOLD_INGOT)
        .name(ChatColor.GOLD + "► Add" + ChatColor.YELLOW + " gold " + ChatColor.GOLD + "spawn")
        .lore(ChatColor.GRAY + "Add new gold spawn")
        .lore(ChatColor.GRAY + "on the place you're standing at.")
        .lore(isOptionDoneList("instances." + arena.getId() + ".goldspawnpoints", 4))
        .build());
    inventory.setItem(ClickPosition.REGISTER_ARENA.getPosition(), new ItemBuilder(XMaterial.FIREWORK_ROCKET.parseItem())
        .name(ChatColor.GOLD + "► " + ChatColor.GREEN + "Register arena")
        .lore(ChatColor.GRAY + "Click this when you're done with configuration.")
        .lore(ChatColor.GRAY + "It will validate and register arena.")
        .build());
    inventory.setItem(ClickPosition.VIEW_SETUP_VIDEO.getPosition(), new ItemBuilder(XMaterial.FILLED_MAP.parseItem())
        .name(ChatColor.GOLD + "► View setup video")
        .lore(ChatColor.GRAY + "Having problems with setup or wanna")
        .lore(ChatColor.GRAY + "know some useful tips? Click to get video link!")
        .build());

    //special blocks
    inventory.setItem(ClickPosition.SPECIAL_BLOCKS.getPosition(), new ItemBuilder(XMaterial.PAPER.parseItem())
        .name(ChatColor.GOLD + "Special blocks section")
        .lore(ChatColor.GRAY + "Items on the right will allow")
        .lore(ChatColor.GRAY + "you to add special game blocks!")
        .build());
    inventory.setItem(ClickPosition.ADD_MYSTERY_CAULDRON.getPosition(), new ItemBuilder(XMaterial.ENDER_CHEST.parseItem())
        .name(ChatColor.GOLD + "► Add mystery cauldron")
        .lore(ChatColor.GRAY + "Target a cauldron and add it to the game")
        .lore(ChatColor.GRAY + "it will cost 1 gold per potion!")
        .lore(ChatColor.GRAY + "Configure cauldron potions in specialblocks.yml file!")
        .build());
    inventory.setItem(ClickPosition.ADD_CONFESSIONAL.getPosition(), new ItemBuilder(XMaterial.ENCHANTING_TABLE.parseItem())
        .name(ChatColor.GOLD + "► Add confessional")
        .lore(ChatColor.GRAY + "Target enchanting table and add praise to the developer")
        .lore(ChatColor.GRAY + "confessional, gift for the developer costs 1 gold!")
        .lore(ChatColor.GOLD + "Add some levers in radius of 3 blocks near the enchant table")
        .lore(ChatColor.GOLD + "to allow users to pray there!")
        .lore(ChatColor.RED + "You can either get gifts or curses from prayer!")
        .build());
    /*inventory.setItem(ClickPosition.SPECIAL_BLOCK_REMOVER.getPosition(), new ItemBuilder(Material.BLAZE_ROD)
        .name(ChatColor.GOLD + "► Special blocks remover wand")
        .lore(ChatColor.GRAY + "Use this wand if you want to remove special blocks")
        .lore(ChatColor.GRAY + "of any arena!")
        .build());*/
  }

  private static String isOptionDone(String path) {
    FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
    if (config.isSet(path)) {
      return ChatColor.GOLD + "" + ChatColor.BOLD + "Done: " + ChatColor.GREEN + "Yes " + ChatColor.GRAY + "(value: " + config.getString(path) + ")";
    }
    return ChatColor.GOLD + "" + ChatColor.BOLD + "Done: " + ChatColor.RED + "No";
  }

  private String isOptionDoneList(String path, int minimum) {
    FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
    if (config.isSet(path)) {
      if (config.getStringList(path).size() < minimum) {
        return ChatColor.GOLD + "" + ChatColor.BOLD + "Done: " + ChatColor.RED + "No - add more spawns";
      }
      return ChatColor.GOLD + "" + ChatColor.BOLD + "Done: " + ChatColor.GREEN + "Yes " + ChatColor.GRAY + "(value: " + config.getStringList(path).size() + ")";
    }
    return ChatColor.GOLD + "" + ChatColor.BOLD + "Done: " + ChatColor.RED + "No";
  }

  private String isOptionDoneBool(String path) {
    FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
    if (config.isSet(path)) {
      if (Bukkit.getServer().getWorlds().get(0).getSpawnLocation().equals(LocationSerializer.getLocation(config.getString(path)))) {
        return ChatColor.GOLD + "" + ChatColor.BOLD + "Done: " + ChatColor.RED + "No";
      }
      return ChatColor.GOLD + "" + ChatColor.BOLD + "Done: " + ChatColor.GREEN + "Yes";
    }
    return ChatColor.GOLD + "" + ChatColor.BOLD + "Done: " + ChatColor.RED + "No";
  }

  public Inventory getInventory() {
    return inventory;
  }

  public void openInventory(Player player) {
    player.openInventory(inventory);
  }

  public enum ClickPosition {
    SET_ENDING(0), SET_LOBBY(1), ADD_STARTING(2), SET_MINIMUM_PLAYERS(3), SET_MAXIMUM_PLAYERS(4), ADD_SIGN(5), SET_MAP_NAME(6),
    ADD_GOLD_SPAWN(7), REGISTER_ARENA(8), VIEW_SETUP_VIDEO(17), SPECIAL_BLOCKS(27), ADD_MYSTERY_CAULDRON(28), ADD_CONFESSIONAL(29),
    SPECIAL_BLOCK_REMOVER(35);

    private int position;

    ClickPosition(int position) {
      this.position = position;
    }

    public static ClickPosition getByPosition(int pos) {
      for (ClickPosition position : ClickPosition.values()) {
        if (position.getPosition() == pos) {
          return position;
        }
      }
      //couldn't find position, return tutorial
      return ClickPosition.VIEW_SETUP_VIDEO;
    }

    /**
     * @return gets position of item in inventory
     */
    public int getPosition() {
      return position;
    }
  }

}
