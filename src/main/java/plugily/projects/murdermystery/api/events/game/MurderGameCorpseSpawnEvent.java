package plugily.projects.murdermystery.api.events.game;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import plugily.projects.minigamesbox.classic.api.event.PlugilyEvent;
import plugily.projects.murdermystery.arena.Arena;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 15.04.2022
 */
public class MurderGameCorpseSpawnEvent extends PlugilyEvent implements Cancellable {

  private static final HandlerList HANDLERS = new HandlerList();
  private boolean isCancelled = false;
  private final Player player;
  private final Location location;

  public MurderGameCorpseSpawnEvent(Arena arena, Player player, Location location) {
    super(arena);
    this.player = player;
    this.location = location;
  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLERS;
  }

  @Override
  public boolean isCancelled() {
    return isCancelled;
  }

  @Override
  public void setCancelled(boolean cancelled) {
    isCancelled = cancelled;
  }

  public Player getPlayer() {
    return player;
  }

  public Location getLocation() {
    return location;
  }

}
