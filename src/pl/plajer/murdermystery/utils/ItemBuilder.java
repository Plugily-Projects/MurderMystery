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

package pl.plajer.murdermystery.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author Plajer
 * <p>
 * Created at 05.08.2018
 */
public class ItemBuilder implements Listener {

  private final ItemStack is;


  public ItemBuilder(final ItemStack is) {
    this.is = is;
  }

  public ItemBuilder name(final String name) {
    final ItemMeta meta = is.getItemMeta();
    meta.setDisplayName(name);
    is.setItemMeta(meta);
    return this;
  }

  public ItemBuilder lore(final String name) {
    final ItemMeta meta = is.getItemMeta();
    List<String> lore = meta.getLore();
    if (lore == null) {
      lore = new ArrayList<>();
    }
    lore.add(name);
    meta.setLore(lore);
    is.setItemMeta(meta);
    return this;
  }

  public ItemStack build() {
    return is;
  }

}
