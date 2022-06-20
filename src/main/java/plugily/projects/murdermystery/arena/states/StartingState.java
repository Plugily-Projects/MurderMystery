/*
 * MurderMystery - Find the murderer, kill him and survive!
 * Copyright (c) 2022  Plugily Projects - maintained by Tigerpanzer_02 and contributors
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

package plugily.projects.murdermystery.arena.states;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import plugily.projects.minigamesbox.classic.arena.PluginArena;
import plugily.projects.minigamesbox.classic.arena.states.PluginStartingState;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.handlers.language.TitleBuilder;
import plugily.projects.minigamesbox.classic.user.User;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.arena.ArenaUtils;
import plugily.projects.murdermystery.arena.role.Role;
import plugily.projects.murdermystery.utils.ItemPosition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Plajer
 * <p>Created at 03.06.2019
 */
public class StartingState extends PluginStartingState {

  int maxmurderer = 1;
  int maxdetectives = 1;

  @Override
  public void handleCall(PluginArena arena) {
    super.handleCall(arena);
    Arena pluginArena = (Arena) getPlugin().getArenaRegistry().getArena(arena.getId());
    if(pluginArena == null) {
      return;
    }

    int totalMurderer = 0;
    int totalDetective = 0;

    for(Player p : arena.getPlayers()) {
      User user = arena.getPlugin().getUserManager().getUser(p);
      totalMurderer += user.getStatistic("CONTRIBUTION_MURDERER");
      totalDetective += user.getStatistic("CONTRIBUTION_DETECTIVE");
    }

    if(!pluginArena.isHideChances()) {
      for(Player player : arena.getPlayers()) {
        String message =
            new MessageBuilder("IN_GAME_MESSAGES_ARENA_ROLE_CHANCES_ACTION_BAR")
                .asKey()
                .player(player)
                .arena(pluginArena)
                .build();
        VersionUtils.sendActionBar(player, message);
      }
    }

    if(arena.getTimer() == 0 || arena.isForceStart()) {
      Map<User, Double> murdererChances = new HashMap<>();
      Map<User, Double> detectiveChances = new HashMap<>();
      int size = pluginArena.getPlayerSpawnPoints().size();
      for(Player player : arena.getPlayers()) {
        player.teleport(pluginArena.getPlayerSpawnPoints().get(size == 1 ? 0 : getPlugin().getRandom().nextInt(size)));
        User user = arena.getPlugin().getUserManager().getUser(player);
        /*
                   //reset local variables to be 100% sure
           User user = plugin.getUserManager().getUser(player);
           user.setStat(StatsStorage.StatisticType.LOCAL_GOLD, 0);
           user.setStat(StatsStorage.StatisticType.LOCAL_CURRENT_PRAY, 0);
           user.setStat(StatsStorage.StatisticType.LOCAL_KILLS, 0);
           user.setStat(StatsStorage.StatisticType.LOCAL_PRAISES, 0);
           user.setStat(StatsStorage.StatisticType.LOCAL_SCORE, 0);
        */
        ArenaUtils.updateNameTagsVisibility(player);
        player.setGameMode(GameMode.ADVENTURE);

        murdererChances.put(
            user,
            ((double) user.getStatistic("CONTRIBUTION_MURDERER") / (double) totalMurderer) * 100.0);
        detectiveChances.put(
            user,
            ((double) user.getStatistic("CONTRIBUTION_DETECTIVE") / (double) totalDetective)
                * 100.0);
      }

      Set<Player> playersToSet = new HashSet<>(arena.getPlayers());

      getMaxRolesToSet(pluginArena);

      addRole(pluginArena, Role.MURDERER, murdererChances, playersToSet);
      addRole(pluginArena, Role.DETECTIVE, detectiveChances, playersToSet);

      for(Player player : playersToSet) {
        new TitleBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_ROLE_INNOCENT")
            .asKey()
            .player(player)
            .arena(pluginArena)
            .sendPlayer();
      }

      arena
          .getPlugin()
          .getDebugger()
          .debug(
              "After: Arena: {0} | Detectives = {1}, Murders = {2}, Players = {3} | Players: Detectives = {4}, Murders = {5}",
              arena.getId(),
              maxdetectives,
              maxmurderer,
              arena.getPlayers().size(),
              pluginArena.getMurdererList(),
              pluginArena.getDetectiveList());

      // Load and append special blocks hologram
      pluginArena.getSpecialBlocks().forEach(pluginArena::loadSpecialBlock);
    }
  }

