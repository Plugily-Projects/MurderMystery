/*
 * MurderMystery - Find the murderer, kill him and survive!
 * Copyright (C)  2021  Plugily Projects - maintained by Tigerpanzer_02, 2Wild4You and contributors
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
 *
 */

package plugily.projects.murdermystery.handlers.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Tom on 5/02/2016.
 */
public class SpecialItem {

  public static final SpecialItem INVALID_ITEM = new SpecialItem("INVALID", new ItemStack(Material.BEDROCK), -1, DisplayStage.LOBBY);
  private final String name;
  private final ItemStack itemStack;
  private int slot;
  private final DisplayStage displayStage;

  public SpecialItem(String name, ItemStack itemStack, int slot, DisplayStage displayStage) {
    this.name = name;
    this.itemStack = itemStack;
    this.slot = slot;
    this.displayStage = displayStage;
  }

  public String getName() {
    return name;
  }

  public ItemStack getItemStack() {
    return itemStack;
  }

  public int getSlot() {
    return slot;
  }

  public void setSlot(int slot) {
    this.slot = slot;
  }

  public DisplayStage getDisplayStage() {
    return displayStage;
  }

  public enum DisplayStage {
    LOBBY, SPECTATOR
  }

}
