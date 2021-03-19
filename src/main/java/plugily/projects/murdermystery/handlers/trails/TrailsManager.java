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

import org.bukkit.entity.Player;
import pl.plajerlair.commonsbox.minecraft.compat.VersionUtils;
import plugily.projects.murdermystery.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @author 2Wild4You, Tigerpanzer_02
 * <p>
 * Created at 19.02.2021
 */

public class TrailsManager {

  private final ArrayList<Trail> registeredTrails = new ArrayList<>();

  private final List<String> blacklistedTrails;

  public TrailsManager(Main plugin) {
    blacklistedTrails = plugin.getConfig().getStringList("Blacklisted-Trails");
    registerTrails();
  }

  public void registerTrails() {
    for(String particle : VersionUtils.getParticleValues()) {
      if(blacklistedTrails.contains(particle.toLowerCase())) {
        continue;
      }
      addTrail(new Trail(particle, "murdermystery.trails." + particle.toString().toLowerCase()));
    }
  }

  public ArrayList<Trail> getRegisteredTrails() {
    return registeredTrails;
  }

  public void addTrail(Trail lastWord) {
    registeredTrails.add(lastWord);
  }

  public boolean gotAnyTrails(Player player) {
    return registeredTrails.stream().anyMatch(trail -> player.hasPermission(trail.getPermission()));
  }

  public Trail getRandomTrail(Player player) {
    //check perms
    List<Trail> perms = registeredTrails.stream().filter(trail -> player.hasPermission(trail.getPermission())).collect(Collectors.toList());
    if(!perms.isEmpty()) {
      return perms.get(ThreadLocalRandom.current().nextInt(perms.size()));
    }
    //fallback
    return registeredTrails.get(0);
  }
}
