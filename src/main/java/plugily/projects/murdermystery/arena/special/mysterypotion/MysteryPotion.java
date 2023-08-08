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

package plugily.projects.murdermystery.arena.special.mysterypotion;

import org.bukkit.potion.PotionEffect;

/**
 * @author Plajer
 * <p>
 * Created at 15.10.2018
 */
public class MysteryPotion {

  private final String name;
  private final String subtitle;
  private final PotionEffect potionEffect;

  public MysteryPotion(String name, String subtitle, PotionEffect potionEffect) {
    this.name = name;
    this.subtitle = subtitle;
    this.potionEffect = potionEffect;
  }

  public String getName() {
    return name;
  }

  public String getSubtitle() {
    return subtitle;
  }

  public PotionEffect getPotionEffect() {
    return potionEffect;
  }

}
