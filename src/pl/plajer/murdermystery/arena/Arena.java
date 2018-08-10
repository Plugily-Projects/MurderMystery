/*
 * Village Defense 3 - Protect villagers from hordes of zombies
 * Copyright (C) 2018  Plajer's Lair - maintained by Plajer
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.golde.bukkit.corpsereborn.CorpseAPI.CorpseAPI;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.handlers.ChatManager;
import pl.plajer.murdermystery.handlers.language.LanguageManager;
import pl.plajer.murdermystery.handlers.language.Locale;
import pl.plajer.murdermystery.murdermysteryapi.MMGameStartEvent;
import pl.plajer.murdermystery.murdermysteryapi.MMGameStateChangeEvent;
import pl.plajer.murdermystery.user.User;
import pl.plajer.murdermystery.user.UserManager;
import pl.plajer.murdermystery.utils.MessageUtils;
import pl.plajerlair.core.services.ReportedException;
import pl.plajerlair.core.utils.ConfigUtils;
import pl.plajerlair.core.utils.MinigameScoreboard;
import pl.plajerlair.core.utils.MinigameUtils;

/**
 * Created by Tom on 12/08/2014.
 */
public class Arena extends BukkitRunnable {

  private final Main plugin;
  private final Set<UUID> players = new HashSet<>();
  private List<Location> goldSpawnPoints = new ArrayList<>();
  private List<Item> goldSpawned = new ArrayList<>();
  private List<Location> playerSpawnPoints = new ArrayList<>();
  private List<ArenaCorpse> corpses = new ArrayList<>();
  private Hologram bowHologram;
  private UUID murderer;
  private UUID detective;
  private UUID fakeDetective;
  private UUID hero;
  private boolean murdererDead;
  private boolean detectiveDead;
  private boolean murdererLocatorReceived;
  private BossBar gameBar;
  private ArenaState arenaState;
  private int minimumPlayers = 2;
  private int maximumPlayers = 10;
  private String mapName = "";
  private int timer;
  private String ID;
  //instead of 3 location fields we use map with GameLocation enum
  private Map<GameLocation, Location> gameLocations = new HashMap<>();
  private boolean ready = true;

  public Arena(String ID, Main plugin) {
    this.plugin = plugin;
    arenaState = ArenaState.WAITING_FOR_PLAYERS;
    this.ID = ID;
    if (plugin.isBossbarEnabled()) {
      gameBar = Bukkit.createBossBar(ChatManager.colorMessage("Bossbar.Main-Title"), BarColor.BLUE, BarStyle.SOLID);
    }
  }

  public boolean isReady() {
    return ready;
  }

  public void setReady(boolean ready) {
    this.ready = ready;
  }

  public void addCorpse(ArenaCorpse corpse) {
    corpses.add(corpse);
  }

  public void setBowHologram(Hologram bowHologram) {
    if (this.bowHologram != null) {
      if (!this.bowHologram.isDeleted()) {
        this.bowHologram.delete();
      }
    }
    this.bowHologram = bowHologram;
  }

  @Nullable
  public BossBar getGameBar() {
    return gameBar;
  }

