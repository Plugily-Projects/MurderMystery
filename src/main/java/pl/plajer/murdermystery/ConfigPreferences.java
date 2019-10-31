/*
 * MurderMystery - Find the murderer, kill him and survive!
 * Copyright (C) 2019  Plajer's Lair - maintained by Tigerpanzer_02, Plajer and contributors
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

package pl.plajer.murdermystery;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import pl.plajer.murdermystery.utils.MessageUtils;
import pl.plajerlair.commonsbox.minecraft.compat.XMaterial;

/**
 * @author Plajer
 * <p>
 * Created at 22.12.2018
 */
public class ConfigPreferences {

  private Main plugin;
  private ItemStack murdererSword;
  private Map<Option, Boolean> options = new HashMap<>();

  public ConfigPreferences(Main plugin) {
    this.plugin = plugin;
    loadOptions();
    loadMurdererSword();
  }

  private void loadMurdererSword() {
    try {
      murdererSword = XMaterial.fromString(plugin.getConfig().getString("Murderer-Sword-Material", "IRON_SWORD").toUpperCase()).parseItem();
    } catch (Exception ex) {
      MessageUtils.errorOccurred();
      Bukkit.getConsoleSender().sendMessage("Can not found Material " + plugin.getConfig().getString("Murderer-Sword-Material", "IRON_SWORD"));
    }
  }

  /**
   * Returns whether option value is true or false
   *
   * @param option option to get value from
   * @return true or false based on user configuration
   */
  public boolean getOption(Option option) {
    return options.get(option);
  }

  private void loadOptions() {
    for (Option option : Option.values()) {
      options.put(option, plugin.getConfig().getBoolean(option.getPath(), option.getDefault()));
    }
  }

  public enum Option {
    BOSSBAR_ENABLED("Bossbar-Enabled", true), BUNGEE_ENABLED("BungeeActivated", false), CHAT_FORMAT_ENABLED("ChatFormat-Enabled", true),
    DATABASE_ENABLED("DatabaseActivated", false), INVENTORY_MANAGER_ENABLED("InventoryManager", true), NAMETAGS_HIDDEN("Nametags-Hidden", true), DISABLE_FALL_DAMAGE("Disable-Fall-Damage", false);

    private String path;
    private boolean def;

    Option(String path, boolean def) {
      this.path = path;
      this.def = def;
    }

    public String getPath() {
      return path;
    }

    /**
     * @return default value of option if absent in config
     */
    public boolean getDefault() {
      return def;
    }
  }

  public ItemStack getMurdererSword() {
    return murdererSword;
  }
}
