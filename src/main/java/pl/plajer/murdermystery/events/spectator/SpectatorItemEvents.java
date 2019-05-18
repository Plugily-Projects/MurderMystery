/*
 * MurderMystery - Find the murderer, kill him and survive!
 * Copyright (C) 2019  Plajer's Lair - maintained by Plajer and contributors
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

package pl.plajer.murdermystery.events.spectator;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.arena.Arena;
import pl.plajer.murdermystery.arena.ArenaRegistry;
import pl.plajer.murdermystery.arena.role.Role;
import pl.plajer.murdermystery.handlers.ChatManager;
import pl.plajer.murdermystery.utils.Utils;
import pl.plajerlair.commonsbox.minecraft.compat.XMaterial;

/**
 * @author Plajer
 * <p>
 * Created at 05.08.2018
 */
public class SpectatorItemEvents implements Listener {

  private Main plugin;
  private SpectatorSettingsMenu spectatorSettingsMenu;

  public SpectatorItemEvents(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
    spectatorSettingsMenu = new SpectatorSettingsMenu(plugin, ChatManager.colorMessage("In-Game.Spectator.Settings-Menu.Inventory-Name"),
        ChatManager.colorMessage("In-Game.Spectator.Settings-Menu.Speed-Name"));
  }

  @EventHandler
  public void onSpectatorItemClick(PlayerInteractEvent e) {
    if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
      if (ArenaRegistry.getArena(e.getPlayer()) == null) {
        return;
      }
      ItemStack stack = e.getPlayer().getInventory().getItemInMainHand();
      if (stack == null || !stack.hasItemMeta() || !stack.getItemMeta().hasDisplayName()) {
        return;
      }
      if (stack.getItemMeta().getDisplayName().equalsIgnoreCase(ChatManager.colorMessage("In-Game.Spectator.Spectator-Item-Name"))) {
        e.setCancelled(true);
        openSpectatorMenu(e.getPlayer().getWorld(), e.getPlayer());
      } else if (stack.getItemMeta().getDisplayName().equalsIgnoreCase(ChatManager.colorMessage("In-Game.Spectator.Settings-Menu.Item-Name"))) {
        e.setCancelled(true);
        spectatorSettingsMenu.openSpectatorSettingsMenu(e.getPlayer());
      }
    }
  }

  private void openSpectatorMenu(World world, Player p) {
    Inventory inventory = plugin.getServer().createInventory(null, Utils.serializeInt(ArenaRegistry.getArena(p).getPlayers().size()),
        ChatManager.colorMessage("In-Game.Spectator.Spectator-Menu-Name"));
    Set<Player> players = ArenaRegistry.getArena(p).getPlayers();
    for (Player player : world.getPlayers()) {
      if (players.contains(player) && !plugin.getUserManager().getUser(player).isSpectator()) {
        ItemStack skull;
        if (plugin.is1_11_R1() || plugin.is1_12_R1()) {
          skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        } else {
          skull = XMaterial.PLAYER_HEAD.parseItem();
        }
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwner(player.getName());
        meta.setDisplayName(player.getName());
        String role = ChatManager.colorMessage("In-Game.Spectator.Target-Player-Role");
        if (Role.isRole(Role.MURDERER, player)) {
          role = StringUtils.replace(role, "%ROLE%", ChatManager.colorMessage("Scoreboard.Roles.Murderer"));
        } else if (Role.isRole(Role.ANY_DETECTIVE, player)) {
          role = StringUtils.replace(role, "%ROLE%", ChatManager.colorMessage("Scoreboard.Roles.Detective"));
        } else {
          role = StringUtils.replace(role, "%ROLE%", ChatManager.colorMessage("Scoreboard.Roles.Innocent"));
        }
        meta.setLore(Collections.singletonList(role));
        skull.setDurability((short) SkullType.PLAYER.ordinal());
        skull.setItemMeta(meta);
        inventory.addItem(skull);
      }
    }
    p.openInventory(inventory);
  }

  @EventHandler
  public void onSpectatorInventoryClick(InventoryClickEvent e) {
    Player p = (Player) e.getWhoClicked();
    if (ArenaRegistry.getArena(p) == null) {
      return;
    }
    Arena arena = ArenaRegistry.getArena(p);
    if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()
        || !e.getCurrentItem().getItemMeta().hasDisplayName() || !e.getCurrentItem().getItemMeta().hasLore()) {
      return;
    }
    if (!e.getView().getTitle().equalsIgnoreCase(ChatManager.colorMessage("In-Game.Spectator.Spectator-Menu-Name"))) {
      return;
    }
    e.setCancelled(true);
    ItemMeta meta = e.getCurrentItem().getItemMeta();
    for (Player player : arena.getPlayers()) {
      if (player.getName().equalsIgnoreCase(meta.getDisplayName()) || ChatColor.stripColor(meta.getDisplayName()).contains(player.getName())) {
        p.sendMessage(ChatManager.formatMessage(arena, ChatManager.colorMessage("Commands.Admin-Commands.Teleported-To-Player"), player));
        p.teleport(player);
        p.closeInventory();
        return;
      }
      p.sendMessage(ChatManager.colorMessage("Commands.Admin-Commands.Player-Not-Found"));
    }
  }

}
