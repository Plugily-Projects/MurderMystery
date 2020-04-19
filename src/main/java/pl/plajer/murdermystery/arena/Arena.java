/*
 * MurderMystery - Find the murderer, kill him and survive!
 * Copyright (C) 2019  Plajer's Lair - maintained by Tigerpanzer_02, Plajer and contributors
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

package pl.plajer.murdermystery.arena;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.golde.bukkit.corpsereborn.CorpseAPI.CorpseAPI;

import pl.plajer.murdermystery.ConfigPreferences;
import pl.plajer.murdermystery.HookManager;
import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.api.StatsStorage;
import pl.plajer.murdermystery.api.events.game.MMGameStartEvent;
import pl.plajer.murdermystery.api.events.game.MMGameStateChangeEvent;
import pl.plajer.murdermystery.arena.corpse.Corpse;
import pl.plajer.murdermystery.arena.managers.ScoreboardManager;
import pl.plajer.murdermystery.arena.options.ArenaOption;
import pl.plajer.murdermystery.arena.role.Role;
import pl.plajer.murdermystery.arena.special.SpecialBlock;
import pl.plajer.murdermystery.handlers.ChatManager;
import pl.plajer.murdermystery.handlers.rewards.Reward;
import pl.plajer.murdermystery.user.User;
import pl.plajer.murdermystery.utils.Debugger;
import pl.plajer.murdermystery.utils.ItemPosition;
import pl.plajer.murdermystery.utils.Utils;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;
import pl.plajerlair.commonsbox.minecraft.serialization.InventorySerializer;
import pl.plajerlair.commonsbox.number.NumberUtils;

public class Arena extends BukkitRunnable {

  private static final Random random = new Random();
  private static final Main plugin = JavaPlugin.getPlugin(Main.class);
  private final String id;

  private Set<Player> players = new HashSet<>();
  private List<Location> goldSpawnPoints = new ArrayList<>();
  private List<Item> goldSpawned = new ArrayList<>();
  private List<Location> playerSpawnPoints = new ArrayList<>();
  private List<Corpse> corpses = new ArrayList<>();
  private List<SpecialBlock> specialBlocks = new ArrayList<>();
  private List<Player> allMurderer = new ArrayList<>();
  private List<Player> allDetectives = new ArrayList<>();

  private int murderers = 0;
  private int detectives = 0;
  private int spawnGoldTimer = 0;
  private int spawnGoldTime = 0;

  //contains murderer, detective, fake detective and hero
  private Map<CharacterType, Player> gameCharacters = new EnumMap<>(CharacterType.class);
  //all arena values that are integers, contains constant and floating values
  private Map<ArenaOption, Integer> arenaOptions = new EnumMap<>(ArenaOption.class);
  //instead of 3 location fields we use map with GameLocation enum
  private Map<GameLocation, Location> gameLocations = new EnumMap<>(GameLocation.class);

  private Hologram bowHologram;
  private boolean detectiveDead;
  private boolean murdererLocatorReceived;
  private boolean hideChances;

  private ArenaState arenaState = ArenaState.WAITING_FOR_PLAYERS;
  private BossBar gameBar;
  private ScoreboardManager scoreboardManager;
  private String mapName = "";
  private boolean ready = true;
  private boolean forceStart = false;

  public Arena(String id) {
    this.id = id;
    for (ArenaOption option : ArenaOption.values()) {
      arenaOptions.put(option, option.getDefaultValue());
    }
    if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSSBAR_ENABLED)) {
      gameBar = Bukkit.createBossBar(ChatManager.colorMessage("Bossbar.Main-Title"), BarColor.BLUE, BarStyle.SOLID);
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
    if (plugin.getHookManager() != null && !plugin.getHookManager().isFeatureEnabled(HookManager.HookFeature.CORPSES)) {
      return;
    }
    corpses.add(corpse);
  }

  public void setBowHologram(Hologram bowHologram) {
    if (this.bowHologram != null && !this.bowHologram.isDeleted()) {
      this.bowHologram.delete();
    }
    this.bowHologram = bowHologram;
  }

  @Override
  public void run() {
    //idle task
    if (getPlayers().isEmpty() && getArenaState() == ArenaState.WAITING_FOR_PLAYERS) {
      return;
    }
    Debugger.performance("ArenaTask", "[PerformanceMonitor] [{0}] Running game task", getId());
    long start = System.currentTimeMillis();

    switch (getArenaState()) {
      case WAITING_FOR_PLAYERS:
        if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
          plugin.getServer().setWhitelist(false);
        }
        if (getPlayers().size() < getMinimumPlayers()) {
          if (getTimer() <= 0) {
            setTimer(15);
            ChatManager.broadcast(this, ChatManager.formatMessage(this, ChatManager.colorMessage("In-Game.Messages.Lobby-Messages.Waiting-For-Players"), getMinimumPlayers()));
            break;
          }
        } else {
          if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSSBAR_ENABLED)) {
            gameBar.setTitle(ChatManager.colorMessage("Bossbar.Waiting-For-Players"));
          }
          ChatManager.broadcast(this, ChatManager.colorMessage("In-Game.Messages.Lobby-Messages.Enough-Players-To-Start"));
          setArenaState(ArenaState.STARTING);
          setTimer(plugin.getConfig().getInt("Starting-Waiting-Time", 60));
          this.showPlayers();
        }
        setTimer(getTimer() - 1);
        break;
      case STARTING:
        if (getPlayers().size() == getMaximumPlayers() && getTimer() >= plugin.getConfig().getInt("Start-Time-On-Full-Lobby", 15) && !forceStart) {
          setTimer(plugin.getConfig().getInt("Start-Time-On-Full-Lobby", 15));
          ChatManager.broadcast(this, ChatManager.colorMessage("In-Game.Messages.Lobby-Messages.Start-In").replace("%TIME%", String.valueOf(getTimer())));
        }
        if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSSBAR_ENABLED)) {
          gameBar.setTitle(ChatManager.colorMessage("Bossbar.Starting-In").replace("%time%", String.valueOf(getTimer())));
          gameBar.setProgress(getTimer() / plugin.getConfig().getDouble("Starting-Waiting-Time", 60));
        }
        for (Player player : getPlayers()) {
          player.setExp((float) (getTimer() / plugin.getConfig().getDouble("Starting-Waiting-Time", 60)));
          player.setLevel(getTimer());
        }
        if (getPlayers().size() < getMinimumPlayers() && !forceStart) {
          if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSSBAR_ENABLED)) {
            gameBar.setTitle(ChatManager.colorMessage("Bossbar.Waiting-For-Players"));
            gameBar.setProgress(1.0);
          }
          ChatManager.broadcast(this, ChatManager.formatMessage(this, ChatManager.colorMessage("In-Game.Messages.Lobby-Messages.Waiting-For-Players"), getMinimumPlayers()));
          setArenaState(ArenaState.WAITING_FOR_PLAYERS);
          Bukkit.getPluginManager().callEvent(new MMGameStartEvent(this));
          setTimer(15);
          for (Player player : getPlayers()) {
            player.setExp(1);
            player.setLevel(0);
          }
          if (forceStart) {
            forceStart = false;
          }
          break;
        }
        int totalMurderer = 0;
        int totalDetective = 0;
        for (Player p : getPlayers()) {
          User user = plugin.getUserManager().getUser(p);
          totalMurderer += user.getStat(StatsStorage.StatisticType.CONTRIBUTION_MURDERER);
          totalDetective += user.getStat(StatsStorage.StatisticType.CONTRIBUTION_DETECTIVE);
        }
        if (!hideChances) {
          for (Player p : getPlayers()) {
            User user = plugin.getUserManager().getUser(p);
            try {
              p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(formatRoleChance(user, totalMurderer, totalDetective)));
            } catch (NumberFormatException ignored) {
              //fail silently
            }
          }
        }
        if (getTimer() == 0 || forceStart) {
          MMGameStartEvent gameStartEvent = new MMGameStartEvent(this);
          Bukkit.getPluginManager().callEvent(gameStartEvent);
          setArenaState(ArenaState.IN_GAME);
          if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSSBAR_ENABLED)) {
            gameBar.setProgress(1.0);
          }
          setTimer(5);
          if (players.isEmpty()) {
            break;
          }
          teleportAllToStartLocation();
          for (Player player : getPlayers()) {
            ArenaUtils.updateNameTagsVisibility(player);
            player.getInventory().clear();
            player.setGameMode(GameMode.ADVENTURE);
            ArenaUtils.hidePlayersOutsideTheGame(player, this);
            player.updateInventory();
            plugin.getUserManager().getUser(player).addStat(StatsStorage.StatisticType.GAMES_PLAYED, 1);
            setTimer(plugin.getConfig().getInt("Classic-Gameplay-Time", 270));
            player.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("In-Game.Messages.Lobby-Messages.Game-Started"));
          }

          Map<User, Double> murdererChances = new HashMap<>();
          Map<User, Double> detectiveChances = new HashMap<>();
          for (Player p : getPlayers()) {
            User user = plugin.getUserManager().getUser(p);
            murdererChances.put(user, ((double) user.getStat(StatsStorage.StatisticType.CONTRIBUTION_MURDERER) / (double) totalMurderer) * 100.0);
            detectiveChances.put(user, ((double) user.getStat(StatsStorage.StatisticType.CONTRIBUTION_DETECTIVE) / (double) totalDetective) * 100.0);
          }
          Map<User, Double> sortedMurderer = murdererChances.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(
            Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

          Set<Player> playersToSet = new HashSet<>(getPlayers());
          int maxmurderer = 1;
          int maxdetectives = 1;
          Debugger.debug(Level.INFO, "Before: Arena: {0} | Detectives = {1}, Murders = {2}, Players = {3} | Configured: Detectives = {4}, Murders = {5}",
            getId(), maxdetectives, maxmurderer, getPlayers().size(), detectives, murderers);
          if (murderers > 1 && getPlayers().size() > murderers) {
            maxmurderer = (getPlayers().size() / murderers);
          }
          if (detectives > 1 && getPlayers().size() > detectives) {
            maxdetectives = (getPlayers().size() / detectives);
          }
          if (getPlayers().size() - (maxmurderer + maxdetectives) < 1) {
            ChatManager.broadcast(this, "Murderers and detectives amount was reduced because there are not enough players");
            //Make sure to have one innocent!
            if (maxdetectives > 1) {
              maxdetectives--;
            } else if (maxmurderer > 1) {
              maxmurderer--;
            }
          }
          Debugger.debug(Level.INFO, "After: Arena: {0} | Detectives = {1}, Murders = {2}, Players = {3} | Configured: Detectives = {4}, Murders = {5}",
            getId(), maxdetectives, maxmurderer, getPlayers().size(), detectives, murderers);
          for (int i = 0; i < (maxmurderer); i++) {
            Player murderer = ((User) sortedMurderer.keySet().toArray()[i]).getPlayer();
            setCharacter(CharacterType.MURDERER, murderer);
            allMurderer.add(murderer);
            plugin.getUserManager().getUser(murderer).setStat(StatsStorage.StatisticType.CONTRIBUTION_MURDERER, 1);
            playersToSet.remove(murderer);
            murderer.sendTitle(ChatManager.colorMessage("In-Game.Messages.Role-Set.Murderer-Title"),
              ChatManager.colorMessage("In-Game.Messages.Role-Set.Murderer-Subtitle"), 5, 40, 5);
            detectiveChances.remove(sortedMurderer.keySet().toArray()[i]);
          }

          Map<User, Double> sortedDetective = detectiveChances.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(
            Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
          for (int i = 0; i < (maxdetectives); i++) {
            Player detective = ((User) sortedDetective.keySet().toArray()[i]).getPlayer();
            setCharacter(CharacterType.DETECTIVE, detective);
            allDetectives.add(detective);
            plugin.getUserManager().getUser(detective).setStat(StatsStorage.StatisticType.CONTRIBUTION_DETECTIVE, 1);
            detective.sendTitle(ChatManager.colorMessage("In-Game.Messages.Role-Set.Detective-Title"),
              ChatManager.colorMessage("In-Game.Messages.Role-Set.Detective-Subtitle"), 5, 40, 5);
            playersToSet.remove(detective);

            ItemPosition.setItem(detective, ItemPosition.BOW, new ItemStack(Material.BOW, 1));
            ItemPosition.setItem(detective, ItemPosition.INFINITE_ARROWS, new ItemStack(Material.ARROW, plugin.getConfig().getInt("Detective-Default-Arrows", 3)));
          }
          Debugger.debug(Level.INFO, "Arena: {0} | Detectives = {1}, Murders = {2}, Players = {3} | Players: Detectives = {4}, Murders = {5}",
            getId(), maxdetectives, maxmurderer, getPlayers().size(), allDetectives, allMurderer);

          for (Player p : playersToSet) {
            p.sendTitle(ChatManager.colorMessage("In-Game.Messages.Role-Set.Innocent-Title"),
              ChatManager.colorMessage("In-Game.Messages.Role-Set.Innocent-Subtitle"), 5, 40, 5);
          }
          if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSSBAR_ENABLED)) {
            gameBar.setTitle(ChatManager.colorMessage("Bossbar.In-Game-Info"));
          }
        }
        if (forceStart) {
          forceStart = false;
        }
        setTimer(getTimer() - 1);
        break;
      case IN_GAME:
        if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
          if (getMaximumPlayers() <= getPlayers().size()) {
            plugin.getServer().setWhitelist(true);
          } else {
            plugin.getServer().setWhitelist(false);
          }
        }
        if (getTimer() <= 0) {
          ArenaManager.stopGame(false, this);
        }
        if (getTimer() <= (plugin.getConfig().getInt("Classic-Gameplay-Time", 270) - 10)
          && getTimer() > (plugin.getConfig().getInt("Classic-Gameplay-Time", 270) - 15)) {
          for (Player p : getPlayers()) {
            p.sendMessage(ChatManager.colorMessage("In-Game.Messages.Murderer-Get-Sword")
              .replace("%time%", String.valueOf(getTimer() - (plugin.getConfig().getInt("Classic-Gameplay-Time", 270) - 15))));
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
          }
          if (getTimer() == (plugin.getConfig().getInt("Classic-Gameplay-Time", 270) - 14)) {
            for (Player p : allMurderer) {
              User murderer = plugin.getUserManager().getUser(p);
              if (murderer.isSpectator()) continue;
              ItemPosition.setItem(p, ItemPosition.MURDERER_SWORD, plugin.getConfigPreferences().getMurdererSword());
              p.getInventory().setHeldItemSlot(0);
            }
          }
        }

        //every 30 secs survive reward
        if (getTimer() % 30 == 0) {
          for (Player p : getPlayersLeft()) {
            if (Role.isRole(Role.INNOCENT, p)) {
              ArenaUtils.addScore(plugin.getUserManager().getUser(p), ArenaUtils.ScoreAction.SURVIVE_TIME, 0);
            }
          }
        }

        if (getTimer() == 30 || getTimer() == 60) {
          String title = ChatManager.colorMessage("In-Game.Messages.Seconds-Left-Title").replace("%time%", String.valueOf(getTimer()));
          String subtitle = ChatManager.colorMessage("In-Game.Messages.Seconds-Left-Subtitle").replace("%time%", String.valueOf(getTimer()));
          for (Player p : getPlayers()) {
            p.sendTitle(title, subtitle, 5, 40, 5);
          }
        }

        if (getTimer() <= 30 || getPlayersLeft().size() == aliveMurderer() + 1) {
          ArenaUtils.updateInnocentLocator(this);
        }
        //no players - stop game
        if (getPlayersLeft().size() == 0) {
          ArenaManager.stopGame(false, this);
        } else
          //winner check
          if (getPlayersLeft().size() == aliveMurderer()) {
            for (Player p : getPlayers()) {
              p.sendTitle(ChatManager.colorMessage("In-Game.Messages.Game-End-Messages.Titles.Lose"),
                ChatManager.colorMessage("In-Game.Messages.Game-End-Messages.Subtitles.Murderer-Kill-Everyone"), 5, 40, 5);
              if (allMurderer.contains(p)) {
                p.sendTitle(ChatManager.colorMessage("In-Game.Messages.Game-End-Messages.Titles.Win"), null, 5, 40, 5);
              }
            }
            ArenaManager.stopGame(false, this);
          } else
            //murderer speed add
            if (getPlayersLeft().size() == aliveMurderer() + 1) {
              for (Player p : allMurderer) {
                if (isMurderAlive(p)) {
                  //no potion because it adds particles which can be identified
                  p.setWalkSpeed(0.3f);
                }
              }
            }

        //don't spawn it every time
        if (spawnGoldTimer == spawnGoldTime) {
          spawnSomeGold();
          spawnGoldTimer = 0;
        } else {
          spawnGoldTimer++;
        }
        setTimer(getTimer() - 1);
        break;
      case ENDING:
        scoreboardManager.stopAllScoreboards();
        if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
          plugin.getServer().setWhitelist(false);
        }
        if (getTimer() <= 0) {
          if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSSBAR_ENABLED)) {
            gameBar.setTitle(ChatManager.colorMessage("Bossbar.Game-Ended"));
          }

          List<Player> playersToQuit = new ArrayList<>(getPlayers());
          for (Player player : playersToQuit) {
            plugin.getUserManager().getUser(player).removeScoreboard();
            player.setGameMode(GameMode.SURVIVAL);
            for (Player players : Bukkit.getOnlinePlayers()) {
              player.showPlayer(players);
              if (ArenaRegistry.getArena(players) == null) {
                players.showPlayer(player);
              }
            }
            for (PotionEffect effect : player.getActivePotionEffects()) {
              player.removePotionEffect(effect.getType());
            }
            player.setWalkSpeed(0.2f);
            player.setFlying(false);
            player.setAllowFlight(false);
            player.getInventory().clear();

            player.getInventory().setArmorContents(null);
            doBarAction(BarAction.REMOVE, player);
            player.setFireTicks(0);
            player.setFoodLevel(20);
          }
          teleportAllToEndLocation();

          if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
            for (Player player : getPlayers()) {
              InventorySerializer.loadInventory(plugin, player);
            }
          }

          ChatManager.broadcast(this, ChatManager.colorMessage("Commands.Teleported-To-The-Lobby"));

          for (User user : plugin.getUserManager().getUsers(this)) {
            user.setSpectator(false);

            for (StatsStorage.StatisticType statistic : StatsStorage.StatisticType.values()) {
              if (!statistic.isPersistent()) {
                user.setStat(statistic, 0);
              }
            }
          }
          plugin.getRewardsHandler().performReward(this, Reward.RewardType.END_GAME);
          players.clear();

          cleanUpArena();
          if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
            if (ConfigUtils.getConfig(plugin, "bungee").getBoolean("Shutdown-When-Game-Ends")) {
              plugin.getServer().shutdown();
            }
          }
          setArenaState(ArenaState.RESTARTING);
        }
        setTimer(getTimer() - 1);
        break;
      case RESTARTING:
        getPlayers().clear();
        setArenaState(ArenaState.WAITING_FOR_PLAYERS);
        if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
          for (Player player : Bukkit.getOnlinePlayers()) {
            ArenaManager.joinAttempt(player, this);
          }
        }
        if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSSBAR_ENABLED)) {
          gameBar.setTitle(ChatManager.colorMessage("Bossbar.Waiting-For-Players"));
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
    String message = ChatManager.colorMessage("In-Game.Messages.Lobby-Messages.Role-Chances-Action-Bar");
    message = StringUtils.replace(message, "%murderer_chance%", NumberUtils.round(((double) user.getStat(StatsStorage.StatisticType.CONTRIBUTION_MURDERER) / (double) murdererPts) * 100.0, 2) + "%");
    message = StringUtils.replace(message, "%detective_chance%", NumberUtils.round(((double) user.getStat(StatsStorage.StatisticType.CONTRIBUTION_DETECTIVE) / (double) detectivePts) * 100.0, 2) + "%");
    return message;
  }

  private void spawnSomeGold() {
    //do not exceed amount of gold per spawn
    if (goldSpawned.size() >= goldSpawnPoints.size()) {
      return;
    }
    Location loc = goldSpawnPoints.get(random.nextInt(goldSpawnPoints.size()));
    Item item = loc.getWorld().dropItem(loc, new ItemStack(Material.GOLD_INGOT, 1));
    goldSpawned.add(item);
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

  public List<Item> getGoldSpawned() {
    return goldSpawned;
  }

  public List<Location> getGoldSpawnPoints() {
    return goldSpawnPoints;
  }

  public void setGoldSpawnPoints(List<Location> goldSpawnPoints) {
    this.goldSpawnPoints = goldSpawnPoints;
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
    if (minimumPlayers < 2) {
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
  public String getMapName() {
    return mapName;
  }

  /**
   * Set arena map name.
   *
   * @param mapname new map name, it's not arena id
   */
  public void setMapName(String mapname) {
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
  public ArenaState getArenaState() {
    return arenaState;
  }

  /**
   * Set game state of arena.
   *
   * @param arenaState new game state of arena
   * @see ArenaState
   */
  public void setArenaState(ArenaState arenaState) {
    this.arenaState = arenaState;
    MMGameStateChangeEvent gameStateChangeEvent = new MMGameStateChangeEvent(this, getArenaState());
    Bukkit.getPluginManager().callEvent(gameStateChangeEvent);
  }

  /**
   * Get all players in arena.
   *
   * @return set of players in arena
   */
  public Set<Player> getPlayers() {
    return players;
  }

  public void teleportToLobby(Player player) {
    player.setFoodLevel(20);
    player.setFlying(false);
    player.setAllowFlight(false);
    for (PotionEffect effect : player.getActivePotionEffects()) {
      player.removePotionEffect(effect.getType());
    }
    player.setWalkSpeed(0.2f);
    Location location = getLobbyLocation();
    if (location == null) {
      System.out.print("LobbyLocation isn't intialized for arena " + getId());
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
    if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSSBAR_ENABLED)) {
      return;
    }
    switch (action) {
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
    for (Player player : getPlayers()) {
      player.teleport(playerSpawnPoints.get(random.nextInt(playerSpawnPoints.size())));
    }
  }

  public void teleportAllToEndLocation() {
    if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)
      && ConfigUtils.getConfig(plugin, "bungee").getBoolean("End-Location-Hub", true)) {
      for (Player player : getPlayers()) {
        plugin.getBungeeManager().connectToHub(player);
      }
      return;
    }
    Location location = getEndLocation();

    if (location == null) {
      location = getLobbyLocation();
      System.out.print("EndLocation for arena " + getId() + " isn't intialized!");
    }
    for (Player player : getPlayers()) {
      player.teleport(location);
    }
  }

  public void teleportToEndLocation(Player player) {
    if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)
      && ConfigUtils.getConfig(plugin, "bungee").getBoolean("End-Location-Hub", true)) {
      plugin.getBungeeManager().connectToHub(player);
      return;
    }
    Location location = getEndLocation();
    if (location == null) {
      location = getLobbyLocation();
      System.out.print("EndLocation for arena " + getId() + " isn't intialized!");
    }

    player.teleport(location);
  }

  public List<Location> getPlayerSpawnPoints() {
    return playerSpawnPoints;
  }

  public void setPlayerSpawnPoints(List<Location> playerSpawnPoints) {
    this.playerSpawnPoints = playerSpawnPoints;
  }

  /**
   * Get end location of arena.
   *
   * @return end location of arena
   */
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
    specialBlocks.add(block);
    Hologram holo;
    switch (block.getSpecialBlockType()) {
      case MYSTERY_CAULDRON:
        holo = HologramsAPI.createHologram(plugin, Utils.getBlockCenter(block.getLocation().clone().add(0, 1.8, 0)));
        holo.appendTextLine(ChatManager.colorMessage("In-Game.Messages.Special-Blocks.Cauldron-Hologram"));
        break;
      case PRAISE_DEVELOPER:
        holo = HologramsAPI.createHologram(plugin, Utils.getBlockCenter(block.getLocation().clone().add(0, 2.0, 0)));
        for (String str : ChatManager.colorMessage("In-Game.Messages.Special-Blocks.Praise-Hologram").split(";")) {
          holo.appendTextLine(str);
        }
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
    Debugger.debug(Level.INFO, "[{0}] Game instance started", getId());
    this.runTaskTimer(plugin, 20L, 20L);
    this.setArenaState(ArenaState.RESTARTING);
  }

  void addPlayer(Player player) {
    players.add(player);
  }

  void removePlayer(Player player) {
    if (player == null) {
      return;
    }
    players.remove(player);
  }

  public List<Player> getPlayersLeft() {
    List<Player> players = new ArrayList<>();
    for (User user : plugin.getUserManager().getUsers(this)) {
      if (!user.isSpectator()) {
        players.add(user.getPlayer());
      }
    }
    return players;
  }

  void showPlayers() {
    for (Player player : getPlayers()) {
      for (Player p : getPlayers()) {
        player.showPlayer(p);
        p.showPlayer(player);
      }
    }
  }

  public void cleanUpArena() {
    if (bowHologram != null && !bowHologram.isDeleted()) {
      bowHologram.delete();
    }
    murdererLocatorReceived = false;
    bowHologram = null;
    gameCharacters.clear();
    allMurderer.clear();
    allDetectives.clear();
    setDetectiveDead(false);
    clearCorpses();
    clearGold();
  }

  public void clearGold() {
    for (Item item : goldSpawned) {
      if (item != null) {
        item.remove();
      }
    }
    goldSpawned.clear();
  }

  public void clearCorpses() {
    if (plugin.getHookManager() != null && !plugin.getHookManager().isFeatureEnabled(HookManager.HookFeature.CORPSES)) {
      return;
    }
    for (Corpse corpse : corpses) {
      if (!corpse.getHologram().isDeleted()) {
        corpse.getHologram().delete();
      }
      if (corpse.getCorpseData() != null) {
        corpse.getCorpseData().destroyCorpseFromEveryone();
        CorpseAPI.removeCorpse(corpse.getCorpseData());
      }
    }
    corpses.clear();
  }

  public boolean isCharacterSet(CharacterType type) {
    return gameCharacters.get(type) != null;
  }

  public void setCharacter(CharacterType type, Player player) {
    gameCharacters.put(type, player);
  }

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
    for (Player p : getPlayersLeft()) {
      if (Role.isRole(Role.ANY_DETECTIVE, p) && isDetectiveAlive(p)) {
        alive++;
      }
    }
    return alive;
  }

  public boolean isDetectiveAlive(Player player) {
    for (Player p : getPlayersLeft()) {
      if (p == player && allDetectives.contains(p)) {
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
    for (Player p : getPlayersLeft()) {
      if (allMurderer.contains(p) && isMurderAlive(p)) {
        alive++;
      }
    }
    return alive;
  }

  public boolean isMurderAlive(Player player) {
    for (Player p : getPlayersLeft()) {
      if (p == player && allMurderer.contains(p)) {
        return true;
      }
    }
    return false;
  }

  public List<Player> getMurdererList() {
    return allMurderer;
  }

  public int getOption(ArenaOption option) {
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

}