  public void run() {
    try {
      //idle task
      if (getPlayers().size() == 0 && getArenaState() == ArenaState.WAITING_FOR_PLAYERS) {
        return;
      }
      updateScoreboard();
      switch (getArenaState()) {
        case WAITING_FOR_PLAYERS:
          if (plugin.isBungeeActivated()) {
            plugin.getServer().setWhitelist(false);
          }
          if (getPlayers().size() < getMinimumPlayers()) {
            if (getTimer() <= 0) {
              setTimer(15);
              String message = ChatManager.formatMessage(this, ChatManager.colorMessage("In-Game.Messages.Lobby-Messages.Waiting-For-Players"), getMinimumPlayers());
              for (Player p : getPlayers()) {
                p.sendMessage(ChatManager.PLUGIN_PREFIX + message);
              }
              return;
            }
          } else {
            if (plugin.isBossbarEnabled()) {
              gameBar.setTitle(ChatManager.colorMessage("Bossbar.Waiting-For-Players"));
            }
            for (Player p : getPlayers()) {
              p.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("In-Game.Messages.Lobby-Messages.Enough-Players-To-Start"));
            }
            setArenaState(ArenaState.STARTING);
            setTimer(Main.STARTING_TIMER_TIME);
            this.showPlayers();
          }
          setTimer(getTimer() - 1);
          break;
        case STARTING:
          if (plugin.isBossbarEnabled()) {
            gameBar.setTitle(ChatManager.colorMessage("Bossbar.Starting-In").replace("%time%", String.valueOf(getTimer())));
            gameBar.setProgress(getTimer() / plugin.getConfig().getDouble("Starting-Waiting-Time", 60));
          }
          for (Player p : getPlayers()) {
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatManager.colorMessage("In-Game.Messages.Lobby-Messages.Role-Chances-Action-Bar")));
          }
          if (getTimer() == 0) {
            MMGameStartEvent gameStartEvent = new MMGameStartEvent(this);
            Bukkit.getPluginManager().callEvent(gameStartEvent);
            setArenaState(ArenaState.IN_GAME);
            if (plugin.isBossbarEnabled()) {
              gameBar.setProgress(1.0);
            }
            setTimer(5);
            teleportAllToStartLocation();
            for (Player player : getPlayers()) {
              player.getInventory().clear();
              player.setGameMode(GameMode.ADVENTURE);
              ArenaUtils.hidePlayersOutsideTheGame(player, this);
              player.updateInventory();
              addStat(player, "gamesplayed");
              setTimer(Main.CLASSIC_TIMER_TIME);
              player.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("In-Game.Messages.Lobby-Messages.Game-Started"));
            }
            Set<Player> playersToSet = getPlayers();
            Player murderer = (Player) playersToSet.toArray()[new Random().nextInt(playersToSet.size())];
            this.murderer = murderer.getUniqueId();
            playersToSet.remove(murderer);
            MessageUtils.sendTitle(murderer, ChatManager.colorMessage("In-Game.Messages.Role-Set.Murderer-Title"), 5, 40, 5);
            MessageUtils.sendSubTitle(murderer, ChatManager.colorMessage("In-Game.Messages.Role-Set.Murderer-Subtitle"), 5, 40, 5);

            Player detective = (Player) playersToSet.toArray()[new Random().nextInt(playersToSet.size())];
            this.detective = detective.getUniqueId();
            MessageUtils.sendTitle(detective, ChatManager.colorMessage("In-Game.Messages.Role-Set.Detective-Title"), 5, 40, 5);
            MessageUtils.sendSubTitle(detective, ChatManager.colorMessage("In-Game.Messages.Role-Set.Detective-Subtitle"), 5, 40, 5);
            playersToSet.remove(detective);

            detective.getInventory().setItem(1, new ItemStack(Material.BOW, 1));
            detective.getInventory().setItem(9, new ItemStack(Material.ARROW, 64));

