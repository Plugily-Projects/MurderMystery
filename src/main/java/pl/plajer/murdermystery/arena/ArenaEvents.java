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

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.api.StatsStorage;
import pl.plajer.murdermystery.arena.role.Role;
import pl.plajer.murdermystery.handlers.ChatManager;
import pl.plajer.murdermystery.handlers.items.SpecialItemManager;
import pl.plajer.murdermystery.user.User;
import pl.plajer.murdermystery.user.UserManager;
import pl.plajer.murdermystery.utils.MessageUtils;
import pl.plajer.murdermystery.utils.Utils;
import pl.plajerlair.core.services.exception.ReportedException;
import pl.plajerlair.core.utils.ItemBuilder;
import pl.plajerlair.core.utils.XMaterial;

/**
 * @author Plajer
 * <p>
 * Created at 13.03.2018
 */
public class ArenaEvents implements Listener {

  private Main plugin;

  public ArenaEvents(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void onFallDamage(EntityDamageEvent e) {
    if (!(e.getEntity() instanceof Player)) {
      return;
    }
    if (!ArenaRegistry.isInArena((Player) e.getEntity())) {
      return;
    }
    if (e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onBowShot(EntityShootBowEvent e) {
    try {
      if (!(e.getEntity() instanceof Player)) {
        return;
      }
      if (!Role.isRole(Role.ANY_DETECTIVE, (Player) e.getEntity())) {
        return;
      }
      User user = UserManager.getUser(e.getEntity().getUniqueId());
      if (user.getCooldown("bow_shot") == 0) {
        user.setCooldown("bow_shot", 5);
        Player player = (Player) e.getEntity();
        Utils.applyActionBarCooldown(player, 5);
      } else {
        e.setCancelled(true);
      }
    } catch (Exception ex) {
      new ReportedException(plugin, ex);
    }
  }

  @EventHandler
  public void onArrowPickup(PlayerPickupArrowEvent e) {
    try {
      if (ArenaRegistry.isInArena(e.getPlayer())) {
        e.setCancelled(true);
        e.getArrow().remove();
      }
    } catch (Exception ex) {
      new ReportedException(plugin, ex);
    }
  }

  @EventHandler
  public void onItemPickup(PlayerPickupItemEvent e) {
    try {
      if (e.getItem().getItemStack().getType() != Material.GOLD_INGOT) {
        return;
      }
      Arena arena = ArenaRegistry.getArena(e.getPlayer());
      if (arena == null) {
        return;
      }
      e.setCancelled(true);
      User user = UserManager.getUser(e.getPlayer().getUniqueId());
      if (user.isSpectator() || arena.getArenaState() != ArenaState.IN_GAME) {
        return;
      }
      e.getItem().remove();
      e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_LAVA_POP, 1, 1);
      arena.getGoldSpawned().remove(e.getItem());
      ItemStack stack = e.getPlayer().getInventory().getItem(8);
      if (stack == null) {
        stack = new ItemStack(Material.GOLD_INGOT, 1);
      } else {
        stack.setAmount(stack.getAmount() + 1);
      }
      e.getPlayer().getInventory().setItem(8, stack);
      user.addStat(StatsStorage.StatisticType.LOCAL_GOLD, 1);
      ArenaUtils.addScore(user, ArenaUtils.ScoreAction.GOLD_PICKUP);
      e.getPlayer().sendMessage(ChatManager.colorMessage("In-Game.Messages.Picked-Up-Gold"));

      if (Role.isRole(Role.ANY_DETECTIVE, e.getPlayer())) {
        return;
      }

      if (user.getStat(StatsStorage.StatisticType.LOCAL_GOLD) == 10) {
        user.setStat(StatsStorage.StatisticType.LOCAL_GOLD, 0);
        MessageUtils.sendTitle(e.getPlayer(), ChatManager.colorMessage("In-Game.Messages.Bow-Messages.Bow-Shot-For-Gold"), 5, 40, 5);
        MessageUtils.sendSubTitle(e.getPlayer(), ChatManager.colorMessage("In-Game.Messages.Bow-Messages.Bow-Shot-Subtitle"), 5, 40, 5);
        e.getPlayer().getInventory().setItem(0, new ItemStack(Material.BOW, 1));
        ItemStack arrow = e.getPlayer().getInventory().getItem(2);
        if (arrow == null) {
          arrow = new ItemStack(Material.ARROW, 1);
        } else {
          arrow.setAmount(arrow.getAmount() + 1);
        }
        if (Role.isRole(Role.MURDERER, e.getPlayer())) {
          e.getPlayer().getInventory().setItem(2, arrow);
        } else {
          e.getPlayer().getInventory().setItem(1, arrow);
        }
        e.getPlayer().getInventory().setItem(8, new ItemStack(Material.GOLD_INGOT, 0));
      }
    } catch (Exception ex) {
      new ReportedException(plugin, ex);
    }
  }

  @EventHandler
  public void onMurdererDamage(EntityDamageByEntityEvent e) {
    try {
      if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof Player)) {
        return;
      }
      Player attacker = (Player) e.getDamager();
      Player victim = (Player) e.getEntity();
      if (!ArenaUtils.areInSameArena(attacker, victim)) {
        return;
      }

      //better check this for future even if anyone else cannot use sword
      if (!Role.isRole(Role.MURDERER, attacker)) {
        return;
      }

      Arena arena = ArenaRegistry.getArena(attacker);
      //todo support for skins later
      if (attacker.getInventory().getItemInMainHand().getType() != Material.IRON_SWORD) {
        e.setCancelled(true);
        return;
      }
      if (arena.getMurderer() != attacker.getUniqueId()) {
        return;
      }

      User user = UserManager.getUser(attacker.getUniqueId());
      if (Role.isRole(Role.MURDERER, victim)) {
        plugin.getRewardsHandler().performMurdererKillRewards(attacker, victim);
      } else if (Role.isRole(Role.ANY_DETECTIVE, victim)) {
        plugin.getRewardsHandler().performDetectiveKillRewards(attacker, victim);
      }

      //todo god damage override add
      victim.damage(100.0);
      victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_DEATH, 50, 1);
      user.addStat(StatsStorage.StatisticType.LOCAL_KILLS, 1);
      ArenaUtils.addScore(user, ArenaUtils.ScoreAction.KILL_PLAYER);

      arena.getPlayersLeft().remove(victim);
      if (Role.isRole(Role.ANY_DETECTIVE, victim)) {
        //if already true, no effect is done :)
        arena.setDetectiveDead(true);
        if (Role.isRole(Role.FAKE_DETECTIVE, victim)) {
          arena.setFakeDetective(null);
        }
        ArenaUtils.dropBowAndAnnounce(arena, victim);
        plugin.getCorpseHandler().spawnCorpse(victim, arena);
      }
    } catch (Exception ex) {
      new ReportedException(plugin, ex);
    }
  }

  @EventHandler
  public void onArrowDamage(EntityDamageByEntityEvent e) {
    try {
      if (!(e.getDamager() instanceof Arrow) || !(e.getEntity() instanceof Player)) {
        return;
      }
      Player attacker = (Player) ((Arrow) e.getDamager()).getShooter();
      Player victim = (Player) e.getEntity();
      if (!ArenaUtils.areInSameArena(attacker, victim)) {
        return;
      }
      Arena arena = ArenaRegistry.getArena(attacker);
      //we won't allow to suicide
      if (attacker.equals(victim)) {
        e.setCancelled(true);
        return;
      }

      //todo god override
      victim.damage(100.0);
      victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_DEATH, 50, 1);
      User user = UserManager.getUser(attacker.getUniqueId());

      if (Role.isRole(Role.MURDERER, attacker)) {
        user.addStat(StatsStorage.StatisticType.LOCAL_KILLS, 1);
        ArenaUtils.addScore(user, ArenaUtils.ScoreAction.KILL_PLAYER);
      }

      plugin.getCorpseHandler().spawnCorpse(victim, arena);
      MessageUtils.sendTitle(victim, ChatManager.colorMessage("In-Game.Messages.Game-End-Messages.Titles.Died"), 5, 40, 5);

      if (Role.isRole(Role.MURDERER, victim)) {
        for (Player p : arena.getPlayers()) {
          MessageUtils.sendTitle(p, ChatManager.colorMessage("In-Game.Messages.Game-End-Messages.Titles.Win"), 5, 40, 5);
          MessageUtils.sendSubTitle(p, ChatManager.colorMessage("In-Game.Messages.Game-End-Messages.Subtitles.Murderer-Stopped"), 5, 40, 5);
          if (Role.isRole(Role.MURDERER, p)) {
            MessageUtils.sendTitle(p, ChatManager.colorMessage("In-Game.Messages.Game-End-Messages.Titles.Lose"), 5, 40, 5);
          }
          if (Role.isRole(Role.INNOCENT, p)) {
            ArenaUtils.addScore(UserManager.getUser(p.getUniqueId()), ArenaUtils.ScoreAction.SURVIVE_GAME);
          } else if (Role.isRole(Role.ANY_DETECTIVE, p)) {
            ArenaUtils.addScore(UserManager.getUser(p.getUniqueId()), ArenaUtils.ScoreAction.WIN_GAME);
            ArenaUtils.addScore(UserManager.getUser(p.getUniqueId()), ArenaUtils.ScoreAction.DETECTIVE_WIN_GAME);
          }
        }
        ArenaUtils.addScore(UserManager.getUser(attacker.getUniqueId()), ArenaUtils.ScoreAction.KILL_MURDERER);
        arena.setHero(attacker.getUniqueId());
        ArenaManager.stopGame(false, arena);
        arena.setArenaState(ArenaState.ENDING);
        arena.setTimer(5);
        MessageUtils.sendTitle(victim, ChatManager.colorMessage("In-Game.Messages.Game-End-Messages.Titles.Lose"), 5, 40, 5);
        MessageUtils.sendSubTitle(victim, ChatManager.colorMessage("In-Game.Messages.Game-End-Messages.Subtitles.Murderer-Stopped"), 5, 40, 5);
      } else if (Role.isRole(Role.ANY_DETECTIVE, victim)) {
        ArenaUtils.dropBowAndAnnounce(arena, victim);
      } else if (Role.isRole(Role.INNOCENT, victim)) {
        if (Role.isRole(Role.MURDERER, attacker)) {
          MessageUtils.sendSubTitle(victim, ChatManager.colorMessage("In-Game.Messages.Game-End-Messages.Subtitles.Murderer-Killed-You"), 5, 40, 5);
        } else {
          MessageUtils.sendSubTitle(victim, ChatManager.colorMessage("In-Game.Messages.Game-End-Messages.Subtitles.Player-Killed-You"), 5, 40, 5);
        }

        //if else, murderer killed, so don't kill him :)
        if (Role.isRole(Role.ANY_DETECTIVE, attacker) || Role.isRole(Role.INNOCENT, attacker)) {
          MessageUtils.sendTitle(attacker, ChatManager.colorMessage("In-Game.Messages.Game-End-Messages.Titles.Died"), 5, 40, 5);
          MessageUtils.sendSubTitle(attacker, ChatManager.colorMessage("In-Game.Messages.Game-End-Messages.Subtitles.Killed-Innocent"), 5, 40, 5);
          attacker.damage(100.0);
          ArenaUtils.addScore(UserManager.getUser(attacker.getUniqueId()), ArenaUtils.ScoreAction.INNOCENT_KILL);
          plugin.getCorpseHandler().spawnCorpse(attacker, arena);
          plugin.getRewardsHandler().performDetectiveKillRewards(attacker, victim);

          if (Role.isRole(Role.ANY_DETECTIVE, attacker)) {
            arena.setDetectiveDead(true);
            if (Role.isRole(Role.FAKE_DETECTIVE, attacker)) {
              arena.setFakeDetective(null);
            }
            ArenaUtils.dropBowAndAnnounce(arena, victim);
          }
        }
      }
    } catch (Exception ex) {
      new ReportedException(plugin, ex);
    }
  }

  //todo environmental death
  @EventHandler(priority = EventPriority.HIGH)
  public void onPlayerDie(PlayerDeathEvent e) {
    try {
      Arena arena = ArenaRegistry.getArena(e.getEntity());
      if (arena == null) {
        return;
      }
      if (e.getEntity().isDead()) {
        e.getEntity().setHealth(e.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
      }
      Location loc = e.getEntity().getLocation();
      e.setDeathMessage("");
      e.getDrops().clear();
      e.setDroppedExp(0);
      e.getEntity().spigot().respawn();
      e.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 3 * 20, 0));
      Player player = e.getEntity();
      if (arena.getArenaState() == ArenaState.STARTING) {
        player.teleport(loc);
        return;
      } else if (arena.getArenaState() == ArenaState.ENDING || arena.getArenaState() == ArenaState.RESTARTING) {
        player.getInventory().clear();
        player.setFlying(false);
        player.setAllowFlight(false);
        User user = UserManager.getUser(player.getUniqueId());
        user.setStat(StatsStorage.StatisticType.LOCAL_GOLD, 0);
        player.teleport(arena.getEndLocation());
        return;
      }
      User user = UserManager.getUser(player.getUniqueId());
      arena.addStat(player, StatsStorage.StatisticType.DEATHS);
      player.teleport(loc);
      user.setSpectator(true);
      player.setGameMode(GameMode.SURVIVAL);
      user.setStat(StatsStorage.StatisticType.LOCAL_GOLD, 0);
      ArenaUtils.hidePlayer(player, arena);
      player.setAllowFlight(true);
      player.setFlying(true);
      player.getInventory().clear();
      arena.getPlayersLeft().remove(player);
      ChatManager.broadcastAction(arena, player, ChatManager.ActionType.DEATH);

      player.getInventory().setItem(0, new ItemBuilder(XMaterial.COMPASS.parseItem()).name(ChatManager.colorMessage("In-Game.Spectator.Spectator-Item-Name")).build());
      player.getInventory().setItem(4, new ItemBuilder(XMaterial.COMPARATOR.parseItem()).name(ChatManager.colorMessage("In-Game.Spectator.Settings-Menu.Item-Name")).build());
      player.getInventory().setItem(8, SpecialItemManager.getSpecialItem("Leave").getItemStack());
    } catch (Exception ex) {
      new ReportedException(plugin, ex);
    }
  }

  @EventHandler
  public void onRespawn(PlayerRespawnEvent e) {
    try {
      Arena arena = ArenaRegistry.getArena(e.getPlayer());
      if (arena == null) {
        return;
      }
      if (arena.getPlayers().contains(e.getPlayer())) {
        Player player = e.getPlayer();
        User user = UserManager.getUser(player.getUniqueId());
        player.setAllowFlight(true);
        player.setFlying(true);
        user.setSpectator(true);
        player.setGameMode(GameMode.SURVIVAL);
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        user.setStat(StatsStorage.StatisticType.LOCAL_GOLD, 0);
      }
    } catch (Exception ex) {
      new ReportedException(plugin, ex);
    }
  }

  @EventHandler
  public void onItemMove(InventoryClickEvent e) {
    if (e.getWhoClicked() instanceof Player) {
      if (ArenaRegistry.getArena((Player) e.getWhoClicked()) != null) {
        e.setResult(Event.Result.DENY);
      }
    }
  }

}
