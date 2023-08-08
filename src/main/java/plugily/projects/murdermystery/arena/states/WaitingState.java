package plugily.projects.murdermystery.arena.states;

import org.bukkit.configuration.file.FileConfiguration;
import plugily.projects.minigamesbox.classic.arena.PluginArena;
import plugily.projects.minigamesbox.classic.arena.states.PluginWaitingState;
import plugily.projects.minigamesbox.classic.utils.configuration.ConfigUtils;
import plugily.projects.murdermystery.arena.Arena;

public class WaitingState extends PluginWaitingState {

  @Override
  public void handleCall(PluginArena arena) {
    super.handleCall(arena);
    Arena pluginArena = (Arena) getPlugin().getArenaRegistry().getArena(arena.getId());
    if(pluginArena == null) {
      return;
    }
    if(arena.getTimer() <= 0) {
      FileConfiguration config =
        ConfigUtils.getConfig(getPlugin(), "arenas");
      pluginArena.setGoldVisuals(config.getBoolean(pluginArena.getId() + ".goldvisuals"));
    }
  }
}
