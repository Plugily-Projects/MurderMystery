/*
 * MurderMystery - Find the murderer, kill him and survive!
 * Copyright (C) 2019  Plajer's Lair - maintained by Plajer and contributors
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

package pl.plajer.murdermystery;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import java.io.File;
import java.util.Arrays;

import me.tigerhix.lib.scoreboard.ScoreboardLib;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

import pl.plajer.murdermystery.api.StatsStorage;
import pl.plajer.murdermystery.arena.Arena;
import pl.plajer.murdermystery.arena.ArenaEvents;
import pl.plajer.murdermystery.arena.ArenaRegistry;
import pl.plajer.murdermystery.arena.special.SpecialBlockEvents;
import pl.plajer.murdermystery.arena.special.mysterypotion.MysteryPotionRegistry;
import pl.plajer.murdermystery.arena.special.pray.PrayerRegistry;
import pl.plajer.murdermystery.commands.MainCommand;
import pl.plajer.murdermystery.events.ChatEvents;
import pl.plajer.murdermystery.events.Events;
import pl.plajer.murdermystery.events.JoinEvent;
import pl.plajer.murdermystery.events.LobbyEvent;
import pl.plajer.murdermystery.events.QuitEvent;
import pl.plajer.murdermystery.events.spectator.SpectatorEvents;
import pl.plajer.murdermystery.events.spectator.SpectatorItemEvents;
import pl.plajer.murdermystery.handlers.BowTrailsHandler;
import pl.plajer.murdermystery.handlers.BungeeManager;
import pl.plajer.murdermystery.handlers.ChatManager;
import pl.plajer.murdermystery.handlers.CorpseHandler;
import pl.plajer.murdermystery.handlers.PermissionsManager;
import pl.plajer.murdermystery.handlers.PlaceholderManager;
import pl.plajer.murdermystery.handlers.SignManager;
import pl.plajer.murdermystery.handlers.items.SpecialItem;
import pl.plajer.murdermystery.handlers.language.LanguageManager;
import pl.plajer.murdermystery.handlers.rewards.RewardsFactory;
import pl.plajer.murdermystery.handlers.setup.SetupInventoryEvents;
import pl.plajer.murdermystery.leaderheads.MurderMysteryDeaths;
import pl.plajer.murdermystery.leaderheads.MurderMysteryGamesPlayed;
import pl.plajer.murdermystery.leaderheads.MurderMysteryHighestScore;
import pl.plajer.murdermystery.leaderheads.MurderMysteryKills;
import pl.plajer.murdermystery.leaderheads.MurderMysteryLoses;
import pl.plajer.murdermystery.leaderheads.MurderMysteryWins;
import pl.plajer.murdermystery.user.User;
import pl.plajer.murdermystery.user.UserManager;
import pl.plajer.murdermystery.user.data.MysqlManager;
import pl.plajer.murdermystery.utils.Debugger;
import pl.plajer.murdermystery.utils.ExceptionLogHandler;
import pl.plajer.murdermystery.utils.MessageUtils;
import pl.plajer.murdermystery.utils.UpdateChecker;
import pl.plajer.murdermystery.utils.services.ServiceRegistry;
import pl.plajerlair.commonsbox.database.MysqlDatabase;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;
import pl.plajerlair.commonsbox.minecraft.serialization.InventorySerializer;

/**
 * @author Plajer
 * <p>
 * Created at 03.08.2018
 */
public class Main extends JavaPlugin {

  private ExceptionLogHandler exceptionLogHandler;
  private String version;
  private boolean forceDisable = false;
  private BungeeManager bungeeManager;
  private RewardsFactory rewardsHandler;
  private MysqlDatabase database;
  private SignManager signManager;
  private MainCommand mainCommand;
  private CorpseHandler corpseHandler;
  private ConfigPreferences configPreferences;
  private HookManager hookManager;
  private UserManager userManager;

  @Override
  public void onEnable() {
    if (!validateIfPluginShouldStart()) {
      return;
    }

    ServiceRegistry.registerService(this);
    exceptionLogHandler = new ExceptionLogHandler();
    LanguageManager.init(this);
    saveDefaultConfig();
    Debugger.setEnabled(getConfig().getBoolean("Debug", false));
    Debugger.debug(Debugger.Level.INFO, "Main setup start");
    configPreferences = new ConfigPreferences(this);
    setupFiles();
    initializeClasses();
    checkUpdate();

    Debugger.debug(Debugger.Level.INFO, "Plugin loaded! Hooking into soft-dependencies in a while!");
    //start hook manager later in order to allow soft-dependencies to fully load
    Bukkit.getScheduler().runTaskLater(this, () -> hookManager = new HookManager(), 20 * 5);
  }

