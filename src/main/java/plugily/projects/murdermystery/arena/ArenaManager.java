/*
 * Village Defense - Protect villagers from hordes of zombies
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
import plugily.projects.minigamesbox.classic.arena.ArenaState;
import plugily.projects.minigamesbox.classic.arena.PluginArena;
import plugily.projects.minigamesbox.classic.arena.PluginArenaManager;
import plugily.projects.minigamesbox.classic.handlers.language.TitleBuilder;
import plugily.projects.minigamesbox.classic.user.User;
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
  public void joinAttempt(@NotNull Player player, @NotNull PluginArena arena) {
    Arena pluginArena = (Arena) plugin.getArenaRegistry().getArena(arena.getId());
    if(pluginArena == null) {
      return;
    }
    super.joinAttempt(player, arena);
    int murderIncrease =
        player.getEffectivePermissions().stream()
            .filter(
                permAttach -> permAttach.getPermission().startsWith("murdermystery.role.murderer."))
            .mapToInt(
                pai ->
                    Integer.parseInt(
                        pai.getPermission()
                            .substring(28 /* remove the permission node to obtain the number*/)))
            .max()
            .orElse(0);
    int detectiveIncrease =
        player.getEffectivePermissions().stream()
            .filter(
                permAttach ->
                    permAttach.getPermission().startsWith("murdermystery.role.detective."))
            .mapToInt(
                pai ->
                    Integer.parseInt(
                        pai.getPermission()
                            .substring(29 /* remove the permission node to obtain the number*/)))
            .max()
            .orElse(0);
    User user = plugin.getUserManager().getUser(player);
    user.adjustStatistic("CONTRIBUTION_MURDERER", murderIncrease);
    user.adjustStatistic("CONTRIBUTION_DETECTIVE", detectiveIncrease);
    ArenaUtils.updateNameTagsVisibility(player);
  }

  @Override
  public void leaveAttempt(@NotNull Player player, @NotNull PluginArena arena) {
    Arena pluginArena = (Arena) plugin.getArenaRegistry().getArena(arena.getId());
    if(pluginArena == null) {
      return;
    }
    super.leaveAttempt(player, arena);
    if(pluginArena.isDeathPlayer(player)) {
      pluginArena.removeDeathPlayer(player);
    }
    User user = plugin.getUserManager().getUser(player);

    int localScore = user.getStatistic("LOCAL_SCORE");
    if(localScore > user.getStatistic("HIGHEST_SCORE")) {
      user.setStatistic("HIGHEST_SCORE", localScore);
    }

    // todo change later
    int murderDecrease =
        player.getEffectivePermissions().stream()
            .filter(
                permAttach -> permAttach.getPermission().startsWith("murdermystery.role.murderer."))
            .mapToInt(
                pai ->
                    Integer.parseInt(
                        pai.getPermission()
                            .substring(28 /* remove the permission node to obtain the number*/)))
            .max()
            .orElse(0);
    int detectiveDecrease =
        player.getEffectivePermissions().stream()
            .filter(
                permAttach ->
                    permAttach.getPermission().startsWith("murdermystery.role.detective."))
            .mapToInt(
                pai ->
                    Integer.parseInt(
                        pai.getPermission()
                            .substring(29 /* remove the permission node to obtain the number*/)))
            .max()
            .orElse(0);
    user.adjustStatistic("CONTRIBUTION_MURDERER", -murderDecrease);
    if(user.getStatistic("CONTRIBUTION_MURDERER") <= 0) {
      user.setStatistic("CONTRIBUTION_MURDERER", 1);
    }
    user.adjustStatistic("CONTRIBUTION_DETECTIVE", -detectiveDecrease);
    if(user.getStatistic("CONTRIBUTION_DETECTIVE") <= 0) {
      user.setStatistic("CONTRIBUTION_DETECTIVE", 1);
    }

    if(arena.getArenaState() == ArenaState.IN_GAME) {
      if(Role.isRole(Role.FAKE_DETECTIVE, user, arena)
          || Role.isRole(Role.INNOCENT, user, arena)) {
        user.setStatistic("CONTRIBUTION_MURDERER", ThreadLocalRandom.current().nextInt(4) + 1);
        user.setStatistic("CONTRIBUTION_DETECTIVE", ThreadLocalRandom.current().nextInt(4) + 1);
      }
    }

    boolean playerHasMurdererRole = Role.isRole(Role.MURDERER, user, arena);
    if(playerHasMurdererRole) {
      pluginArena.removeFromMurdererList(player);
    }

    if(arena.getArenaState() == ArenaState.IN_GAME && !user.isSpectator()) {
      List<Player> playersLeft = arena.getPlayersLeft();

      // -1 cause we didn't remove player yet
      if(playersLeft.size() - 1 > 1) {
        if(playerHasMurdererRole) {
          if(pluginArena.getMurdererList().isEmpty()) {
            List<Player> players = new ArrayList<>();
            for(Player gamePlayer : playersLeft) {
              User userGamePlayer = plugin.getUserManager().getUser(gamePlayer);
              if(gamePlayer == player
                  || Role.isRole(Role.ANY_DETECTIVE, userGamePlayer, arena)
                  || Role.isRole(Role.MURDERER, userGamePlayer, arena)) {
                continue;
              }
              players.add(gamePlayer);
            }

            Player newMurderer =
                players.get(
                    players.size() == 1 ? 0 : ThreadLocalRandom.current().nextInt(players.size()));
            if(newMurderer != null) {
              plugin
                  .getDebugger()
                  .debug("A murderer left the game. New murderer: {0}", newMurderer.getName());
              pluginArena.setCharacter(Arena.CharacterType.MURDERER, newMurderer);
              pluginArena.addToMurdererList(newMurderer);
            }

            new TitleBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_ROLE_CHANGE")
                .asKey()
                .player(player)
                .arena(pluginArena)
                .sendArena();

            if(newMurderer != null) {
              new TitleBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_ROLE_MURDERER")
                  .asKey()
                  .player(player)
                  .arena(pluginArena)
                  .sendArena();
              ItemPosition.setItem(
                  plugin.getUserManager().getUser(newMurderer),
                  ItemPosition.MURDERER_SWORD,
                  plugin.getSwordSkinManager().getRandomSwordSkin(player));
            }

            user.setStatistic("CONTRIBUTION_MURDERER", 1);
          } else {
            plugin.getDebugger().debug("No new murderer added as there are some");
          }
        } else if(Role.isRole(Role.ANY_DETECTIVE, user, arena)
            && pluginArena.lastAliveDetective()) {
          pluginArena.setDetectiveDead(true);
          if(Role.isRole(Role.FAKE_DETECTIVE, user, arena)) {
            pluginArena.setCharacter(Arena.CharacterType.FAKE_DETECTIVE, null);
          } else {
            user.setStatistic("CONTRIBUTION_DETECTIVE", 1);
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
  public void stopGame(boolean quickStop, @NotNull PluginArena arena) {
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
    for(Player player : arena.getPlayers()) {
      if(!quickStop) {
        User user = plugin.getUserManager().getUser(player);
        if(!quickStop && Role.isAnyRole(user, arena)) {
          boolean hasDeathRole = Role.isRole(Role.DEATH, user, arena);

          if(!hasDeathRole && !Role.isRole(Role.SPECTATOR, user, arena)) {
            if(Role.isRole(Role.FAKE_DETECTIVE, user, arena)
                || Role.isRole(Role.INNOCENT, user, arena)) {
              user.setStatistic(
                  "CONTRIBUTION_MURDERER", ThreadLocalRandom.current().nextInt(4) + 1);
              user.setStatistic(
                  "CONTRIBUTION_DETECTIVE", ThreadLocalRandom.current().nextInt(4) + 1);
            }
            boolean hasMurdererRole = Role.isRole(Role.MURDERER, user, arena);
            if(murderWon || !hasMurdererRole) {
              user.adjustStatistic("WINS", 1);
              plugin
                  .getRewardsHandler()
                  .performReward(player, plugin.getRewardsHandler().getRewardType("WIN"));
            } else {
              user.adjustStatistic("LOSES", 1);
              plugin
                  .getRewardsHandler()
                  .performReward(player, plugin.getRewardsHandler().getRewardType("LOSE"));
            }
          } else if(hasDeathRole) {
            user.adjustStatistic("LOSES", 1);
            plugin
                .getRewardsHandler()
                .performReward(player, plugin.getRewardsHandler().getRewardType("LOSE"));
          }
        }
      }
      super.stopGame(quickStop, arena);
    }
  }
}
