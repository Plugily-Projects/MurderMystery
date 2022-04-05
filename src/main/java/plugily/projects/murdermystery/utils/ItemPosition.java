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

package plugily.projects.murdermystery.utils;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import plugily.projects.minigamesbox.classic.user.User;
import plugily.projects.murdermystery.arena.role.Role;

/**
 * @author Plajer
 * <p>
 * Created at 17.10.2018
 */
public enum ItemPosition {

  ARROWS(2, 2), BOW(0, 1), BOW_LOCATOR(4, 4), MURDERER_SWORD(1, 1), INNOCENTS_LOCATOR(4, 4), INFINITE_ARROWS(9, 9), GOLD_INGOTS(8, 8),
  POTION(3, 3);

  private final int murdererItemPosition;
  private final int otherRolesItemPosition;

  ItemPosition(int murdererItemPosition, int otherRolesItemPosition) {
    this.murdererItemPosition = murdererItemPosition;
    this.otherRolesItemPosition = otherRolesItemPosition;
  }

  /**
   * Adds target item to specified hotbar position sorta different for each role.
   * Item will be added if there is already set or will be set when no item is set in the position.
   *
   * @param user       player to add item to
   * @param itemPosition position of item to set/add
   * @param itemStack    itemstack to be added at itemPostion or set at itemPosition
   */
  public static void addItem(User user, ItemPosition itemPosition, ItemStack itemStack) {
    int itemPos = Role.isRole(Role.MURDERER, user) ? itemPosition.getMurdererItemPosition()
        : itemPosition.getOtherRolesItemPosition();

    if (itemPos < 0) {
      return;
    }

    Inventory inv = user.getPlayer().getInventory();
    ItemStack item = inv.getItem(itemPos);

    if(item != null) {
      item.setAmount(item.getAmount() + itemStack.getAmount());
      return;
    }

    inv.setItem(itemPos, itemStack);
  }

  /**
   * Sets target item in specified hotbar position sorta different for each role.
   * If item there is already set it will be incremented by itemStack amount if possible.
   *
   * @param user       player to set item to
   * @param itemPosition position of item to set
   * @param itemStack    itemstack to set at itemPosition
   */
  public static void setItem(User user, ItemPosition itemPosition, ItemStack itemStack) {
    if(itemPosition.getMurdererItemPosition() >= 0 && Role.isRole(Role.MURDERER, user)) {
      user.getPlayer().getInventory().setItem(itemPosition.getMurdererItemPosition(), itemStack);
    } else if (itemPosition.getOtherRolesItemPosition() >= 0) {
      user.getPlayer().getInventory().setItem(itemPosition.getOtherRolesItemPosition(), itemStack);
    }
  }

  public int getMurdererItemPosition() {
    return murdererItemPosition;
  }

  public int getOtherRolesItemPosition() {
    return otherRolesItemPosition;
  }

}
