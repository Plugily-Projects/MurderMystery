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

package pl.plajer.murdermystery;

import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;

import pl.plajer.murdermystery.utils.Debugger;

/**
 * @author Plajer
 * <p>
 * Created at 28.04.2019
 */
public class HookManager {

  private Map<HookFeature, Boolean> hooks = new EnumMap<>(HookFeature.class);

  public HookManager() {
    enableHooks();
  }

  private void enableHooks() {
    for (HookFeature feature : HookFeature.values()) {
      boolean hooked = true;
      for (Hook requiredHook : feature.getRequiredHooks()) {
        if (!Bukkit.getPluginManager().isPluginEnabled(requiredHook.getPluginName())) {
          hooks.put(feature, false);
          Debugger.debug(Level.INFO, "[HookManager] Feature {0} won't be enabled because {1} is not installed! Please install it in order to enable this feature in-game!",
            feature.name(), requiredHook.getPluginName());
          hooked = false;
          break;
        }
      }
      if (hooked) {
        hooks.put(feature, true);
        Debugger.debug(Level.INFO, "[HookManager] Feature {0} enabled!", feature.name());
      }
    }
  }

  public boolean isFeatureEnabled(HookFeature feature) {
    return hooks.get(feature);
  }

  public enum HookFeature {
    //todo hidden name tags hook
    CORPSES(Hook.CORPSE_REBORN, Hook.HOLOGRAPHIC_DISPLAYS);

    private Hook[] requiredHooks;

    HookFeature(Hook... requiredHooks) {
      this.requiredHooks = requiredHooks;
    }

    public Hook[] getRequiredHooks() {
      return requiredHooks;
    }
  }

  public enum Hook {
    CORPSE_REBORN("CorpseReborn"), HOLOGRAPHIC_DISPLAYS("HolographicDisplays");

    private String pluginName;

    Hook(String pluginName) {
      this.pluginName = pluginName;
    }

    public String getPluginName() {
      return pluginName;
    }
  }

}
