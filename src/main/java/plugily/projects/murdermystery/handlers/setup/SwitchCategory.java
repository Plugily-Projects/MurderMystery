package plugily.projects.murdermystery.handlers.setup;

import org.bukkit.plugin.java.JavaPlugin;
import plugily.projects.minigamesbox.classic.PluginMain;
import plugily.projects.minigamesbox.classic.handlers.setup.categories.PluginSwitchCategory;
import plugily.projects.minigamesbox.classic.handlers.setup.items.category.BooleanItem;
import plugily.projects.minigamesbox.classic.handlers.setup.items.category.SwitchItem;
import plugily.projects.minigamesbox.classic.utils.helper.ItemBuilder;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XMaterial;
import plugily.projects.minigamesbox.inventory.normal.NormalFastInv;
import plugily.projects.murdermystery.Main;

import java.util.Arrays;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 01.07.2022
 */
public class SwitchCategory extends PluginSwitchCategory {
  @Override
  public void addItems(NormalFastInv gui) {
    super.addItems(gui);
    BooleanItem goldVisuals = new BooleanItem(getSetupInventory(), new ItemBuilder(XMaterial.REDSTONE.parseMaterial()), "Gold Visuals", "Enables gold visuals to spawn\nsome particle effects above gold locations", "goldvisuals");
    gui.setItem((getInventoryLine() * 9) + 1, goldVisuals);
    getItemList().add(goldVisuals);
  }

}
