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

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import plugily.projects.minigamesbox.classic.PluginMain;
import plugily.projects.minigamesbox.classic.api.StatisticType;
import plugily.projects.minigamesbox.classic.arena.PluginArena;
import plugily.projects.minigamesbox.classic.arena.options.ArenaOption;
import plugily.projects.minigamesbox.classic.commonsbox.number.NumberUtils;
import plugily.projects.minigamesbox.classic.handlers.language.Message;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.handlers.permissions.PermissionCategory;
import plugily.projects.minigamesbox.classic.handlers.placeholder.Placeholder;
import plugily.projects.minigamesbox.classic.handlers.reward.RewardType;
import plugily.projects.minigamesbox.classic.handlers.setup.PluginSetupInventory;
import plugily.projects.minigamesbox.classic.handlers.setup.SetupUtilities;
import plugily.projects.minigamesbox.classic.preferences.ConfigOption;
import plugily.projects.minigamesbox.classic.user.User;
import plugily.projects.minigamesbox.classic.utils.services.locale.Locale;
import plugily.projects.minigamesbox.classic.utils.services.locale.LocaleRegistry;
import plugily.projects.minigamesbox.classic.utils.services.metrics.Metrics;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.arena.ArenaEvents;
import plugily.projects.murdermystery.arena.ArenaManager;
import plugily.projects.murdermystery.arena.ArenaRegistry;
import plugily.projects.murdermystery.arena.ArenaUtils;
import plugily.projects.murdermystery.arena.role.Role;
import plugily.projects.murdermystery.arena.special.SpecialBlockEvents;
import plugily.projects.murdermystery.arena.special.mysterypotion.MysteryPotionRegistry;
import plugily.projects.murdermystery.arena.special.pray.PrayerRegistry;
import plugily.projects.murdermystery.commands.arguments.ArgumentsRegistry;
import plugily.projects.murdermystery.events.PluginEvents;
import plugily.projects.murdermystery.handlers.CorpseHandler;
import plugily.projects.murdermystery.handlers.lastwords.LastWordsManager;
import plugily.projects.murdermystery.handlers.setup.SetupInventory;
import plugily.projects.murdermystery.handlers.skins.sword.SwordSkinManager;
import plugily.projects.murdermystery.handlers.trails.BowTrailsHandler;
import plugily.projects.murdermystery.handlers.trails.TrailsManager;

import java.io.File;
import java.util.Arrays;

/**
 * Created by Tigerpanzer_02 on 13.03.2022
 */
public class Main extends PluginMain {
  private FileConfiguration entityUpgradesConfig;
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

  @TestOnly
  protected Main(
      JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
    super(loader, description, dataFolder, file);
  }

