/*
 * MurderMystery - Find the murderer, kill him and survive!
 * Copyright (C) 2019  Plajer's Lair - maintained by Tigerpanzer_02, Plajer and contributors
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

package pl.plajer.murdermystery.events;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import pl.plajer.murdermystery.ConfigPreferences;
import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.api.StatsStorage;
import pl.plajer.murdermystery.arena.Arena;
import pl.plajer.murdermystery.arena.ArenaManager;
import pl.plajer.murdermystery.arena.ArenaRegistry;
import pl.plajer.murdermystery.arena.ArenaUtils;
import pl.plajer.murdermystery.arena.role.Role;
import pl.plajer.murdermystery.handlers.ChatManager;
import pl.plajer.murdermystery.handlers.items.SpecialItemManager;
import pl.plajer.murdermystery.user.User;
import pl.plajer.murdermystery.utils.Utils;
import pl.plajerlair.commonsbox.minecraft.compat.XMaterial;

/**
 * @author Plajer
 * <p>
 * Created at 05.08.2018
 */
public class Events implements Listener {

  private Main plugin;

  public Events(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void onItemSwap(PlayerSwapHandItemsEvent e) {
    if (ArenaRegistry.isInArena(e.getPlayer())) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onDrop(PlayerDropItemEvent event) {
    Arena arena = ArenaRegistry.getArena(event.getPlayer());
    if (arena == null) {
      return;
    }
    event.setCancelled(true);
  }

  @EventHandler
  public void onSwordThrow(PlayerInteractEvent e) {
    Arena arena = ArenaRegistry.getArena(e.getPlayer());
    if (arena == null) {
      return;
    }
    if (!Role.isRole(Role.MURDERER, e.getPlayer())) {
      return;
    }
    if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.PHYSICAL) {
      return;
    }
    final Player attacker = e.getPlayer();
    final User attackerUser = plugin.getUserManager().getUser(attacker);
    //todo not hardcoded!
    if (attacker.getInventory().getItemInMainHand().getType() != Material.IRON_SWORD) {
      return;
    }
    if (attackerUser.getCooldown("sword_shoot") > 0) {
      return;
    }
    attackerUser.setCooldown("sword_shoot", 5);
    attacker.setCooldown(Material.IRON_SWORD, 20);
    final ArmorStand stand = (ArmorStand) attacker.getWorld().spawnEntity(attacker.getLocation(), EntityType.ARMOR_STAND);
    stand.setVisible(false);
    stand.setInvulnerable(true);
    stand.setItemInHand(new ItemStack(Material.IRON_SWORD, 1));
    stand.setRightArmPose(new EulerAngle(Math.toRadians(350.0), Math.toRadians(attacker.getLocation().getPitch() * -1.0), Math.toRadians(90.0)));
    stand.setCollidable(false);
    stand.setSilent(true);
    swordFlyTask(arena, attacker, attackerUser, stand);
    Utils.applyActionBarCooldown(attacker, 5);
  }

  private void swordFlyTask(Arena arena, Player attacker, User attackerUser, ArmorStand stand) {
    new BukkitRunnable() {
      double posModifier = 0;
      Location loc = attacker.getLocation();
      Vector direction = loc.getDirection().normalize();

      @Override
      public void run() {
        posModifier += plugin.getConfig().getDouble("Murderer-Sword-Speed", 0.65);
        double x = direction.getX() * posModifier;
        double y = direction.getY() * posModifier + 0.65;
        double z = direction.getZ() * posModifier;
        loc.add(x, y, z);
        stand.teleport(loc);
        for (Entity en : loc.getChunk().getEntities()) {
          if (!(en instanceof Player)) {
            continue;
          }
          Player victim = (Player) en;
          if (ArenaRegistry.isInArena(victim) && plugin.getUserManager().getUser(victim).isSpectator()) {
            continue;
          }
          if (victim.getLocation().distance(loc) < 1.0 && !victim.equals(attacker)) {
            killBySword(arena, attackerUser, victim);
          }
        }
        loc.subtract(x, y, z);
        if (posModifier > 20 || stand.getLocation().add(0, 1, 0).getBlock().getType().isSolid()) {
          this.cancel();
          stand.remove();
        }
      }
    }.runTaskTimer(plugin, 0, 1);
  }

  private void killBySword(Arena arena, User attackerUser, Player victim) {
    victim.damage(100.0);
    victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_DEATH, 50, 1);
    victim.sendTitle(ChatManager.colorMessage("In-Game.Messages.Game-End-Messages.Titles.Died"),
      ChatManager.colorMessage("In-Game.Messages.Game-End-Messages.Subtitles.Murderer-Killed-You"), 5, 40, 5);
    attackerUser.addStat(StatsStorage.StatisticType.LOCAL_KILLS, 1);
    attackerUser.addStat(StatsStorage.StatisticType.KILLS, 1);
    ArenaUtils.addScore(attackerUser, ArenaUtils.ScoreAction.KILL_PLAYER, 0);
    if (Role.isRole(Role.ANY_DETECTIVE, victim)) {
      if (Role.isRole(Role.FAKE_DETECTIVE, victim)) {
        arena.setCharacter(Arena.CharacterType.FAKE_DETECTIVE, null);
      }
      ArenaUtils.dropBowAndAnnounce(arena, victim);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onCommandExecute(PlayerCommandPreprocessEvent event) {
    Arena arena = ArenaRegistry.getArena(event.getPlayer());
    if (arena == null) {
      return;
    }
    if (!plugin.getConfig().getBoolean("Block-Commands-In-Game", true)) {
      return;
    }
    for (String msg : plugin.getConfig().getStringList("Whitelisted-Commands")) {
      if (event.getMessage().contains(msg)) {
        return;
      }
    }
    if (event.getPlayer().isOp() || event.getPlayer().hasPermission("murdermystery.admin") || event.getPlayer().hasPermission("murdermystery.command.bypass")) {
      return;
    }
    if (event.getMessage().startsWith("/mm") || event.getMessage().startsWith("/murdermystery")
      || event.getMessage().startsWith("/murdermysteryadmin") || event.getMessage().contains("leave")
      || event.getMessage().contains("stats") || event.getMessage().startsWith("/mma")) {
      return;
    }
    event.setCancelled(true);
    event.getPlayer().sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("In-Game.Only-Command-Ingame-Is-Leave"));
  }

  @EventHandler
  public void onInGameInteract(PlayerInteractEvent e) {
    Arena arena = ArenaRegistry.getArena(e.getPlayer());
    if (arena == null || e.getClickedBlock() == null) {
      return;
    }
    if (e.getClickedBlock().getType() == XMaterial.PAINTING.parseMaterial() || e.getClickedBlock().getType() == XMaterial.FLOWER_POT.parseMaterial()) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onLeave(PlayerInteractEvent event) {
    if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.PHYSICAL) {
      return;
    }
    Arena arena = ArenaRegistry.getArena(event.getPlayer());
    ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
    if (arena == null || !Utils.isNamed(itemStack)) {
      return;
    }
    String key = SpecialItemManager.getRelatedSpecialItem(itemStack);
    if (key == null) {
      return;
    }
    if (SpecialItemManager.getRelatedSpecialItem(itemStack).equalsIgnoreCase("Leave")) {
      event.setCancelled(true);
      if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
        plugin.getBungeeManager().connectToHub(event.getPlayer());
      } else {
        ArenaManager.leaveAttempt(event.getPlayer(), arena);
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onFoodLevelChange(FoodLevelChangeEvent event) {
    if (event.getEntity().getType() == EntityType.PLAYER && ArenaRegistry.isInArena((Player) event.getEntity())) {
      event.setFoodLevel(20);
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  //highest priority to fully protect our game (i didn't set it because my test server was destroyed, n-no......)
  public void onBlockBreakEvent(BlockBreakEvent event) {
    if (!ArenaRegistry.isInArena(event.getPlayer())) {
      return;
    }
    event.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.HIGH)
  //highest priority to fully protect our game (i didn't set it because my test server was destroyed, n-no......)
  public void onBuild(BlockPlaceEvent event) {
    if (!ArenaRegistry.isInArena(event.getPlayer())) {
      return;
    }
    event.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.HIGH)
  //highest priority to fully protect our game (i didn't set it because my test server was destroyed, n-no......)
  public void onHangingBreakEvent(HangingBreakByEntityEvent event) {
    if (event.getEntity() instanceof ItemFrame || event.getEntity() instanceof Painting) {
      if (event.getRemover() instanceof Player && ArenaRegistry.isInArena((Player) event.getRemover())) {
        event.setCancelled(true);
        return;
      }
      if (!(event.getRemover() instanceof Arrow)) {
        return;
      }
      Arrow arrow = (Arrow) event.getRemover();
      if (arrow.getShooter() instanceof Player && ArenaRegistry.isInArena((Player) arrow.getShooter())) {
        event.setCancelled(true);
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onArmorStandDestroy(EntityDamageByEntityEvent e) {
    if (!(e.getEntity() instanceof LivingEntity)) {
      return;
    }
    final LivingEntity livingEntity = (LivingEntity) e.getEntity();
    if (!livingEntity.getType().equals(EntityType.ARMOR_STAND)) {
      return;
    }
    if (e.getDamager() instanceof Player && ArenaRegistry.isInArena((Player) e.getDamager())) {
      e.setCancelled(true);
    } else if (e.getDamager() instanceof Arrow) {
      Arrow arrow = (Arrow) e.getDamager();
      if (arrow.getShooter() instanceof Player && ArenaRegistry.isInArena((Player) arrow.getShooter())) {
        e.setCancelled(true);
        return;
      }
      e.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onInteractWithArmorStand(PlayerArmorStandManipulateEvent event) {
    if (ArenaRegistry.isInArena(event.getPlayer())) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onCraft(PlayerInteractEvent event) {
    if (!ArenaRegistry.isInArena(event.getPlayer())) {
      return;
    }
    if (event.getPlayer().getTargetBlock(null, 7).getType() == XMaterial.CRAFTING_TABLE.parseMaterial()) {
      event.setCancelled(true);
    }
  }

}
