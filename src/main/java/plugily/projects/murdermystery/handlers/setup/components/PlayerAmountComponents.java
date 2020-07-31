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

package plugily.projects.murdermystery.handlers.setup.components;

import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import pl.plajerlair.commonsbox.minecraft.compat.XMaterial;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;
import pl.plajerlair.commonsbox.minecraft.item.ItemBuilder;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.handlers.ChatManager;
import plugily.projects.murdermystery.handlers.setup.SetupInventory;

/**
 * @author Plajer
 * <p>
 * Created at 25.05.2019
 */
public class PlayerAmountComponents implements SetupComponent {

  private SetupInventory setupInventory;

  @Override
  public void prepare(SetupInventory setupInventory) {
    this.setupInventory = setupInventory;
  }

  @Override
  public void injectComponents(StaticPane pane) {
    FileConfiguration config = setupInventory.getConfig();
    Arena arena = setupInventory.getArena();
    Main plugin = setupInventory.getPlugin();
    pane.addItem(new GuiItem(new ItemBuilder(Material.COAL).amount(setupInventory.getSetupUtilities().getMinimumValueHigherThanZero("minimumplayers"))
      .name(ChatManager.colorRawMessage("&e&lSet Minimum Players Amount"))
      .lore(ChatColor.GRAY + "LEFT click to decrease")
      .lore(ChatColor.GRAY + "RIGHT click to increase")
      .lore(ChatColor.DARK_GRAY + "(how many players are needed")
      .lore(ChatColor.DARK_GRAY + "for game to start lobby countdown)")
      .lore("", setupInventory.getSetupUtilities().isOptionDone("instances." + arena.getId() + ".minimumplayers"))
      .build(), e -> {
      if (e.getClick().isRightClick()) {
        e.getInventory().getItem(e.getSlot()).setAmount(e.getCurrentItem().getAmount() + 1);
      }
      if (e.getClick().isLeftClick()) {
        e.getInventory().getItem(e.getSlot()).setAmount(e.getCurrentItem().getAmount() - 1);
      }
      if (e.getInventory().getItem(e.getSlot()).getAmount() <= 1) {
        e.getWhoClicked().sendMessage(ChatManager.colorRawMessage("&c&l✖ &cWarning | Please do not set amount lower than 2! Game is designed for 2 or more players!"));
        e.getInventory().getItem(e.getSlot()).setAmount(2);
      }
      config.set("instances." + arena.getId() + ".minimumplayers", e.getCurrentItem().getAmount());
      arena.setMinimumPlayers(e.getCurrentItem().getAmount());
      ConfigUtils.saveConfig(plugin, config, "arenas");
      new SetupInventory(arena, setupInventory.getPlayer()).openInventory();
    }), 3, 0);

    pane.addItem(new GuiItem(new ItemBuilder(Material.REDSTONE)
      .amount(setupInventory.getSetupUtilities().getMinimumValueHigherThanZero("maximumplayers"))
      .name(ChatManager.colorRawMessage("&e&lSet Maximum Players Amount"))
      .lore(ChatColor.GRAY + "LEFT click to decrease")
      .lore(ChatColor.GRAY + "RIGHT click to increase")
      .lore(ChatColor.DARK_GRAY + "(how many players arena can hold)")
      .lore("", setupInventory.getSetupUtilities().isOptionDone("instances." + arena.getId() + ".maximumplayers"))
      .build(), e -> {
      if (e.getClick().isRightClick()) {
        e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() + 1);
      }
      if (e.getClick().isLeftClick()) {
        e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() - 1);
      }
      if (e.getInventory().getItem(e.getSlot()).getAmount() <= 1) {
        e.getWhoClicked().sendMessage(ChatManager.colorRawMessage("&c&l✖ &cWarning | Please do not set amount lower than 2! Game is designed for 2 or more players!"));
        e.getInventory().getItem(e.getSlot()).setAmount(2);
      }
      config.set("instances." + arena.getId() + ".maximumplayers", e.getCurrentItem().getAmount());
      arena.setMaximumPlayers(e.getCurrentItem().getAmount());
      ConfigUtils.saveConfig(plugin, config, "arenas");
      new SetupInventory(arena, setupInventory.getPlayer()).openInventory();
    }), 4, 0);

    pane.addItem(new GuiItem(new ItemBuilder(XMaterial.IRON_SWORD.parseItem())
      .amount(setupInventory.getSetupUtilities().getMinimumValueHigherThanZero("playerpermurderer"))
      .name(ChatManager.colorRawMessage("&e&lSet Player Per Murderer Amount"))
      .lore(ChatColor.GRAY + "LEFT click to decrease")
      .lore(ChatColor.GRAY + "RIGHT click to increase")
      .lore(ChatColor.DARK_GRAY + "How many murderer should be ingame? This means ")
      .lore(ChatColor.DARK_GRAY + "one murderer for that amount of players. Default: ")
      .lore(ChatColor.DARK_GRAY + "5 players are 1 murderer, that means if we have ")
      .lore(ChatColor.DARK_GRAY + "14 Players it will calculate 2 murderer! ")
      .lore(ChatColor.DARK_GRAY + "Set it to 1 if you want only one murderer ")
      .lore("", setupInventory.getSetupUtilities().isOptionDone("instances." + arena.getId() + ".playerpermurderer"))
      .build(), e -> {
      if (e.getClick().isRightClick()) {
        e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() + 1);
      }
      if (e.getClick().isLeftClick() && e.getCurrentItem().getAmount() > 1) {
        e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() - 1);
      }
      if (e.getInventory().getItem(e.getSlot()).getAmount() < 1) {
        e.getWhoClicked().sendMessage(ChatManager.colorRawMessage("&c&l✖ &cWarning | Please do not set amount lower than 1! Game is not designed for more murderers than players!"));
        e.getInventory().getItem(e.getSlot()).setAmount(1);
      }
      config.set("instances." + arena.getId() + ".playerpermurderer", e.getCurrentItem().getAmount());
      arena.setMurderers(e.getCurrentItem().getAmount());
      ConfigUtils.saveConfig(plugin, config, "arenas");
      new SetupInventory(arena, setupInventory.getPlayer()).openInventory();
    }), 3, 1);


    pane.addItem(new GuiItem(new ItemBuilder(XMaterial.BOW.parseItem())
      .amount(setupInventory.getSetupUtilities().getMinimumValueHigherThanZero("playerperdetective"))
      .name(ChatManager.colorRawMessage("&e&lSet Player Per Detective Amount"))
      .lore(ChatColor.GRAY + "LEFT click to decrease")
      .lore(ChatColor.GRAY + "RIGHT click to increase")
      .lore(ChatColor.DARK_GRAY + "How many detectives should be ingame? This means ")
      .lore(ChatColor.DARK_GRAY + "one detective for that amount of players. Default: ")
      .lore(ChatColor.DARK_GRAY + "7 players are 1 detective, that means if we have ")
      .lore(ChatColor.DARK_GRAY + "18 Players it will calculate 2 detectives! ")
      .lore(ChatColor.DARK_GRAY + "Set it to 1 if you want only one detectives ")
      .lore("", setupInventory.getSetupUtilities().isOptionDone("instances." + arena.getId() + ".playerperdetective"))
      .build(), e -> {
      if (e.getClick().isRightClick()) {
        e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() + 1);
      }
      if (e.getClick().isLeftClick() && e.getCurrentItem().getAmount() > 1) {
        e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() - 1);
      }
      if (e.getInventory().getItem(e.getSlot()).getAmount() < 1) {
        e.getWhoClicked().sendMessage(ChatManager.colorRawMessage("&c&l✖ &cWarning | Please do not set amount lower than 1! Game is not designed for more detectives than players!"));
        e.getInventory().getItem(e.getSlot()).setAmount(1);
      }
      config.set("instances." + arena.getId() + ".playerperdetective", e.getCurrentItem().getAmount());
      arena.setDetectives(e.getCurrentItem().getAmount());
      ConfigUtils.saveConfig(plugin, config, "arenas");
      new SetupInventory(arena, setupInventory.getPlayer()).openInventory();
    }), 4, 1);
  }

}
