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

package plugily.projects.murdermystery.events.spectator;

import plugily.projects.commonsbox.minecraft.item.ItemUtils;
import plugily.projects.commonsbox.minecraft.misc.stuff.ComplementAccessor;
import plugily.projects.commonsbox.number.NumberUtils;
import plugily.projects.commonsbox.minecraft.compat.VersionUtils;
import plugily.projects.commonsbox.minecraft.compat.events.api.CBPlayerInteractEvent;
import plugily.projects.commonsbox.minecraft.compat.xseries.XMaterial;
import plugily.projects.inventoryframework.gui.GuiItem;
import plugily.projects.inventoryframework.gui.type.ChestGui;
import plugily.projects.inventoryframework.pane.OutlinePane;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.arena.ArenaManager;
import plugily.projects.murdermystery.arena.ArenaRegistry;
import plugily.projects.murdermystery.arena.role.Role;
import plugily.projects.murdermystery.handlers.items.SpecialItemManager;
import plugily.projects.murdermystery.utils.Utils;

import java.util.ArrayList;

/**
 * @author Plajer
 * <p>
 * Created at 05.08.2018
 */
public class SpectatorItemEvents implements Listener {

  private final Main plugin;
  private final SpectatorSettingsMenu spectatorSettingsMenu;

  public SpectatorItemEvents(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
    spectatorSettingsMenu = new SpectatorSettingsMenu(plugin, plugin.getChatManager().colorMessage("In-Game.Spectator.Settings-Menu.Inventory-Name"),
        plugin.getChatManager().colorMessage("In-Game.Spectator.Settings-Menu.Speed-Name"));
  }

  @EventHandler
  public void onSpectatorItemClick(CBPlayerInteractEvent e) {
    if(e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.PHYSICAL) {
      return;
    }
    Arena arena = ArenaRegistry.getArena(e.getPlayer());
    ItemStack stack = VersionUtils.getItemInHand(e.getPlayer());
    if(arena == null || !ItemUtils.isItemStackNamed(stack)) {
      return;
    }
    if(plugin.getSpecialItemManager().getRelatedSpecialItem(stack).getName().equals(SpecialItemManager.SpecialItems.PLAYERS_LIST.getName())) {
      e.setCancelled(true);
      openSpectatorMenu(e.getPlayer(), arena);
    } else if(plugin.getSpecialItemManager().getRelatedSpecialItem(stack).getName().equals(SpecialItemManager.SpecialItems.SPECTATOR_OPTIONS.getName())) {
      e.setCancelled(true);
      spectatorSettingsMenu.openSpectatorSettingsMenu(e.getPlayer());
    } else if(plugin.getSpecialItemManager().getRelatedSpecialItem(stack).getName().equals(SpecialItemManager.SpecialItems.SPECTATOR_LEAVE_ITEM.getName())) {
      e.setCancelled(true);
      ArenaManager.leaveAttempt(e.getPlayer(), arena);
    }
  }

  private void openSpectatorMenu(Player player, Arena arena) {
    int rows = Utils.serializeInt(arena.getPlayers().size()) / 9;
    ChestGui gui = new ChestGui(rows, plugin.getChatManager().colorMessage("In-Game.Spectator.Spectator-Menu-Name"));
    OutlinePane pane = new OutlinePane(9, rows);
    gui.addPane(pane);

    ItemStack skull = XMaterial.PLAYER_HEAD.parseItem();

    for(Player arenaPlayer : arena.getPlayers()) {
      if(plugin.getUserManager().getUser(arenaPlayer).isSpectator()) {
        continue;
      }
      //Get the raw role message and replace old placeholder, we don't want to do this inside the for loop.
      String roleRaw = plugin.getChatManager().colorMessage("In-Game.Spectator.Target-Player-Role", arenaPlayer);
      roleRaw = StringUtils.replace(roleRaw, "%ROLE%", "%role%");
      ItemStack cloneSkull = skull.clone();
      SkullMeta meta = VersionUtils.setPlayerHead(arenaPlayer, (SkullMeta) cloneSkull.getItemMeta());
      ComplementAccessor.getComplement().setDisplayName(meta, arenaPlayer.getName());

      ArrayList<String> lore = new ArrayList<>();

      lore.add(plugin.getChatManager().colorMessage("In-Game.Spectator.Target-Player-Health")
          .replace("%health%", Double.toString(NumberUtils.round(arenaPlayer.getHealth(), 2))));
      String role = roleRaw;
      if(Role.isRole(Role.MURDERER, player, arena)) {
        role = StringUtils.replace(role, "%role%", plugin.getChatManager().colorMessage("Scoreboard.Roles.Murderer"));
      } else if(Role.isRole(Role.ANY_DETECTIVE, player, arena)) {
        role = StringUtils.replace(role, "%role%", plugin.getChatManager().colorMessage("Scoreboard.Roles.Detective"));
      } else {
        role = StringUtils.replace(role, "%role%", plugin.getChatManager().colorMessage("Scoreboard.Roles.Innocent"));
      }
      lore.add(role);
      ComplementAccessor.getComplement().setLore(meta, lore);
      cloneSkull.setItemMeta(meta);
      pane.addItem(new GuiItem(cloneSkull, e -> {
        e.setCancelled(true);
        e.getWhoClicked().sendMessage(plugin.getChatManager().formatMessage(arena, plugin.getChatManager().colorMessage("Commands.Admin-Commands.Teleported-To-Player"), arenaPlayer));
        e.getWhoClicked().closeInventory();
        e.getWhoClicked().teleport(arenaPlayer);
      }));
    }
    gui.show(player);
  }

}
