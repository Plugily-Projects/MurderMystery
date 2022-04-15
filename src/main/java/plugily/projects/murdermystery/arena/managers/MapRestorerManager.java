package plugily.projects.murdermystery.arena.managers;

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
    arena.getGameCharacters().clear();
    arena.getMurdererList().clear();
    arena.getDetectiveList().clear();
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
