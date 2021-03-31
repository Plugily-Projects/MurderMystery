/*
 * MurderMystery - Find the murderer, kill him and survive!
 * Copyright (C) 2020  Plugily Projects - maintained by Tigerpanzer_02, 2Wild4You and contributors
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

package plugily.projects.murdermystery.arena;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.golde.bukkit.corpsereborn.CorpseAPI.CorpseAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.plajerlair.commonsbox.minecraft.compat.ServerVersion;
import pl.plajerlair.commonsbox.minecraft.compat.VersionUtils;
import pl.plajerlair.commonsbox.minecraft.compat.xseries.XSound;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;
import pl.plajerlair.commonsbox.minecraft.serialization.InventorySerializer;
import pl.plajerlair.commonsbox.number.NumberUtils;
import plugily.projects.murdermystery.ConfigPreferences;
import plugily.projects.murdermystery.HookManager;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.api.StatsStorage;
import plugily.projects.murdermystery.api.events.game.MMGameStartEvent;
import plugily.projects.murdermystery.api.events.game.MMGameStateChangeEvent;
import plugily.projects.murdermystery.arena.corpse.Corpse;
import plugily.projects.murdermystery.arena.corpse.Stand;
import plugily.projects.murdermystery.arena.managers.ScoreboardManager;
import plugily.projects.murdermystery.arena.options.ArenaOption;
import plugily.projects.murdermystery.arena.role.Role;
import plugily.projects.murdermystery.arena.special.SpecialBlock;
import plugily.projects.murdermystery.arena.special.pray.PrayerRegistry;
import plugily.projects.murdermystery.handlers.ChatManager;
import plugily.projects.murdermystery.handlers.hologram.ArmorStandHologram;
import plugily.projects.murdermystery.handlers.rewards.Reward;
import plugily.projects.murdermystery.user.User;
import plugily.projects.murdermystery.utils.Debugger;
import plugily.projects.murdermystery.utils.ItemPosition;
import plugily.projects.murdermystery.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Arena extends BukkitRunnable {

  private static final Random random = new Random();
  private static final Main plugin = JavaPlugin.getPlugin(Main.class);
  private final ChatManager chatManager = plugin.getChatManager();
  private final String id;

  private final Set<Player> players = new HashSet<>();
  private final List<Item> goldSpawned = new ArrayList<>();
  private final List<Corpse> corpses = new ArrayList<>();
  private final List<Stand> stands = new ArrayList<>();
  private final List<SpecialBlock> specialBlocks = new ArrayList<>();
  private final List<Player> allMurderer = new ArrayList<>(), allDetectives = new ArrayList<>(),
      spectators = new ArrayList<>(), deaths = new ArrayList<>();

  //contains murderer, detective, fake detective and hero
  private final Map<CharacterType, Player> gameCharacters = new EnumMap<>(CharacterType.class);
  //all arena values that are integers, contains constant and floating values
  private final Map<ArenaOption, Integer> arenaOptions = new EnumMap<>(ArenaOption.class);
  //instead of 3 location fields we use map with GameLocation enum
  private final Map<GameLocation, Location> gameLocations = new EnumMap<>(GameLocation.class);

  private final ScoreboardManager scoreboardManager;

  private List<Location> goldSpawnPoints = new ArrayList<>(), playerSpawnPoints = new ArrayList<>();

  private int murderers = 0, detectives = 0, spawnGoldTimer = 0, spawnGoldTime = 0;

  private boolean detectiveDead, murdererLocatorReceived, hideChances, ready = true, forceStart = false, goldVisuals = false;
  private ArenaState arenaState = ArenaState.WAITING_FOR_PLAYERS;
  private BossBar gameBar;
  private String mapName = "";

  public Arena(String id) {
    this.id = id;
    for(ArenaOption option : ArenaOption.values()) {
      arenaOptions.put(option, option.getDefaultValue());
    }
    if(plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSSBAR_ENABLED) && ServerVersion.Version.isCurrentEqualOrHigher(ServerVersion.Version.v1_9_R1)) {
      gameBar = Bukkit.createBossBar(chatManager.colorMessage("Bossbar.Main-Title"), BarColor.BLUE, BarStyle.SOLID);
    }
    scoreboardManager = new ScoreboardManager(this);
  }

  public boolean isReady() {
    return ready;
  }

  public void setReady(boolean ready) {
    this.ready = ready;
  }

  public void addCorpse(Corpse corpse) {
    if(plugin.getHookManager() != null && !plugin.getHookManager().isFeatureEnabled(HookManager.HookFeature.CORPSES)) {
      return;
    }
    corpses.add(corpse);
  }

  public void addHead(Stand stand) {
    stands.add(stand);
  }

  @Override
  public void run() {
    //idle task
    if(players.isEmpty() && arenaState == ArenaState.WAITING_FOR_PLAYERS) {
      return;
    }
    Debugger.performance("ArenaTask", "[PerformanceMonitor] [{0}] Running game task", getId());
    long start = System.currentTimeMillis();

    switch(arenaState) {
      case WAITING_FOR_PLAYERS:
        if(plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
          plugin.getServer().setWhitelist(false);
        }
        if(players.size() < getMinimumPlayers()) {
          if(getTimer() <= 0) {
            setTimer(45);
            chatManager.broadcast(this, chatManager.formatMessage(this, chatManager.colorMessage("In-Game.Messages.Lobby-Messages.Waiting-For-Players"), getMinimumPlayers()));
            break;
          }
        } else {
          if(plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSSBAR_ENABLED) && ServerVersion.Version.isCurrentEqualOrHigher(ServerVersion.Version.v1_9_R1)) {
            gameBar.setTitle(chatManager.colorMessage("Bossbar.Waiting-For-Players"));
          }
          chatManager.broadcast(this, chatManager.colorMessage("In-Game.Messages.Lobby-Messages.Enough-Players-To-Start"));
          setArenaState(ArenaState.STARTING);
          setTimer(plugin.getConfig().getInt("Starting-Waiting-Time", 60));
          this.showPlayers();
        }
        setTimer(getTimer() - 1);
        break;
      case STARTING:
        if(players.size() == getMaximumPlayers() && getTimer() >= plugin.getConfig().getInt("Start-Time-On-Full-Lobby", 15) && !forceStart) {
          setTimer(plugin.getConfig().getInt("Start-Time-On-Full-Lobby", 15));
          chatManager.broadcast(this, chatManager.colorMessage("In-Game.Messages.Lobby-Messages.Start-In").replace("%TIME%", Integer.toString(getTimer())));
        }
        if(plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSSBAR_ENABLED) && ServerVersion.Version.isCurrentEqualOrHigher(ServerVersion.Version.v1_9_R1)) {
          gameBar.setTitle(chatManager.colorMessage("Bossbar.Starting-In").replace("%time%", Integer.toString(getTimer())));
          gameBar.setProgress(getTimer() / plugin.getConfig().getDouble("Starting-Waiting-Time", 60));
        }
        for(Player player : players) {
          player.setExp((float) (getTimer() / plugin.getConfig().getDouble("Starting-Waiting-Time", 60)));
          player.setLevel(getTimer());
        }
        if(players.size() < getMinimumPlayers() && !forceStart) {
          if(plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSSBAR_ENABLED) && ServerVersion.Version.isCurrentEqualOrHigher(ServerVersion.Version.v1_9_R1)) {
            gameBar.setTitle(chatManager.colorMessage("Bossbar.Waiting-For-Players"));
            gameBar.setProgress(1.0);
          }
          chatManager.broadcast(this, chatManager.formatMessage(this, chatManager.colorMessage("In-Game.Messages.Lobby-Messages.Waiting-For-Players"), getMinimumPlayers()));
          setArenaState(ArenaState.WAITING_FOR_PLAYERS);
          Bukkit.getPluginManager().callEvent(new MMGameStartEvent(this));
          setTimer(15);
          for(Player player : players) {
            player.setExp(1);
            player.setLevel(0);
          }
          if(forceStart) {
            forceStart = false;
          }
          break;
        }
        int totalMurderer = 0;
        int totalDetective = 0;
        for(Player p : players) {
          User user = plugin.getUserManager().getUser(p);
          totalMurderer += user.getStat(StatsStorage.StatisticType.CONTRIBUTION_MURDERER);
          totalDetective += user.getStat(StatsStorage.StatisticType.CONTRIBUTION_DETECTIVE);
        }
        if(!hideChances) {
          for(Player p : players) {
            VersionUtils.sendActionBar(p, formatRoleChance(plugin.getUserManager().getUser(p), totalMurderer, totalDetective));
          }
        }
        if(getTimer() == 0 || forceStart) {
          Bukkit.getPluginManager().callEvent(new MMGameStartEvent(this));
          setArenaState(ArenaState.IN_GAME);
          if(plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSSBAR_ENABLED) && ServerVersion.Version.isCurrentEqualOrHigher(ServerVersion.Version.v1_9_R1)) {
            gameBar.setProgress(1.0);
          }
          setTimer(5);
          if(players.isEmpty()) {
            break;
          }
          teleportAllToStartLocation();
          for(Player player : players) {
            //reset local variables to be 100% sure
            User user = plugin.getUserManager().getUser(player);
            user.setStat(StatsStorage.StatisticType.LOCAL_GOLD, 0);
            user.setStat(StatsStorage.StatisticType.LOCAL_CURRENT_PRAY, 0);
            user.setStat(StatsStorage.StatisticType.LOCAL_KILLS, 0);
            user.setStat(StatsStorage.StatisticType.LOCAL_PRAISES, 0);
            user.setStat(StatsStorage.StatisticType.LOCAL_SCORE, 0);

            ArenaUtils.updateNameTagsVisibility(player);
            player.getInventory().clear();
            player.setGameMode(GameMode.ADVENTURE);
            ArenaUtils.hidePlayersOutsideTheGame(player, this);
            player.updateInventory();
            user.addStat(StatsStorage.StatisticType.GAMES_PLAYED, 1);
            setTimer(plugin.getConfig().getInt("Classic-Gameplay-Time", 270));
            player.sendMessage(chatManager.getPrefix() + chatManager.colorMessage("In-Game.Messages.Lobby-Messages.Game-Started"));
          }

          Map<User, Double> murdererChances = new HashMap<>(), detectiveChances = new HashMap<>();
          for(Player p : players) {
            User user = plugin.getUserManager().getUser(p);
            murdererChances.put(user, ((double) user.getStat(StatsStorage.StatisticType.CONTRIBUTION_MURDERER) / (double) totalMurderer) * 100.0);
            detectiveChances.put(user, ((double) user.getStat(StatsStorage.StatisticType.CONTRIBUTION_DETECTIVE) / (double) totalDetective) * 100.0);
          }
          //shuffling map to avoid the same murders on the next round
          List<Map.Entry<User, Double>> shuffledMurderer = new ArrayList<>(murdererChances.entrySet());
          Collections.shuffle(shuffledMurderer);
          //
          Map<User, Double> sortedMurderer = shuffledMurderer.stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(
              Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

          Set<Player> playersToSet = new HashSet<>(players);
          int maxmurderer = 1;
          int maxdetectives = 1;
          Debugger.debug("Before: Arena: {0} | Detectives = {1}, Murders = {2}, Players = {3} | Configured: Detectives = {4}, Murders = {5}",
              getId(), maxdetectives, maxmurderer, players.size(), detectives, murderers);
          if(murderers > 1 && players.size() > murderers) {
            maxmurderer = (players.size() / murderers);
          }
          if(detectives > 1 && players.size() > detectives) {
            maxdetectives = (players.size() / detectives);
          }
          if(players.size() - (maxmurderer + maxdetectives) < 1) {
            Debugger.debug("{0} Murderers and detectives amount was reduced because there are not enough players", this);
            //Make sure to have one innocent!
            if(maxdetectives > 1) {
              maxdetectives--;
            } else if(maxmurderer > 1) {
              maxmurderer--;
            }
          }
          Debugger.debug("After: Arena: {0} | Detectives = {1}, Murders = {2}, Players = {3} | Configured: Detectives = {4}, Murders = {5}",
              getId(), maxdetectives, maxmurderer, players.size(), detectives, murderers);
          for(int i = 0; i < maxmurderer; i++) {
            Player murderer = ((User) sortedMurderer.keySet().toArray()[i]).getPlayer();
            setCharacter(CharacterType.MURDERER, murderer);
            allMurderer.add(murderer);
            plugin.getUserManager().getUser(murderer).setStat(StatsStorage.StatisticType.CONTRIBUTION_MURDERER, 1);
            playersToSet.remove(murderer);
            VersionUtils.sendTitles(murderer, chatManager.colorMessage("In-Game.Messages.Role-Set.Murderer-Title"),
                chatManager.colorMessage("In-Game.Messages.Role-Set.Murderer-Subtitle"), 5, 40, 5);
            detectiveChances.remove(sortedMurderer.keySet().toArray()[i]);
          }
          //shuffling map to avoid the same detectives on the next round
          List<Map.Entry<User, Double>> shuffledDetectives = new ArrayList<>(detectiveChances.entrySet());
          Collections.shuffle(shuffledDetectives);

          Map<User, Double> sortedDetective = shuffledDetectives.stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(
              Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
          for(int i = 0; i < maxdetectives; i++) {
            Player detective = ((User) sortedDetective.keySet().toArray()[i]).getPlayer();
            setCharacter(CharacterType.DETECTIVE, detective);
            allDetectives.add(detective);
            plugin.getUserManager().getUser(detective).setStat(StatsStorage.StatisticType.CONTRIBUTION_DETECTIVE, 1);
            VersionUtils.sendTitles(detective, chatManager.colorMessage("In-Game.Messages.Role-Set.Detective-Title"),
                chatManager.colorMessage("In-Game.Messages.Role-Set.Detective-Subtitle"), 5, 40, 5);
            playersToSet.remove(detective);
            detective.getInventory().setHeldItemSlot(0);
            ItemPosition.setItem(detective, ItemPosition.BOW, new ItemStack(Material.BOW, 1));
            ItemPosition.setItem(detective, ItemPosition.INFINITE_ARROWS, new ItemStack(Material.ARROW, plugin.getConfig().getInt("Detective-Default-Arrows", 3)));
          }
          Debugger.debug("Arena: {0} | Detectives = {1}, Murders = {2}, Players = {3} | Players: Detectives = {4}, Murders = {5}",
              getId(), maxdetectives, maxmurderer, players.size(), allDetectives, allMurderer);

          for(Player p : playersToSet) {
            VersionUtils.sendTitles(p, chatManager.colorMessage("In-Game.Messages.Role-Set.Innocent-Title"),
                chatManager.colorMessage("In-Game.Messages.Role-Set.Innocent-Subtitle"), 5, 40, 5);
          }
          if(plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSSBAR_ENABLED) && ServerVersion.Version.isCurrentEqualOrHigher(ServerVersion.Version.v1_9_R1)) {
            gameBar.setTitle(chatManager.colorMessage("Bossbar.In-Game-Info"));
          }

          // Load and append special blocks hologram
          specialBlocks.forEach(this::loadSpecialBlock);
        }
        if(forceStart) {
          forceStart = false;
        }
        setTimer(getTimer() - 1);
        break;
      case IN_GAME:
        if(plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
          plugin.getServer().setWhitelist(getMaximumPlayers() <= players.size());
        }
        if(getTimer() <= 0) {
          ArenaManager.stopGame(false, this);
        }
        if(getTimer() <= (plugin.getConfig().getInt("Classic-Gameplay-Time", 270) - 10)
            && getTimer() > (plugin.getConfig().getInt("Classic-Gameplay-Time", 270) - 15)) {
          for(Player p : players) {
            p.sendMessage(chatManager.colorMessage("In-Game.Messages.Murderer-Get-Sword")
                .replace("%time%", String.valueOf(getTimer() - (plugin.getConfig().getInt("Classic-Gameplay-Time", 270) - 15))));
            XSound.UI_BUTTON_CLICK.play(p.getLocation(), 1, 1);
          }
          if(getTimer() == (plugin.getConfig().getInt("Classic-Gameplay-Time", 270) - 14)) {
            if(allMurderer.isEmpty()) ArenaManager.stopGame(false, this);
            for(Player p : allMurderer) {
              User murderer = plugin.getUserManager().getUser(p);
              if(murderer.isSpectator() || !p.isOnline() || murderer.getArena() != this)
                continue;
              p.getInventory().setHeldItemSlot(0);
              ItemPosition.setItem(p, ItemPosition.MURDERER_SWORD, plugin.getConfigPreferences().getMurdererSword());
            }
          }
        }

        // Cache at least once for fast processing
        List<Player> playersLeft = getPlayersLeft();

        //every 30 secs survive reward
        if(getTimer() % 30 == 0) {
          for(Player p : playersLeft) {
            if(Role.isRole(Role.INNOCENT, p)) {
              ArenaUtils.addScore(plugin.getUserManager().getUser(p), ArenaUtils.ScoreAction.SURVIVE_TIME, 0);
            }
          }
        }

        if(getTimer() == 30 || getTimer() == 60) {
          String title = chatManager.colorMessage("In-Game.Messages.Seconds-Left-Title").replace("%time%", String.valueOf(getTimer()));
          String subtitle = chatManager.colorMessage("In-Game.Messages.Seconds-Left-Subtitle").replace("%time%", String.valueOf(getTimer()));
          for(Player p : players) {
            VersionUtils.sendTitles(p, title, subtitle, 5, 40, 5);
          }
        }

        if(getTimer() <= 30 || playersLeft.size() == aliveMurderer() + 1) {
          if(plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INNOCENT_LOCATOR)) {
            ArenaUtils.updateInnocentLocator(this);
          }
        }
        //no players - stop game
        if(playersLeft.isEmpty()) {
          ArenaManager.stopGame(false, this);
        } else
          //winner check
          if(playersLeft.size() == aliveMurderer()) {
            for(Player p : players) {
              VersionUtils.sendTitles(p, chatManager.colorMessage("In-Game.Messages.Game-End-Messages.Titles.Lose"),
                  chatManager.colorMessage("In-Game.Messages.Game-End-Messages.Subtitles.Murderer-Kill-Everyone"), 5, 40, 5);
              if(allMurderer.contains(p)) {
                VersionUtils.sendTitles(p, chatManager.colorMessage("In-Game.Messages.Game-End-Messages.Titles.Win"), null, 5, 40, 5);
              }
            }
            ArenaManager.stopGame(false, this);
          } else
            //murderer speed add
            if(plugin.getConfigPreferences().getOption(ConfigPreferences.Option.MURDERER_SPEED_ENABLED)) {
              if(playersLeft.size() == aliveMurderer() + 1) {
                for(Player p : allMurderer) {
                  if(isMurderAlive(p)) {
                    //no potion because it adds particles which can be identified
                    int multiplier = plugin.getConfig().getInt("Speed-Effect-Murderer.Speed", 3);
                    if(multiplier > 1 && multiplier <= 10) {
                      p.setWalkSpeed(0.1f * plugin.getConfig().getInt("Speed-Effect-Murderer.Speed", 3));
                    }
                  }
                }
              }
            }
        //don't spawn it every time
        if(spawnGoldTimer == spawnGoldTime) {
          spawnSomeGold();
          spawnGoldTimer = 0;
        } else {
          spawnGoldTimer++;
        }
        setTimer(getTimer() - 1);
        break;
      case ENDING:
        scoreboardManager.stopAllScoreboards();
        if(plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
          plugin.getServer().setWhitelist(false);
        }
        if(getTimer() <= 0) {
          if(plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSSBAR_ENABLED) && ServerVersion.Version.isCurrentEqualOrHigher(ServerVersion.Version.v1_9_R1)) {
            gameBar.setTitle(chatManager.colorMessage("Bossbar.Game-Ended"));
          }

          for(Player player : new ArrayList<>(players)) {
            plugin.getUserManager().getUser(player).removeScoreboard(this);
            player.setGameMode(GameMode.SURVIVAL);
            for(Player players : Bukkit.getOnlinePlayers()) {
              VersionUtils.showPlayer(plugin, player, players);
              if(!ArenaRegistry.isInArena(players)) {
                VersionUtils.showPlayer(plugin, players, player);
              }
            }
            player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
            player.setWalkSpeed(0.2f);
            player.setFlying(false);
            player.setAllowFlight(false);
            player.getInventory().clear();

            player.getInventory().setArmorContents(null);
            doBarAction(BarAction.REMOVE, player);
            player.setFireTicks(0);
            player.setFoodLevel(20);
            PrayerRegistry.getRush().remove(player);
            PrayerRegistry.getBan().remove(player);
          }
          teleportAllToEndLocation();

          if(plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
            for(Player player : players) {
              InventorySerializer.loadInventory(plugin, player);
            }
          }

          chatManager.broadcast(this, chatManager.colorMessage("Commands.Teleported-To-The-Lobby"));

          for(User user : plugin.getUserManager().getUsers(this)) {
            user.setSpectator(false);
            VersionUtils.setCollidable(user.getPlayer(), true);
            plugin.getUserManager().saveAllStatistic(user);
          }
          plugin.getRewardsHandler().performReward(this, Reward.RewardType.END_GAME);
          players.clear();

          deaths.clear();
          spectators.clear();

          cleanUpArena();
          if(plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)
              && ConfigUtils.getConfig(plugin, "bungee").getBoolean("Shutdown-When-Game-Ends")) {
            plugin.getServer().shutdown();
          }
          setArenaState(ArenaState.RESTARTING);
        }
        setTimer(getTimer() - 1);
        break;
      case RESTARTING:
        players.clear();
        setArenaState(ArenaState.WAITING_FOR_PLAYERS);
        if(plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
          ArenaRegistry.shuffleBungeeArena();
          for(Player player : Bukkit.getOnlinePlayers()) {
            ArenaManager.joinAttempt(player, ArenaRegistry.getArenas().get(ArenaRegistry.getBungeeArena()));
          }
        }
        if(plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSSBAR_ENABLED) && ServerVersion.Version.isCurrentEqualOrHigher(ServerVersion.Version.v1_9_R1)) {
          gameBar.setTitle(chatManager.colorMessage("Bossbar.Waiting-For-Players"));
        }

        if(goldVisuals) {
          startGoldVisuals();
        }

        break;
      default:
        break; //o.o?
    }
    Debugger.performance("ArenaTask", "[PerformanceMonitor] [{0}] Game task finished took {1}ms",

        getId(), System.

            currentTimeMillis() - start);
  }

  private String formatRoleChance(User user, int murdererPts, int detectivePts) throws NumberFormatException {
    String message = chatManager.colorMessage("In-Game.Messages.Lobby-Messages.Role-Chances-Action-Bar");
    message = StringUtils.replace(message, "%murderer_chance%", NumberUtils.round(((double) user.getStat(StatsStorage.StatisticType.CONTRIBUTION_MURDERER) / (double) murdererPts) * 100.0, 2) + "%");
    message = StringUtils.replace(message, "%detective_chance%", NumberUtils.round(((double) user.getStat(StatsStorage.StatisticType.CONTRIBUTION_DETECTIVE) / (double) detectivePts) * 100.0, 2) + "%");
    return message;
  }

  private void spawnSomeGold() {
    //may users want to disable it and want much gold on there map xD
    if(!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DISABLE_GOLD_LIMITER)) {
      //do not exceed amount of gold per spawn
      if(goldSpawned.size() >= goldSpawnPoints.size()) {
        return;
      }
    }
    if(goldSpawnPoints.isEmpty()) {
      return;
    }
    if(plugin.getConfigPreferences().getOption(ConfigPreferences.Option.SPAWN_GOLD_EVERY_SPAWNER_MODE)) {
      for(Location location : goldSpawnPoints) {
        goldSpawned.add(location.getWorld().dropItem(location, new ItemStack(Material.GOLD_INGOT, 1)));
      }
    } else {
      Location loc = goldSpawnPoints.get(random.nextInt(goldSpawnPoints.size()));
      goldSpawned.add(loc.getWorld().dropItem(loc, new ItemStack(Material.GOLD_INGOT, 1)));
    }
  }

  public void setMurderers(int murderers) {
    this.murderers = murderers;
  }

  public void setSpawnGoldTime(int spawnGoldTime) {
    this.spawnGoldTime = spawnGoldTime;
  }

  public void setHideChances(boolean hideChances) {
    this.hideChances = hideChances;
  }

  public boolean isDetectiveDead() {
    return detectiveDead;
  }

  public void setDetectiveDead(boolean detectiveDead) {
    this.detectiveDead = detectiveDead;
  }

  public void setDetectives(int detectives) {
    this.detectives = detectives;
  }

  public boolean isMurdererLocatorReceived() {
    return murdererLocatorReceived;
  }

  public void setMurdererLocatorReceived(boolean murdererLocatorReceived) {
    this.murdererLocatorReceived = murdererLocatorReceived;
  }

  public void setForceStart(boolean forceStart) {
    this.forceStart = forceStart;
  }

  public ScoreboardManager getScoreboardManager() {
    return scoreboardManager;
  }

  @NotNull
  public List<Item> getGoldSpawned() {
    return goldSpawned;
  }

  @NotNull
  public List<Location> getGoldSpawnPoints() {
    return goldSpawnPoints;
  }

  public void setGoldSpawnPoints(@NotNull List<Location> goldSpawnPoints) {
    this.goldSpawnPoints = goldSpawnPoints;
  }

  public void toggleGoldVisuals() {
    if(goldSpawnPoints.isEmpty() || goldVisuals) {
      goldVisuals = false;
      return;
    }
    setGoldVisuals(true);
  }

  private BukkitTask visualTask;

  private void startGoldVisuals() {
    if(visualTask != null) {
      return;
    }
    visualTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
      if(!plugin.isEnabled() || !goldVisuals || goldSpawnPoints.isEmpty() || arenaState != ArenaState.WAITING_FOR_PLAYERS) {
        //we need to cancel it that way as the arena class is an task
        visualTask.cancel();
        return;
      }
      for(Location goldLocations : goldSpawnPoints) {
        Location goldLocation = goldLocations.clone();
        goldLocation.add(0, 0.4, 0);
        java.util.Iterator<? extends Player> iterator = Bukkit.getOnlinePlayers().iterator();
        if (iterator.hasNext()) {
          VersionUtils.sendParticles("REDSTONE", iterator.next(), goldLocation, 10);
        }
      }
    }, 20L, 20L);
  }

  public boolean isGoldVisuals() {
    return goldVisuals;
  }

  public void setGoldVisuals(boolean goldVisuals) {
    this.goldVisuals = goldVisuals;
    if(goldVisuals) {
      startGoldVisuals();
    }
  }

  /**
   * Get arena identifier used to get arenas by string.
   *
   * @return arena name
   * @see ArenaRegistry#getArena(String)
   */
  public String getId() {
    return id;
  }

  /**
   * Get minimum players needed.
   *
   * @return minimum players needed to start arena
   */
  public int getMinimumPlayers() {
    return getOption(ArenaOption.MINIMUM_PLAYERS);
  }

  /**
   * Set minimum players needed.
   *
   * @param minimumPlayers players needed to start arena
   */
  public void setMinimumPlayers(int minimumPlayers) {
    if(minimumPlayers < 2) {
      Debugger.debug(Level.WARNING, "Minimum players amount for arena cannot be less than 2! Got {0}", minimumPlayers);
      setOptionValue(ArenaOption.MINIMUM_PLAYERS, 2);
      return;
    }
    setOptionValue(ArenaOption.MINIMUM_PLAYERS, minimumPlayers);
  }

  /**
   * Get arena map name.
   *
   * @return arena map name, it's not arena id
   * @see #getId()
   */
  @NotNull
  public String getMapName() {
    return mapName;
  }

  /**
   * Set arena map name.
   *
   * @param mapname new map name, it's not arena id
   */
  public void setMapName(@NotNull String mapname) {
    this.mapName = mapname;
  }

  /**
   * Get timer of arena.
   *
   * @return timer of lobby time / time to next wave
   */
  public int getTimer() {
    return getOption(ArenaOption.TIMER);
  }

  /**
   * Modify game timer.
   *
   * @param timer timer of lobby / time to next wave
   */
  public void setTimer(int timer) {
    setOptionValue(ArenaOption.TIMER, timer);
  }

  /**
   * Return maximum players arena can handle.
   *
   * @return maximum players arena can handle
   */
  public int getMaximumPlayers() {
    return getOption(ArenaOption.MAXIMUM_PLAYERS);
  }

  /**
   * Set maximum players arena can handle.
   *
   * @param maximumPlayers how many players arena can handle
   */
  public void setMaximumPlayers(int maximumPlayers) {
    setOptionValue(ArenaOption.MAXIMUM_PLAYERS, maximumPlayers);
  }

  /**
   * Return game state of arena.
   *
   * @return game state of arena
   * @see ArenaState
   */
  @NotNull
  public ArenaState getArenaState() {
    return arenaState;
  }

  /**
   * Set game state of arena.
   *
   * @param arenaState new game state of arena
   * @see ArenaState
   */
  public void setArenaState(@NotNull ArenaState arenaState) {
    this.arenaState = arenaState;

    Bukkit.getPluginManager().callEvent(new MMGameStateChangeEvent(this, arenaState));

    plugin.getSignManager().updateSigns();
  }

  /**
   * Get all players in arena.
   *
   * @return set of players in arena
   */
  @NotNull
  public Set<Player> getPlayers() {
    return players;
  }

  public void teleportToLobby(Player player) {
    player.setFoodLevel(20);
    player.setFlying(false);
    player.setAllowFlight(false);
    player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
    player.setWalkSpeed(0.2f);
    Location location = getLobbyLocation();
    if(location == null) {
      System.out.print("LobbyLocation isn't intialized for arena " + getId());
      return;
    }
    player.teleport(location);
  }

  /**
   * Executes boss bar action for arena
   *
   * @param action add or remove a player from boss bar
   * @param p      player
   */
  public void doBarAction(BarAction action, Player p) {
    if(!ServerVersion.Version.isCurrentEqualOrHigher(ServerVersion.Version.v1_9_R1)
        || !plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSSBAR_ENABLED)) {
      return;
    }
    switch(action) {
      case ADD:
        gameBar.addPlayer(p);
        break;
      case REMOVE:
        gameBar.removePlayer(p);
        break;
      default:
        break;
    }
  }

  /**
   * Get lobby location of arena.
   *
   * @return lobby location of arena
   */
  @Nullable
  public Location getLobbyLocation() {
    return gameLocations.get(GameLocation.LOBBY);
  }

  /**
   * Set lobby location of arena.
   *
   * @param loc new lobby location of arena
   */
  public void setLobbyLocation(Location loc) {
    gameLocations.put(GameLocation.LOBBY, loc);
  }

  public void teleportToStartLocation(Player player) {
    player.teleport(playerSpawnPoints.get(random.nextInt(playerSpawnPoints.size())));
  }

  private void teleportAllToStartLocation() {
    for(Player player : players) {
      player.teleport(playerSpawnPoints.get(random.nextInt(playerSpawnPoints.size())));
    }
  }

  public void teleportAllToEndLocation() {
    if(plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)
        && ConfigUtils.getConfig(plugin, "bungee").getBoolean("End-Location-Hub", true)) {
      players.forEach(plugin.getBungeeManager()::connectToHub);
      return;
    }

    Location location = getEndLocation();
    if(location == null) {
      location = getLobbyLocation();
      System.out.print("EndLocation for arena " + getId() + " isn't intialized!");
    }

    if(location != null) {
      for(Player player : players) {
        player.teleport(location);
      }
    }
  }

  public void teleportToEndLocation(Player player) {
    if(plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)
        && ConfigUtils.getConfig(plugin, "bungee").getBoolean("End-Location-Hub", true)) {
      plugin.getBungeeManager().connectToHub(player);
      return;
    }

    Location location = getEndLocation();
    if(location == null) {
      System.out.print("EndLocation for arena " + getId() + " isn't intialized!");
      location = getLobbyLocation();
    }

    if(location != null) {
      player.teleport(location);
    }
  }

  public List<Location> getPlayerSpawnPoints() {
    return playerSpawnPoints;
  }

  public void setPlayerSpawnPoints(@NotNull List<Location> playerSpawnPoints) {
    this.playerSpawnPoints = playerSpawnPoints;
  }

  /**
   * Get end location of arena.
   *
   * @return end location of arena
   */
  @Nullable
  public Location getEndLocation() {
    return gameLocations.get(GameLocation.END);
  }

  /**
   * Set end location of arena.
   *
   * @param endLoc new end location of arena
   */
  public void setEndLocation(Location endLoc) {
    gameLocations.put(GameLocation.END, endLoc);
  }

  public void loadSpecialBlock(SpecialBlock block) {
    if (!specialBlocks.contains(block)) {
      specialBlocks.add(block);
    }

    switch(block.getSpecialBlockType()) {
      case MYSTERY_CAULDRON:
        block.setArmorStandHologram(new ArmorStandHologram(Utils.getBlockCenter(block.getLocation()), chatManager.colorMessage("In-Game.Messages.Special-Blocks.Cauldron-Hologram")));
        break;
      case PRAISE_DEVELOPER:
        ArmorStandHologram prayer = new ArmorStandHologram(Utils.getBlockCenter(block.getLocation()));
        for(String str : chatManager.colorMessage("In-Game.Messages.Special-Blocks.Praise-Hologram").split(";")) {
          prayer.appendLine(str);
        }
        block.setArmorStandHologram(prayer);
        break;
      case HORSE_PURCHASE:
      case RAPID_TELEPORTATION:
        //not yet implemented
      default:
        break;
    }
  }

  public List<SpecialBlock> getSpecialBlocks() {
    return specialBlocks;
  }

  public void start() {
    Debugger.debug("[{0}] Game instance started", getId());
    runTaskTimer(plugin, 20L, 20L);
    setArenaState(ArenaState.RESTARTING);
  }

  void addPlayer(Player player) {
    players.add(player);
  }

  void removePlayer(Player player) {
    if(player != null) {
      players.remove(player);
    }
  }

  public List<Player> getPlayersLeft() {
    List<Player> list = new ArrayList<>();

    for (Player player : players) {
      User user = plugin.getUserManager().getUser(player);
      if (!user.isSpectator()) {
        list.add(user.getPlayer());
      }
    }

    return list;
  }

  void showPlayers() {
    for(Player player : players) {
      for(Player p : players) {
        VersionUtils.showPlayer(plugin, player, p);
        VersionUtils.showPlayer(plugin, p, player);
      }
    }
  }

  public void cleanUpArena() {
    removeBowHolo();

    murdererLocatorReceived = false;
    gameCharacters.clear();
    allMurderer.clear();
    allDetectives.clear();
    setDetectiveDead(false);
    clearCorpses();
    clearGold();
  }

  public void clearGold() {
    goldSpawned.stream().filter(Objects::nonNull).forEach(Item::remove);
    goldSpawned.clear();
  }

  public void clearCorpses() {
    if(plugin.getHookManager() != null && !plugin.getHookManager().isFeatureEnabled(HookManager.HookFeature.CORPSES)) {
      for(Stand stand : stands) {
        if(!stand.getHologram().isDeleted()) {
          stand.getHologram().delete();
        }
        if(stand.getStand() != null) {
          stand.getStand().remove();
        }
      }
      stands.clear();
      return;
    }
    for(Corpse corpse : corpses) {
      if(!corpse.getHologram().isDeleted()) {
        corpse.getHologram().delete();
      }
      if(corpse.getCorpseData() != null) {
        corpse.getCorpseData().destroyCorpseFromEveryone();
        CorpseAPI.removeCorpse(corpse.getCorpseData());
      }
    }
    corpses.clear();
  }

  public boolean isCharacterSet(CharacterType type) {
    return gameCharacters.containsKey(type);
  }

  public void setCharacter(CharacterType type, Player player) {
    gameCharacters.put(type, player);
  }

  @Nullable
  public Player getCharacter(CharacterType type) {
    return gameCharacters.get(type);
  }

  public void addToDetectiveList(Player player) {
    allDetectives.add(player);
  }

  public boolean lastAliveDetective() {
    return aliveDetective() <= 1;
  }

  public int aliveDetective() {
    int alive = 0;
    for(Player p : getPlayersLeft()) {
      if(Role.isRole(Role.ANY_DETECTIVE, p) && isDetectiveAlive(p)) {
        alive++;
      }
    }
    return alive;
  }

  public boolean isDetectiveAlive(Player player) {
    for(Player p : getPlayersLeft()) {
      if(p == player && allDetectives.contains(p)) {
        return true;
      }
    }
    return false;
  }

  public List<Player> getDetectiveList() {
    return allDetectives;
  }

  public void addToMurdererList(Player player) {
    allMurderer.add(player);
  }

  public void removeFromMurdererList(Player player) {
    allMurderer.remove(player);
  }


  public boolean lastAliveMurderer() {
    return aliveMurderer() == 1;
  }

  public int aliveMurderer() {
    int alive = 0;
    for(Player p : getPlayersLeft()) {
      if(Role.isRole(Role.MURDERER, p) && isMurderAlive(p)) {
        alive++;
      }
    }
    return alive;
  }

  public boolean isMurderAlive(Player player) {
    for(Player p : getPlayersLeft()) {
      if(p == player && allMurderer.contains(p)) {
        return true;
      }
    }
    return false;
  }

  public List<Player> getMurdererList() {
    return allMurderer;
  }

  public int getOption(@NotNull ArenaOption option) {
    return arenaOptions.get(option);
  }

  public void setOptionValue(ArenaOption option, int value) {
    arenaOptions.put(option, value);
  }

  public void addOptionValue(ArenaOption option, int value) {
    arenaOptions.put(option, arenaOptions.get(option) + value);
  }

  public enum BarAction {
    ADD, REMOVE
  }

  public enum GameLocation {
    LOBBY, END
  }

  public enum CharacterType {
    MURDERER, DETECTIVE, FAKE_DETECTIVE, HERO
  }


  private ArmorStandHologram bowHologram;

  public void removeBowHolo() {
    if(bowHologram != null && !bowHologram.isDeleted()) {
      bowHologram.delete();
    }

    bowHologram = null;
  }

  public void setBowHologram(ArmorStandHologram bowHologram) {
    if(bowHologram == null) {
      this.bowHologram = null;
      return;
    }

    this.bowHologram = bowHologram;
  }

  public ArmorStandHologram getBowHologram() {
    return bowHologram;
  }

  public void addDeathPlayer(Player player) {
    deaths.add(player);
  }

  public void removeDeathPlayer(Player player) {
    deaths.remove(player);
  }

  public boolean isDeathPlayer(Player player) {
    return deaths.contains(player);
  }

  public void addSpectatorPlayer(Player player) {
    spectators.add(player);
  }

  public void removeSpectatorPlayer(Player player) {
    spectators.remove(player);
  }

  public boolean isSpectatorPlayer(Player player) {
    return spectators.contains(player);
  }

}
