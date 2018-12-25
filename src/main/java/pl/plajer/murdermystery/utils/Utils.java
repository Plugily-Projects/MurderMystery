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

package pl.plajer.murdermystery.utils;

import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.arena.ArenaRegistry;
import pl.plajer.murdermystery.arena.ArenaState;
import pl.plajer.murdermystery.handlers.ChatManager;
import pl.plajerlair.core.utils.MinigameUtils;

/**
 * @author Plajer
 * <p>
 * Created at 06.10.2018
 */
public class Utils {

  private static Main plugin = JavaPlugin.getPlugin(Main.class);

  public static void applyActionBarCooldown(Player p, int seconds) {
    new BukkitRunnable() {
      int ticks = 0;

      @Override
      public void run() {
        if (!ArenaRegistry.isInArena(p) || ArenaRegistry.getArena(p).getArenaState() != ArenaState.IN_GAME) {
          this.cancel();
        }
        if (ticks >= seconds * 20) {
          this.cancel();
        }
        String progress = MinigameUtils.getProgressBar(ticks, 5 * 20, 10, "■", "&a", "&c");
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatManager.colorMessage("In-Game.Cooldown-Format")
            .replace("%progress%", progress).replace("%time%", String.valueOf((double) (100 - ticks) / 20))));
        ticks += 10;
      }
    }.runTaskTimer(plugin, 10, 10);
  }

  public static List<Block> getNearbyBlocks(Location location, int radius) {
    List<Block> blocks = new ArrayList<>();
    for (int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
      for (int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; y++) {
        for (int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {
          blocks.add(location.getWorld().getBlockAt(x, y, z));
        }
      }
    }
    return blocks;
  }

  public static Location getBlockCenter(Location loc) {
    return new Location(loc.getWorld(),
        getRelativeCoord(loc.getBlockX()),
        getRelativeCoord(loc.getBlockY()),
        getRelativeCoord(loc.getBlockZ()));
  }

  private static double getRelativeCoord(int i) {
    double d = i;
    d = d < 0 ? d - .5 : d + .5;
    return d;
  }

  /**
   * Due to spigot bug if coordinate is negative getTargetBlock will display negative coord -1
   *
   * @param loc location to fix coordinates for
   * @return fixed location value
   */
  public static Location fixLocation(Location loc) {
    Location fixedLocation = loc.clone();
    if (loc.getZ() < 0) {
      fixedLocation.add(0, 0, 1);
    }
    if (loc.getX() < 0) {
      fixedLocation.add(1, 0, 0);
    }
    return fixedLocation;
  }
}
