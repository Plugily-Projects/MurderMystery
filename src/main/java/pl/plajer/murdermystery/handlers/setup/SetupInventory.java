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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.arena.Arena;
import pl.plajerlair.core.utils.ConfigUtils;
import pl.plajerlair.core.utils.ItemBuilder;
import pl.plajerlair.core.utils.LocationUtils;
import pl.plajerlair.core.utils.XMaterial;

/**
 * @author Plajer
 * <p>
 * Created at 05.08.2018
 */
public class SetupInventory {

  private static Main plugin = JavaPlugin.getPlugin(Main.class);
  private Inventory inventory;

  public SetupInventory(Arena arena) {
    this.inventory = Bukkit.createInventory(null, 9 * 4, "MM Arena: " + arena.getID());

    addItem(new ItemBuilder(new ItemStack(Material.REDSTONE_BLOCK))
        .name(ChatColor.GOLD + "► Set" + ChatColor.RED + " ending " + ChatColor.GOLD + "location")
        .lore(ChatColor.GRAY + "Click to set the ending location")
        .lore(ChatColor.GRAY + "on the place where you are standing.")
        .lore(ChatColor.DARK_GRAY + "(location where players will be teleported")
        .lore(ChatColor.DARK_GRAY + "after the game)")
        .lore(isOptionDoneBool("instances." + arena.getID() + ".Endlocation"))
        .build());
    addItem(new ItemBuilder(new ItemStack(Material.LAPIS_BLOCK))
        .name(ChatColor.GOLD + "► Set" + ChatColor.WHITE + " lobby " + ChatColor.GOLD + "location")
        .lore(ChatColor.GRAY + "Click to set the lobby location")
        .lore(ChatColor.GRAY + "on the place where you are standing")
        .lore(isOptionDoneBool("instances." + arena.getID() + ".lobbylocation"))
        .build());

    addItem(new ItemBuilder(new ItemStack(Material.EMERALD_BLOCK))
        .name(ChatColor.GOLD + "► Add" + ChatColor.YELLOW + " starting " + ChatColor.GOLD + "location")
        .lore(ChatColor.GRAY + "Click to add the starting location")
        .lore(ChatColor.GRAY + "on the place where you are standing.")
        .lore(ChatColor.DARK_GRAY + "(locations where players will be teleported")
        .lore(ChatColor.DARK_GRAY + "when game starts)")
        .lore(isOptionDoneList("instances." + arena.getID() + ".playerspawnpoints"))
        .build());
    addItem(new ItemBuilder(new ItemStack(Material.COAL, ConfigUtils.getConfig(plugin, "arenas").getInt("instances." + arena.getID() + ".minimumplayers")))
        .name(ChatColor.GOLD + "► Set" + ChatColor.DARK_GREEN + " minimum players " + ChatColor.GOLD + "size")
        .lore(ChatColor.GRAY + "LEFT click to decrease")
        .lore(ChatColor.GRAY + "RIGHT click to increase")
        .lore(ChatColor.DARK_GRAY + "(how many players are needed")
        .lore(ChatColor.DARK_GRAY + "for game to start lobby countdown)")
        .lore(isOptionDone("instances." + arena.getID() + ".minimumplayers"))
        .build());
    addItem(new ItemBuilder(new ItemStack(Material.REDSTONE, ConfigUtils.getConfig(plugin, "arenas").getInt("instances." + arena.getID() + ".maximumplayers")))
        .name(ChatColor.GOLD + "► Set" + ChatColor.GREEN + " maximum players " + ChatColor.GOLD + "size")
        .lore(ChatColor.GRAY + "LEFT click to decrease")
        .lore(ChatColor.GRAY + "RIGHT click to increase")
        .lore(ChatColor.DARK_GRAY + "(how many players arena can hold)")
        .lore(isOptionDone("instances." + arena.getID() + ".maximumplayers"))
        .build());
    if (!plugin.isBungeeActivated()) {
      addItem(new ItemBuilder(new ItemStack(Material.SIGN))
          .name(ChatColor.GOLD + "► Add game" + ChatColor.AQUA + " sign")
          .lore(ChatColor.GRAY + "Target a sign and click this.")
          .lore(ChatColor.DARK_GRAY + "(this will set target sign as game sign)")
          .build());
    }
    addItem(new ItemBuilder(new ItemStack(Material.NAME_TAG))
        .name(ChatColor.GOLD + "► Set" + ChatColor.RED + " map name " + ChatColor.GOLD + "(currently: " + arena.getMapName() + ")")
        .lore(ChatColor.GRAY + "Replace this name tag with named name tag.")
        .lore(ChatColor.GRAY + "It will be set as arena name.")
        .lore(ChatColor.RED + "" + ChatColor.BOLD + "Drop name tag here don't move")
        .lore(ChatColor.RED + "" + ChatColor.BOLD + "it and replace with new!!!")
        .build());
    addItem(new ItemBuilder(new ItemStack(Material.GOLD_INGOT, 1))
        .name(ChatColor.GOLD + "► Add" + ChatColor.YELLOW + " gold " + ChatColor.GOLD + "spawn")
        .lore(ChatColor.GRAY + "Add new gold spawn")
        .lore(ChatColor.GRAY + "on the place you're standing at.")
        .lore(isOptionDoneList("instances." + arena.getID() + ".goldspawnpoints"))
        .build());
    addItem(new ItemBuilder(new ItemStack(Material.FIREWORK))
        .name(ChatColor.GOLD + "► " + ChatColor.GREEN + "Register arena")
        .lore(ChatColor.GRAY + "Click this when you're done with configuration.")
        .lore(ChatColor.GRAY + "It will validate and register arena.")
        .build());
    inventory.setItem(17, new ItemBuilder(XMaterial.FILLED_MAP.parseItem())
        .name(ChatColor.GOLD + "► View setup video")
        .lore(ChatColor.GRAY + "Having problems with setup or wanna")
        .lore(ChatColor.GRAY + "know some useful tips? Click to get video link!")
        .build());

    //special blocks
    inventory.setItem(27, new ItemBuilder(XMaterial.PAPER.parseItem())
        .name(ChatColor.GOLD + "Special blocks section")
        .lore(ChatColor.GRAY + "Items on the right will allow")
        .lore(ChatColor.GRAY + "you to add special game blocks!")
        .build());
    inventory.setItem(28, new ItemBuilder(XMaterial.ENDER_CHEST.parseItem())
        .name(ChatColor.GOLD + "► Add mystery cauldron")
        .lore(ChatColor.GRAY + "Target a cauldron and add it to the game")
        .lore(ChatColor.GRAY + "it will cost 1 gold per potion!")
        .lore(ChatColor.GRAY + "Configure cauldron potions in specialblocks.yml file!")
        .build());
    inventory.setItem(29, new ItemBuilder(XMaterial.END_PORTAL.parseItem())
        .name(ChatColor.GOLD + "► Add confessional")
        .lore(ChatColor.GRAY + "Target end portal and add praise to the developer")
        .lore(ChatColor.GRAY + "confessional, gift for the developer costs 1 gold!")
        .lore(ChatColor.GOLD + "Add some levers in radius of 3 blocks near the end portal")
        .lore(ChatColor.GOLD + "to allow users to pray there!")
        .lore(ChatColor.RED + "You can either get gifts or curses from prayer!")
        .lore(ChatColor.RED + "If you don't gift anything to end portal you'll get only curses!")
        .build());
  }