  private void addRole(
      Arena arena, Role role, Map<User, Double> chances, Set<Player> playersToSet) {
    String roleName = role.toString();
    // shuffling map to avoid the same roles for players on the next round
    List<Map.Entry<User, Double>> shuffledChances = new ArrayList<>(chances.entrySet());
    Collections.shuffle(shuffledChances);
    //
    Map<User, Double> sortedChances =
        shuffledChances.stream()
            .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

    Object[] sortedChancesUser = sortedChances.keySet().toArray();
    int amount = role == Role.MURDERER ? maxmurderer : maxdetectives;
    for(int i = 0; i < amount; i++) {
      if(i >= sortedChancesUser.length) break;
      User user = (User) sortedChancesUser[i];
      Player userPlayer = user.getPlayer();
      arena.setCharacter(Arena.CharacterType.valueOf(roleName), userPlayer);
      user.setStatistic("CONTRIBUTION_" + roleName, 1);
      playersToSet.remove(userPlayer);
      new TitleBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_ROLE_" + roleName)
          .asKey()
          .arena(arena)
          .player(user.getPlayer())
          .sendPlayer();
      if(role == Role.MURDERER) {
        arena.getMurdererList().add(userPlayer);
      } else if(role == Role.DETECTIVE) {
        arena.getDetectiveList().add(userPlayer);
        userPlayer.getInventory().setHeldItemSlot(0);
        ItemPosition.setItem(user, ItemPosition.BOW, new ItemStack(Material.BOW, 1));
        ItemPosition.setItem(
            user,
            ItemPosition.INFINITE_ARROWS,
            new ItemStack(
                Material.ARROW, getPlugin().getConfig().getInt("Bow.Amount.Arrows.Detective", 3)));
      }
    }
  }

  private void getMaxRolesToSet(Arena arena) {
    int playersSize = arena.getPlayers().size();
    arena
        .getPlugin()
        .getDebugger()
        .debug(
            "Before: Arena: {0} | Detectives = {1}, Murders = {2}, Players = {3} | Configured: Detectives = {4}, Murders = {5}",
            arena.getId(),
            maxdetectives,
            maxmurderer,
            playersSize,
            arena.getArenaOption("DETECTIVE_DIVIDER"),
            arena.getArenaOption("MURDERER_DIVIDER"));
    if(arena.getArenaOption("MURDERER_DIVIDER") > 1
        && playersSize > arena.getArenaOption("MURDERER_DIVIDER")) {
      maxmurderer = (playersSize / arena.getArenaOption("MURDERER_DIVIDER"));
    }
    if(arena.getArenaOption("DETECTIVE_DIVIDER") > 1
        && playersSize > arena.getArenaOption("DETECTIVE_DIVIDER")) {
      maxdetectives = (playersSize / arena.getArenaOption("DETECTIVE_DIVIDER"));
    }
    if(playersSize - (maxmurderer + maxdetectives) < 1) {
      arena
          .getPlugin()
          .getDebugger()
          .debug(
              "{0} Murderers and detectives amount was reduced because there are not enough players",
              arena.getId());
      // Make sure to have one innocent!
      if(maxdetectives > 1) {
        maxdetectives--;
      } else if(maxmurderer > 1) {
        maxmurderer--;
      }
    }

    arena
        .getPlugin()
        .getDebugger()
        .debug(
            "After: Arena: {0} | Detectives = {1}, Murders = {2}, Players = {3} | Configured: Detectives = {4}, Murders = {5}",
            arena.getId(),
            maxdetectives,
            maxmurderer,
            playersSize,
            arena.getArenaOption("DETECTIVE_DIVIDER"),
            arena.getArenaOption("MURDERER_DIVIDER"));
  }
}
