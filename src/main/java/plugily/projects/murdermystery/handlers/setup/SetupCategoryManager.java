package plugily.projects.murdermystery.handlers.setup;

import plugily.projects.minigamesbox.classic.handlers.setup.SetupInventory;
import plugily.projects.minigamesbox.classic.handlers.setup.categories.PluginSetupCategoryManager;
import plugily.projects.minigamesbox.classic.handlers.setup.categories.SetupCategory;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 01.07.2022
 */
public class SetupCategoryManager extends PluginSetupCategoryManager {

  public SetupCategoryManager(SetupInventory setupInventory) {
    super(setupInventory);
    getCategoryHandler().put(SetupCategory.LOCATIONS, new LocationCategory());
    getCategoryHandler().put(SetupCategory.SPECIFIC, new SpecificCategory());
    getCategoryHandler().put(SetupCategory.SWITCH, new SwitchCategory());
    super.init();
  }

}
