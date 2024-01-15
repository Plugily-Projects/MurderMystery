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

package plugily.projects.murdermystery.events;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import plugily.projects.minigamesbox.classic.user.User;
import plugily.projects.minigamesbox.classic.utils.version.ServerVersion;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XMaterial;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XSound;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.arena.ArenaUtils;
import plugily.projects.murdermystery.arena.role.Role;

/**
 * @author Plajer
 * <p>Created at 05.08.2018
 */
public class PluginEvents implements Listener {

  private final Main plugin;

  public PluginEvents(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void onSwordThrow(PlayerInteractEvent event) {
    if(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.PHYSICAL) {
      return;
    }
    Player attacker = event.getPlayer();
    Arena arena = plugin.getArenaRegistry().getArena(attacker);
    if(arena == null) {
      return;
    }

    User attackerUser = plugin.getUserManager().getUser(attacker);
    if(!Role.isRole(Role.MURDERER, attackerUser, arena)) {
      return;
    }

    ItemStack murdererSword = plugin.getSwordSkinManager().getMurdererSword(attacker);

    if(murdererSword == null) {
      return;
    }

    if(VersionUtils.getItemInHand(attacker).getType() != murdererSword.getType()) {
      return;
    }
    if(attackerUser.getCooldown("sword_shoot") > 0) {
      return;
    }
    createFlyingSword(attacker, attackerUser);

    int swordFlyCooldown = plugin.getConfig().getInt("Sword.Cooldown.Fly", 5);
    if(swordFlyCooldown <= 0) {
      return;
    }
    attackerUser.setCooldown("sword_shoot", swordFlyCooldown);
    if(ServerVersion.Version.isCurrentLower(ServerVersion.Version.v1_11_R1)) {
      attackerUser.setCooldown("sword_attack", (plugin.getConfig().getInt("Sword.Cooldown.Attack", 1)));
    } else {
      VersionUtils.setMaterialCooldown(attacker ,plugin.getSwordSkinManager().getMurdererSword(attacker).getType(), 20 * (plugin.getConfig().getInt("Sword.Cooldown.Attack", 1)));
    }
    plugin.getBukkitHelper().applyActionBarCooldown(attacker, swordFlyCooldown);
  }

  private void createFlyingSword(Player attacker, User attackerUser) {
    Location loc = attacker.getLocation();
    Vector vec = loc.getDirection();
    vec.normalize().multiply(plugin.getConfig().getDouble("Sword.Speed", 0.65));
    Location standStart = plugin.getBukkitHelper().rotateAroundAxisY(new Vector(1.0D, 0.0D, 0.0D), loc.getYaw()).toLocation(attacker.getWorld()).add(loc);
    standStart.setYaw(loc.getYaw());
    ArmorStand stand = (ArmorStand) attacker.getWorld().spawnEntity(standStart, EntityType.ARMOR_STAND);
    stand.setVisible(false);
    if(ServerVersion.Version.isCurrentHigher(ServerVersion.Version.v1_8_R3)) {
      stand.setInvulnerable(true);
      stand.setSilent(true);
    }

    VersionUtils.setItemInHand(stand, plugin.getSwordSkinManager().getMurdererSword(attacker));

    stand.setRightArmPose(new EulerAngle(Math.toRadians(350.0), Math.toRadians(loc.getPitch() * -1.0), Math.toRadians(90.0)));
    VersionUtils.setCollidable(stand, false);

    stand.setGravity(false);
    stand.setRemoveWhenFarAway(true);

    if(ServerVersion.Version.isCurrentEqualOrHigher(ServerVersion.Version.v1_8_R3)) {
      stand.setMarker(true);
    }

    Location initialise = plugin.getBukkitHelper().rotateAroundAxisY(new Vector(-0.8D, 1.45D, 0.0D), loc.getYaw()).toLocation(attacker.getWorld()).add(standStart)
      .add(plugin.getBukkitHelper().rotateAroundAxisY(plugin.getBukkitHelper().rotateAroundAxisX(new Vector(0.0D, 0.0D, 1.0D), loc.getPitch()), loc.getYaw()));
    int maxRange = plugin.getConfig().getInt("Sword.Fly.Range", 20);
    double maxHitRange = plugin.getConfig().getDouble("Sword.Fly.Radius", 0.5);
    new BukkitRunnable() {
      @Override
      public void run() {
        VersionUtils.teleport(stand, standStart.add(vec));
        initialise.add(vec);
        initialise.getWorld().getNearbyEntities(initialise, maxHitRange, maxHitRange, maxHitRange)
          .forEach(entity -> {
            if(entity instanceof Player) {
              Player victim = (Player) entity;
              Arena arena = plugin.getArenaRegistry().getArena(victim);
              if(arena == null) {
                return;
              }
              if(!plugin.getUserManager().getUser(victim).isSpectator() && !victim.equals(attacker)) {
                killBySword(arena, attackerUser, victim);
                cancel();
                stand.remove();
              }
            }
          });
        if(loc.distance(initialise) > maxRange || initialise.getBlock().getType().isSolid()) {
          cancel();
          stand.remove();
        }
      }
    }.runTaskTimer(plugin, 0, 1);
  }

  private void killBySword(Arena arena, User attackerUser, Player victim) {
    Arena victimArena = plugin.getArenaRegistry().getArena(victim);
    if(arena == null) {
      return;
    }
    User user = plugin.getUserManager().getUser(victim);

    // check if victim is murderer
    if(Role.isRole(Role.MURDERER, user, victimArena)) {
      return;
    }
    XSound.ENTITY_PLAYER_DEATH.play(victim.getLocation(), 50, 1);
    victim.damage(100.0);
    attackerUser.adjustStatistic("LOCAL_KILLS", 1);
    attackerUser.adjustStatistic("KILLS", 1);
    arena.adjustContributorValue(Role.DETECTIVE, user, plugin.getRandom().nextInt(2));
    ArenaUtils.addScore(attackerUser, ArenaUtils.ScoreAction.KILL_PLAYER, 0);
    if(Role.isRole(Role.ANY_DETECTIVE, user, victimArena) && arena.lastAliveDetective()) {
      if(Role.isRole(Role.FAKE_DETECTIVE, user, victimArena)) {
        arena.setCharacter(Arena.CharacterType.FAKE_DETECTIVE, null);
      }
      ArenaUtils.dropBowAndAnnounce(arena, victim);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  // highest priority to fully protect our game
  public void onBlockBreak(BlockBreakEvent event) {
    Arena arena = plugin.getArenaRegistry().getArena(event.getPlayer());
    if(arena == null) {
      return;
    }
    event.setCancelled(true);
    if(event.getBlock().getType() != XMaterial.ARMOR_STAND.parseMaterial()) {
      return;
    }
    plugin.getHologramManager().getArmorStands().removeIf(armorStand -> {
      boolean isSameType = armorStand.getLocation().getBlock().getType() == event.getBlock().getType();
      if(isSameType) {
        armorStand.remove();
        armorStand.setCustomNameVisible(false);
      }
      return isSameType;
    });
  }

  @EventHandler(priority = EventPriority.HIGH)
  // highest priority to fully protect our game
  public void onBuild(BlockPlaceEvent event) {
    Arena arena = plugin.getArenaRegistry().getArena(event.getPlayer());
    if(arena == null) {
      return;
    }
    event.setCancelled(true);
  }
}
