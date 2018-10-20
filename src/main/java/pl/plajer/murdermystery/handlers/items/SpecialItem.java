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

package pl.plajer.murdermystery.handlers.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.handlers.ChatManager;
import pl.plajerlair.core.utils.ConfigUtils;
import pl.plajerlair.core.utils.XMaterial;

/**
 * @author Plajer
 * <p>
 * Created at 03.08.2018
 */
public class SpecialItem {

  private ItemStack itemStack;
  private int slot;
  private String name;

  public SpecialItem(String name) {
    this.name = name;
  }

  public static void loadAll() {
    new SpecialItem("Leave").load(ChatColor.RED + "Leave", new String[] {
        ChatColor.GRAY + "Click to teleport to hub"
    }, XMaterial.WHITE_BED.parseMaterial(), 8);
  }

  public void load(String displayName, String[] lore, Material material, int slot) {
    FileConfiguration config = ConfigUtils.getConfig(JavaPlugin.getPlugin(Main.class), "lobbyitems");

    if (!config.contains(name)) {
      config.set(name + ".data", 0);
      config.set(name + ".displayname", displayName);
      config.set(name + ".lore", Arrays.asList(lore));
      config.set(name + ".material-name", material.toString());
      config.set(name + ".slot", slot);
    } else {
      if (!config.isSet(name + ".material-name")) {
        config.set(name + ".material-name", material.toString());
        Main.debug(Main.LogLevel.INFO, "Found outdated item in lobbyitems.yml! We've converted it to the newest version!");
      }
    }
    ConfigUtils.saveConfig(JavaPlugin.getPlugin(Main.class), config, "lobbyitems");
    SpecialItem item = new SpecialItem(name);
    ItemStack stack = XMaterial.fromString(config.getString(name + ".material-name").toUpperCase()).parseItem();
    ItemMeta meta = stack.getItemMeta();
    meta.setDisplayName(ChatManager.colorRawMessage(config.getString(name + ".displayname")));

    List<String> colorizedLore = new ArrayList<>();
    for (String str : config.getStringList(name + ".lore")) {
      colorizedLore.add(ChatManager.colorRawMessage(str));
    }
    meta.setLore(colorizedLore);
    stack.setItemMeta(meta);

    item.itemStack = stack;
    item.slot = config.getInt(name + ".slot");
    SpecialItemManager.addItem(name, item);

  }

  public int getSlot() {
    return slot;
  }

  public ItemStack getItemStack() {
    return itemStack;
  }
}
