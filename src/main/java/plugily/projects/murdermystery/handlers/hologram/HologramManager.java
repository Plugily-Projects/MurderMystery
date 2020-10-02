package plugily.projects.murdermystery.handlers.hologram;

import org.bukkit.entity.ArmorStand;

import java.util.ArrayList;
import java.util.List;

public class HologramManager {
  private static final List<ArmorStand> armorStands = new ArrayList<>();

  public static List<ArmorStand> getArmorStands() {
    return armorStands;
  }
}
