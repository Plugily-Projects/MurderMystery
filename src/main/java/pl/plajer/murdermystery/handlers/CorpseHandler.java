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

package pl.plajer.murdermystery.handlers;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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

import pl.plajer.murdermystery.HookManager;
import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.arena.Arena;
import pl.plajer.murdermystery.arena.ArenaRegistry;
import pl.plajer.murdermystery.arena.corpse.Corpse;
import pl.plajer.murdermystery.arena.corpse.Stand;
import pl.plajerlair.commonsbox.minecraft.compat.XMaterial;

/**
 * @author Plajer
 * <p>
 * Created at 07.10.2018
 */
public class CorpseHandler implements Listener {

  private Main plugin;
  private Corpses.CorpseData lastSpawnedCorpse;
  private Map<String, String> registeredLastWords = new HashMap<>();

  public CorpseHandler(Main plugin) {
    this.plugin = plugin;
    registerLastWord("murdermystery.lastwords.meme", ChatManager.colorMessage("In-Game.Messages.Last-Words.Meme"));
    registerLastWord("murdermystery.lastwords.rage", ChatManager.colorMessage("In-Game.Messages.Last-Words.Rage"));
    registerLastWord("murdermystery.lastwords.pro", ChatManager.colorMessage("In-Game.Messages.Last-Words.Pro"));
    registerLastWord("default", ChatManager.colorMessage("In-Game.Messages.Last-Words.Default"));
    //run bit later than hook manager to ensure it's not null
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      if (plugin.getHookManager() != null && plugin.getHookManager().isFeatureEnabled(HookManager.HookFeature.CORPSES)) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
      }
    }, 25L * 5);
  }

  public void registerLastWord(String permission, String lastWord) {
    registeredLastWords.put(permission, lastWord);
  }

  public void spawnCorpse(Player p, Arena arena) {
    if (plugin.getHookManager() != null && !plugin.getHookManager().isFeatureEnabled(HookManager.HookFeature.CORPSES)) {
      ArmorStand stand = p.getLocation().getWorld().spawn(p.getLocation().add(0.0D, -1.25D, 0.0D), ArmorStand.class);
      ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
      SkullMeta meta = (SkullMeta)head.getItemMeta();
      meta.setOwner(p.getName());
      head.setItemMeta(meta);
      stand.setVisible(false);
      stand.setHelmet(head);
      stand.setGravity(false);
      stand.setCustomNameVisible(false);
      stand.setHeadPose(new EulerAngle(Math.toRadians(p.getLocation().getX()), Math.toRadians(p.getLocation().getPitch()), Math.toRadians(p.getLocation().getZ())));
      Hologram hologram = getLastWordsHologram(p);
      arena.addHead(new Stand(hologram, stand));
      Bukkit.getScheduler().runTaskLater(plugin, () -> {
        hologram.delete();
        Bukkit.getScheduler().runTaskLater(plugin, stand::remove, 20 * 20);
      }, 15 * 20);
      return;
    }
    Hologram hologram = getLastWordsHologram(p);
    Corpses.CorpseData corpse = CorpseAPI.spawnCorpse(p, p.getLocation());
    lastSpawnedCorpse = corpse;
    arena.addCorpse(new Corpse(hologram, corpse));
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      hologram.delete();
      Bukkit.getScheduler().runTaskLater(plugin, corpse::destroyCorpseFromEveryone, 20 * 20);
    }, 15 * 20);
  }

  private Hologram getLastWordsHologram(Player p) {
    Hologram hologram = HologramsAPI.createHologram(plugin, p.getLocation().clone().add(0, 1.7, 0));
    hologram.appendTextLine(ChatManager.colorMessage("In-Game.Messages.Corpse-Last-Words", p).replace("%player%", p.getName()));
    boolean found = false;
    for (String perm : registeredLastWords.keySet()) {
      if (p.hasPermission(perm)) {
        hologram.appendTextLine(registeredLastWords.get(perm));
        found = true;
        break;
      }
    }
    if (!found) {
      hologram.appendTextLine(registeredLastWords.get("default"));
    }
    return hologram;
  }

  @EventHandler
  public void onCorpseSpawn(CorpseSpawnEvent e) {
    if (!plugin.getConfig().getBoolean("Override-Corpses-Spawn", true)) {
      return;
    }
    if (lastSpawnedCorpse == null) {
      return;
    }
    if (!e.getCorpse().equals(lastSpawnedCorpse)) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onCorpseClick(CorpseClickEvent e) {
    if (ArenaRegistry.isInArena(e.getClicker())) {
      e.setCancelled(true);
      e.getClicker().closeInventory();
    }
  }

}
