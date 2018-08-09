/*
 * Village Defense 3 - Protect villagers from hordes of zombies
 * Copyright (C) 2018  Plajer's Lair - maintained by Plajer
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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.golde.bukkit.corpsereborn.CorpseAPI.CorpseAPI;
import org.golde.bukkit.corpsereborn.nms.Corpses;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.handlers.ChatManager;
import pl.plajer.murdermystery.utils.MessageUtils;

/**
 * @author Plajer
 * <p>
 * Created at 13.03.2018
 */
public class ArenaUtils {

  private static Main plugin = JavaPlugin.getPlugin(Main.class);

  public static void updateInnocentLocator(Arena arena){
    if(!arena.isMurdererLocatorReceived()) {
      ItemStack innocentLocator = new ItemStack(Material.COMPASS, 1);
      ItemMeta innocentMeta = innocentLocator.getItemMeta();
      innocentMeta.setDisplayName(ChatManager.colorMessage("In-Game.Innocent-Locator-Item-Name"));
      innocentLocator.setItemMeta(innocentMeta);
      Bukkit.getPlayer(arena.getMurderer()).getInventory().setItem(4, innocentLocator);
      arena.setMurdererLocatorReceived(true);

      for(Player p : arena.getPlayersLeft()){
        if(p.getUniqueId() == arena.getMurderer()) continue;
        MessageUtils.sendTitle(p, ChatManager.colorMessage("In-Game.Watch-Out-Title"), 5, 40, 5);
        MessageUtils.sendTitle(p, ChatManager.colorMessage("In-Game.Watch-Out-Subtitle"), 5, 40, 5);
      }
    }
    for(Player p : arena.getPlayersLeft()){
      if(p.getUniqueId() == arena.getMurderer()) continue;
      Bukkit.getPlayer(arena.getMurderer()).setCompassTarget(p.getLocation());
      break;
    }
  }

  private static void addBowLocator(Arena arena, Location loc){
    ItemStack bowLocator = new ItemStack(Material.COMPASS, 1);
    ItemMeta bowMeta = bowLocator.getItemMeta();
    bowMeta.setDisplayName(ChatManager.colorMessage("In-Game.Bow-Locator-Item-Name"));
    bowLocator.setItemMeta(bowMeta);
    for(Player p : arena.getPlayersLeft()){
      if(isRole(Role.INNOCENT, p)){
        p.getInventory().setItem(4, bowLocator);
        p.setCompassTarget(loc);
      }
    }
  }

  public static void dropBowAndAnnounce(Arena arena, Player victim) {
    for (Player p : arena.getPlayers()) {
      MessageUtils.sendTitle(p, ChatManager.colorMessage("In-Game.Messages.Bow-Messages.Bow-Dropped-Title"), 5, 40, 5);
    }
    for (Player p : arena.getPlayersLeft()) {
      MessageUtils.sendSubTitle(p, ChatManager.colorMessage("In-Game.Messages.Bow-Messages.Bow-Dropped-Subtitle"), 5, 40, 5);
    }

    Hologram hologram = HologramsAPI.createHologram(plugin, victim.getLocation().clone().add(0, 0.5, 0));
    ItemLine itemLine = hologram.appendItemLine(new ItemStack(Material.BOW, 1));

    itemLine.setPickupHandler(player -> {
      if (isRole(Role.INNOCENT, player)) {
        player.playSound(player.getLocation(), Sound.BLOCK_LAVA_POP, 1F, 2F);
        hologram.delete();

        for(Player loopPlayer : arena.getPlayersLeft()){
          if(isRole(Role.INNOCENT, loopPlayer)) {
            loopPlayer.getInventory().setItem(4, new ItemStack(Material.AIR, 1));
          }
        }

        arena.setFakeDetective(victim.getUniqueId());
        victim.getInventory().setItem(0, new ItemStack(Material.BOW, 1));
        victim.getInventory().setItem(9, new ItemStack(Material.ARROW, 64));
        for (Player loopPlayer : arena.getPlayers()) {
          loopPlayer.sendMessage(ChatManager.colorMessage("In-Game.Messages.Bow-Messages.Pickup-Bow-Message"));
        }
      }
    });
    arena.setBowHologram(hologram);
    addBowLocator(arena, hologram.getLocation());
  }

  public static void spawnCorpse(Player p, Arena arena) {
    Corpses.CorpseData corpse = CorpseAPI.spawnCorpse(p, p.getLocation());
    Hologram hologram = HologramsAPI.createHologram(plugin, p.getLocation().clone().add(0, 1.5, 0));
    hologram.appendTextLine(ChatManager.colorMessage("In-Game.Messages.Corpse-Last-Words").replace("%player%", p.getName()));
    //todo multi last words
    hologram.appendTextLine(ChatManager.colorMessage("In-Game.Messages.Last-Words.Default"));
    arena.addCorpse(new ArenaCorpse(hologram, corpse));
  }

  public static boolean isRole(Role role, Player p) {
    Arena arena = ArenaRegistry.getArena(p);
    if (arena == null) {
      return false;
    }
    switch (role) {
      case DETECTIVE:
        return arena.getDetective() == p.getUniqueId();
      case FAKE_DETECTIVE:
        return arena.getFakeDetective() != null && arena.getFakeDetective() == p.getUniqueId();
      case MURDERER:
        return arena.getMurderer() == p.getUniqueId();
      case ANY_DETECTIVE:
        return arena.getDetective() == p.getUniqueId() || (arena.getFakeDetective() != null && arena.getFakeDetective() == p.getUniqueId());
      case INNOCENT:
        return arena.getDetective() != p.getUniqueId() && (arena.getFakeDetective() != null && arena.getFakeDetective() != p.getUniqueId()) && arena.getMurderer() != p.getUniqueId();
      default:
        return false;
    }
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

  public enum Role {
    DETECTIVE, FAKE_DETECTIVE, MURDERER, ANY_DETECTIVE, INNOCENT
  }

}
