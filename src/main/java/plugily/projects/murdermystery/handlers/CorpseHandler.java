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

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import pl.plajerlair.commonsbox.minecraft.compat.XMaterial;

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
import plugily.projects.murdermystery.HookManager;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.arena.ArenaRegistry;
import plugily.projects.murdermystery.arena.corpse.Corpse;
import plugily.projects.murdermystery.arena.corpse.Stand;
import plugily.projects.murdermystery.utils.Utils;

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

  public CorpseHandler(Main plugin) {
    this.plugin = plugin;
    chatManager = plugin.getChatManager();
    registerLastWord("murdermystery.lastwords.meme", chatManager.colorMessage("In-Game.Messages.Last-Words.Meme"));
    registerLastWord("murdermystery.lastwords.rage", chatManager.colorMessage("In-Game.Messages.Last-Words.Rage"));
    registerLastWord("murdermystery.lastwords.pro", chatManager.colorMessage("In-Game.Messages.Last-Words.Pro"));
    registerLastWord("default", chatManager.colorMessage("In-Game.Messages.Last-Words.Default"));
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
      SkullMeta meta = (SkullMeta) head.getItemMeta();
      meta = Utils.setPlayerHead(p, meta);
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
    hologram.appendTextLine(chatManager.colorMessage("In-Game.Messages.Corpse-Last-Words", p).replace("%player%", p.getName()));
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
    if (!plugin.getConfig().getBoolean("Override-Corpses-Spawn", true) || lastSpawnedCorpse == null) {
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
