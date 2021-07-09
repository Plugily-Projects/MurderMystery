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
import pl.plajerlair.commonsbox.minecraft.compat.VersionUtils;
import pl.plajerlair.commonsbox.minecraft.compat.xseries.XSound;
import pl.plajerlair.commonsbox.minecraft.hologram.ArmorStandHologram;
import pl.plajerlair.commonsbox.minecraft.misc.stuff.ComplementAccessor;
import plugily.projects.murdermystery.ConfigPreferences;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.api.StatsStorage;
import plugily.projects.murdermystery.arena.role.Role;
import plugily.projects.murdermystery.handlers.ChatManager;
import plugily.projects.murdermystery.user.User;
import plugily.projects.murdermystery.utils.ItemPosition;
import plugily.projects.murdermystery.utils.Utils;

/**
 * @author Plajer
 * <p>
 * Created at 13.03.2018
 */
public class ArenaUtils {

  private static final Main plugin = JavaPlugin.getPlugin(Main.class);
  private static final ChatManager chatManager = plugin.getChatManager();

  public static void onMurdererDeath(Arena arena) {
    for(Player player : arena.getPlayers()) {
      VersionUtils.sendTitles(player, chatManager.colorMessage("In-Game.Messages.Game-End-Messages.Titles.Win", player),
          chatManager.colorMessage("In-Game.Messages.Game-End-Messages.Subtitles.Murderer-Stopped", player), 5, 40, 5);
      if(Role.isRole(Role.MURDERER, player)) {
        VersionUtils.sendTitles(player, chatManager.colorMessage("In-Game.Messages.Game-End-Messages.Titles.Lose", player), null, 5, 40, 5);
      }
      User loopUser = plugin.getUserManager().getUser(player);
      if(Role.isRole(Role.INNOCENT, player)) {
        addScore(loopUser, ScoreAction.SURVIVE_GAME, 0);
      } else if(Role.isRole(Role.ANY_DETECTIVE, player)) {
        addScore(loopUser, ScoreAction.WIN_GAME, 0);
        addScore(loopUser, ScoreAction.DETECTIVE_WIN_GAME, 0);
      }
    }
    for(Player murderer : arena.getMurdererList()) {
      VersionUtils.sendTitles(murderer, chatManager.colorMessage("In-Game.Messages.Game-End-Messages.Titles.Lose", murderer),
          chatManager.colorMessage("In-Game.Messages.Game-End-Messages.Subtitles.Murderer-Stopped", murderer), 5, 40, 5);
    }
    //we must call it ticks later due to instant respawn bug
    Bukkit.getScheduler().runTaskLater(plugin, () -> ArenaManager.stopGame(false, arena), 10);
  }

  public static void addScore(User user, ScoreAction action, int amount) {
    String s = plugin.getConfig().getString("AddScore-Sound", "");
    if(!s.isEmpty()) {
      Sound sound = XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound();

      try {
        sound = Sound.valueOf(s.toUpperCase());
      } catch(IllegalArgumentException e) {
      }

      XSound.matchXSound(sound).play(user.getPlayer().getLocation(), 1F, 2F);
    }

    String msg = chatManager.colorMessage("In-Game.Messages.Bonus-Score");

    if(action == ScoreAction.GOLD_PICKUP && amount > 1) {
      int score = action.points * amount;

      msg = StringUtils.replace(msg, "%score%", Integer.toString(score));
      msg = StringUtils.replace(msg, "%action%", action.action);

      user.setStat(StatsStorage.StatisticType.LOCAL_SCORE, user.getStat(StatsStorage.StatisticType.LOCAL_SCORE) + (score));
      user.getPlayer().sendMessage(msg);
      return;
    }

    if(action == ScoreAction.DETECTIVE_WIN_GAME) {
      int innocents = 0;
      for(Player p : user.getArena().getPlayersLeft()) {
        if(Role.isRole(Role.INNOCENT, p)) {
          innocents++;
        }
      }

      int overallInnocents = 100 * innocents;

      user.setStat(StatsStorage.StatisticType.LOCAL_SCORE, user.getStat(StatsStorage.StatisticType.LOCAL_SCORE) + overallInnocents);

      msg = StringUtils.replace(msg, "%score%", Integer.toString(overallInnocents));
      msg = StringUtils.replace(msg, "%action%", action.action.replace("%amount%", Integer.toString(innocents)));

      user.getPlayer().sendMessage(msg);
      return;
    }

    msg = StringUtils.replace(msg, "%score%", Integer.toString(action.points));

    if(action.points < 0) {
      msg = StringUtils.replace(msg, "+", "");
    }

    msg = StringUtils.replace(msg, "%action%", action.action);

    user.setStat(StatsStorage.StatisticType.LOCAL_SCORE, user.getStat(StatsStorage.StatisticType.LOCAL_SCORE) + action.points);
    user.getPlayer().sendMessage(msg);
  }

