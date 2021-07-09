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

package plugily.projects.murdermystery.handlers.setup;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import plugily.projects.commonsbox.minecraft.serialization.LocationSerializer;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.handlers.ChatManager;

/**
 * @author Plajer
 * <p>
 * Created at 25.05.2019
 */
public class SetupUtilities {

  private final FileConfiguration config;
  private final Arena arena;
  private final ChatManager chatManager;

  SetupUtilities(FileConfiguration config, Arena arena, ChatManager chatManager) {
    this.config = config;
    this.arena = arena;
    this.chatManager = chatManager;
  }

  public String isOptionDone(String path) {
    if(config.isSet(path)) {
      return chatManager.colorRawMessage("&a&l✔ Completed &7(value: &8" + config.getString(path) + "&7)");
    }
    return chatManager.colorRawMessage("&c&l✘ Not Completed");
  }

  public String isOptionDoneList(String path, int minimum) {
    if(config.isSet(path)) {
      if(config.getStringList(path).size() < minimum) {
        return chatManager.colorRawMessage("&c&l✘ Not Completed | &cPlease add more spawns");
      }
      return chatManager.colorRawMessage("&a&l✔ Completed &7(value: &8" + config.getStringList(path).size() + "&7)");
    }
    return chatManager.colorRawMessage("&c&l✘ Not Completed");
  }

  public String isOptionDoneBool(String path) {
    if(config.isSet(path)) {
      if(Bukkit.getServer().getWorlds().get(0).getSpawnLocation().equals(LocationSerializer.getLocation(config.getString(path)))) {
        return chatManager.colorRawMessage("&c&l✘ Not Completed");
      }
      return chatManager.colorRawMessage("&a&l✔ Completed");
    }
    return chatManager.colorRawMessage("&c&l✘ Not Completed");
  }

  public int getMinimumValueHigherThanZero(String path) {
    int amount = config.getInt("instances." + arena.getId() + "." + path);
    return amount == 0 ? 1 : amount;
  }

}
