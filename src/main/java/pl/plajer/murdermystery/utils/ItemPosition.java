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

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import pl.plajer.murdermystery.arena.role.Role;

/**
 * @author Plajer
 * <p>
 * Created at 17.10.2018
 */
public enum ItemPosition {

  ARROWS(2, 1, true), BOW(0, 1, false), BOW_LOCATOR(-1, 4, false), MURDERER_SWORD(1, -1, false), INNOCENTS_LOCATOR(4, -1, false), INFINITE_ARROWS(9, 9, false), GOLD_INGOTS(8, 8, true),
  POTION(3, 3, false);

  private int murdererItemPosition;
  private int otherRolesItemPosition;
  private boolean incrementable;

  ItemPosition(int murdererItemPosition, int otherRolesItemPosition, boolean incrementable) {
    this.murdererItemPosition = murdererItemPosition;
    this.otherRolesItemPosition = otherRolesItemPosition;
    this.incrementable = incrementable;
  }

  /**
   * Sets target item in specified hotbar position sorta different for each role.
   * If item there is already set it will be incremented by itemStack amount if possible.
   * @param player player to set item to
   * @param itemPosition position of item to set
   * @param itemStack itemstack to set at itemPosition
   */
  public static void setItem(Player player, ItemPosition itemPosition, ItemStack itemStack) {
    Inventory inv = player.getInventory();
    if(Role.isRole(Role.MURDERER, player)) {
      if(inv.getItem(itemPosition.getMurdererItemPosition()) != null && inv.getItem(itemPosition.getMurdererItemPosition()).isSimilar(itemStack) && itemPosition.isIncrementable()) {
        inv.getItem(itemPosition.getMurdererItemPosition()).setAmount(inv.getItem(itemPosition.getMurdererItemPosition()).getAmount() + itemStack.getAmount());
        return;
      }
      inv.setItem(itemPosition.getMurdererItemPosition(), itemStack);
    } else {
      if(inv.getItem(itemPosition.getOtherRolesItemPosition()) != null && inv.getItem(itemPosition.getOtherRolesItemPosition()).isSimilar(itemStack) && itemPosition.isIncrementable()) {
        inv.getItem(itemPosition.getOtherRolesItemPosition()).setAmount(inv.getItem(itemPosition.getOtherRolesItemPosition()).getAmount() + itemStack.getAmount());
        return;
      }
      inv.setItem(itemPosition.getOtherRolesItemPosition(), itemStack);
    }
  }

  public int getMurdererItemPosition() {
    return murdererItemPosition;
  }

  public int getOtherRolesItemPosition() {
    return otherRolesItemPosition;
  }

  public boolean isIncrementable() {
    return incrementable;
  }
}