            for (Player p : playersToSet) {
              MessageUtils.sendTitle(p, ChatManager.colorMessage("In-Game.Messages.Role-Set.Innocent-Title"), 5, 40, 5);
              MessageUtils.sendSubTitle(p, ChatManager.colorMessage("In-Game.Messages.Role-Set.Innocent-Subtitle"), 5, 40, 5);
            }
            if (plugin.isBossbarEnabled()) {
              gameBar.setTitle(ChatManager.colorMessage("Bossbar.In-Game-Info"));
            }
            return;
          }
          setTimer(getTimer() - 1);
          break;
        case IN_GAME:
          if (plugin.isBungeeActivated()) {
            if (getMaximumPlayers() <= getPlayers().size()) {
              plugin.getServer().setWhitelist(true);
            } else {
              plugin.getServer().setWhitelist(false);
            }
          }
          if (getTimer() <= 0) {
            setArenaState(ArenaState.ENDING);
            ArenaManager.stopGame(false, this);
            setTimer(5);
          }
          if (getTimer() <= (Main.CLASSIC_TIMER_TIME - 10) && getTimer() > (Main.CLASSIC_TIMER_TIME - 15)) {
            for (Player p : getPlayers()) {
              p.sendMessage(ChatManager.colorMessage("In-Game.Messages.Murderer-Get-Sword").replace("%time%", String.valueOf(getTimer() - (Main.CLASSIC_TIMER_TIME - 15))));
              p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            }
            if (getTimer() == (Main.CLASSIC_TIMER_TIME - 14)) {
              Bukkit.getPlayer(murderer).getInventory().setItem(1, new ItemStack(Material.IRON_SWORD, 1));
              Bukkit.getPlayer(murderer).getInventory().setHeldItemSlot(0);
            }
          }

          if (getTimer() == 30 || getTimer() == 60) {
            for (Player p : getPlayers()) {
              MessageUtils.sendTitle(p, ChatManager.colorMessage("In-Game.Messages.Seconds-Left-Title").replace("%time%", String.valueOf(getTimer())), 5, 30, 5);
              MessageUtils.sendSubTitle(p, ChatManager.colorMessage("In-Game.Messages.Seconds-Left-Subtitle").replace("%time%", String.valueOf(getTimer())), 5, 30, 5);
            }
          }

          if (getTimer() <= 30 || getPlayersLeft().size() == 2) {
            ArenaUtils.updateInnocentLocator(this);
          }

          switch (getPlayersLeft().size()) {
            //game end
            case 0:
              setArenaState(ArenaState.ENDING);
              ArenaManager.stopGame(false, this);
              setTimer(5);
              return;

            //winner check
            case 1:
              if (getPlayersLeft().get(0).getUniqueId() == murderer) {
                for (Player p : getPlayers()) {
                  MessageUtils.sendTitle(p, ChatManager.colorMessage("In-Game.Messages.Game-End-Messages.Titles.Lose"), 5, 40, 5);
                  MessageUtils.sendSubTitle(p, ChatManager.colorMessage("In-Game.Messages.Game-End-Messages.Subtitles.Murderer-Kill-Everyone"), 5, 40, 5);
                  if (p.getUniqueId() == murderer) {
                    MessageUtils.sendTitle(p, ChatManager.colorMessage("In-Game.Messages.Game-End-Messages.Titles.Win"), 5, 40, 5);
                  }
                }
                ArenaManager.stopGame(false, this);
                setArenaState(ArenaState.ENDING);
                setTimer(5);
                return;
              }

              //murderer speed add
            case 2:
              Bukkit.getPlayer(murderer).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));
              break;
          }

          //don't spawn it every time
          if (new Random().nextInt(2) == 2) {
            spawnSomeGold();
          }
          setTimer(getTimer() - 1);
          break;
        case ENDING:
          if (plugin.isBungeeActivated()) {
            plugin.getServer().setWhitelist(false);
          }
          if (getTimer() <= 0) {
            if (plugin.isBossbarEnabled()) {
              gameBar.setTitle(ChatManager.colorMessage("Bossbar.Game-Ended"));
            }

            for (Player player : getPlayers()) {
              UserManager.getUser(player.getUniqueId()).removeScoreboard();
              player.setGameMode(GameMode.SURVIVAL);
              for (Player players : Bukkit.getOnlinePlayers()) {
                player.showPlayer(players);
                players.hidePlayer(player);
              }
              for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
              }
              player.setFlying(false);
              player.setAllowFlight(false);
              player.getInventory().clear();

              player.getInventory().setArmorContents(null);
              if (plugin.isBossbarEnabled()) {
                gameBar.removePlayer(player);
              }
              player.setFireTicks(0);
              player.setFoodLevel(20);
              for (Player players : plugin.getServer().getOnlinePlayers()) {
                if (ArenaRegistry.getArena(players) != null) {
                  players.showPlayer(player);
                }
                player.showPlayer(players);
              }
            }
            teleportAllToEndLocation();

            if (plugin.isInventoryManagerEnabled()) {
              for (Player player : getPlayers()) {
                plugin.getInventoryManager().loadInventory(player);
              }
            }

            for (Player p : getPlayers()) {
              p.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("Commands.Teleported-To-The-Lobby"));
            }

            for (User user : UserManager.getUsers(this)) {
              user.setSpectator(false);
              user.setFakeDead(false);
            }
            plugin.getRewardsHandler().performEndGameRewards(this);
            players.clear();
            if (plugin.isBungeeActivated()) {
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
          cleanUpArena();

          if (plugin.isBungeeActivated()) {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
              this.addPlayer(player);
            }
          }
          break;
        default:
          break; //o.o?
      }
    } catch (Exception ex) {
      new ReportedException(plugin, ex);
    }
  }

  private void updateScoreboard() {
    if (getPlayers().size() == 0 || getArenaState() == ArenaState.RESTARTING) {
      return;
    }
    MinigameScoreboard scoreboard;
    for (Player p : getPlayers()) {
      if (p == null) {
        continue;
      }
      User user = UserManager.getUser(p.getUniqueId());
      scoreboard = new MinigameScoreboard("PL_MM", "PL_CR", ChatManager.colorMessage("Scoreboard.Title"));
      List<String> lines;
      String access = "";
      if (ArenaUtils.isRole(ArenaUtils.Role.MURDERER, p)) {
        access = "-Murderer";
      }
      if (getArenaState() == ArenaState.IN_GAME) {
        if (LanguageManager.getPluginLocale() == Locale.ENGLISH) {
          lines = LanguageManager.getLanguageFile().getStringList("Scoreboard.Content.Playing" + access);
        } else {
          lines = Arrays.asList(ChatManager.colorMessage("Scoreboard.Content.Playing" + access).split(";"));
        }
      } else {
        if (LanguageManager.getPluginLocale() == Locale.ENGLISH) {
          lines = LanguageManager.getLanguageFile().getStringList("Scoreboard.Content." + getArenaState().getFormattedName());
        } else {
          lines = Arrays.asList(ChatManager.colorMessage("Scoreboard.Content." + getArenaState().getFormattedName()).split(";"));
        }
      }
      for (String line : lines) {
        scoreboard.addRow(formatScoreboardLine(line, user));
      }
      scoreboard.finish();
      scoreboard.display(p);
    }
  }

  private String formatScoreboardLine(String line, User user) {
    String formattedLine = line;
    formattedLine = formattedLine.replace("%TIME%", String.valueOf(getTimer()));
    formattedLine = formattedLine.replace("%FORMATTED_TIME%", MinigameUtils.formatIntoMMSS(getTimer()));
    int innocents = 0;
    for (Player p : getPlayersLeft()) {
      if (ArenaUtils.isRole(ArenaUtils.Role.ANY_DETECTIVE, p)) continue;
      innocents++;
    }
    if (!getPlayersLeft().contains(user.toPlayer())) {
      formattedLine = formattedLine.replace("%ROLE%", ChatManager.colorMessage("Scoreboard.Roles.Dead"));
    } else {
      if (murderer == user.toPlayer().getUniqueId()) {
        formattedLine = formattedLine.replace("%ROLE%", ChatManager.colorMessage("Scoreboard.Roles.Murderer"));
      } else if (detective == user.toPlayer().getUniqueId() || (fakeDetective != null && fakeDetective == user.toPlayer().getUniqueId())) {
        formattedLine = formattedLine.replace("%ROLE%", ChatManager.colorMessage("Scoreboard.Roles.Detective"));
      } else {
        formattedLine = formattedLine.replace("%ROLE%", ChatManager.colorMessage("Scoreboard.Roles.Innocent"));
      }
    }
    formattedLine = formattedLine.replace("%INNOCENTS%", String.valueOf(innocents));
    formattedLine = formattedLine.replace("%PLAYERS%", String.valueOf(getPlayers().size()));
    formattedLine = formattedLine.replace("%MIN_PLAYERS%", String.valueOf(getMinimumPlayers()));
    if (detectiveDead && fakeDetective == null) {
      formattedLine = formattedLine.replace("%DETECTIVE_STATUS%", ChatManager.colorMessage("Scoreboard.Detective-Died-No-Bow"));
    }
    if (detectiveDead && fakeDetective != null) {
      formattedLine = formattedLine.replace("%DETECTIVE_STATUS%", ChatManager.colorMessage("Scoreboard.Detective-Died-Bow"));
    }
    if (!detectiveDead) {
      formattedLine = formattedLine.replace("%DETECTIVE_STATUS%", ChatManager.colorMessage("Scoreboard.Detective-Status-Normal"));
    }
    //should be for murderer only
    formattedLine = formattedLine.replace("%KILLS%", String.valueOf(user.getInt("local_kills")));
    //todo
    formattedLine = formattedLine.replace("%SCORE%", "soon");
    formattedLine = ChatManager.colorRawMessage(formattedLine);
    return formattedLine;
  }

  private void spawnSomeGold() {
    //do not exceed amount of gold per spawn
    if (goldSpawned.size() >= goldSpawnPoints.size()) {
      return;
    }
    Location loc = goldSpawnPoints.get(new Random().nextInt(goldSpawnPoints.size()));
    Item item = loc.getWorld().dropItem(loc, new ItemStack(Material.GOLD_INGOT, 1));
    goldSpawned.add(item);
  }

  public UUID getMurderer() {
    return murderer;
  }

  public void setMurderer(UUID murderer) {
    this.murderer = murderer;
  }

  public UUID getDetective() {
    return detective;
  }

  public UUID getFakeDetective() {
    return fakeDetective;
  }

  public void setFakeDetective(UUID fakeDetective) {
    this.fakeDetective = fakeDetective;
  }

  public boolean isMurdererDead() {
    return murdererDead;
  }

  public void setMurdererDead(boolean murdererDead) {
    this.murdererDead = murdererDead;
  }

  public boolean isDetectiveDead() {
    return detectiveDead;
  }

  public void setDetectiveDead(boolean detectiveDead) {
    this.detectiveDead = detectiveDead;
  }

  public boolean isMurdererLocatorReceived() {
    return murdererLocatorReceived;
  }

  public void setMurdererLocatorReceived(boolean murdererLocatorReceived) {
    this.murdererLocatorReceived = murdererLocatorReceived;
  }

  public UUID getHero() {
    return hero;
  }

  public void setHero(UUID hero) {
    this.hero = hero;
  }

  public List<Item> getGoldSpawned() {
    return goldSpawned;
  }

  /**
   * Get arena identifier used to get arenas by string.
   *
   * @return arena name
   * @see ArenaRegistry#getArena(String)
   */
  public String getID() {
    return ID;
  }

  /**
   * Get minimum players needed.
   *
   * @return minimum players needed to start arena
   */
  public int getMinimumPlayers() {
    return minimumPlayers;
  }

  /**
   * Set minimum players needed.
   *
   * @param minimumPlayers players needed to start arena
   */
  public void setMinimumPlayers(int minimumPlayers) {
    this.minimumPlayers = minimumPlayers;
  }

  /**
   * Get arena map name.
   *
   * @return arena map name, [b]it's not arena ID[/b]
   * @see #getID()
   */
  public String getMapName() {
    return mapName;
  }

  /**
   * Set arena map name.
   *
   * @param mapname new map name, [b]it's not arena ID[/b]
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
    return timer;
  }

  /**
   * Modify game timer.
   *
   * @param timer timer of lobby / time to next wave
   */
  public void setTimer(int timer) {
    this.timer = timer;
  }

  /**
   * Return maximum players arena can handle.
   *
   * @return maximum players arena can handle
   */
  public int getMaximumPlayers() {
    return maximumPlayers;
  }

  /**
   * Set maximum players arena can handle.
   *
   * @param maximumPlayers how many players arena can handle
   */
  public void setMaximumPlayers(int maximumPlayers) {
    this.maximumPlayers = maximumPlayers;
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
  public HashSet<Player> getPlayers() {
    HashSet<Player> list = new HashSet<>();
    for (UUID uuid : players) {
      list.add(Bukkit.getPlayer(uuid));
    }
    return list;
  }

  public void teleportToLobby(Player player) {
    Location location = getLobbyLocation();
    player.setFoodLevel(20);
    player.setFlying(false);
    player.setAllowFlight(false);
    for (PotionEffect effect : player.getActivePotionEffects()) {
      player.removePotionEffect(effect.getType());
    }
    if (location == null) {
      System.out.print("LobbyLocation isn't intialized for arena " + getID());
    }
    player.teleport(location);
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
    player.teleport(playerSpawnPoints.get(new Random().nextInt(playerSpawnPoints.size())));
  }

  private void teleportAllToStartLocation() {
    for (Player player : getPlayers()) {
      player.teleport(playerSpawnPoints.get(new Random().nextInt(playerSpawnPoints.size())));
    }
  }

  public void teleportAllToEndLocation() {
    if (plugin.isBungeeActivated()) {
      for (Player player : getPlayers()) {
        plugin.getBungeeManager().connectToHub(player);
      }
      return;
    }
    Location location = getEndLocation();

    if (location == null) {
      location = getLobbyLocation();
      System.out.print("EndLocation for arena " + getID() + " isn't intialized!");
    }
    for (Player player : getPlayers()) {
      player.teleport(location);
    }
  }

  public void teleportToEndLocation(Player player) {
    if (plugin.isBungeeActivated()) {
      plugin.getBungeeManager().connectToHub(player);
      return;
    }
    Location location = getEndLocation();
    if (location == null) {
      location = getLobbyLocation();
      System.out.print("EndLocation for arena " + getID() + " isn't intialized!");
    }

    player.teleport(location);
  }

  public void setGoldSpawnPoints(List<Location> goldSpawnPoints) {
    this.goldSpawnPoints = goldSpawnPoints;
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

  public void start() {
    Main.debug("Game instance started, arena " + this.getID(), System.currentTimeMillis());
    this.runTaskTimer(plugin, 20L, 20L);
    this.setArenaState(ArenaState.RESTARTING);
  }

  void addPlayer(Player player) {
    players.add(player.getUniqueId());
  }

  void removePlayer(Player player) {
    if (player == null || player.getUniqueId() == null) {
      return;
    }
    players.remove(player.getUniqueId());
  }

  void addStat(Player player, String stat) {
    User user = UserManager.getUser(player.getUniqueId());
    user.addInt(stat, 1);
  }

  public List<Player> getPlayersLeft() {
    List<Player> players = new ArrayList<>();
    for (User user : UserManager.getUsers(this)) {
      if (!user.isSpectator() || !user.isFakeDead()) {
        players.add(user.toPlayer());
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
    setMurdererDead(false);
    setMurderer(null);
    setDetectiveDead(false);
    detective = null;
    setFakeDetective(null);
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
    for (ArenaCorpse corpse : corpses) {
      if (!corpse.getHologram().isDeleted()) {
        corpse.getHologram().delete();
      }
      CorpseAPI.removeCorpse(corpse.getCorpseData());
      corpse.getCorpseData().destroyCorpseFromEveryone();
    }
    corpses.clear();
  }

  public enum GameLocation {
    LOBBY, END
  }

}
