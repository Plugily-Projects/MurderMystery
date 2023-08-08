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

package plugily.projects.murdermystery.arena.corpse;

import org.bukkit.entity.ArmorStand;

import plugily.projects.minigamesbox.classic.utils.hologram.ArmorStandHologram;

/**
 * @author Plajer
 * <p>
 * Created at 07.08.2018
 */
public class Stand {

  private final ArmorStandHologram hologram;
  private final ArmorStand stand;

  public Stand(ArmorStandHologram hologram, ArmorStand stand) {
    this.hologram = hologram;
    this.stand = stand;
  }

  public ArmorStandHologram getHologram() {
    return hologram;
  }

  public ArmorStand getStand() {
    return stand;
  }
}
