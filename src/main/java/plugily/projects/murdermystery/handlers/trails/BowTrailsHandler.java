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

package plugily.projects.murdermystery.handlers.trails;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.scheduler.BukkitRunnable;
import pl.plajerlair.commonsbox.minecraft.compat.VersionUtils;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.arena.ArenaRegistry;
import plugily.projects.murdermystery.utils.Debugger;

/**
 * @author 2Wild4You, Tigerpanzer_02
 * <p>
 * Created at 19.02.2021
 */
public class BowTrailsHandler implements Listener {

  private final Main plugin;

  public BowTrailsHandler(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void onArrowShoot(EntityShootBowEvent event) {
    if(!(event.getEntity() instanceof Player && event.getProjectile() instanceof Arrow)) {
      return;
    }
    Player player = (Player) event.getEntity();
    Entity projectile = event.getProjectile();
    if(!ArenaRegistry.isInArena(player) || projectile.isDead() || projectile.isOnGround()) {
      return;
    }
    if(!plugin.getTrailsManager().gotAnyTrails(player)) {
      return;
    }
    new BukkitRunnable() {
      @Override
      public void run() {
        if(projectile.isDead() || projectile.isOnGround()) {
          this.cancel();
        }
        Trail trail = plugin.getTrailsManager().getRandomTrail(player);
        Debugger.debug("Spawned particle with perm {0} for player {1}", trail.getPermission(), player.getName());
        VersionUtils.sendParticles(trail.getName(), player, player.getLocation(), 3);
      }
    }.runTaskTimer(plugin, 0, 0);
  }
}