  @Override
  public void onEnable() {
    long start = System.currentTimeMillis();
    registerLocales();
    super.onEnable();
    getDebugger().debug("[System] [Plugin] Initialization start");
    registerPlaceholders();
    addMessages();
    addAdditionalValues();
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
    addArenaOptions();
    Arena.init(this);
    ArenaUtils.init(this);
    new ArenaEvents(this);
    arenaManager = new ArenaManager(this);
    arenaRegistry = new ArenaRegistry(this);
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

  public void registerLocales() {
    Arrays.asList(
            new Locale(
                "Chinese (Traditional)",
                "简体中文",
                "zh_HK",
                "POEditor contributors",
                Arrays.asList("中文(傳統)", "中國傳統", "chinese_traditional", "zh")),
            new Locale(
                "Chinese (Simplified)",
                "简体中文",
                "zh_CN",
                "POEditor contributors",
                Arrays.asList("简体中文", "中文", "chinese", "chinese_simplified", "cn")),
            new Locale(
                "Czech",
                "Český",
                "cs_CZ",
                "POEditor contributors",
                Arrays.asList("czech", "cesky", "český", "cs")),
            new Locale(
                "Dutch",
                "Nederlands",
                "nl_NL",
                "POEditor contributors",
                Arrays.asList("dutch", "nederlands", "nl")),
            new Locale(
                "English",
                "English",
                "en_GB",
                "Tigerpanzer_02",
                Arrays.asList("default", "english", "en")),
            new Locale(
                "French",
                "Français",
                "fr_FR",
                "POEditor contributors",
                Arrays.asList("french", "francais", "français", "fr")),
            new Locale(
                "German",
                "Deutsch",
                "de_DE",
                "Tigerkatze and POEditor contributors",
                Arrays.asList("deutsch", "german", "de")),
            new Locale(
                "Hungarian",
                "Magyar",
                "hu_HU",
                "POEditor contributors",
                Arrays.asList("hungarian", "magyar", "hu")),
            new Locale(
                "Indonesian",
                "Indonesia",
                "id_ID",
                "POEditor contributors",
                Arrays.asList("indonesian", "indonesia", "id")),
            new Locale(
                "Italian",
                "Italiano",
                "it_IT",
                "POEditor contributors",
                Arrays.asList("italian", "italiano", "it")),
            new Locale(
                "Korean",
                "한국의",
                "ko_KR",
                "POEditor contributors",
                Arrays.asList("korean", "한국의", "kr")),
            new Locale(
                "Lithuanian",
                "Lietuviešu",
                "lt_LT",
                "POEditor contributors",
                Arrays.asList("lithuanian", "lietuviešu", "lietuviesu", "lt")),
            new Locale(
                "Polish", "Polski", "pl_PL", "Plajer", Arrays.asList("polish", "polski", "pl")),
            new Locale(
                "Portuguese (BR)",
                "Português Brasileiro",
                "pt_BR",
                "POEditor contributors",
                Arrays.asList("brazilian", "brasil", "brasileiro", "pt-br", "pt_br")),
            new Locale(
                "Romanian",
                "Românesc",
                "ro_RO",
                "POEditor contributors",
                Arrays.asList("romanian", "romanesc", "românesc", "ro")),
            new Locale(
                "Russian",
                "Pусский",
                "ru_RU",
                "POEditor contributors",
                Arrays.asList("russian", "pусский", "pyccknn", "russkiy", "ru")),
            new Locale(
                "Spanish",
                "Español",
                "es_ES",
                "POEditor contributors",
                Arrays.asList("spanish", "espanol", "español", "es")),
            new Locale(
                "Thai", "Thai", "th_TH", "POEditor contributors", Arrays.asList("thai", "th")),
            new Locale(
                "Turkish",
                "Türk",
                "tr_TR",
                "POEditor contributors",
                Arrays.asList("turkish", "turk", "türk", "tr")),
            new Locale(
                "Vietnamese",
                "Việt",
                "vn_VN",
                "POEditor contributors",
                Arrays.asList("vietnamese", "viet", "việt", "vn")))
        .forEach(LocaleRegistry::registerLocale);
  }

  public void addAdditionalValues() {
    getConfigPreferences()
        .registerOption(
            "CORPSES_INTEGRATION_OVERWRITE",
            new ConfigOption("Corpses.Integration-Overwrite", true));
    getConfigPreferences()
        .registerOption("BOW_KILL_DETECTIVE", new ConfigOption("Bow.Kill-Detective", true));
    getConfigPreferences().registerOption("HIDE_DEATH", new ConfigOption("Hide.Death", false));
    getConfigPreferences()
        .registerOption("HIDE_NAMETAGS", new ConfigOption("Hide.Nametags", false));
    getConfigPreferences().registerOption("GOLD_SPAWNER_MODE_ALL", new ConfigOption("Gold.Spawner-Mode", false));
    getConfigPreferences().registerOption("GOLD_LIMITER", new ConfigOption("Gold.Limiter", false));
    getConfigPreferences().registerOption("MURDERER_LOCATOR", new ConfigOption("Murderer.Locator", true));
    getStatsStorage()
        .registerStatistic(
            "KILLS", new StatisticType("kills", true, "int(11) NOT NULL DEFAULT '0'"));
    getStatsStorage()
        .registerStatistic(
            "DEATHS", new StatisticType("deaths", true, "int(11) NOT NULL DEFAULT '0'"));
    getStatsStorage()
        .registerStatistic(
            "HIGHEST_SCORE",
            new StatisticType("highest_score", true, "int(11) NOT NULL DEFAULT '0'"));
    getStatsStorage()
        .registerStatistic(
            "CONTRIBUTION_DETECTIVE",
            new StatisticType("contribution_detective", true, "int(11) NOT NULL DEFAULT '0'"));
    getStatsStorage()
        .registerStatistic(
            "CONTRIBUTION_MURDERER",
            new StatisticType("contribution_murderer", true, "int(11) NOT NULL DEFAULT '0'"));
    getStatsStorage()
        .registerStatistic(
            "PASS_MURDERER",
            new StatisticType("pass_murderer", true, "int(11) NOT NULL DEFAULT '0'"));
    getStatsStorage()
        .registerStatistic(
            "PASS_DETECTIVE",
            new StatisticType("pass_detective", true, "int(11) NOT NULL DEFAULT '0'"));
    getStatsStorage()
        .registerStatistic(
            "LOCAL_PRAISES",
            new StatisticType("local_praises", false, "int(11) NOT NULL DEFAULT '0'"));
    getStatsStorage()
        .registerStatistic(
            "LOCAL_SCORE", new StatisticType("local_score", false, "int(11) NOT NULL DEFAULT '0'"));
    getStatsStorage()
        .registerStatistic(
            "LOCAL_PRAY", new StatisticType("local_pray", false, "int(11) NOT NULL DEFAULT '0'"));
    getStatsStorage()
        .registerStatistic(
            "LOCAL_GOLD", new StatisticType("local_gold", false, "int(11) NOT NULL DEFAULT '0'"));
    getStatsStorage()
        .registerStatistic(
            "LOCAL_KILLS", new StatisticType("local_kills", false, "int(11) NOT NULL DEFAULT '0'"));

    getPermissionsManager()
        .registerPermissionCategory(
            "CHANCES_BOOSTER", new PermissionCategory("Chances-Boost", null));
    getPermissionsManager()
        .registerPermissionCategory(
            "MURDERER_BOOSTER", new PermissionCategory("Murderer-Boost", null));
    getPermissionsManager()
        .registerPermissionCategory(
            "DETECTIVE_BOOSTER", new PermissionCategory("Detective-Boost", null));

    getRewardsHandler().registerRewardType("KILL_DETECTIVE", new RewardType("detective-kill"));
    getRewardsHandler().registerRewardType("KILL_MURDERER", new RewardType("murderer-kill"));
    getRewardsHandler().registerRewardType("WIN", new RewardType("win"));
    getRewardsHandler().registerRewardType("LOSE", new RewardType("lose"));
    getRewardsHandler().registerRewardType("PLAYER_DEATH", new RewardType("player-death"));
    getRewardsHandler().registerRewardType("GOLD_PICKUP", new RewardType("gold-pickup"));

    getSpecialItemManager().registerSpecialItem("ROLE_PASS", "Role-Pass");
  }

  public void addMessages() {
    getMessageManager().registerMessage("", new Message("", ""));
    getMessageManager()
        .registerMessage(
            "SCOREBOARD_ROLES_DETECTIVE", new Message("Scoreboard.Roles.Detective", ""));
    getMessageManager()
        .registerMessage("SCOREBOARD_ROLES_MURDERER", new Message("Scoreboard.Roles.Murderer", ""));
    getMessageManager()
        .registerMessage("SCOREBOARD_ROLES_INNOCENT", new Message("Scoreboard.Roles.Innocent", ""));
    getMessageManager()
        .registerMessage("SCOREBOARD_ROLES_DEAD", new Message("Scoreboard.Roles.Dead", ""));
    getMessageManager()
        .registerMessage(
            "SCOREBOARD_DETECTIVE_ALIVE", new Message("Scoreboard.Detective.Alive", ""));
    getMessageManager()
        .registerMessage(
            "SCOREBOARD_DETECTIVE_BOW_DROPPED",
            new Message("Scoreboard.Detective.Bow.Dropped", ""));
    getMessageManager()
        .registerMessage(
            "SCOREBOARD_DETECTIVE_BOW_PICKED", new Message("Scoreboard.Detective.Bow.Picked", ""));

    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_GAME_END_PLACEHOLDERS_MURDERER_STOPPED",
            new Message("In-Game.Messages.Game-End.Placeholders.Murderer.Stopped", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_GAME_END_PLACEHOLDERS_MURDERER_KILLED_YOU",
            new Message("In-Game.Messages.Game-End.Placeholders.Murderer.Killed.You", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_GAME_END_PLACEHOLDERS_MURDERER_KILLED_ALL",
            new Message("In-Game.Messages.Game-End.Placeholders.Murderer.Killed.All", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_GAME_END_PLACEHOLDERS_INNOCENT_KILLED_YOU",
            new Message("In-Game.Messages.Game-End.Placeholders.Innocent.Killed.You", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_GAME_END_PLACEHOLDERS_INNOCENT_KILLED_WRONGLY",
            new Message("In-Game.Messages.Game-End.Placeholders.Innocent.Killed.All", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_GAME_END_PLACEHOLDERS_NOBODY",
            new Message("In-Game.Messages.Game-End.Placeholders.Nobody", ""));

    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_ROLE_CHANCES_ACTION_BAR",
            new Message("In-Game.Messages.Arena.Chances.Action-Bar", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_COOLDOWN", new Message("In-Game.Messages.Arena.Cooldown", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_LOCATOR_BOW",
            new Message("In-Game.Messages.Arena.Locator.Bow", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_LOCATOR_INNOCENT",
            new Message("In-Game.Messages.Arena.Locator.Innocent", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_LOCATOR_WATCH_OUT",
            new Message("In-Game.Messages.Arena.Locator.Watch-Out", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PASS_NAME",
            new Message("In-Game.Messages.Arena.Pass.Name", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PASS_ROLE_MURDERER_NAME",
            new Message("In-Game.Messages.Arena.Pass.Role.Murderer.Name", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PASS_ROLE_MURDERER_LORE",
            new Message("In-Game.Messages.Arena.Pass.Role.Murderer.Lore", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PASS_ROLE_DETECTIVE_NAME",
            new Message("In-Game.Messages.Arena.Pass.Role.Detective.Name", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PASS_ROLE_DETECTIVE_LORE",
            new Message("In-Game.Messages.Arena.Pass.Role.Detective.Lore", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PASS_FAIL",
            new Message("In-Game.Messages.Arena.Pass.Fail", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PASS_SUCCESS",
            new Message("In-Game.Messages.Arena.Pass.Success", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PASS_CHANGE",
            new Message("In-Game.Messages.Arena.Pass.Change", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_TIME_LEFT",
            new Message("In-Game.Messages.Arena.Playing.Time-Left", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_ROLE_CHANGE",
            new Message("In-Game.Messages.Arena.Playing.Role.Change", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_ROLE_MURDERER",
            new Message("In-Game.Messages.Arena.Playing.Role.Murderer", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_ROLE_DETECTIVE",
            new Message("In-Game.Messages.Arena.Playing.Role.Detective", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_ROLE_INNOCENT",
            new Message("In-Game.Messages.Arena.Playing.Role.Innocent", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_SCORE_BONUS",
            new Message("In-Game.Messages.Arena.Playing.Score.Bonus", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_SCORE_GOLD",
            new Message("In-Game.Messages.Arena.Playing.Score.Gold", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_SCORE_ACTION_KILL_PLAYER",
            new Message("In-Game.Messages.Arena.Playing.Score.Action.Kill.Player", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_SCORE_ACTION_KILL_MURDERER",
            new Message("In-Game.Messages.Arena.Playing.Score.Action.Kill.Murderer", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_SCORE_ACTION_KILL_INNOCENT",
            new Message("In-Game.Messages.Arena.Playing.Score.Action.Kill.Innocent", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_SCORE_ACTION_PICKUP_GOLD",
            new Message("In-Game.Messages.Arena.Playing.Score.Action.Pickup.Gold", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_SCORE_ACTION_SURVIVING_TIME",
            new Message("In-Game.Messages.Arena.Playing.Score.Action.Surviving.Time", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_SCORE_ACTION_SURVIVING_END",
            new Message("In-Game.Messages.Arena.Playing.Score.Action.Surviving.End", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_SCORE_ACTION_WIN",
            new Message("In-Game.Messages.Arena.Playing.Score.Action.Win", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_SCORE_ACTION_DETECTIVE",
            new Message("In-Game.Messages.Arena.Playing.Score.Action.Detective", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_SWORD_SOON",
            new Message("In-Game.Messages.Arena.Playing.Sword.Soon", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_SPECIAL_BLOCKS_CAULDRON_POTION",
            new Message("In-Game.Messages.Arena.Playing.Special-Blocks.Potion", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_SPECIAL_BLOCKS_CAULDRON_HOLOGRAM",
            new Message("In-Game.Messages.Arena.Playing.Special-Blocks.Hologram", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_SPECIAL_BLOCKS_NOT_ENOUGH_GOLD",
            new Message("In-Game.Messages.Arena.Playing.Special-Blocks.Not-Enough-Gold", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_SPECIAL_BLOCKS_PRAY_HOLOGRAM",
            new Message("In-Game.Messages.Arena.Playing.Special-Blocks.Pray.Hologram", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_SPECIAL_BLOCKS_PRAY_CHAT",
            new Message("In-Game.Messages.Arena.Playing.Special-Blocks.Pray.Chat", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_SPECIAL_BLOCKS_PRAY_PAY",
            new Message("In-Game.Messages.Arena.Playing.Special-Blocks.Pray.Pay", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_SPECIAL_BLOCKS_PRAY_PRAISE_HEARD",
            new Message("In-Game.Messages.Arena.Playing.Special-Blocks.Pray.Praise.Heard", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_SPECIAL_BLOCKS_PRAY_PRAISE_FEELING_BLESSED",
            new Message(
                "In-Game.Messages.Arena.Playing.Special-Blocks.Pray.Praise.Feeling.Blessed", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_SPECIAL_BLOCKS_PRAY_PRAISE_FEELING_CURSED",
            new Message(
                "In-Game.Messages.Arena.Playing.Special-Blocks.Pray.Praise.Feeling.Cursed", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_SPECIAL_BLOCKS_PRAY_PRAISE_GIFTS_DETECTIVE_REVELATION",
            new Message(
                "In-Game.Messages.Arena.Playing.Special-Blocks.Pray.Praise.Gifts.Detective-Revelation",
                ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_SPECIAL_BLOCKS_PRAY_PRAISE_GIFTS_GOLD_RUSH",
            new Message(
                "In-Game.Messages.Arena.Playing.Special-Blocks.Pray.Praise.Gifts.Gold-Rush", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_SPECIAL_BLOCKS_PRAY_PRAISE_GIFTS_SINGLE_COMPENSATION",
            new Message(
                "In-Game.Messages.Arena.Playing.Special-Blocks.Pray.Praise.Gifts.Single-Compensation",
                ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_SPECIAL_BLOCKS_PRAY_PRAISE_GIFTS_BOW",
            new Message("In-Game.Messages.Arena.Playing.Special-Blocks.Pray.Praise.Gifts.Bow", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_SPECIAL_BLOCKS_PRAY_PRAISE_CURSES_SLOWNESS",
            new Message(
                "In-Game.Messages.Arena.Playing.Special-Blocks.Pray.Praise.Curses.Slowness", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_SPECIAL_BLOCKS_PRAY_PRAISE_CURSES_BLINDNESS",
            new Message(
                "In-Game.Messages.Arena.Playing.Special-Blocks.Pray.Praise.Curses.Blindness", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_SPECIAL_BLOCKS_PRAY_PRAISE_CURSES_GOLD",
            new Message(
                "In-Game.Messages.Arena.Playing.Special-Blocks.Pray.Praise.Curses.Gold", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_SPECIAL_BLOCKS_PRAY_PRAISE_CURSES_DEATH",
            new Message(
                "In-Game.Messages.Arena.Playing.Special-Blocks.Pray.Praise.Curses.Death", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_BOW_DROPPED",
            new Message("In-Game.Messages.Arena.Playing.Bow.Dropped", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_BOW_PICKUP",
            new Message("In-Game.Messages.Arena.Playing.Bow.Pickup", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_BOW_SHOT_GOLD",
            new Message("In-Game.Messages.Arena.Playing.Bow.Shot.Gold", ""));
    getMessageManager()
        .registerMessage(
            "IN_GAME_MESSAGES_ARENA_PLAYING_BOW_SHOT_TITLE",
            new Message("In-Game.Messages.Arena.Playing.Bow.Shot.Title", ""));
    getMessageManager()
        .registerMessage(
            "LEADERBOARD_STATISTICS_CONTRIBUTION_DETECTIVE",
            new Message("Leaderboard.Statistics.Detective-Contribution", ""));
    getMessageManager()
        .registerMessage(
            "LEADERBOARD_STATISTICS_CONTRIBUTION_MURDERER",
            new Message("Leaderboard.Statistics.Murderer-Contribution", ""));

    getMessageManager()
        .registerMessage(
            "LEADERBOARD_STATISTICS_PASS_DETECTIVE",
            new Message("Leaderboard.Statistics.Detective-Pass", ""));
    getMessageManager()
        .registerMessage(
            "LEADERBOARD_STATISTICS_PASS_MURDERER",
            new Message("Leaderboard.Statistics.Murderer-Pass", ""));
    getMessageManager().registerMessage("LEADERBOARD_STATISTICS_KILLS", new Message("Leaderboard.Statistics.Kills", ""));
    getMessageManager().registerMessage("LEADERBOARD_STATISTICS_DEATHS", new Message("Leaderboard.Statistics.Deaths", ""));
    getMessageManager().registerMessage("LEADERBOARD_STATISTICS_HIGHEST_SCORE", new Message("Leaderboard.Statistics.Highest-Score", ""));
  }

  public void registerPlaceholders() {

    getPlaceholderManager()
        .registerPlaceholder(
            new Placeholder(
                "detective_list",
                Placeholder.PlaceholderType.ARENA,
                Placeholder.PlaceholderExecutor.ALL) {
              @Override
              public String getValue(Player player, PluginArena arena) {
                Arena pluginArena = getArenaRegistry().getArena(arena.getId());
                if(pluginArena == null) {
                  return null;
                }

                StringBuilder detectives = new StringBuilder();
                for(Player p : pluginArena.getDetectiveList()) {
                  detectives.append(p.getName()).append(", ");
                }

                int index = detectives.length() - 2;
                if(index > 0 && index < detectives.length()) {
                  detectives.deleteCharAt(index);
                }

                return (pluginArena.isDetectiveDead() ? ChatColor.STRIKETHROUGH : "")
                    + detectives.toString();
              }
            });

    getPlaceholderManager()
        .registerPlaceholder(
            new Placeholder(
                "murderer_list",
                Placeholder.PlaceholderType.ARENA,
                Placeholder.PlaceholderExecutor.ALL) {
              @Override
              public String getValue(Player player, PluginArena arena) {
                Arena pluginArena = getArenaRegistry().getArena(arena.getId());
                if(pluginArena == null) {
                  return null;
                }

                StringBuilder murders = new StringBuilder();
                for(Player p : pluginArena.getMurdererList()) {
                  User user = getUserManager().getUser(p);
                  int localKills = user.getStatistic("LOCAL_KILLS");
                  murders.append(p.getName());
                  if(pluginArena.getMurdererList().size() > 1) {
                    murders.append(" (").append(localKills).append("), ");
                  }
                }
                if(pluginArena.getMurdererList().size() > 1) {
                  murders.deleteCharAt(murders.length() - 2);
                }

                return (pluginArena.aliveMurderer() == 1 ? "" : ChatColor.STRIKETHROUGH)
                    + murders.toString();
              }
            });

    getPlaceholderManager()
        .registerPlaceholder(
            new Placeholder(
                "murderer_kills",
                Placeholder.PlaceholderType.ARENA,
                Placeholder.PlaceholderExecutor.ALL) {
              @Override
              public String getValue(Player player, PluginArena arena) {
                Arena pluginArena = getArenaRegistry().getArena(arena.getId());
                if(pluginArena == null) {
                  return null;
                }
                int murdererKills = 0;
                for(Player p : pluginArena.getMurdererList()) {
                  User user = getUserManager().getUser(p);
                  int localKills = user.getStatistic("LOCAL_KILLS");
                  murdererKills += localKills;
                }
                return Integer.toString(murdererKills);
              }
            });

    getPlaceholderManager()
        .registerPlaceholder(
            new Placeholder(
                "hero", Placeholder.PlaceholderType.ARENA, Placeholder.PlaceholderExecutor.ALL) {
              @Override
              public String getValue(Player player, PluginArena arena) {
                Arena pluginArena = getArenaRegistry().getArena(arena.getId());
                if(pluginArena == null) {
                  return null;
                }
                Player hero = pluginArena.getCharacter(Arena.CharacterType.HERO);
                return hero != null
                    ? hero.getName()
                    : new MessageBuilder("IN_GAME_MESSAGES_GAME_END_PLACEHOLDERS_NOBODY")
                    .asKey()
                    .build();
              }
            });

    getPlaceholderManager()
        .registerPlaceholder(
            new Placeholder(
                "murderer_chance",
                Placeholder.PlaceholderType.ARENA,
                Placeholder.PlaceholderExecutor.ALL) {
              @Override
              public String getValue(Player player, PluginArena arena) {
                Arena pluginArena = getArenaRegistry().getArena(arena.getId());
                if(pluginArena == null) {
                  return null;
                }

                int totalMurderer = 0;

                for(Player p : arena.getPlayers()) {
                  User user = getUserManager().getUser(p);
                  totalMurderer += user.getStatistic("CONTRIBUTION_MURDERER");
                }
                if(totalMurderer == 0) {
                  totalMurderer = 1;
                }
                User user = getUserManager().getUser(player);
                return NumberUtils.round(
                    ((double) user.getStatistic("CONTRIBUTION_MURDERER")
                        / (double) totalMurderer)
                        * 100.0,
                    2)
                    + "%";
              }
            });

    getPlaceholderManager()
        .registerPlaceholder(
            new Placeholder(
                "detective_chance",
                Placeholder.PlaceholderType.ARENA,
                Placeholder.PlaceholderExecutor.ALL) {
              @Override
              public String getValue(Player player, PluginArena arena) {
                Arena pluginArena = getArenaRegistry().getArena(arena.getId());
                if(pluginArena == null) {
                  return null;
                }

                int totalDetectives = 0;

                for(Player p : arena.getPlayers()) {
                  User user = getUserManager().getUser(p);
                  totalDetectives += user.getStatistic("CONTRIBUTION_DETECTIVE");
                }
                if(totalDetectives == 0) {
                  totalDetectives = 1;
                }
                User user = getUserManager().getUser(player);
                return NumberUtils.round(
                    ((double) user.getStatistic("CONTRIBUTION_DETECTIVE")
                        / (double) totalDetectives)
                        * 100.0,
                    2)
                    + "%";
              }
            });

    getPlaceholderManager()
        .registerPlaceholder(
            new Placeholder(
                "detective_status",
                Placeholder.PlaceholderType.ARENA,
                Placeholder.PlaceholderExecutor.ALL) {
              @Override
              public String getValue(Player player, PluginArena arena) {
                Arena pluginArena = getArenaRegistry().getArena(arena.getId());
                if(pluginArena == null) {
                  return null;
                }

                if(pluginArena.isDetectiveDead()) {
                  if(!pluginArena.isCharacterSet(Arena.CharacterType.FAKE_DETECTIVE)) {
                    return new MessageBuilder("SCOREBOARD_DETECTIVE_BOW_DROPPED").asKey().build();
                  } else {
                    return new MessageBuilder("SCOREBOARD_DETECTIVE_BOW_PICKED").asKey().build();
                  }
                } else {
                  return new MessageBuilder("SCOREBOARD_DETECTIVE_ALIVE").asKey().build();
                }
              }
            });

    getPlaceholderManager()
        .registerPlaceholder(
            new Placeholder(
                "innocent_size",
                Placeholder.PlaceholderType.ARENA,
                Placeholder.PlaceholderExecutor.ALL) {
              @Override
              public String getValue(Player player, PluginArena arena) {
                Arena pluginArena = getArenaRegistry().getArena(arena.getId());
                if(pluginArena == null) {
                  return null;
                }
                int innocents = 0;
                for(Player p : arena.getPlayersLeft()) {
                  if(!Role.isRole(Role.MURDERER, getUserManager().getUser(p))) {
                    innocents++;
                  }
                }
                return Integer.toString(innocents);
              }
            });

    getPlaceholderManager()
        .registerPlaceholder(
            new Placeholder(
                "player_role", Placeholder.PlaceholderType.ARENA, Placeholder.PlaceholderExecutor.ALL) {
              @Override
              public String getValue(Player player, PluginArena arena) {
                Arena pluginArena = getArenaRegistry().getArena(arena.getId());
                if(pluginArena == null) {
                  return null;
                }
                User user = getUserManager().getUser(player);
                String role;
                if(pluginArena.isDeathPlayer(player)) {
                  role = new MessageBuilder("SCOREBOARD_ROLES_DEAD").asKey().build();
                } else if(Role.isRole(Role.MURDERER, user, arena)) {
                  role = new MessageBuilder("SCOREBOARD_ROLES_DETECTIVE").asKey().build();
                } else if(Role.isRole(Role.ANY_DETECTIVE, user, arena)) {
                  role = new MessageBuilder("SCOREBOARD_ROLES_MURDERER").asKey().build();
                } else {
                  role = new MessageBuilder("SCOREBOARD_ROLES_INNOCENT").asKey().build();
                }
                return role;
              }
            });

    getPlaceholderManager()
        .registerPlaceholder(
            new Placeholder(
                "summary_player",
                Placeholder.PlaceholderType.ARENA,
                Placeholder.PlaceholderExecutor.ALL) {
              @Override
              public String getValue(Player player, PluginArena arena) {
                return getSummary(player, arena);
              }

              @Nullable
              private String getSummary(Player player, PluginArena arena) {
                Arena pluginArena = getArenaRegistry().getArena(arena.getId());
                if(pluginArena == null) {
                  return null;
                }
                String summaryEnding;

                if(pluginArena.getMurdererList().containsAll(pluginArena.getPlayersLeft())
                    && pluginArena.getMurdererList().contains(player)) {
                  summaryEnding =
                      new MessageBuilder("IN_GAME_MESSAGES_GAME_END_PLACEHOLDERS_WIN")
                          .asKey()
                          .arena(pluginArena)
                          .build();
                } else if(!pluginArena.getMurdererList().containsAll(pluginArena.getPlayersLeft())
                    && !pluginArena.getMurdererList().contains(player)) {
                  summaryEnding =
                      new MessageBuilder("IN_GAME_MESSAGES_GAME_END_PLACEHOLDERS_WIN")
                          .asKey()
                          .arena(pluginArena)
                          .build();
                } else {
                  summaryEnding =
                      new MessageBuilder("IN_GAME_MESSAGES_GAME_END_PLACEHOLDERS_LOSE")
                          .asKey()
                          .arena(pluginArena)
                          .build();
                }
                return summaryEnding;
              }
            });
    getPlaceholderManager()
        .registerPlaceholder(
            new Placeholder(
                "summary", Placeholder.PlaceholderType.ARENA, Placeholder.PlaceholderExecutor.ALL) {
              @Override
              public String getValue(Player player, PluginArena arena) {
                return getSummary(arena);
              }

              @Override
              public String getValue(PluginArena arena) {
                return getSummary(arena);
              }

              @Nullable
              private String getSummary(PluginArena arena) {
                Arena pluginArena = getArenaRegistry().getArena(arena.getId());
                if(pluginArena == null) {
                  return null;
                }
                String summaryEnding;

                if(pluginArena.getMurdererList().containsAll(pluginArena.getPlayersLeft())) {
                  summaryEnding =
                      new MessageBuilder(
                          "IN_GAME_MESSAGES_GAME_END_PLACEHOLDERS_MURDERER_KILLED_ALL")
                          .asKey()
                          .arena(pluginArena)
                          .build();
                } else {
                  summaryEnding =
                      new MessageBuilder("IN_GAME_MESSAGES_GAME_END_PLACEHOLDERS_MURDERER_STOPPED")
                          .asKey()
                          .arena(pluginArena)
                          .build();
                }
                return summaryEnding;
              }
            });
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

  private void addArenaOptions() {
    getArenaOptionManager().registerArenaOption("DETECTIVE_DIVIDER", new ArenaOption("null", 1));
    getArenaOptionManager().registerArenaOption("MURDERER_DIVIDER", new ArenaOption("null", 1));
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
  public PluginSetupInventory openSetupInventory(PluginArena arena, Player player) {
    return new SetupInventory(this, arena, player);
  }

  @Override
  public PluginSetupInventory openSetupInventory(
      PluginArena arena, Player player, SetupUtilities.InventoryStage inventoryStage) {
    return new SetupInventory(this, arena, player, inventoryStage);
  }
}