  private boolean validateIfPluginShouldStart() {
    version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    if (!(version.equalsIgnoreCase("v1_11_R1") || version.equalsIgnoreCase("v1_12_R1") || version.equalsIgnoreCase("v1_13_R1")
        || version.equalsIgnoreCase("v1_13_R2") || version.equalsIgnoreCase("v1_14_R1"))) {
      MessageUtils.thisVersionIsNotSupported();
      Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Your server version is not supported by Murder Mystery!");
      Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Sadly, we must shut off. Maybe you consider changing your server version?");
      forceDisable = true;
      getServer().getPluginManager().disablePlugin(this);
      return false;
    }
    try {
      Class.forName("org.spigotmc.SpigotConfig");
    } catch (Exception e) {
      MessageUtils.thisVersionIsNotSupported();
      Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Your server software is not supported by Murder Mystery!");
      Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "We support only Spigot and Spigot forks only! Shutting off...");
      forceDisable = true;
      getServer().getPluginManager().disablePlugin(this);
      return false;
    }
    return true;
  }

  @Override
  public void onDisable() {
    if (forceDisable) {
      return;
    }
    Debugger.debug(Debugger.Level.INFO, "System disable init");
    Bukkit.getLogger().removeHandler(exceptionLogHandler);
    saveAllUserStatistics();
    if (hookManager.isFeatureEnabled(HookManager.HookFeature.CORPSES)) {
      for (Hologram hologram : HologramsAPI.getHolograms(this)) {
        hologram.delete();
      }
    }
    if (configPreferences.getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
      getMySQLDatabase().shutdownConnPool();
    }

    for (Arena arena : ArenaRegistry.getArenas()) {
      arena.getScoreboardManager().stopAllScoreboards();
      for (Player player : arena.getPlayers()) {
        arena.doBarAction(Arena.BarAction.REMOVE, player);
        arena.teleportToEndLocation(player);
        if (configPreferences.getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
          InventorySerializer.loadInventory(this, player);
        } else {
          player.getInventory().clear();
          player.getInventory().setArmorContents(null);
          for (PotionEffect pe : player.getActivePotionEffects()) {
            player.removePotionEffect(pe.getType());
          }
        }
      }
      arena.teleportAllToEndLocation();
      arena.cleanUpArena();
    }
  }

  private void initializeClasses() {
    ScoreboardLib.setPluginInstance(this);
    if (getConfig().getBoolean("BungeeActivated", false)) {
      bungeeManager = new BungeeManager(this);
    }
    if (configPreferences.getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
      FileConfiguration config = ConfigUtils.getConfig(this, "mysql");
      database = new MysqlDatabase(config.getString("user"), config.getString("password"), config.getString("address"));
    }
    userManager = new UserManager(this);
    SpecialItem.loadAll();
    PermissionsManager.init();
    new ChatManager(ChatManager.colorMessage("In-Game.Plugin-Prefix"));
    mainCommand = new MainCommand(this, true);
    new ArenaEvents(this);
    new SpectatorEvents(this);
    new QuitEvent(this);
    new SetupInventoryEvents(this);
    new JoinEvent(this);
    new ChatEvents(this);
    registerSoftDependenciesAndServices();
    User.cooldownHandlerTask();
    ArenaRegistry.registerArenas();
    new Events(this);
    new LobbyEvent(this);
    new SpectatorItemEvents(this);
    rewardsHandler = new RewardsFactory(this);
    signManager = new SignManager(this);
    corpseHandler = new CorpseHandler(this);
    new BowTrailsHandler(this);
    MysteryPotionRegistry.init(this);
    PrayerRegistry.init(this);
    new SpecialBlockEvents(this);
  }

  private void registerSoftDependenciesAndServices() {
    startPluginMetrics();
    if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
      Debugger.debug(Debugger.Level.INFO, "Hooking into PlaceholderAPI");
      new PlaceholderManager().register();
    }
    if (Bukkit.getPluginManager().isPluginEnabled("LeaderHeads")) {
      Debugger.debug(Debugger.Level.INFO, "Hooking into LeaderHeads");
      new MurderMysteryDeaths();
      new MurderMysteryGamesPlayed();
      new MurderMysteryHighestScore();
      new MurderMysteryKills();
      new MurderMysteryLoses();
      new MurderMysteryWins();
    }
  }

  private void startPluginMetrics() {
    Metrics metrics = new Metrics(this);
    metrics.addCustomChart(new Metrics.SimplePie("database_enabled", () -> String.valueOf(configPreferences.getOption(ConfigPreferences.Option.DATABASE_ENABLED))));
    metrics.addCustomChart(new Metrics.SimplePie("bungeecord_hooked", () -> String.valueOf(configPreferences.getOption(ConfigPreferences.Option.BUNGEE_ENABLED))));
    metrics.addCustomChart(new Metrics.SimplePie("locale_used", () -> LanguageManager.getPluginLocale().getPrefix()));
    metrics.addCustomChart(new Metrics.SimplePie("update_notifier", () -> {
      if (getConfig().getBoolean("Update-Notifier.Enabled", true)) {
        if (getConfig().getBoolean("Update-Notifier.Notify-Beta-Versions", true)) {
          return "Enabled with beta notifier";
        } else {
          return "Enabled";
        }
      } else {
        if (getConfig().getBoolean("Update-Notifier.Notify-Beta-Versions", true)) {
          return "Beta notifier only";
        } else {
          return "Disabled";
        }
      }
    }));
  }

  private void checkUpdate() {
    if (!getConfig().getBoolean("Update-Notifier.Enabled", true)) {
      return;
    }
    UpdateChecker.init(this, 66614).requestUpdateCheck().whenComplete((result, exception) -> {
      if (!result.requiresUpdate()) {
        return;
      }
      if (result.getNewestVersion().contains("b")) {
        if (getConfig().getBoolean("Update-Notifier.Notify-Beta-Versions", true)) {
          Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[MurderMystery] Your software is ready for update! However it's a BETA VERSION. Proceed with caution.");
          Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[MurderMystery] Current version %old%, latest version %new%".replace("%old%", getDescription().getVersion()).replace("%new%",
              result.getNewestVersion()));
        }
        return;
      }
      MessageUtils.updateIsHere();
      Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Your MurderMystery plugin is outdated! Download it to keep with latest changes and fixes.");
      Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Disable this option in config.yml if you wish.");
      Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Current version: " + ChatColor.RED + getDescription().getVersion() + ChatColor.YELLOW + " Latest version: " + ChatColor.GREEN + result.getNewestVersion());
    });
  }

  private void setupFiles() {
    for (String fileName : Arrays.asList("arenas", "bungee", "rewards", "stats", "lobbyitems", "mysql", "specialblocks")) {
      File file = new File(getDataFolder() + File.separator + fileName + ".yml");
      if (!file.exists()) {
        saveResource(fileName + ".yml", false);
      }
    }
  }

  public boolean is1_11_R1() {
    return version.equalsIgnoreCase("v1_11_R1");
  }

  public boolean is1_12_R1() {
    return version.equalsIgnoreCase("v1_12_R1");
  }

  public boolean is1_14_R1() {
    return version.equalsIgnoreCase("v1_14_R1");
  }

  public RewardsFactory getRewardsHandler() {
    return rewardsHandler;
  }

  public BungeeManager getBungeeManager() {
    return bungeeManager;
  }

  public ConfigPreferences getConfigPreferences() {
    return configPreferences;
  }

  public MysqlDatabase getMySQLDatabase() {
    return database;
  }

  public SignManager getSignManager() {
    return signManager;
  }

  public CorpseHandler getCorpseHandler() {
    return corpseHandler;
  }

  public HookManager getHookManager() {
    return hookManager;
  }

  public UserManager getUserManager() {
    return userManager;
  }

  private void saveAllUserStatistics() {
    for (Player player : getServer().getOnlinePlayers()) {
      User user = userManager.getUser(player);

      //copy of userManager#saveStatistic but without async database call that's not allowed in onDisable method.
      for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
        if (!stat.isPersistent()) {
          continue;
        }
        if (userManager.getDatabase() instanceof MysqlManager) {
          ((MysqlManager) userManager.getDatabase()).getDatabase().executeUpdate("UPDATE playerstats SET " + stat.getName() + "=" + user.getStat(stat) + " WHERE UUID='" + user.getPlayer().getUniqueId().toString() + "';");
          continue;
        }
        userManager.getDatabase().saveStatistic(user, stat);
      }
    }
  }


}
