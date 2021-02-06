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

package plugily.projects.murdermystery.arena;

import org.bukkit.plugin.java.JavaPlugin;

import plugily.projects.murdermystery.Main;

/**
 * @author TomTheDeveloper
 * <p>
 * Contains all GameStates.
 */
public enum ArenaState {
  WAITING_FOR_PLAYERS("Waiting"), STARTING("Starting"), IN_GAME("Playing"), ENDING("Ending"), RESTARTING("Restarting");

  String formattedName;
  String placeholder;

  ArenaState(String formattedName) {
    this.formattedName = formattedName;
    this.placeholder = JavaPlugin.getPlugin(Main.class).getChatManager().colorMessage("Placeholders.Game-States." + formattedName);
  }

  public String getFormattedName() {
    return formattedName;
  }

  public String getPlaceholder() {
    return placeholder;
  }
}
