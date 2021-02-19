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

package plugily.projects.murdermystery.handlers;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.scheduler.BukkitRunnable;
import pl.plajerlair.commonsbox.minecraft.compat.VersionUtils;
import pl.plajerlair.commonsbox.minecraft.compat.xseries.XParticleLegacy;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.arena.ArenaRegistry;
import plugily.projects.murdermystery.utils.Debugger;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Plajer
 * <p>
 * Created at 19.10.2018
 */
public class BowTrailsHandler implements Listener {

  private final Map<String, String> registeredTrails = new LinkedHashMap<>();
  private final Main plugin;

  public BowTrailsHandler(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
    for(XParticleLegacy particle : XParticleLegacy.values()) {
      registerBowTrail("murdermystery.trails." + particle.getName().toLowerCase(), particle.getName());
    }
  }

  public void registerBowTrail(String permission, String particle) {
    registeredTrails.put(permission, particle);
  }

  @EventHandler
  public void onArrowShoot(EntityShootBowEvent e) {
    if(!(e.getEntity() instanceof Player && e.getProjectile() instanceof Arrow)) {
      return;
    }
    if(!ArenaRegistry.isInArena((Player) e.getEntity()) || e.getProjectile().isDead() || e.getProjectile().isOnGround()) {
      return;
    }
    for(String perm : registeredTrails.keySet()) {
      if(e.getEntity().hasPermission(perm)) {
        new BukkitRunnable() {
          @Override
          public void run() {
            if(e.getProjectile().isDead() || e.getProjectile().isOnGround()) {
              this.cancel();
            }
            Debugger.debug("Spawned particle with perm {0} for player {1}", perm, e.getEntity().getName());
            VersionUtils.sendParticles(registeredTrails.get(perm), ((Player) e.getEntity()).getPlayer(), e.getEntity().getLocation(), 3);
          }
        }.runTaskTimer(plugin, 0, 0);
        break;
      }
    }
  }

}
