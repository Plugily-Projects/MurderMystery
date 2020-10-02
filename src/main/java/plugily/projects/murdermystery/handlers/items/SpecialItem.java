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

package plugily.projects.murdermystery.handlers.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import pl.plajerlair.commonsbox.minecraft.compat.XMaterial;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.handlers.ChatManager;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Plajer
 * <p>
 * Created at 03.08.2018
 */
public class SpecialItem {

  private ItemStack itemStack;
  private int slot;
  private final String name;
  private final Main plugin = JavaPlugin.getPlugin(Main.class);

  public SpecialItem(String name) {
    this.name = name;
  }

  public static void loadAll() {
    new SpecialItem("Leave").load(ChatColor.RED + "Leave", new String[]{
      ChatColor.GRAY + "Click to teleport to hub"
    }, XMaterial.WHITE_BED.parseMaterial(), 8);
  }

  public void load(String displayName, String[] lore, Material material, int slot) {
    FileConfiguration config = ConfigUtils.getConfig(plugin, "lobbyitems");
    ChatManager chatManager = plugin.getChatManager();

    if (!config.contains(name)) {
      config.set(name + ".data", 0);
      config.set(name + ".displayname", displayName);
      config.set(name + ".lore", Arrays.asList(lore));
      config.set(name + ".material-name", material.toString());
      config.set(name + ".slot", slot);
    }
    ConfigUtils.saveConfig(plugin, config, "lobbyitems");
    ItemStack stack = XMaterial.matchXMaterial(config.getString(name + ".material-name", "STONE").toUpperCase())
        .orElse(XMaterial.STONE).parseItem();
    ItemMeta meta = stack.getItemMeta();
    meta.setDisplayName(chatManager.colorRawMessage(config.getString(name + ".displayname")));

    List<String> colorizedLore = config.getStringList(name + ".lore").stream().map(chatManager::colorRawMessage)
      .collect(Collectors.toList());
    meta.setLore(colorizedLore);
    stack.setItemMeta(meta);

    SpecialItem item = new SpecialItem(name);
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
