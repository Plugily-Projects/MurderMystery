/*
 * Murder Mystery is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Murder Mystery is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Murder Mystery.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.plajer.murdermystery.events.spectator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.arena.Arena;
import pl.plajer.murdermystery.arena.ArenaRegistry;
import pl.plajer.murdermystery.arena.ArenaState;
import pl.plajer.murdermystery.user.User;
import pl.plajer.murdermystery.user.UserManager;

/**
 * @author Plajer
 * <p>
 * Created at 05.08.2018
 */
public class SpectatorEvents implements Listener {

  private Main plugin;

  public SpectatorEvents(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onSpectatorTarget(EntityTargetEvent e) {
    if (!(e.getTarget() instanceof Player)) {
      return;
    }
    if (UserManager.getUser(e.getTarget().getUniqueId()).isSpectator()) {
      e.setCancelled(true);
      e.setTarget(null);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onSpectatorTarget(EntityTargetLivingEntityEvent e) {
    if (!(e.getTarget() instanceof Player)) {
      return;
    }
    if (UserManager.getUser(e.getTarget().getUniqueId()).isSpectator()) {
      e.setCancelled(true);
      e.setTarget(null);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onBlockPlace(BlockPlaceEvent event) {
    if (UserManager.getUser(event.getPlayer().getUniqueId()).isSpectator()) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onBlockBreak(BlockBreakEvent event) {
    if (UserManager.getUser(event.getPlayer().getUniqueId()).isSpectator()) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onDropItem(PlayerDropItemEvent event) {
    if (UserManager.getUser(event.getPlayer().getUniqueId()).isSpectator()) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onBucketEmpty(PlayerBucketEmptyEvent event) {
    if (UserManager.getUser(event.getPlayer().getUniqueId()).isSpectator()) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onInteract(PlayerInteractEntityEvent event) {
    if (UserManager.getUser(event.getPlayer().getUniqueId()).isSpectator()) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onShear(PlayerShearEntityEvent event) {
    if (UserManager.getUser(event.getPlayer().getUniqueId()).isSpectator()) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onConsume(PlayerItemConsumeEvent event) {
    if (UserManager.getUser(event.getPlayer().getUniqueId()).isSpectator()) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onFoodLevelChange(FoodLevelChangeEvent event) {
    if (!(event.getEntity() instanceof Player)) {
      return;
    }
    Player player = (Player) event.getEntity();
    if (UserManager.getUser(player.getUniqueId()).isSpectator()) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onDamage(EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player)) {
      return;
    }
    Player player = (Player) event.getEntity();
    if (!UserManager.getUser(player.getUniqueId()).isSpectator() || ArenaRegistry.getArena(player) == null) {
      return;
    }
    if (player.getLocation().getY() < 1) {
      player.teleport(ArenaRegistry.getArena(player).getPlayerSpawnPoints().get(0));
    }
    event.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onDamageByBlock(EntityDamageByBlockEvent event) {
    if (!(event.getEntity() instanceof Player)) {
      return;
    }
    Player player = (Player) event.getEntity();
    if (UserManager.getUser(player.getUniqueId()).isSpectator()) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onDamageByEntity(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player)) {
      return;
    }
    Player player = (Player) event.getDamager();
    if (UserManager.getUser(player.getUniqueId()).isSpectator()) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onPickup(PlayerPickupItemEvent event) {
    if (UserManager.getUser(event.getPlayer().getUniqueId()).isSpectator()) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onTarget(EntityTargetEvent e) {
    if (!(e.getTarget() instanceof Player)) {
      return;
    }
    if (UserManager.getUser(e.getTarget().getUniqueId()).isSpectator() || UserManager.getUser(e.getTarget().getUniqueId()).isFakeDead()) {
      if (e.getEntity() instanceof ExperienceOrb || e.getEntity() instanceof Zombie || e.getEntity() instanceof Wolf) {
        e.setCancelled(true);
        e.setTarget(null);
      }
    }
  }

  //this will spawn orb at spec location when it's taken by spectator
  @EventHandler
  public void onPickup(PlayerExpChangeEvent e) {
    if (UserManager.getUser(e.getPlayer().getUniqueId()).isSpectator()) {
      Location loc = e.getPlayer().getLocation();
      e.setAmount(0);
      Bukkit.getScheduler().runTaskLater(plugin, () -> loc.getWorld().spawnEntity(loc, EntityType.EXPERIENCE_ORB), 30);
    }
  }

  @EventHandler
  public void onSpectate(PlayerPickupItemEvent event) {
    if (UserManager.getUser(event.getPlayer().getUniqueId()).isSpectator()) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onSpectate(PlayerDropItemEvent event) {
    Arena arena = ArenaRegistry.getArena(event.getPlayer());
    if (arena == null) {
      return;
    }
    if (arena.getArenaState() != ArenaState.IN_GAME
            || UserManager.getUser(event.getPlayer().getUniqueId()).isFakeDead()) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onInteractEntityInteract(PlayerInteractEntityEvent event) {
    User user = UserManager.getUser(event.getPlayer().getUniqueId());
    if (user.isFakeDead() || user.isSpectator()) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onRightClick(PlayerInteractEvent event) {
    Arena arena = ArenaRegistry.getArena(event.getPlayer());
    if (arena != null && UserManager.getUser(event.getPlayer().getUniqueId()).isSpectator()) {
      event.setCancelled(true);
    }
  }

}