  private static String isOptionDone(String path) {
    if (ConfigUtils.getConfig(plugin, "arenas").isSet(path)) {
      return ChatColor.GOLD + "" + ChatColor.BOLD + "Done: " + ChatColor.GREEN + "Yes " + ChatColor.GRAY + "(value: " + ConfigUtils.getConfig(plugin, "arenas").getString(path) + ")";
    }
    return ChatColor.GOLD + "" + ChatColor.BOLD + "Done: " + ChatColor.RED + "No";
  }

  private String isOptionDoneList(String path) {
    if (ConfigUtils.getConfig(plugin, "arenas").isSet(path)) {
      if (path.contains(".doors")) {
        return ChatColor.GOLD + "" + ChatColor.BOLD + "Done: " + ChatColor.GREEN + "Yes " + ChatColor.GRAY + "(value: " + ConfigUtils.getConfig(plugin, "arenas")
            .getStringList(path).size() + ")";
      }
      return ChatColor.GOLD + "" + ChatColor.BOLD + "Done: " + ChatColor.GREEN + "Yes " + ChatColor.GRAY + "(value: " + ConfigUtils.getConfig(plugin, "arenas")
          .getStringList(path).size() + ")";
    }
    return ChatColor.GOLD + "" + ChatColor.BOLD + "Done: " + ChatColor.RED + "No";
  }

  private String isOptionDoneBool(String path) {
    if (ConfigUtils.getConfig(plugin, "arenas").isSet(path)) {
      if (Bukkit.getServer().getWorlds().get(0).getSpawnLocation().equals(LocationUtils.getLocation(ConfigUtils.getConfig(plugin, "arenas").getString(path)))) {
        return ChatColor.GOLD + "" + ChatColor.BOLD + "Done: " + ChatColor.RED + "No";
      }
      return ChatColor.GOLD + "" + ChatColor.BOLD + "Done: " + ChatColor.GREEN + "Yes";
    }
    return ChatColor.GOLD + "" + ChatColor.BOLD + "Done: " + ChatColor.RED + "No";
  }

  public void addItem(ItemStack itemStack) {
    inventory.addItem(itemStack);
  }

  public Inventory getInventory() {
    return inventory;
  }

  public void openInventory(Player player) {
    player.openInventory(inventory);
  }

}
