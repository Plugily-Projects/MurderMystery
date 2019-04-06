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

package pl.plajer.murdermystery.events.spectator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import pl.plajerlair.commonsbox.minecraft.compat.XMaterial;
import pl.plajerlair.commonsbox.minecraft.item.ItemBuilder;

/**
 * @author Plajer
 * <p>
 * Created at 06.04.2019
 */
@Deprecated //api subject to merge
public class SpectatorSettingsMenu implements Listener {

  private String inventoryName;
  private String speedOptionName;
  private Inventory inv;

  public SpectatorSettingsMenu(JavaPlugin plugin, String inventoryName, String speedOptionName) {
    this.inventoryName = inventoryName;
    this.speedOptionName = speedOptionName;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
    initInventory();
  }

  public void openSpectatorSettingsMenu(Player player) {
    player.openInventory(this.inv);
  }

  @EventHandler
  public void onSpectatorMenuClick(InventoryClickEvent e) {
    if (e.getInventory() == null || !e.getInventory().getName().equals(color(inventoryName))) {
      return;
    }
    if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) {
      return;
    }
    Player p = (Player) e.getWhoClicked();
    p.closeInventory();
    if (e.getCurrentItem().getType() == Material.LEATHER_BOOTS) {
      p.removePotionEffect(PotionEffectType.SPEED);
      p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
    } else if (e.getCurrentItem().getType() == Material.CHAINMAIL_BOOTS) {
      p.removePotionEffect(PotionEffectType.SPEED);
      p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
    } else if (e.getCurrentItem().getType() == Material.IRON_BOOTS) {
      p.removePotionEffect(PotionEffectType.SPEED);
      p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2, false, false));
    } else if (e.getCurrentItem().getType() == XMaterial.GOLDEN_BOOTS.parseMaterial()) {
      p.removePotionEffect(PotionEffectType.SPEED);
      p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 3, false, false));
    } else if (e.getCurrentItem().getType() == Material.DIAMOND_BOOTS) {
      p.removePotionEffect(PotionEffectType.SPEED);
      p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 4, false, false));
    }
  }

  private void initInventory() {
    Inventory inv = Bukkit.createInventory(null, 9 * 4, inventoryName);
    inv.setItem(11, new ItemBuilder(Material.LEATHER_BOOTS)
        .name(color(speedOptionName + " I")).build());
    inv.setItem(12, new ItemBuilder(Material.CHAINMAIL_BOOTS)
        .name(color(speedOptionName + " II")).build());
    inv.setItem(13, new ItemBuilder(Material.IRON_BOOTS)
        .name(color(speedOptionName + " III")).build());
    inv.setItem(14, new ItemBuilder(XMaterial.GOLDEN_BOOTS.parseItem())
        .name(color(speedOptionName + " IV")).build());
    inv.setItem(15, new ItemBuilder(Material.DIAMOND_BOOTS)
        .name(color(speedOptionName + " V")).build());
    this.inv = inv;
  }

  private String color(String message) {
    return ChatColor.translateAlternateColorCodes('&', message);
  }

}
