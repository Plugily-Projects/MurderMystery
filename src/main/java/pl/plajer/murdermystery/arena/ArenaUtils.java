/*
 * MurderMystery - Find the murderer, kill him and survive!
 * Copyright (C) 2019  Plajer's Lair - maintained by Plajer and contributors
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

package pl.plajer.murdermystery.arena;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.ItemLine;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.api.StatsStorage;
import pl.plajer.murdermystery.arena.role.Role;
import pl.plajer.murdermystery.handlers.ChatManager;
import pl.plajer.murdermystery.user.User;
import pl.plajer.murdermystery.utils.ItemPosition;
import pl.plajer.murdermystery.utils.MessageUtils;

/**
 * @author Plajer
 * <p>
 * Created at 13.03.2018
 */
public class ArenaUtils {

  private static Main plugin = JavaPlugin.getPlugin(Main.class);

  public static void addScore(User user, ScoreAction action, int amount) {
    String msg = ChatManager.colorMessage("In-Game.Messages.Bonus-Score");
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
    msg = StringUtils.replace(msg, "%action%", action.getAction());
    user.setStat(StatsStorage.StatisticType.LOCAL_SCORE, user.getStat(StatsStorage.StatisticType.LOCAL_SCORE) + action.getPoints());
    user.getPlayer().sendMessage(msg);
  }

  public static void updateInnocentLocator(Arena arena) {
    if (!arena.isMurdererLocatorReceived()) {
      ItemStack innocentLocator = new ItemStack(Material.COMPASS, 1);
      ItemMeta innocentMeta = innocentLocator.getItemMeta();
      innocentMeta.setDisplayName(ChatManager.colorMessage("In-Game.Innocent-Locator-Item-Name"));
      innocentLocator.setItemMeta(innocentMeta);
      ItemPosition.setItem(arena.getCharacter(Arena.CharacterType.MURDERER), ItemPosition.INNOCENTS_LOCATOR, innocentLocator);
      arena.setMurdererLocatorReceived(true);

      for (Player p : arena.getPlayersLeft()) {
        if (Role.isRole(Role.MURDERER, p)) {
          continue;
        }
        MessageUtils.sendTitle(p, ChatManager.colorMessage("In-Game.Watch-Out-Title"));
        MessageUtils.sendSubTitle(p, ChatManager.colorMessage("In-Game.Watch-Out-Subtitle"));
      }
    }
    for (Player p : arena.getPlayersLeft()) {
      if (Role.isRole(Role.MURDERER, p)) {
        continue;
      }
      arena.getCharacter(Arena.CharacterType.MURDERER).setCompassTarget(p.getLocation());
      break;
    }
  }

  private static void addBowLocator(Arena arena, Location loc) {
    ItemStack bowLocator = new ItemStack(Material.COMPASS, 1);
    ItemMeta bowMeta = bowLocator.getItemMeta();
    bowMeta.setDisplayName(ChatManager.colorMessage("In-Game.Bow-Locator-Item-Name"));
    bowLocator.setItemMeta(bowMeta);
    for (Player p : arena.getPlayersLeft()) {
      if (Role.isRole(Role.INNOCENT, p)) {
        ItemPosition.setItem(p, ItemPosition.BOW_LOCATOR, bowLocator);
        p.setCompassTarget(loc);
      }
    }
  }

  public static void dropBowAndAnnounce(Arena arena, Player victim) {
    if (arena.getPlayersLeft().size() > 1) {
      for (Player p : arena.getPlayers()) {
        MessageUtils.sendTitle(p, ChatManager.colorMessage("In-Game.Messages.Bow-Messages.Bow-Dropped-Title"));
      }
      for (Player p : arena.getPlayersLeft()) {
        MessageUtils.sendSubTitle(p, ChatManager.colorMessage("In-Game.Messages.Bow-Messages.Bow-Dropped-Subtitle"));
      }
    }

    Hologram hologram = HologramsAPI.createHologram(plugin, victim.getLocation().clone().add(0, 0.6, 0));
    ItemLine itemLine = hologram.appendItemLine(new ItemStack(Material.BOW, 1));

    itemLine.setPickupHandler(player -> {
      if (Role.isRole(Role.INNOCENT, player)) {
        player.playSound(player.getLocation(), Sound.BLOCK_LAVA_POP, 1F, 2F);
        hologram.delete();

        for (Player loopPlayer : arena.getPlayersLeft()) {
          if (Role.isRole(Role.INNOCENT, loopPlayer)) {
            ItemPosition.setItem(loopPlayer, ItemPosition.BOW_LOCATOR, new ItemStack(Material.AIR, 1));
          }
        }

        arena.setCharacter(Arena.CharacterType.FAKE_DETECTIVE, player);
        ItemPosition.setItem(player, ItemPosition.BOW, new ItemStack(Material.BOW, 1));
        ItemPosition.setItem(player, ItemPosition.INFINITE_ARROWS, new ItemStack(Material.ARROW, 64));
        ChatManager.broadcast(arena, ChatManager.colorMessage("In-Game.Messages.Bow-Messages.Pickup-Bow-Message"));
      }
    });
    arena.setBowHologram(hologram);
    addBowLocator(arena, hologram.getLocation());
  }

  public static boolean areInSameArena(Player one, Player two) {
    if (ArenaRegistry.getArena(one) == null || ArenaRegistry.getArena(two) == null) {
      return false;
    }
    return ArenaRegistry.getArena(one).equals(ArenaRegistry.getArena(two));
  }

  public static void hidePlayer(Player p, Arena arena) {
    for (Player player : arena.getPlayers()) {
      player.hidePlayer(p);
    }
  }

  public static void showPlayer(Player p, Arena arena) {
    for (Player player : arena.getPlayers()) {
      player.showPlayer(p);
    }
  }

  public static void hidePlayersOutsideTheGame(Player player, Arena arena) {
    for (Player players : plugin.getServer().getOnlinePlayers()) {
      if (arena.getPlayers().contains(players)) {
        continue;
      }
      player.hidePlayer(players);
      players.hidePlayer(player);
    }
  }

  public enum ScoreAction {
    KILL_PLAYER(100, ChatManager.colorMessage("In-Game.Messages.Score-Actions.Kill-Player")), KILL_MURDERER(200, ChatManager.colorMessage("In-Game.Messages.Score-Actions.Kill-Murderer")),
    GOLD_PICKUP(15, ChatManager.colorMessage("In-Game.Messages.Score-Actions.Gold-Pickup")), SURVIVE_TIME(150, ChatManager.colorMessage("In-Game.Messages.Score-Actions.Survive")),
    SURVIVE_GAME(200, ChatManager.colorMessage("In-Game.Messages.Score-Actions.Survive-Till-End")), WIN_GAME(100, ChatManager.colorMessage("In-Game.Messages.Score-Actions.Win-Game")),
    DETECTIVE_WIN_GAME(0, ChatManager.colorMessage("In-Game.Messages.Score-Actions.Detective-Reward")), INNOCENT_KILL(-100, ChatManager.colorMessage("In-Game.Messages.Score-Actions.Innocent-Kill"));

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
