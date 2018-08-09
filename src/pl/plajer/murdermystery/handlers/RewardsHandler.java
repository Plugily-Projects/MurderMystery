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

import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.arena.Arena;
import pl.plajer.murdermystery.arena.ArenaRegistry;
import pl.plajerlair.core.utils.ConfigUtils;

/**
 * @author Plajer
 * <p>
 * Created at 03.08.2018
 */
public class RewardsHandler {

  private FileConfiguration config;
  private Main plugin;
  private boolean enabled;

  public RewardsHandler(Main plugin) {
    this.plugin = plugin;
    enabled = plugin.getConfig().getBoolean("Rewards-Enabled");
    config = ConfigUtils.getConfig(plugin, "rewards");
  }

  public void performEndGameRewards(Arena arena) {
    if (!enabled) {
      return;
    }
    for (String string : config.getStringList("rewards.endgame")) {
      performCommand(arena, string);
    }
  }

  public void performDetectiveKillRewards(Player killer, Player victim) {
    if (!enabled) {
      return;
    }
    for (String string : config.getStringList("rewards.detectivekill")) {
      performCommand(killer, null, string);
      performCommand(null, victim, string);
    }
  }

  public void performMurdererKillRewards(Player killer, Player victim) {
    if (!enabled) {
      return;
    }
    for (String string : config.getStringList("rewards.murdererkill")) {
      performCommand(killer, null, string);
      performCommand(null, victim, string);
    }
  }

  private void performCommand(Arena arena, String string) {
    if (!enabled) {
      return;
    }
    String command = formatCommandPlaceholders(string, arena, null, null);
    if (command.contains("chance(")) {
      int loc = command.indexOf(")");
      if (loc == -1) {
        plugin.getLogger().warning("rewards.yml configuration is broken! Make sure you don't forget using ')' character in chance condition!");
        return;
      }
      String chanceStr = command.substring(0, loc).replaceAll("[^0-9]+", "");
      int chance = Integer.parseInt(chanceStr);
      command = command.replace("chance(" + chanceStr + "):", "");
      if (ThreadLocalRandom.current().nextInt(0, 100) > chance) {
        return;
      }
    }
    if (command.contains("p:") || command.contains("%PLAYER%")) {
      for (Player player : arena.getPlayers()) {
        if (command.contains("p:")) {
          player.performCommand(command.replaceFirst("p:", "").replace("%PLAYER%", player.getName()));
        } else {
          plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command.replace("%PLAYER%", player.getName()));
        }
      }
    }
    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
  }

  private void performCommand(@Nullable Player attacker, @Nullable Player victim, String string) {
    if (!enabled) {
      return;
    }
    if(attacker == null && victim == null){
      return;
    }
    Player eventPlayer = attacker == null ? victim : attacker;
    Arena arena = ArenaRegistry.getArena(eventPlayer);
    if (arena == null) {
      return;
    }
    String command = formatCommandPlaceholders(string, arena, attacker, victim);
    if (command.contains("chance(")) {
      int loc = command.indexOf(")");
      if (loc == -1) {
        plugin.getLogger().warning("rewards.yml configuration is broken! Make sure you don't forget using ')' character in chance condition!");
        return;
      }
      String chanceStr = command.substring(0, loc).replaceAll("[^0-9]+", "");
      int chance = Integer.parseInt(chanceStr);
      command = command.replace("chance(" + chanceStr + "):", "");
      if (ThreadLocalRandom.current().nextInt(0, 100) > chance) {
        return;
      }
    }
    if (command.contains("p:") || command.contains("%PLAYER%")) {
      eventPlayer.performCommand(command.replaceFirst("p:", "").replace("%PLAYER%", eventPlayer.getName()));
    } else {
      plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command.replace("%PLAYER%", eventPlayer.getName()));
    }
  }

  private String formatCommandPlaceholders(String command, Arena arena, @Nullable Player attacker, @Nullable Player victim) {
    String formatted = command;
    if(attacker != null) {
      formatted = StringUtils.replace(formatted, "%ATTACKER%", attacker.getName());
    }
    if(victim != null){
      formatted = StringUtils.replace(formatted, "%VICTIM%", victim.getName());
    }
    formatted = StringUtils.replace(formatted, "%ARENA-ID%", arena.getID());
    formatted = StringUtils.replace(formatted, "%MAPNAME%", arena.getMapName());
    formatted = StringUtils.replace(formatted, "%PLAYERAMOUNT%", String.valueOf(arena.getPlayers().size()));
    return formatted;
  }

}
