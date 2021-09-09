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

package plugily.projects.murdermystery.handlers;

import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.EulerAngle;
import org.golde.bukkit.corpsereborn.CorpseAPI.CorpseAPI;
import org.golde.bukkit.corpsereborn.CorpseAPI.events.CorpseClickEvent;
import org.golde.bukkit.corpsereborn.CorpseAPI.events.CorpseSpawnEvent;
import org.golde.bukkit.corpsereborn.nms.Corpses;

import plugily.projects.commonsbox.minecraft.compat.ServerVersion;
import plugily.projects.commonsbox.minecraft.compat.VersionUtils;
import plugily.projects.commonsbox.minecraft.compat.xseries.XMaterial;
import plugily.projects.commonsbox.minecraft.hologram.ArmorStandHologram;
import plugily.projects.commonsbox.minecraft.hologram.HologramManager;
import plugily.projects.murdermystery.HookManager;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.arena.ArenaRegistry;
import plugily.projects.murdermystery.arena.corpse.Corpse;
import plugily.projects.murdermystery.arena.corpse.Stand;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Plajer
 * <p>
 * Created at 07.10.2018
 */
public class CorpseHandler implements Listener {

  private final Main plugin;
  private final ChatManager chatManager;
  private Corpses.CorpseData lastSpawnedCorpse;

  private final Map<String, String> registeredLastWords = new HashMap<>();
  private final ItemStack head = XMaterial.PLAYER_HEAD.parseItem();

  public CorpseHandler(Main plugin) {
    this.plugin = plugin;
    chatManager = plugin.getChatManager();
    //run bit later than hook manager to ensure it's not null
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      if(plugin.getHookManager().isFeatureEnabled(HookManager.HookFeature.CORPSES)) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
      }
    }, 20 * 7);
  }

  public void registerLastWord(String permission, String lastWord) {
    registeredLastWords.put(permission, lastWord);
  }

  @SuppressWarnings("deprecation")
  public void spawnCorpse(Player p, Arena arena) {
    if(!plugin.getHookManager().isFeatureEnabled(HookManager.HookFeature.CORPSES)) {
      ArmorStand stand = p.getLocation().getWorld().spawn(p.getLocation().add(0.0D, -1.25D, 0.0D), ArmorStand.class);
      SkullMeta meta = (SkullMeta) head.getItemMeta();
      meta = VersionUtils.setPlayerHead(p, meta);
      head.setItemMeta(meta);

      stand.setVisible(false);
      if(ServerVersion.Version.isCurrentEqualOrHigher(ServerVersion.Version.v1_16_R1)) {
        stand.getEquipment().setHelmet(head);
      } else {
        stand.setHelmet(head);
      }
      stand.setGravity(false);
      stand.setCustomNameVisible(false);
      stand.setHeadPose(new EulerAngle(Math.toRadians(p.getLocation().getX()), Math.toRadians(p.getLocation().getPitch()), Math.toRadians(p.getLocation().getZ())));

      HologramManager.getArmorStands().add(stand);
      ArmorStandHologram hologram = getLastWordsHologram(p);
      arena.addHead(new Stand(hologram, stand));
      Bukkit.getScheduler().runTaskLater(plugin, () -> {
        hologram.delete();
        HologramManager.getArmorStands().remove(stand);
        Bukkit.getScheduler().runTaskLater(plugin, stand::remove, 20 * 20);
      }, 15 * 20);
      return;
    }
    ArmorStandHologram hologram = getLastWordsHologram(p);
    Corpses.CorpseData corpse = CorpseAPI.spawnCorpse(p, p.getLocation());
    lastSpawnedCorpse = corpse;
    //spawns 2 corpses - Corpses.CorpseData corpse = lastSpawnedCorpse = CorpseAPI.spawnCorpse(p, p.getLocation());
    arena.addCorpse(new Corpse(hologram, corpse));
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      hologram.delete();
      Bukkit.getScheduler().runTaskLater(plugin, corpse::destroyCorpseFromEveryone, 20 * 20);
    }, 15 * 20);
  }

  private ArmorStandHologram getLastWordsHologram(Player player) {
    ArmorStandHologram hologram = new ArmorStandHologram(player.getLocation());
    hologram.appendLine(chatManager.colorMessage("In-Game.Messages.Corpse-Last-Words", player).replace("%player%", player.getName()));
    hologram.appendLine(plugin.getLastWordsManager().getRandomLastWord(player));
    return hologram;
  }

  @EventHandler
  public void onCorpseSpawn(CorpseSpawnEvent e) {
    if(lastSpawnedCorpse == null) {
      return;
    }
    if(plugin.getConfig().getBoolean("Override-Corpses-Spawn", true) && !lastSpawnedCorpse.equals(e.getCorpse())) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onCorpseClick(CorpseClickEvent e) {
    if(ArenaRegistry.isInArena(e.getClicker())) {
      e.setCancelled(true);
      e.getClicker().closeInventory();
    }
  }

}
