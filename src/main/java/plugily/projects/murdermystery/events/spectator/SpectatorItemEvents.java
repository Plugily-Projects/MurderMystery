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

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.SkullType;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import pl.plajerlair.commonsbox.minecraft.compat.VersionUtils;
import pl.plajerlair.commonsbox.minecraft.compat.xseries.XMaterial;
import pl.plajerlair.commonsbox.minecraft.misc.stuff.ComplementAccessor;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.arena.ArenaRegistry;
import plugily.projects.murdermystery.arena.role.Role;
import plugily.projects.murdermystery.utils.Utils;

import java.util.Collections;

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
  public void onSpectatorItemClick(PlayerInteractEvent e) {
    if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() != Action.PHYSICAL) {
      if(ArenaRegistry.getArena(e.getPlayer()) == null) {
        return;
      }
      ItemStack stack = VersionUtils.getItemInHand(e.getPlayer());
      if(!stack.hasItemMeta() || !stack.getItemMeta().hasDisplayName()) {
        return;
      }
      if(ComplementAccessor.getComplement().getDisplayName(stack.getItemMeta()).equalsIgnoreCase(plugin.getChatManager().colorMessage("In-Game.Spectator.Spectator-Item-Name"))) {
        e.setCancelled(true);
        openSpectatorMenu(e.getPlayer().getWorld(), e.getPlayer());
      } else if(ComplementAccessor.getComplement().getDisplayName(stack.getItemMeta()).equalsIgnoreCase(plugin.getChatManager().colorMessage("In-Game.Spectator.Settings-Menu.Item-Name"))) {
        e.setCancelled(true);
        spectatorSettingsMenu.openSpectatorSettingsMenu(e.getPlayer());
      }
    }
  }

  private void openSpectatorMenu(World world, Player p) {
    Arena arena = ArenaRegistry.getArena(p);
    Inventory inventory = ComplementAccessor.getComplement().createInventory(null, Utils.serializeInt(arena.getPlayers().size()),
      plugin.getChatManager().colorMessage("In-Game.Spectator.Spectator-Menu-Name"));

    //Get the raw role message and replace old placeholder, we don't want to do this inside the for loop.
    String roleRaw = plugin.getChatManager().colorMessage("In-Game.Spectator.Target-Player-Role", p);
    roleRaw = StringUtils.replace(roleRaw, "%ROLE%", "%role%");

    for(Player player : world.getPlayers()) {
      if(arena.getPlayers().contains(player) && !plugin.getUserManager().getUser(player).isSpectator()) {
        ItemStack skull = XMaterial.PLAYER_HEAD.parseItem();
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta = VersionUtils.setPlayerHead(player, meta);
        ComplementAccessor.getComplement().setDisplayName(meta, player.getName());

        String role = roleRaw;
        if(Role.isRole(Role.MURDERER, player)) {
          role = StringUtils.replace(role, "%role%", plugin.getChatManager().colorMessage("Scoreboard.Roles.Murderer"));
        } else if(Role.isRole(Role.ANY_DETECTIVE, player)) {
          role = StringUtils.replace(role, "%role%", plugin.getChatManager().colorMessage("Scoreboard.Roles.Detective"));
        } else {
          role = StringUtils.replace(role, "%role%", plugin.getChatManager().colorMessage("Scoreboard.Roles.Innocent"));
        }
        ComplementAccessor.getComplement().setLore(meta, Collections.singletonList(role));
        VersionUtils.setDurability(skull, (short) SkullType.PLAYER.ordinal());
        skull.setItemMeta(meta);
        inventory.addItem(skull);
      }
    }
    p.openInventory(inventory);
  }

  @EventHandler
  public void onSpectatorInventoryClick(InventoryClickEvent e) {
    Player p = (Player) e.getWhoClicked();
    Arena arena = ArenaRegistry.getArena(p);
    ItemStack currentItem = e.getCurrentItem();
    if(arena == null || currentItem == null || !currentItem.hasItemMeta()
      || !currentItem.getItemMeta().hasDisplayName() || !currentItem.getItemMeta().hasLore()) {
      return;
    }
    if(!ComplementAccessor.getComplement().getTitle(e.getView()).equalsIgnoreCase(plugin.getChatManager().colorMessage("In-Game.Spectator.Spectator-Menu-Name", p))) {
      return;
    }
    e.setCancelled(true);
    ItemMeta meta = currentItem.getItemMeta();
    String dName = ComplementAccessor.getComplement().getDisplayName(meta);
    for(Player player : arena.getPlayers()) {
      if(player.getName().equalsIgnoreCase(dName) || ChatColor.stripColor(dName).contains(player.getName())) {
        p.sendMessage(plugin.getChatManager().formatMessage(arena, plugin.getChatManager().colorMessage("Commands.Admin-Commands.Teleported-To-Player"), player));
        p.teleport(player);
        p.closeInventory();
        return;
      }
    }
    p.sendMessage(plugin.getChatManager().colorMessage("Commands.Admin-Commands.Player-Not-Found"));
  }

}
