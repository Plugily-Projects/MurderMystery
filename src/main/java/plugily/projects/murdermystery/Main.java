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

package plugily.projects.murdermystery;

import org.jetbrains.annotations.TestOnly;
import plugily.projects.minigamesbox.classic.PluginMain;
import plugily.projects.minigamesbox.classic.handlers.setup.SetupInventory;
import plugily.projects.minigamesbox.classic.handlers.setup.categories.PluginSetupCategoryManager;
import plugily.projects.minigamesbox.classic.utils.services.metrics.Metrics;
import plugily.projects.murdermystery.arena.*;
import plugily.projects.murdermystery.arena.special.SpecialBlockEvents;
import plugily.projects.murdermystery.arena.special.mysterypotion.MysteryPotionRegistry;
import plugily.projects.murdermystery.arena.special.pray.PrayerRegistry;
import plugily.projects.murdermystery.boot.AdditionalValueInitializer;
import plugily.projects.murdermystery.boot.MessageInitializer;
import plugily.projects.murdermystery.boot.PlaceholderInitializer;
import plugily.projects.murdermystery.commands.arguments.ArgumentsRegistry;
import plugily.projects.murdermystery.events.PluginEvents;
import plugily.projects.murdermystery.handlers.CorpseHandler;
import plugily.projects.murdermystery.handlers.lastwords.LastWordsManager;
import plugily.projects.murdermystery.handlers.setup.SetupCategoryManager;
import plugily.projects.murdermystery.handlers.skins.sword.SwordSkinManager;
import plugily.projects.murdermystery.handlers.trails.BowTrailsHandler;
import plugily.projects.murdermystery.handlers.trails.TrailsManager;

/**
 * Created by Tigerpanzer_02 on 13.03.2022
 */
public class Main extends PluginMain {
  private ArenaRegistry arenaRegistry;
  private ArenaManager arenaManager;
  private ArgumentsRegistry argumentsRegistry;
  private LastWordsManager lastWordsManager;
  private TrailsManager trailsManager;
  private SwordSkinManager swordSkinManager;
  private HookManager hookManager;
  private CorpseHandler corpseHandler;

  @TestOnly
  public Main() {
    super();
  }

  @Override
  public void onEnable() {
    long start = System.currentTimeMillis();
    MessageInitializer messageInitializer = new MessageInitializer(this);
    super.onEnable();
    getDebugger().debug("[System] [Plugin] Initialization start");
    arenaRegistry = new ArenaRegistry(this);
    new PlaceholderInitializer(this);
    messageInitializer.registerMessages();
    new AdditionalValueInitializer(this);
    initializePluginClasses();

    if(getConfigPreferences().getOption("HIDE_NAMETAGS")) {
      getServer().getScheduler().scheduleSyncRepeatingTask(this, () ->
        getServer().getOnlinePlayers().forEach(ArenaUtils::updateNameTagsVisibility), 60, 140);
    }

    getDebugger().debug("Full {0} plugin enabled", getName());
    getDebugger()
      .debug(
        "[System] [Plugin] Initialization finished took {0}ms",
        System.currentTimeMillis() - start);
  }

  public void initializePluginClasses() {
    addFileName("lastwords");
    addFileName("powerups");
    addFileName("skins");
    addFileName("special_blocks");
    addFileName("trails");
    Arena.init(this);
    ArenaUtils.init(this);
    new ArenaEvents(this);
    arenaManager = new ArenaManager(this);
    arenaRegistry.registerArenas();
    getSignManager().loadSigns();
    getSignManager().updateSigns();
    argumentsRegistry = new ArgumentsRegistry(this);

    lastWordsManager = new LastWordsManager(this);
    new BowTrailsHandler(this);
    MysteryPotionRegistry.init(this);
    PrayerRegistry.init(this);
    new SpecialBlockEvents(this);
    trailsManager = new TrailsManager(this);
    hookManager = new HookManager(this);
    corpseHandler = new CorpseHandler(this);
    swordSkinManager = new SwordSkinManager(this);
    new PluginEvents(this);
    addPluginMetrics();
  }

  private void addPluginMetrics() {
    getMetrics()
      .addCustomChart(
        new Metrics.SimplePie(
          "hooked_addons",
          () -> {
            if(getServer().getPluginManager().getPlugin("MurderMystery-Extension") != null) {
              return "Extension";
            }
            return "None";
          }));
  }

  @Override
  public ArenaRegistry getArenaRegistry() {
    return arenaRegistry;
  }

  @Override
  public ArgumentsRegistry getArgumentsRegistry() {
    return argumentsRegistry;
  }

  @Override
  public ArenaManager getArenaManager() {
    return arenaManager;
  }

  public LastWordsManager getLastWordsManager() {
    return lastWordsManager;
  }

  public TrailsManager getTrailsManager() {
    return trailsManager;
  }

  public SwordSkinManager getSwordSkinManager() {
    return swordSkinManager;
  }

  public HookManager getHookManager() {
    return hookManager;
  }

  public CorpseHandler getCorpseHandler() {
    return corpseHandler;
  }

  @Override
  public PluginSetupCategoryManager getSetupCategoryManager(SetupInventory setupInventory) {
    return new SetupCategoryManager(setupInventory);
  }
}
