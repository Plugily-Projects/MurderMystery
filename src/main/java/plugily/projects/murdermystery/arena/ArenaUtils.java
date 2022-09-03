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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import plugily.projects.minigamesbox.classic.arena.ArenaState;
import plugily.projects.minigamesbox.classic.arena.PluginArena;
import plugily.projects.minigamesbox.classic.arena.PluginArenaUtils;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.handlers.language.TitleBuilder;
import plugily.projects.minigamesbox.classic.user.User;
import plugily.projects.minigamesbox.classic.utils.hologram.ArmorStandHologram;
import plugily.projects.minigamesbox.classic.utils.misc.complement.ComplementAccessor;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XSound;
import plugily.projects.murdermystery.arena.role.Role;
import plugily.projects.murdermystery.utils.ItemPosition;

/**
 * @author Plajer
 * <p>Created at 13.03.2018
 */
public class ArenaUtils extends PluginArenaUtils {

  private ArenaUtils() {
    super();
  }

  public static void onMurdererDeath(Arena arena) {
    for(Player player : arena.getPlayers()) {
      VersionUtils.sendSubTitle(player, getPlugin().getLanguageManager().getLanguageMessage("In-Game.Messages.Game-End.Placeholders.Murderer.Stopped"), 5, 40, 5);
      User loopUser = getPlugin().getUserManager().getUser(player);
      if(Role.isRole(Role.INNOCENT, loopUser, arena)) {
        addScore(loopUser, ScoreAction.SURVIVE_GAME, 0);
      } else if(Role.isRole(Role.ANY_DETECTIVE, loopUser, arena)) {
        addScore(loopUser, ScoreAction.WIN_GAME, 0);
        addScore(loopUser, ScoreAction.DETECTIVE_WIN_GAME, 0);
      }
    }
    //we must call it ticks later due to instant respawn bug
    Bukkit.getScheduler().runTaskLater(getPlugin(), () -> getPlugin().getArenaManager().stopGame(false, arena), 10);
  }

