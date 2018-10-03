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

package pl.plajer.murdermystery.handlers;

import org.bukkit.plugin.java.JavaPlugin;

import pl.plajer.murdermystery.Main;

/**
 * @author Plajer
 * <p>
 * Created at 03.08.2018
 */
public class PermissionsManager {

  private static Main plugin = JavaPlugin.getPlugin(Main.class);
  private static String joinFullPerm = "murdermystery.fullgames";
  private static String joinPerm = "murdermystery.join.<arena>";

  public static void init() {
    setupPermissions();
  }

  public static String getJoinFullGames() {
    return joinFullPerm;
  }

  private static void setJoinFullGames(String joinFullGames) {
    PermissionsManager.joinFullPerm = joinFullGames;
  }

  public static String getJoinPerm() {
    return joinPerm;
  }

  private static void setJoinPerm(String joinPerm) {
    PermissionsManager.joinPerm = joinPerm;
  }

  private static void setupPermissions() {
    PermissionsManager.setJoinFullGames(plugin.getConfig().getString("Basic-Permissions.Full-Games-Permission", "murdermystery.fullgames"));
    PermissionsManager.setJoinPerm(plugin.getConfig().getString("Basic-Permissions.Join-Permission", "murdermystery.join.<arena>"));
    Main.debug(Main.LogLevel.INFO, "Basic permissions registered");
  }

}
