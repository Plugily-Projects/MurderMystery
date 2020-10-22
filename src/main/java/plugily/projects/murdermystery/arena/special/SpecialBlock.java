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

package plugily.projects.murdermystery.arena.special;

import org.bukkit.Location;
import plugily.projects.murdermystery.handlers.hologram.ArmorStandHologram;

/**
 * @author Plajer
 * <p>
 * Created at 15.10.2018
 */
public class SpecialBlock {

  private final Location location;
  private final SpecialBlockType specialBlockType;
  private ArmorStandHologram armorStandHologram;

  public SpecialBlock(Location location, SpecialBlockType specialBlockType) {
    this.location = location;
    this.specialBlockType = specialBlockType;
  }

  public Location getLocation() {
    return location;
  }

  public SpecialBlockType getSpecialBlockType() {
    return specialBlockType;
  }

  public ArmorStandHologram getArmorStandHologram() {
    return armorStandHologram;
  }

  public void setArmorStandHologram(ArmorStandHologram armorStandHologram) {
    this.armorStandHologram = armorStandHologram;
  }

  public enum SpecialBlockType {
    HORSE_PURCHASE, MYSTERY_CAULDRON, PRAISE_DEVELOPER, RAPID_TELEPORTATION
  }

}
