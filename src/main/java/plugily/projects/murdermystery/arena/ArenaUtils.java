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

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import plugily.projects.murdermystery.ConfigPreferences;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.api.StatsStorage;
import plugily.projects.murdermystery.arena.role.Role;
import plugily.projects.murdermystery.handlers.ChatManager;
import plugily.projects.murdermystery.handlers.hologram.ArmorStandHologram;
import plugily.projects.murdermystery.user.User;
import plugily.projects.murdermystery.utils.ItemPosition;
import plugily.projects.murdermystery.utils.NMS;

/**
 * @author Plajer
 * <p>
 * Created at 13.03.2018
 */
public class ArenaUtils {

  private static final Main plugin = JavaPlugin.getPlugin(Main.class);
  private static final ChatManager chatManager = plugin.getChatManager();

  public static void onMurdererDeath(Arena arena) {
    for (Player player : arena.getPlayers()) {
      player.sendTitle(chatManager.colorMessage("In-Game.Messages.Game-End-Messages.Titles.Win", player),
        chatManager.colorMessage("In-Game.Messages.Game-End-Messages.Subtitles.Murderer-Stopped", player), 5, 40, 5);
      if (Role.isRole(Role.MURDERER, player)) {
        player.sendTitle(chatManager.colorMessage("In-Game.Messages.Game-End-Messages.Titles.Lose", player), null, 5, 40, 5);
      }
      User loopUser = plugin.getUserManager().getUser(player);
      if (Role.isRole(Role.INNOCENT, player)) {
        ArenaUtils.addScore(loopUser, ArenaUtils.ScoreAction.SURVIVE_GAME, 0);
      } else if (Role.isRole(Role.ANY_DETECTIVE, player)) {
        ArenaUtils.addScore(loopUser, ArenaUtils.ScoreAction.WIN_GAME, 0);
        ArenaUtils.addScore(loopUser, ArenaUtils.ScoreAction.DETECTIVE_WIN_GAME, 0);
      }
    }
    for (Player murderer : arena.getMurdererList()) {
      murderer.sendTitle(chatManager.colorMessage("In-Game.Messages.Game-End-Messages.Titles.Lose", murderer),
        chatManager.colorMessage("In-Game.Messages.Game-End-Messages.Subtitles.Murderer-Stopped", murderer), 5, 40, 5);
    }
    //we must call it ticks later due to instant respawn bug
    Bukkit.getScheduler().runTaskLater(plugin, () -> ArenaManager.stopGame(false, arena), 10);
  }

  public static void addScore(User user, ScoreAction action, int amount) {
    String s = plugin.getConfig().getString("AddScore-Sound", "");
    if (!s.isEmpty()) {
      user.getPlayer().playSound(user.getPlayer().getLocation(), Sound.valueOf(s.toUpperCase()), 1F, 2F);
    }

    String msg = chatManager.colorMessage("In-Game.Messages.Bonus-Score");
    msg = StringUtils.replace(msg, "%score%", String.valueOf(action.getPoints()));

    if (action == ScoreAction.GOLD_PICKUP && amount > 1) {
      msg = StringUtils.replace(msg, "%score%", String.valueOf(action.getPoints() * amount));
      msg = StringUtils.replace(msg, "%action%", action.getAction());
      user.setStat(StatsStorage.StatisticType.LOCAL_SCORE, user.getStat(StatsStorage.StatisticType.LOCAL_SCORE) + (action.getPoints() * amount));
      user.getPlayer().sendMessage(msg);
      return;
    }

    if (action == ScoreAction.DETECTIVE_WIN_GAME) {
      int innocents = 0;
      for (Player p : user.getArena().getPlayersLeft()) {
        if (Role.isRole(Role.INNOCENT, p)) {
          innocents++;
        }
      }

      user.setStat(StatsStorage.StatisticType.LOCAL_SCORE, user.getStat(StatsStorage.StatisticType.LOCAL_SCORE) + (100 * innocents));
      msg = StringUtils.replace(msg, "%score%", String.valueOf(100 * innocents));
      msg = StringUtils.replace(msg, "%action%", action.getAction().replace("%amount%", String.valueOf(innocents)));
      user.getPlayer().sendMessage(msg);
      return;
    }

    msg = StringUtils.replace(msg, "%score%", String.valueOf(action.getPoints()));

    if (action.getPoints() < 0) {
      msg = StringUtils.replace(msg, "+", "");
    }

    msg = StringUtils.replace(msg, "%action%", action.getAction());

    user.setStat(StatsStorage.StatisticType.LOCAL_SCORE, user.getStat(StatsStorage.StatisticType.LOCAL_SCORE) + action.getPoints());
    user.getPlayer().sendMessage(msg);
  }

