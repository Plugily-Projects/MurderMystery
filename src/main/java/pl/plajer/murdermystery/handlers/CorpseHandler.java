/*
 * Murder Mystery is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Murder Mystery is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Murder Mystery.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.plajer.murdermystery.handlers;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.golde.bukkit.corpsereborn.CorpseAPI.CorpseAPI;
import org.golde.bukkit.corpsereborn.CorpseAPI.events.CorpseSpawnEvent;
import org.golde.bukkit.corpsereborn.nms.Corpses;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.arena.Arena;
import pl.plajer.murdermystery.arena.ArenaCorpse;
import pl.plajerlair.core.services.exception.ReportedException;

/**
 * @author Plajer
 * <p>
 * Created at 07.10.2018
 */
public class CorpseHandler implements Listener {

  private Main plugin;
  private Corpses.CorpseData lastSpawnedCorpse;

  public CorpseHandler(Main plugin) {
    this.plugin = plugin;
    if (plugin.getConfig().getBoolean("Override-Corpses-Spawn", true)) {
      plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
  }

  public void spawnCorpse(Player p, Arena arena) {
    try {
      Corpses.CorpseData corpse = CorpseAPI.spawnCorpse(p, p.getLocation());
      Hologram hologram = HologramsAPI.createHologram(plugin, p.getLocation().clone().add(0, 1.5, 0));
      hologram.appendTextLine(ChatManager.colorMessage("In-Game.Messages.Corpse-Last-Words").replace("%player%", p.getName()));
      //todo priority note to wiki
      if (p.hasPermission("murdermystery.lastwords.meme")) {
        hologram.appendTextLine(ChatManager.colorMessage("In-Game.Messages.Last-Words.Meme"));
      } else if (p.hasPermission("murdermystery.lastwords.rage")) {
        hologram.appendTextLine(ChatManager.colorMessage("In-Game.Messages.Last-Words.Rage"));
      } else if (p.hasPermission("murdermystery.lastwords.pro")) {
        hologram.appendTextLine(ChatManager.colorMessage("In-Game.Messages.Last-Words.Pro"));
      } else {
        hologram.appendTextLine(ChatManager.colorMessage("In-Game.Messages.Last-Words.Default"));
      }
      lastSpawnedCorpse = corpse;
      arena.addCorpse(new ArenaCorpse(hologram, corpse));
    } catch (Exception ex) {
      new ReportedException(plugin, ex);
    }
  }

  @EventHandler
  public void onCorpseSpawn(CorpseSpawnEvent e) {
    if (lastSpawnedCorpse == null) {
      return;
    }
    if (e.getCorpse() != lastSpawnedCorpse) {
      e.setCancelled(true);
    }
  }

}
