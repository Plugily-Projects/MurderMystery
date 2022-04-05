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

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import plugily.projects.murdermystery.Main;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author Plajer
 * <p>
 * Created at 28.04.2019
 */
public class HookManager {

  private final Map<HookFeature, Boolean> hooks = new EnumMap<>(HookFeature.class);
  private final Main plugin;
  public HookManager(Main plugin) {
    this.plugin = plugin;
    enableHooks();
  }

  private void enableHooks() {
    for(HookFeature feature : HookFeature.values()) {
      boolean hooked = true;
      for(Hook requiredHook : feature.getRequiredHooks()) {
        if(!Bukkit.getPluginManager().isPluginEnabled(requiredHook.pluginName)) {
          hooks.put(feature, false);
          plugin.getDebugger().debug("[HookManager] Feature {0} won't be enabled because {1} is not installed! Please install it in order to enable this feature in-game!",
            feature.name(), requiredHook.pluginName);
          hooked = false;
          break;
        }
      }
      if(hooked) {
        hooks.put(feature, true);
        plugin.getDebugger().debug("[HookManager] Feature {0} enabled!", feature.name());
      }
    }
  }

  public boolean isFeatureEnabled(HookFeature feature) {
    return hooks.getOrDefault(feature, false);
  }

  public enum HookFeature {
    CORPSES(Hook.CORPSE_REBORN);

    private final Hook[] requiredHooks;

    HookFeature(Hook... requiredHooks) {
      this.requiredHooks = requiredHooks;
    }

    public Hook[] getRequiredHooks() {
      return requiredHooks;
    }
  }

  public enum Hook {
    CORPSE_REBORN("CorpseReborn");

    private final String pluginName;

    Hook(String pluginName) {
      this.pluginName = pluginName;
    }

    public String getPluginName() {
      return pluginName;
    }
  }

}
