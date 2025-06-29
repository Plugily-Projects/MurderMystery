package plugily.projects.murdermystery.boot;

import plugily.projects.minigamesbox.api.preferences.IConfigPreferences;
import plugily.projects.minigamesbox.classic.api.StatisticType;
import plugily.projects.minigamesbox.classic.api.StatsStorage;
import plugily.projects.minigamesbox.classic.arena.options.ArenaOption;
import plugily.projects.minigamesbox.classic.arena.options.ArenaOptionManager;
import plugily.projects.minigamesbox.classic.handlers.items.SpecialItemManager;
import plugily.projects.minigamesbox.classic.handlers.permissions.PermissionCategory;
import plugily.projects.minigamesbox.classic.handlers.permissions.PermissionsManager;
import plugily.projects.minigamesbox.classic.handlers.reward.RewardType;
import plugily.projects.minigamesbox.classic.handlers.reward.RewardsFactory;
import plugily.projects.minigamesbox.classic.preferences.ConfigOption;
import plugily.projects.murdermystery.Main;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 15.10.2022
 */
public class AdditionalValueInitializer {

  private final Main plugin;

  public AdditionalValueInitializer(Main plugin) {
    this.plugin = plugin;
    registerConfigOptions();
    registerStatistics();
    registerPermission();
    registerRewards();
    registerSpecialItems();
    registerArenaOptions();
  }

  private void registerConfigOptions() {
    getConfigPreferences().registerOption("CORPSES_INTEGRATION_OVERWRITE", new ConfigOption("Corpses.Integration-Overwrite", true));
    getConfigPreferences().registerOption("BOW_KILL_DETECTIVE", new ConfigOption("Bow.Kill-Detective", true));
    getConfigPreferences().registerOption("HIDE_DEATH", new ConfigOption("Hide.Death", false));
    getConfigPreferences().registerOption("HIDE_NAMETAGS", new ConfigOption("Hide.Nametags", false));
    getConfigPreferences().registerOption("GOLD_SPAWNER_MODE_ALL", new ConfigOption("Gold.Spawner-Mode", false));
    getConfigPreferences().registerOption("GOLD_LIMITER", new ConfigOption("Gold.Limiter", false));
    getConfigPreferences().registerOption("GOLD_MULTIPLE", new ConfigOption("Gold.Multiple", false));
    getConfigPreferences().registerOption("MURDERER_LOCATOR", new ConfigOption("Murderer.Locator", true));
    getConfigPreferences().registerOption("SCOREBOARD_DISPLAY", new ConfigOption("Scoreboard.Display", true));
  }

  private void registerStatistics() {
    getStatsStorage().registerStatistic("KILLS", new StatisticType("kills", true, "int(11) NOT NULL DEFAULT '0'"));
    getStatsStorage().registerStatistic("DEATHS", new StatisticType("deaths", true, "int(11) NOT NULL DEFAULT '0'"));
    getStatsStorage().registerStatistic("HIGHEST_SCORE", new StatisticType("highest_score", true, "int(11) NOT NULL DEFAULT '0'"));
    getStatsStorage().registerStatistic("CONTRIBUTION_DETECTIVE", new StatisticType("contribution_detective", true, "int(11) NOT NULL DEFAULT '0'"));
    getStatsStorage().registerStatistic("CONTRIBUTION_MURDERER", new StatisticType("contribution_murderer", true, "int(11) NOT NULL DEFAULT '0'"));
    getStatsStorage().registerStatistic("PASS_MURDERER", new StatisticType("pass_murderer", true, "int(11) NOT NULL DEFAULT '0'"));
    getStatsStorage().registerStatistic("PASS_DETECTIVE", new StatisticType("pass_detective", true, "int(11) NOT NULL DEFAULT '0'"));
    getStatsStorage().registerStatistic("DETECTIVE_WINS", new StatisticType("detective_wins", true, "int(11) NOT NULL DEFAULT '0'"));
    getStatsStorage().registerStatistic("MURDERER_WINS", new StatisticType("murderer_wins", true, "int(11) NOT NULL DEFAULT '0'"));
    getStatsStorage().registerStatistic("LOCAL_PRAISES", new StatisticType("local_praises", false, "int(11) NOT NULL DEFAULT '0'"));
    getStatsStorage().registerStatistic("LOCAL_SCORE", new StatisticType("local_score", false, "int(11) NOT NULL DEFAULT '0'"));
    getStatsStorage().registerStatistic("LOCAL_PRAY", new StatisticType("local_pray", false, "int(11) NOT NULL DEFAULT '0'"));
    getStatsStorage().registerStatistic("LOCAL_GOLD", new StatisticType("local_gold", false, "int(11) NOT NULL DEFAULT '0'"));
    getStatsStorage().registerStatistic("LOCAL_KILLS", new StatisticType("local_kills", false, "int(11) NOT NULL DEFAULT '0'"));
    getStatsStorage().registerStatistic("LOCAL_CURRENT_PRAY", new StatisticType("local_current_pray", false, "int(11) NOT NULL DEFAULT '0'"));
  }

  private void registerPermission() {
    getPermissionsManager().registerPermissionCategory("CHANCES_BOOSTER", new PermissionCategory("Chances-Boost", null));
    getPermissionsManager().registerPermissionCategory("MURDERER_BOOSTER", new PermissionCategory("Murderer-Boost", null));
    getPermissionsManager().registerPermissionCategory("DETECTIVE_BOOSTER", new PermissionCategory("Detective-Boost", null));
  }

  private void registerRewards() {
    getRewardsHandler().registerRewardType("KILL_DETECTIVE", new RewardType("detective-kill"));
    getRewardsHandler().registerRewardType("KILL_MURDERER", new RewardType("murderer-kill"));
    getRewardsHandler().registerRewardType("WIN", new RewardType("win"));
    getRewardsHandler().registerRewardType("LOSE", new RewardType("lose"));
    getRewardsHandler().registerRewardType("PLAYER_DEATH", new RewardType("player-death"));
    getRewardsHandler().registerRewardType("GOLD_PICKUP", new RewardType("gold-pickup"));
  }

  private void registerSpecialItems() {
    getSpecialItemManager().registerSpecialItem("ROLE_PASS", "Role-Pass");
  }

  private void registerArenaOptions() {
    getArenaOptionManager().registerArenaOption("DETECTIVE_DIVIDER", new ArenaOption("playerperdetective", 1));
    getArenaOptionManager().registerArenaOption("MURDERER_DIVIDER", new ArenaOption("playerpermurderer", 1));
  }

  private IConfigPreferences getConfigPreferences() {
    return plugin.getConfigPreferences();
  }

  private StatsStorage getStatsStorage() {
    return plugin.getStatsStorage();
  }

  private PermissionsManager getPermissionsManager() {
    return plugin.getPermissionsManager();
  }

  private RewardsFactory getRewardsHandler() {
    return plugin.getRewardsHandler();
  }

  private SpecialItemManager getSpecialItemManager() {
    return plugin.getSpecialItemManager();
  }

  private ArenaOptionManager getArenaOptionManager() {
    return plugin.getArenaOptionManager();
  }

}
