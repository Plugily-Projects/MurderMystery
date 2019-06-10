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
import org.bukkit.configuration.file.FileConfiguration;

import pl.plajer.murdermystery.arena.Arena;
import pl.plajer.murdermystery.handlers.ChatManager;
import pl.plajerlair.commonsbox.minecraft.serialization.LocationSerializer;

/**
 * @author Plajer
 * <p>
 * Created at 25.05.2019
 */
public class SetupUtilities {

  private FileConfiguration config;
  private Arena arena;

  SetupUtilities(FileConfiguration config, Arena arena) {
    this.config = config;
    this.arena = arena;
  }

  public String isOptionDone(String path) {
    if (config.isSet(path)) {
      return ChatManager.colorRawMessage("&a&l✔ Completed &7(value: &8" + config.getString(path) + "&7)");
    }
    return ChatManager.colorRawMessage("&c&l✘ Not Completed");
  }

  public String isOptionDoneList(String path, int minimum) {
    if (config.isSet(path)) {
      if (config.getStringList(path).size() < minimum) {
        return ChatManager.colorRawMessage("&c&l✘ Not Completed | &cPlease add more spawns");
      }
      return ChatManager.colorRawMessage("&a&l✔ Completed &7(value: &8" + config.getStringList(path).size() + "&7)");
    }
    return ChatManager.colorRawMessage("&c&l✘ Not Completed");
  }

  public String isOptionDoneBool(String path) {
    if (config.isSet(path)) {
      if (Bukkit.getServer().getWorlds().get(0).getSpawnLocation().equals(LocationSerializer.getLocation(config.getString(path)))) {
        return ChatManager.colorRawMessage("&c&l✘ Not Completed");
      }
      return ChatManager.colorRawMessage("&a&l✔ Completed");
    }
    return ChatManager.colorRawMessage("&c&l✘ Not Completed");
  }

  public int getMinimumValueHigherThanZero(String path) {
    int amount = config.getInt("instances." + arena.getId() + "." + path);
    if (amount == 0) {
      amount = 1;
    }
    return amount;
  }

}
