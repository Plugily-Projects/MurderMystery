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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.utils.MessageUtils;
import pl.plajerlair.core.utils.ConfigUtils;

/**
 * @author Plajer
 * <p>
 * Created at 03.08.2018
 */
public class SpecialItem {

  private Material material;
  private Byte data = null;
  private String[] lore;
  private String displayName;
  private int slot;
  private String name;

  public SpecialItem(String name) {
    this.name = name;

  }

  public static void loadAll() {
    new SpecialItem("Leave").load(ChatColor.RED + "Leave", new String[]{
            ChatColor.GRAY + "Click to teleport to hub"
    }, Material.BED, 8);
  }

  //todo data id!
  public void load(String displayName, String[] lore, Material material, int slot) {
    FileConfiguration config = ConfigUtils.getConfig(JavaPlugin.getPlugin(Main.class), "lobbyitems");

    if (!config.contains(name)) {
      config.set(name + ".data", 0);
      config.set(name + ".displayname", displayName);
      config.set(name + ".lore", Arrays.asList(lore));
      config.set(name + ".material", material.getId());
      config.set(name + ".slot", slot);
    }
    try {
      config.save(ConfigUtils.getFile(JavaPlugin.getPlugin(Main.class), "lobbyitems"));
    } catch (IOException e) {
      e.printStackTrace();
      MessageUtils.errorOccured();
      Bukkit.getConsoleSender().sendMessage("Cannot save file lobbyitems.yml!");
      Bukkit.getConsoleSender().sendMessage("Create blank file lobbyitems.yml or restart the server!");
    }
    SpecialItem particleItem = new SpecialItem(name);
    particleItem.setData(config.getInt(name + ".data"));
    particleItem.setMaterial(Material.getMaterial(config.getInt(name + ".material")));
    particleItem.setLore(config.getStringList(name + ".lore"));
    particleItem.setDisplayName(config.getString(name + ".displayname"));
    particleItem.setSlot(config.getInt(name + ".slot"));
    SpecialItemManager.addEntityItem(name, particleItem);

  }

  public Material getMaterial() {
    return material;
  }

  public void setMaterial(Material material) {
    this.material = material;
  }

  public byte getData() {
    return data;
  }

  public void setData(Integer data) {
    this.data = data.byteValue();
  }

  public void setLore(List<String> lore) {
    this.lore = lore.toArray(new String[0]);
  }

  public String getDisplayName() {
    return ChatColor.translateAlternateColorCodes('&', displayName);
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public int getSlot() {
    return slot;
  }

  public void setSlot(int slot) {
    this.slot = slot;
  }

  public ItemStack getItemStack() {
    ItemStack itemStack;
    if (data != null) {
      itemStack = new ItemStack(getMaterial(), 1, getData());
    } else {
      itemStack = new ItemStack(getMaterial());

    }
    ItemMeta im = itemStack.getItemMeta();
    im.setDisplayName(ChatColor.translateAlternateColorCodes('&', this.getDisplayName()));
    im.setLore(Arrays.asList(lore));
    itemStack.setItemMeta(im);
    return itemStack;
  }


}