  public static void updateInnocentLocator(Arena arena) {
    java.util.List<Player> list = arena.getPlayersLeft();

    if(!arena.isMurdererLocatorReceived()) {
      ItemStack innocentLocator = new ItemStack(Material.COMPASS, 1);
      ItemMeta innocentMeta = innocentLocator.getItemMeta();
      ComplementAccessor.getComplement()
          .setDisplayName(
              innocentMeta,
              new MessageBuilder("IN_GAME_MESSAGES_ARENA_LOCATOR_INNOCENT").asKey().build());
      innocentLocator.setItemMeta(innocentMeta);
      for(Player p : list) {
        if(arena.isMurderAlive(p)) {
          ItemPosition.setItem(getPlugin().getUserManager().getUser(p), ItemPosition.INNOCENTS_LOCATOR, innocentLocator);
        }
      }
      arena.setMurdererLocatorReceived(true);

      for(Player p : list) {
        if(Role.isRole(Role.MURDERER, getPlugin().getUserManager().getUser(p), arena)) {
          continue;
        }
        new TitleBuilder("IN_GAME_MESSAGES_ARENA_LOCATOR_WATCH_OUT")
            .asKey()
            .player(p)
            .arena(arena)
            .sendPlayer();
      }
    }

    for(Player p : list) {
      if(Role.isRole(Role.MURDERER, getPlugin().getUserManager().getUser(p), arena)) {
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

  public static void dropBowAndAnnounce(Arena arena, Player victim) {
    if(arena.getBowHologram() != null) {
      return;
    }
    new TitleBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_BOW_DROPPED").asKey().arena(arena).sendArena();

    ArmorStandHologram hologram =
        new ArmorStandHologram(victim.getLocation()).appendItem(new ItemStack(Material.BOW, 1));

    arena.setBowHologram(hologram);
    addBowLocator(arena, hologram.getLocation());
  }

  private static void addBowLocator(Arena arena, Location loc) {
    ItemStack bowLocator = new ItemStack(Material.COMPASS, 1);
    ItemMeta bowMeta = bowLocator.getItemMeta();
    ComplementAccessor.getComplement()
        .setDisplayName(
            bowMeta, new MessageBuilder("IN_GAME_MESSAGES_ARENA_LOCATOR_BOW").asKey().build());
    bowLocator.setItemMeta(bowMeta);
    for(Player p : arena.getPlayersLeft()) {
      User user = getPlugin().getUserManager().getUser(p);
      if(Role.isRole(Role.INNOCENT, user, arena)) {
        ItemPosition.setItem(user, ItemPosition.BOW_LOCATOR, bowLocator);
        p.setCompassTarget(loc);
      }
    }
  }

  public static void updateNameTagsVisibility(final Player p) {
    if(!getPlugin().getConfigPreferences().getOption("HIDE_NAMETAGS")) {
      return;
    }
    for(Player players : getPlugin().getServer().getOnlinePlayers()) {
      PluginArena arena = getPlugin().getArenaRegistry().getArena(players);
      if(arena == null) {
        continue;
      }
      VersionUtils.updateNameTagsVisibility(
          p, players, "MMHide", arena.getArenaState() != ArenaState.IN_GAME);
    }
  }

  public static void addScore(User user, ScoreAction action, int amount) {
    XSound.matchXSound(XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound())
        .play(user.getPlayer().getLocation(), 1F, 2F);

    if(action == ScoreAction.GOLD_PICKUP && amount > 1) {
      int score = action.points * amount;
      new MessageBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_SCORE_BONUS")
          .asKey()
          .player(user.getPlayer())
          .arena(user.getArena())
          .integer(score)
          .value(action.action)
          .sendPlayer();
      user.adjustStatistic("LOCAL_SCORE", score);
      return;
    }

    if(action == ScoreAction.DETECTIVE_WIN_GAME) {
      int innocents = 0;
      Arena arena = (Arena) user.getArena();

      for(Player p : arena.getPlayersLeft()) {
        if(Role.isRole(Role.INNOCENT, getPlugin().getUserManager().getUser(p), arena)) {
          innocents++;
        }
      }

      int overallInnocents = 100 * innocents;

      user.adjustStatistic("LOCAL_SCORE", overallInnocents);
      new MessageBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_SCORE_BONUS")
          .asKey()
          .player(user.getPlayer())
          .arena(user.getArena())
          .integer(overallInnocents)
          .value(action.action.replace("%amount%", Integer.toString(innocents)))
          .sendPlayer();
      return;
    }
    String msg =
        new MessageBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_SCORE_BONUS")
            .asKey()
            .player(user.getPlayer())
            .arena(user.getArena())
            .integer(action.points)
            .value(action.action)
            .build();

    if(action.points < 0) {
      msg = msg.replace("+", "");
    }

    user.adjustStatistic("LOCAL_SCORE", action.points);
    user.getPlayer().sendMessage(msg);
  }

  public enum ScoreAction {
    KILL_PLAYER(
        100,
        new MessageBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_SCORE_ACTION_KILL_PLAYER")
            .asKey()
            .build()),
    KILL_MURDERER(
        200,
        new MessageBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_SCORE_ACTION_KILL_MURDERER")
            .asKey()
            .build()),
    GOLD_PICKUP(
        15,
        new MessageBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_SCORE_ACTION_PICKUP_GOLD")
            .asKey()
            .build()),
    SURVIVE_TIME(
        150,
        new MessageBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_SCORE_ACTION_SURVIVING_TIME")
            .asKey()
            .build()),
    SURVIVE_GAME(
        200,
        new MessageBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_SCORE_ACTION_SURVIVING_END")
            .asKey()
            .build()),
    WIN_GAME(
        100, new MessageBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_SCORE_ACTION_WIN").asKey().build()),
    DETECTIVE_WIN_GAME(
        0,
        new MessageBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_SCORE_ACTION_DETECTIVE")
            .asKey()
            .build()),
    INNOCENT_KILL(
        -100,
        new MessageBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_SCORE_ACTION_KILL_INNOCENT")
            .asKey()
            .build());

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
