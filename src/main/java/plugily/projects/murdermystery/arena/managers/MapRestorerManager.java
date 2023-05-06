/*
 * MurderMystery - Find the murderer, kill him and survive!
 * Copyright (c) 2022  Plugily Projects - maintained by Tigerpanzer_02 and contributors
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

package plugily.projects.murdermystery.arena.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.golde.bukkit.corpsereborn.CorpseAPI.CorpseAPI;
import plugily.projects.minigamesbox.classic.arena.managers.PluginMapRestorerManager;
import plugily.projects.murdermystery.HookManager;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.arena.corpse.Corpse;
import plugily.projects.murdermystery.arena.corpse.Stand;

import java.util.Objects;

public class MapRestorerManager extends PluginMapRestorerManager {

  public final Arena arena;

  public MapRestorerManager(Arena arena) {
    super(arena);
    this.arena = arena;
  }

  @Override
  public void fullyRestoreArena() {
    cleanUpArena();
    super.fullyRestoreArena();
  }


  public void cleanUpArena() {
    removeBowHolo();
    arena.setMurdererLocatorReceived(false);
    arena.getDetectiveContributions().clear();
    arena.getMurdererContributions().clear();
    arena.getGameCharacters().clear();
    arena.getMurdererList().clear();
    arena.getDetectiveList().clear();
    arena.getDeaths().clear();
    arena.setDetectiveDead(false);
    clearCorpses();
    clearGold();
  }

  public void removeBowHolo() {
    if(arena.getBowHologram() != null && !arena.getBowHologram().isDeleted()) {
      arena.getBowHologram().delete();
    }
    arena.setBowHologram(null);
  }

  public void clearGold() {
    arena.getGoldSpawned().stream().filter(Objects::nonNull).forEach(Item::remove);
    arena.getGoldSpawned().clear();
  }

  public void clearCorpses() {
    if(!arena.getPlugin().getHookManager().isFeatureEnabled(HookManager.HookFeature.CORPSES)) {
      for(Stand stand : arena.getStands()) {
        if(!stand.getHologram().isDeleted()) {
          stand.getHologram().delete();
        }
        if(stand.getStand() != null) {
          stand.getStand().remove();
        }
      }
      arena.getStands().clear();
      return;
    }
    for(Corpse corpse : arena.getCorpses()) {
      if(!corpse.getHologram().isDeleted()) {
        corpse.getHologram().delete();
      }
      if(corpse.getCorpseData() != null) {
        corpse.getCorpseData().destroyCorpseFromEveryone();
        CorpseAPI.removeCorpse(corpse.getCorpseData());
      }
    }
    arena.getCorpses().clear();
  }
}
