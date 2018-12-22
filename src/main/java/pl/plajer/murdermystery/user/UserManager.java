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

package pl.plajer.murdermystery.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.arena.Arena;
import pl.plajerlair.core.debug.Debugger;
import pl.plajerlair.core.debug.LogLevel;

/**
 * @author Plajer
 * <p>
 * Created at 03.08.2018
 */
public class UserManager {

  private static HashMap<UUID, User> users = new HashMap<>();

  public static void registerUser(UUID uuid) {
    Debugger.debug(LogLevel.INFO, "Registering new user with UUID: " + uuid);
    users.put(uuid, new User(uuid));
  }

  public static User getUser(UUID uuid) {
    if (users.containsKey(uuid)) {
      return users.get(uuid);
    } else {
      users.put(uuid, new User(uuid));
      return users.get(uuid);
    }
  }

  public static List<User> getUsers(Arena arena) {
    List<User> users = new ArrayList<>();
    for (Player player : arena.getPlayers()) {
      users.add(getUser(player.getUniqueId()));
    }
    return users;
  }

  public static void removeUser(UUID uuid) {
    users.remove(uuid);
  }

}
