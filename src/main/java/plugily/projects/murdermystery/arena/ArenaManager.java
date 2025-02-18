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

package plugily.projects.murdermystery.arena;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import plugily.projects.minigamesbox.api.arena.IArenaState;
import plugily.projects.minigamesbox.api.arena.IPluginArena;
import plugily.projects.minigamesbox.api.user.IUser;
import plugily.projects.minigamesbox.classic.arena.PluginArenaManager;
import plugily.projects.minigamesbox.classic.handlers.language.TitleBuilder;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.arena.managers.MapRestorerManager;
import plugily.projects.murdermystery.arena.role.Role;
import plugily.projects.murdermystery.arena.special.SpecialBlock;
import plugily.projects.murdermystery.utils.ItemPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Plajer
 * <p>Created at 13.05.2018
 */
public class ArenaManager extends PluginArenaManager {

  private final Main plugin;

  public ArenaManager(Main plugin) {
    super(plugin);
    this.plugin = plugin;
  }

  @Override
  public void joinAttempt(@NotNull Player player, @NotNull IPluginArena arena) {
    Arena pluginArena = (Arena) plugin.getArenaRegistry().getArena(arena.getId());
    if(pluginArena == null) {
      return;
    }
    super.joinAttempt(player, arena);
    ArenaUtils.updateNameTagsVisibility(player);
  }

  @Override
  public void leaveAttempt(@NotNull Player player, @NotNull IPluginArena arena) {
    Arena pluginArena = (Arena) plugin.getArenaRegistry().getArena(arena.getId());
    if(pluginArena == null) {
      return;
    }
    super.leaveAttempt(player, arena);
    if(pluginArena.isDeathPlayer(player)) {
      pluginArena.removeDeathPlayer(player);
    }
    IUser user = plugin.getUserManager().getUser(player);

    int localScore = user.getStatistic("LOCAL_SCORE");
    if(localScore > user.getStatistic("HIGHEST_SCORE")) {
      user.setStatistic("HIGHEST_SCORE", localScore);
    }

    boolean playerHasMurdererRole = Role.isRole(Role.MURDERER, user, arena);
    if(playerHasMurdererRole) {
      pluginArena.removeFromMurdererList(player);
    }

    if(arena.getArenaState() == IArenaState.IN_GAME && !user.isSpectator()) {
      List<Player> playersLeft = arena.getPlayersLeft();

      // -1 cause we didn't remove player yet
      if(playersLeft.size() - 1 > 1) {
        if(playerHasMurdererRole) {
          if(pluginArena.getMurdererList().isEmpty()) {
            List<Player> players = new ArrayList<>();
            for(Player gamePlayer : playersLeft) {
              IUser userGamePlayer = plugin.getUserManager().getUser(gamePlayer);
              if(gamePlayer == player || Role.isRole(Role.ANY_DETECTIVE, userGamePlayer, arena) || Role.isRole(Role.MURDERER, userGamePlayer, arena)) {
                continue;
              }
              players.add(gamePlayer);
            }

            Player newMurderer = players.get(players.size() == 1 ? 0 : ThreadLocalRandom.current().nextInt(players.size()));
            if(newMurderer != null) {
              plugin.getDebugger().debug("A murderer left the game. New murderer: {0}", newMurderer.getName());
              pluginArena.setCharacter(Arena.CharacterType.MURDERER, newMurderer);
              pluginArena.addToMurdererList(newMurderer);
            }

            new TitleBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_ROLE_CHANGE").asKey().player(player).arena(pluginArena).sendArena();
            if(newMurderer != null) {
              new TitleBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_ROLE_MURDERER").asKey().player(player).arena(pluginArena).sendArena();
              ItemPosition.setItem(plugin.getUserManager().getUser(newMurderer), ItemPosition.MURDERER_SWORD, plugin.getSwordSkinManager().getRandomSwordSkin(player));
            }
          } else {
            plugin.getDebugger().debug("No new murderer added as there are some");
          }
        } else if(Role.isRole(Role.ANY_DETECTIVE, user, arena)
          && pluginArena.lastAliveDetective()) {
          pluginArena.setDetectiveDead(true);
          if(Role.isRole(Role.FAKE_DETECTIVE, user, arena)) {
            pluginArena.setCharacter(Arena.CharacterType.FAKE_DETECTIVE, null);
          }
          ArenaUtils.dropBowAndAnnounce(pluginArena, player);
        }
        plugin.getCorpseHandler().spawnCorpse(player, pluginArena);
      } else {
        stopGame(false, arena);
      }
    }
  }

  @Override
  public void stopGame(boolean quickStop, @NotNull IPluginArena arena) {
    Arena pluginArena = (Arena) plugin.getArenaRegistry().getArena(arena.getId());
    if(pluginArena == null) {
      return;
    }
    for(SpecialBlock specialBlock : pluginArena.getSpecialBlocks()) {
      if(specialBlock.getArmorStandHologram() != null) {
        specialBlock.getArmorStandHologram().delete();
      }
    }
    ((MapRestorerManager) pluginArena.getMapRestorerManager()).removeBowHolo();
    boolean murderWon = arena.getPlayersLeft().size() == pluginArena.aliveMurderer();
    for(Player player : arena.getPlayersLeft()) {
      if(!quickStop) {
        IUser user = plugin.getUserManager().getUser(player);
        if(Role.isAnyRole(user, arena)) {
          boolean hasDeathRole = Role.isRole(Role.DEATH, user, arena);
          int multiplicator = 1;
          if(!hasDeathRole) {
            multiplicator = arena.getMaximumPlayers();
          }
          pluginArena.adjustContributorValue(Role.MURDERER, user, plugin.getRandom().nextInt(10 * multiplicator));
          pluginArena.adjustContributorValue(Role.DETECTIVE, user, plugin.getRandom().nextInt(10 * multiplicator));
          if(!hasDeathRole) {
            boolean hasMurdererRole = Role.isRole(Role.MURDERER, user, arena);
            if(murderWon || !hasMurdererRole) {
              user.adjustStatistic("WINS", 1);
              plugin.getRewardsHandler().performReward(player, plugin.getRewardsHandler().getRewardType("WIN"));
            } else {
              user.adjustStatistic("LOSES", 1);
              plugin.getRewardsHandler().performReward(player, plugin.getRewardsHandler().getRewardType("LOSE"));
            }
          } else {
            user.adjustStatistic("LOSES", 1);
            plugin.getRewardsHandler().performReward(player, plugin.getRewardsHandler().getRewardType("LOSE"));
          }
        }
      }
    }
    super.stopGame(quickStop, arena);
  }
}
