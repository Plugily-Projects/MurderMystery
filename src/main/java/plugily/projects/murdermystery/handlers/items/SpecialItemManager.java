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

package plugily.projects.murdermystery.handlers.items;

import org.bukkit.inventory.ItemStack;

import pl.plajerlair.commonsbox.minecraft.misc.stuff.ComplementAccessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Plajer
 * <p>
 * Created at 03.08.2018
 */
public class SpecialItemManager {

  private static final Map<String, List<SpecialItem>> specialItems = new HashMap<>();

  public static void addItem(String name, List<SpecialItem> entityItem) {
    specialItems.put(name, entityItem);
  }

  public static SpecialItem getSpecialItem(String name) {
    List<SpecialItem> specialitem = specialItems.getOrDefault(name, new java.util.ArrayList<>());
    return specialitem.get(ThreadLocalRandom.current().nextInt(specialitem.size()));
  }

  public static String getRelatedSpecialItem(ItemStack itemStack) {
    for(Map.Entry<String, List<SpecialItem>> map : specialItems.entrySet()) {
      List<SpecialItem> entityItem = map.getValue();
      if(!entityItem.isEmpty() && ComplementAccessor.getComplement().getDisplayName(entityItem.get(0).getItemStack().getItemMeta())
        .equalsIgnoreCase(ComplementAccessor.getComplement().getDisplayName(itemStack.getItemMeta()))) {
        return map.getKey();
      }
    }
    return null;
  }
}
