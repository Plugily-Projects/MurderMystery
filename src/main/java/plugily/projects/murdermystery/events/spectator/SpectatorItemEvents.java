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
import org.bukkit.*;
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
import pl.plajerlair.commonsbox.minecraft.compat.XMaterial;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.arena.ArenaRegistry;
import plugily.projects.murdermystery.arena.role.Role;
import plugily.projects.murdermystery.handlers.ChatManager;
import plugily.projects.murdermystery.utils.Utils;

import java.util.Collections;
import java.util.Set;

/**
 * @author Plajer
 * <p>
 * Created at 05.08.2018
 */
public class SpectatorItemEvents implements Listener {

  private final Main plugin;
  private final ChatManager chatManager;
  private final SpectatorSettingsMenu spectatorSettingsMenu;
  private final boolean usesPaperSpigot = Bukkit.getServer().getVersion().contains("Paper");

  public SpectatorItemEvents(Main plugin) {
    this.plugin = plugin;
    chatManager = plugin.getChatManager();
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
    spectatorSettingsMenu = new SpectatorSettingsMenu(plugin, chatManager.colorMessage("In-Game.Spectator.Settings-Menu.Inventory-Name"),
      chatManager.colorMessage("In-Game.Spectator.Settings-Menu.Speed-Name"));
  }

  @EventHandler
  public void onSpectatorItemClick(PlayerInteractEvent e) {
    if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() != Action.PHYSICAL) {
      if (ArenaRegistry.getArena(e.getPlayer()) == null) {
        return;
      }
      ItemStack stack = e.getPlayer().getInventory().getItemInMainHand();
      if (!stack.hasItemMeta() || !stack.getItemMeta().hasDisplayName()) {
        return;
      }
      if (stack.getItemMeta().getDisplayName().equalsIgnoreCase(chatManager.colorMessage("In-Game.Spectator.Spectator-Item-Name"))) {
        e.setCancelled(true);
        openSpectatorMenu(e.getPlayer().getWorld(), e.getPlayer());
      } else if (stack.getItemMeta().getDisplayName().equalsIgnoreCase(chatManager.colorMessage("In-Game.Spectator.Settings-Menu.Item-Name"))) {
        e.setCancelled(true);
        spectatorSettingsMenu.openSpectatorSettingsMenu(e.getPlayer());
      }
    }
  }

  private void openSpectatorMenu(World world, Player p) {
    Inventory inventory = plugin.getServer().createInventory(null, Utils.serializeInt(ArenaRegistry.getArena(p).getPlayers().size()),
      chatManager.colorMessage("In-Game.Spectator.Spectator-Menu-Name"));
    Set<Player> players = ArenaRegistry.getArena(p).getPlayers();

    //Get the raw role message and replace old placeholder, we don't want to do this inside the for loop.
    String roleRaw = chatManager.colorMessage("In-Game.Spectator.Target-Player-Role", p);
    roleRaw = StringUtils.replace(roleRaw, "%ROLE%", "%role");

    for (Player player : world.getPlayers()) {
      if (players.contains(player) && !plugin.getUserManager().getUser(player).isSpectator()) {
        ItemStack skull = XMaterial.PLAYER_HEAD.parseItem();
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (usesPaperSpigot && player.getPlayerProfile().hasTextures()) {
          meta.setPlayerProfile(player.getPlayerProfile());
        } else {
          meta.setOwningPlayer(player);
        }
        meta.setDisplayName(player.getName());

        String role = roleRaw;
        if (Role.isRole(Role.MURDERER, player)) {
          role = StringUtils.replace(role, "%role%", chatManager.colorMessage("Scoreboard.Roles.Murderer"));
        } else if (Role.isRole(Role.ANY_DETECTIVE, player)) {
          role = StringUtils.replace(role, "%role%", chatManager.colorMessage("Scoreboard.Roles.Detective"));
        } else {
          role = StringUtils.replace(role, "%role%", chatManager.colorMessage("Scoreboard.Roles.Innocent"));
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
    if (!e.getView().getTitle().equalsIgnoreCase(chatManager.colorMessage("In-Game.Spectator.Spectator-Menu-Name", p))) {
      return;
    }
    e.setCancelled(true);
    ItemMeta meta = e.getCurrentItem().getItemMeta();
    for (Player player : arena.getPlayers()) {
      if (player.getName().equalsIgnoreCase(meta.getDisplayName()) || ChatColor.stripColor(meta.getDisplayName()).contains(player.getName())) {
        p.sendMessage(chatManager.formatMessage(arena, chatManager.colorMessage("Commands.Admin-Commands.Teleported-To-Player"), player));
        p.teleport(player);
        p.closeInventory();
        return;
      }
    }
    p.sendMessage(chatManager.colorMessage("Commands.Admin-Commands.Player-Not-Found"));
  }

}