  public static void updateInnocentLocator(Arena arena) {
    if (!arena.isMurdererLocatorReceived()) {
      ItemStack innocentLocator = new ItemStack(Material.COMPASS, 1);
      ItemMeta innocentMeta = innocentLocator.getItemMeta();
      innocentMeta.setDisplayName(chatManager.colorMessage("In-Game.Innocent-Locator-Item-Name"));
      innocentLocator.setItemMeta(innocentMeta);
      for (Player p : arena.getPlayersLeft()) {
        if (arena.isMurderAlive(p)) {
          ItemPosition.setItem(p, ItemPosition.INNOCENTS_LOCATOR, innocentLocator);
        }
      }
      arena.setMurdererLocatorReceived(true);

      for (Player p : arena.getPlayersLeft()) {
        if (Role.isRole(Role.MURDERER, p)) {
          continue;
        }
        p.sendTitle(chatManager.colorMessage("In-Game.Watch-Out-Title", p), chatManager.colorMessage("In-Game.Watch-Out-Subtitle", p), 5, 40, 5);
      }
    }
    for (Player p : arena.getPlayersLeft()) {
      if (Role.isRole(Role.MURDERER, p)) {
        continue;
      }
      for (Player murder : arena.getMurdererList()) {
        if (arena.isMurderAlive(murder)) {
          murder.setCompassTarget(p.getLocation());
        }
      }
      break;
    }
  }

  private static void addBowLocator(Arena arena, Location loc) {
    ItemStack bowLocator = new ItemStack(Material.COMPASS, 1);
    ItemMeta bowMeta = bowLocator.getItemMeta();
    bowMeta.setDisplayName(chatManager.colorMessage("In-Game.Bow-Locator-Item-Name"));
    bowLocator.setItemMeta(bowMeta);
    for (Player p : arena.getPlayersLeft()) {
      if (Role.isRole(Role.INNOCENT, p)) {
        ItemPosition.setItem(p, ItemPosition.BOW_LOCATOR, bowLocator);
        p.setCompassTarget(loc);
      }
    }
  }

  public static void dropBowAndAnnounce(Arena arena, Player victim) {
    if (arena.getBowHologram() != null) {
      return;
    }
    if (arena.getPlayersLeft().size() > 1) {
      for (Player p : arena.getPlayers()) {
        p.sendTitle(chatManager.colorMessage("In-Game.Messages.Bow-Messages.Bow-Dropped-Title", p), null, 5, 40, 5);
      }
      for (Player p : arena.getPlayersLeft()) {
        p.sendTitle(null, chatManager.colorMessage("In-Game.Messages.Bow-Messages.Bow-Dropped-Subtitle", p), 5, 40, 5);
      }
    }

    ArmorStandHologram hologram = new ArmorStandHologram(victim.getLocation())
      .appendItem(new ItemStack(Material.BOW, 1));

    arena.setBowHologram(hologram);
    addBowLocator(arena, hologram.getLocation());
  }

  public static boolean areInSameArena(Player one, Player two) {
    return ArenaRegistry.getArena(one) != null && ArenaRegistry.getArena(one).equals(ArenaRegistry.getArena(two));
  }

  public static void hidePlayer(Player p, Arena arena) {
    for (Player player : arena.getPlayers()) {
      NMS.hidePlayer(player, p);
    }
  }

  public static void showPlayer(Player p, Arena arena) {
    for (Player player : arena.getPlayers()) {
      NMS.showPlayer(player, p);
    }
  }

  public static void hidePlayersOutsideTheGame(Player player, Arena arena) {
    for (Player players : plugin.getServer().getOnlinePlayers()) {
      if (arena.getPlayers().contains(players)) {
        continue;
      }
      NMS.hidePlayer(player, players);
      NMS.hidePlayer(players, player);
    }
  }

  public static void updateNameTagsVisibility(final Player p) {
    if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.NAMETAGS_HIDDEN)) {
      return;
    }
    for (Player players : plugin.getServer().getOnlinePlayers()) {
      Arena arena = ArenaRegistry.getArena(players);
      if (arena == null) {
        continue;
      }
      Scoreboard scoreboard = players.getScoreboard();
      if (scoreboard == Bukkit.getScoreboardManager().getMainScoreboard()) {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
      }
      Team team = scoreboard.getTeam("MMHide");
      if (team == null) {
        team = scoreboard.registerNewTeam("MMHide");
      }
      team.setCanSeeFriendlyInvisibles(false);
      team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
      if (arena.getArenaState() == ArenaState.IN_GAME) {
        team.addEntry(p.getName());
      } else {
        team.removeEntry(p.getName());
      }
      players.setScoreboard(scoreboard);
    }
  }

  public enum ScoreAction {
    KILL_PLAYER(100, chatManager.colorMessage("In-Game.Messages.Score-Actions.Kill-Player")), KILL_MURDERER(200, chatManager.colorMessage("In-Game.Messages.Score-Actions.Kill-Murderer")),
    GOLD_PICKUP(15, chatManager.colorMessage("In-Game.Messages.Score-Actions.Gold-Pickup")), SURVIVE_TIME(150, chatManager.colorMessage("In-Game.Messages.Score-Actions.Survive")),
    SURVIVE_GAME(200, chatManager.colorMessage("In-Game.Messages.Score-Actions.Survive-Till-End")), WIN_GAME(100, chatManager.colorMessage("In-Game.Messages.Score-Actions.Win-Game")),
    DETECTIVE_WIN_GAME(0, chatManager.colorMessage("In-Game.Messages.Score-Actions.Detective-Reward")), INNOCENT_KILL(-100, chatManager.colorMessage("In-Game.Messages.Score-Actions.Innocent-Kill"));

    int points;
    String action;

    ScoreAction(int points, String action) {
      this.points = points;
      this.action = action;
    }

    public int getPoints() {
      return points;
    }

    public String getAction() {
      return action;
    }
  }

}
