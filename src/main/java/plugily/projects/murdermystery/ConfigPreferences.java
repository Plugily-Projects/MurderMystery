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

package plugily.projects.murdermystery;

import org.bukkit.inventory.ItemStack;
import pl.plajerlair.commonsbox.minecraft.compat.XMaterial;
import plugily.projects.murdermystery.utils.Debugger;
import plugily.projects.murdermystery.utils.MessageUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Plajer
 * <p>
 * Created at 22.12.2018
 */
public class ConfigPreferences {

  private final Main plugin;
  private ItemStack murdererSword;
  private final Map<Option, Boolean> options = new HashMap<>();

  public ConfigPreferences(Main plugin) {
    this.plugin = plugin;
    loadOptions();
    loadMurdererSword();
  }

  private void loadMurdererSword() {
    try {
      murdererSword = XMaterial.matchXMaterial(plugin.getConfig().getString("Murderer-Sword-Material", "IRON_SWORD").toUpperCase()).get().parseItem();
    } catch(Exception ex) {
      MessageUtils.errorOccurred();
      Debugger.sendConsoleMsg("Can not found Material " + plugin.getConfig().getString("Murderer-Sword-Material"));
      //Set the murdererSword to avoid errors
      murdererSword = XMaterial.IRON_SWORD.parseItem();
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
    for(Option option : Option.values()) {
      options.put(option, plugin.getConfig().getBoolean(option.getPath(), option.getDefault()));
    }
  }

  public ItemStack getMurdererSword() {
    return murdererSword;
  }

  public enum Option {
    BOSSBAR_ENABLED("Bossbar-Enabled", true), BUNGEE_ENABLED("BungeeActivated", false), CHAT_FORMAT_ENABLED("ChatFormat-Enabled", true),
    DATABASE_ENABLED("DatabaseActivated", false), INVENTORY_MANAGER_ENABLED("InventoryManager", true), NAMETAGS_HIDDEN("Nametags-Hidden", true),
    DISABLE_FALL_DAMAGE("Disable-Fall-Damage", false), ENABLE_SHORT_COMMANDS("Enable-Short-Commands", false), ENABLE_KILL_DETECTIVE_IF_INNOCENT_KILLED("Enable-Kill-Detective-If-Innocent-Killed", true),
    MURDERER_SPEED_ENABLED("Speed-Effect-Murderer.Enabled", true), SPAWN_GOLD_EVERY_SPAWNER_MODE("Change-Gold-Spawner-Mode-To-All", false), DISABLE_GOLD_LIMITER("Disable-Gold-Limiter", false),
    DISABLE_SEPARATE_CHAT("Disable-Separate-Chat", false), DISABLE_PARTIES("Disable-Parties", true), INNOCENT_LOCATOR("Enable-Innocent-Locator", true),
    DISABLE_DEATH_MESSAGE("Hide-Death", false);

    private final String path;
    private final boolean def;

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
}
