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

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import plugily.projects.minigamesbox.api.user.IUser;
import plugily.projects.minigamesbox.classic.arena.PluginArena;
import plugily.projects.minigamesbox.classic.arena.states.PluginStartingState;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.handlers.language.TitleBuilder;
import plugily.projects.minigamesbox.classic.utils.actionbar.ActionBar;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.arena.ArenaUtils;
import plugily.projects.murdermystery.arena.role.Role;
import plugily.projects.murdermystery.arena.special.pray.PrayerRegistry;
import plugily.projects.murdermystery.utils.ItemPosition;

import java.util.*;
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

    if(!pluginArena.isHideChances()) {
      for(Player player : arena.getPlayersLeft()) {
        pluginArena.getPlugin().getActionBarManager().addActionBar(player, new ActionBar((new MessageBuilder("IN_GAME_MESSAGES_ARENA_ROLE_CHANCES_ACTION_BAR")).asKey().player(player).arena(pluginArena), ActionBar.ActionBarType.DISPLAY));
      }
    }

    if(arena.getTimer() == 0 || arena.isForceStart()) {
      int size = pluginArena.getPlayerSpawnPoints().size();
      for(Player player : arena.getPlayersLeft()) {
        VersionUtils.teleport(player, pluginArena.getPlayerSpawnPoints().get(getPlugin().getRandom().nextInt(size)));
        IUser user = arena.getPlugin().getUserManager().getUser(player);
        user.resetNonePersistentStatistics();
        PrayerRegistry.getRush().remove(player);
        PrayerRegistry.getBan().remove(player);
        ArenaUtils.updateNameTagsVisibility(player);
        player.setGameMode(GameMode.ADVENTURE);
        getPlugin().getActionBarManager().clearActionBarsFromPlayer(player);
      }

      Set<Player> playersToSet = new HashSet<>(arena.getPlayersLeft());

      getMaxRolesToSet(pluginArena);

      addRole(pluginArena, Role.MURDERER, playersToSet);
      addRole(pluginArena, Role.DETECTIVE, playersToSet);

      for(Player player : playersToSet) {
        new TitleBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_ROLE_INNOCENT").asKey().player(player).arena(pluginArena).sendPlayer();
      }

      arena.getPlugin().getDebugger().debug("After: Arena: {0} | Detectives = {1}, Murders = {2}, Players = {3} | Players: Detectives = {4}, Murders = {5}", arena.getId(), maxdetectives, maxmurderer, arena.getPlayersLeft().size(), pluginArena.getDetectiveList(), pluginArena.getMurdererList());

      // Load and append special blocks hologram
      pluginArena.getSpecialBlocks().forEach(pluginArena::loadSpecialBlock);
    }
  }

  private void addRole(Arena arena, Role role, Set<Player> playersToSet) {
    String roleName = role.toString();

    List<IUser> chancesRanking = getPlugin().getUserManager().getUsers(arena).stream().filter(user -> playersToSet.contains(user.getPlayer())).sorted(Comparator.comparingInt(user -> arena.getContributorValue(role, user))).collect(Collectors.toList());
    Collections.reverse(chancesRanking);
    List<Player> chancesPlayer = new ArrayList<>();
    for(IUser user : chancesRanking) {
      chancesPlayer.add(user.getPlayer());
    }
    getPlugin().getDebugger().debug("Arena {0} | Role add {1} | List {2}", arena.getId(), roleName, chancesPlayer);

    int amount = role == Role.MURDERER ? maxmurderer : maxdetectives;
    for(int i = 0; i < amount; i++) {
      IUser user = chancesRanking.get(i);
      Player userPlayer = user.getPlayer();
      arena.setCharacter(role, userPlayer);
      arena.resetContributorValue(role, user);
      playersToSet.remove(userPlayer);
      new TitleBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_ROLE_" + roleName).asKey().arena(arena).player(userPlayer).sendPlayer();
      if(role == Role.MURDERER) {
        arena.getMurdererList().add(userPlayer);
      } else if(role == Role.DETECTIVE) {
        arena.getDetectiveList().add(userPlayer);
        userPlayer.getInventory().setHeldItemSlot(0);
        Bukkit.getScheduler().runTaskLater(arena.getPlugin(), () -> {
          ItemPosition.setItem(user, ItemPosition.BOW, new ItemStack(Material.BOW, 1));
          ItemPosition.setItem(user, ItemPosition.INFINITE_ARROWS, new ItemStack(Material.ARROW, getPlugin().getConfig().getInt("Bow.Amount.Arrows.Detective", 3)));
        }, 20);
      }
    }
  }

  private void getMaxRolesToSet(Arena arena) {
    int playersSize = arena.getPlayersLeft().size();
    arena.getPlugin().getDebugger().debug("Before: Arena: {0} | Detectives = {1}, Murders = {2}, Players = {3} | Configured: Detectives = {4}, Murders = {5}", arena.getId(), maxdetectives, maxmurderer, playersSize, arena.getArenaOption("DETECTIVE_DIVIDER"), arena.getArenaOption("MURDERER_DIVIDER"));
    if(arena.getArenaOption("MURDERER_DIVIDER") > 1 && playersSize > arena.getArenaOption("MURDERER_DIVIDER")) {
      maxmurderer = (playersSize / arena.getArenaOption("MURDERER_DIVIDER"));
    }
    if(arena.getArenaOption("DETECTIVE_DIVIDER") > 1 && playersSize > arena.getArenaOption("DETECTIVE_DIVIDER")) {
      maxdetectives = (playersSize / arena.getArenaOption("DETECTIVE_DIVIDER"));
    }
    if(playersSize - (maxmurderer + maxdetectives) < 1) {
      arena.getPlugin().getDebugger().debug("{0} Murderers and detectives amount was reduced because there are not enough players", arena.getId());
      // Make sure to have one innocent!
      if(maxdetectives > 1) {
        maxdetectives--;
      } else if(maxmurderer > 1) {
        maxmurderer--;
      }
    }
    arena.getPlugin().getDebugger().debug("After: Arena: {0} | Detectives = {1}, Murders = {2}, Players = {3} | Configured: Detectives = {4}, Murders = {5}", arena.getId(), maxdetectives, maxmurderer, playersSize, arena.getArenaOption("DETECTIVE_DIVIDER"), arena.getArenaOption("MURDERER_DIVIDER"));
  }
}
