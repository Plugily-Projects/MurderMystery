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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;
import pl.plajerlair.commonsbox.minecraft.item.ItemBuilder;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.utils.Debugger;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Created by Tom on 5/02/2016.
 */
public class SpecialItemManager {

  private final List<SpecialItem> specialItems = new ArrayList<>();
  private final FileConfiguration config;
  private final Main plugin;

  public SpecialItemManager(Main plugin) {
    this.plugin = plugin;
    this.config = ConfigUtils.getConfig(plugin, "special_items");
    registerItems();
  }

  public void addItem(SpecialItem item) {
    specialItems.add(item);
  }

  @NotNull
  public SpecialItem getSpecialItem(String name) {
    for(SpecialItem item : specialItems) {
      if(item.getName().equals(name)) {
        return item;
      }
    }
    return SpecialItem.INVALID_ITEM;
  }

  @NotNull
  public SpecialItem getRelatedSpecialItem(ItemStack itemStack) {
    for(SpecialItem item : specialItems) {
      if(item.getItemStack().isSimilar(itemStack)) {
        return item;
      }
    }
    return SpecialItem.INVALID_ITEM;
  }

  public void registerItems() {
    for(String key : config.getKeys(false)) {
      if("Version".equals(key)) {
        continue;
      }
      Material mat;
      String name;
      List<String> lore;
      int slot;
      try {
        try {
          mat = Material.valueOf(config.getString(key + ".material-name", "BEDROCK").toUpperCase());
        } catch(IllegalArgumentException e) {
          mat = Material.BEDROCK;
        }
        name = plugin.getChatManager().colorRawMessage(config.getString(key + ".displayname"));
        lore = config.getStringList(key + ".lore").stream()
            .map(itemLore -> itemLore = plugin.getChatManager().colorRawMessage(itemLore))
            .collect(Collectors.toList());
        slot = config.getInt(key + ".slot");
      } catch(Exception ex) {
        plugin.getLogger().log(Level.WARNING, "Configuration of " + key + "is missing a value. (material-name, displayname, lore or slot)");
        continue;
      }
      SpecialItem.DisplayStage stage;
      try {
        stage = SpecialItem.DisplayStage.valueOf(config.getString(key + ".stage").toUpperCase());
      } catch(Exception ex) {
        Debugger.debug(Level.WARNING, "Invalid display stage of special item " + key + " in special_items.yml! Please use lobby or spectator!");
        stage = SpecialItem.DisplayStage.LOBBY;
      }
      SpecialItem item = new SpecialItem(key, new ItemBuilder(mat).name(name).lore(lore).build(), slot, stage);
      addItem(item);
    }
  }

  public List<SpecialItem> getSpecialItems() {
    return specialItems;
  }

  public enum SpecialItems {
    ROLE_PASS("Role-Pass"), FORCESTART("Forcestart"), LOBBY_LEAVE_ITEM("Leave-Lobby"), PLAYERS_LIST("Player-List"),
    SPECTATOR_OPTIONS("Spectator-Options"), SPECTATOR_LEAVE_ITEM("Leave-Spectator"), BASE_SELECTOR("Base-Selector");

    private final String name;

    SpecialItems(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

}
