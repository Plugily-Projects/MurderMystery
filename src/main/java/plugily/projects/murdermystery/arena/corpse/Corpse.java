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

package plugily.projects.murdermystery.arena.corpse;

import org.golde.bukkit.corpsereborn.nms.Corpses;

import plugily.projects.minigamesbox.classic.utils.hologram.ArmorStandHologram;

/**
 * @author Plajer
 * <p>
 * Created at 07.08.2018
 */
public class Corpse {

  private final ArmorStandHologram hologram;
  private final Corpses.CorpseData corpseData;

  public Corpse(ArmorStandHologram hologram, Corpses.CorpseData corpseData) {
    this.hologram = hologram;
    this.corpseData = corpseData;
  }

  public ArmorStandHologram getHologram() {
    return hologram;
  }

  public Corpses.CorpseData getCorpseData() {
    return corpseData;
  }
}