  public static void updateInnocentLocator(Arena arena) {
    java.util.List<Player> list = arena.getPlayersLeft();

    if(!arena.isMurdererLocatorReceived()) {
      ItemStack innocentLocator = new ItemStack(Material.COMPASS, 1);
      ItemMeta innocentMeta = innocentLocator.getItemMeta();
      ComplementAccessor.getComplement().setDisplayName(innocentMeta, chatManager.colorMessage("In-Game.Innocent-Locator-Item-Name"));
      innocentLocator.setItemMeta(innocentMeta);
      for(Player p : list) {
        if(arena.isMurderAlive(p)) {
          ItemPosition.setItem(p, ItemPosition.INNOCENTS_LOCATOR, innocentLocator);
        }
      }
      arena.setMurdererLocatorReceived(true);

      for(Player p : list) {
        if(Role.isRole(Role.MURDERER, p)) {
          continue;
        }
        VersionUtils.sendTitles(p, chatManager.colorMessage("In-Game.Watch-Out-Title", p), chatManager.colorMessage("In-Game.Watch-Out-Subtitle", p), 5, 40, 5);
      }
    }

    for(Player p : list) {
      if(Role.isRole(Role.MURDERER, p)) {
        continue;
      }
      for(Player murder : arena.getMurdererList()) {
        if(arena.isMurderAlive(murder)) {
          murder.setCompassTarget(p.getLocation());
        }
      }
      break;
    }
  }

  private static void addBowLocator(Arena arena, Location loc) {
    ItemStack bowLocator = new ItemStack(Material.COMPASS, 1);
    ItemMeta bowMeta = bowLocator.getItemMeta();
    ComplementAccessor.getComplement().setDisplayName(bowMeta, chatManager.colorMessage("In-Game.Bow-Locator-Item-Name"));
    bowLocator.setItemMeta(bowMeta);
    for(Player p : arena.getPlayersLeft()) {
      if(Role.isRole(Role.INNOCENT, p)) {
        ItemPosition.setItem(p, ItemPosition.BOW_LOCATOR, bowLocator);
        p.setCompassTarget(loc);
      }
    }
  }

  public static void dropBowAndAnnounce(Arena arena, Player victim) {
    if(arena.getBowHologram() != null) {
      return;
    }
    java.util.List<Player> list = arena.getPlayersLeft();
    if(list.size() > 1) {
      for(Player p : arena.getPlayers()) {
        VersionUtils.sendTitles(p, chatManager.colorMessage("In-Game.Messages.Bow-Messages.Bow-Dropped-Title", p), null, 5, 40, 5);
      }
      for(Player p : list) {
        VersionUtils.sendTitles(p, null, chatManager.colorMessage("In-Game.Messages.Bow-Messages.Bow-Dropped-Subtitle", p), 5, 40, 5);
      }
    }

    ArmorStandHologram hologram = new ArmorStandHologram(victim.getLocation())
        .appendItem(new ItemStack(Material.BOW, 1));

    arena.setBowHologram(hologram);
    addBowLocator(arena, hologram.getLocation());
  }

  public static boolean areInSameArena(Player one, Player two) {
    Arena a1 = ArenaRegistry.getArena(one);
    return a1 != null && a1.equals(ArenaRegistry.getArena(two));
  }

  public static void hidePlayer(Player p, Arena arena) {
    for(Player player : arena.getPlayers()) {
      if(plugin.getUserManager().getUser(player).isSpectator()) {
        VersionUtils.showPlayer(plugin, player, p);
      } else {
        VersionUtils.hidePlayer(plugin, player, p);
      }
    }
  }

  public static void showPlayer(Player p, Arena arena) {
    for(Player player : arena.getPlayers()) {
      VersionUtils.showPlayer(plugin, player, p);
    }
  }

  public static void hidePlayersOutsideTheGame(Player player, Arena arena) {
    for(Player players : plugin.getServer().getOnlinePlayers()) {
      if(arena.getPlayers().contains(players)) {
        continue;
      }
      VersionUtils.hidePlayer(plugin, player, players);
      VersionUtils.hidePlayer(plugin, players, player);
    }
  }

  public static void updateNameTagsVisibility(final Player p) {
    if(!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.NAMETAGS_HIDDEN)) {
      return;
    }
    for(Player players : plugin.getServer().getOnlinePlayers()) {
      Arena arena = ArenaRegistry.getArena(players);
      if(arena == null) {
        continue;
      }
      VersionUtils.updateNameTagsVisibility(p, players, "MMHide", arena.getArenaState() != ArenaState.IN_GAME);
    }
  }

  public static void arenaForceStart(Player player) {
    if(!Utils.hasPermission(player, "murdermystery.admin.forcestart")) {
      return;
    }
    if(!Utils.checkIsInGameInstance(player)) {
      return;
    }
    Arena arena = ArenaRegistry.getArena(player);
    if(arena.getPlayers().size() < 2) {
      chatManager.broadcast(arena, chatManager.formatMessage(arena, chatManager.colorMessage("In-Game.Messages.Lobby-Messages.Waiting-For-Players"), arena.getMinimumPlayers()));
      return;
    }
    if(arena.getArenaState() == ArenaState.WAITING_FOR_PLAYERS || arena.getArenaState() == ArenaState.STARTING) {
      arena.setArenaState(ArenaState.STARTING);
      arena.setForceStart(true);
      arena.setTimer(0);
      for(Player arenaPlayers : arena.getPlayers()) {
        arenaPlayers.sendMessage(chatManager.getPrefix() + chatManager.colorMessage("In-Game.Messages.Admin-Messages.Set-Starting-In-To-0"));
      }
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
