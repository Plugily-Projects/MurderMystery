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

package plugily.projects.murdermystery.arena;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import plugily.projects.minigamesbox.classic.arena.ArenaState;
import plugily.projects.minigamesbox.classic.arena.PluginArena;
import plugily.projects.minigamesbox.classic.arena.managers.PluginMapRestorerManager;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.user.User;
import plugily.projects.minigamesbox.classic.utils.hologram.ArmorStandHologram;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.arena.corpse.Corpse;
import plugily.projects.murdermystery.arena.corpse.Stand;
import plugily.projects.murdermystery.arena.managers.MapRestorerManager;
import plugily.projects.murdermystery.arena.managers.ScoreboardManager;
import plugily.projects.murdermystery.arena.role.Role;
import plugily.projects.murdermystery.arena.states.InGameState;
import plugily.projects.murdermystery.arena.states.RestartingState;
import plugily.projects.murdermystery.arena.states.StartingState;
import plugily.projects.murdermystery.HookManager;
import plugily.projects.murdermystery.arena.special.SpecialBlock;

import java.util.*;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 17.12.2021
 */
public class Arena extends PluginArena {

  private static Main plugin;

  private final List<Player> spectators = new ArrayList<>();
  private final List<Player> deaths = new ArrayList<>();
  private final List<Player> detectives = new ArrayList<>();
  private final List<Player> murderers = new ArrayList<>();
  private final List<Item> goldSpawned = new ArrayList<>();
  private final List<Corpse> corpses = new ArrayList<>();
  private final List<Stand> stands = new ArrayList<>();
  private final List<SpecialBlock> specialBlocks = new ArrayList<>();
  private List<Location> goldSpawnPoints = new ArrayList<>();
  private List<Location> playerSpawnPoints = new ArrayList<>();
  private int spawnGoldTimer = 0;
  private int spawnGoldTime = 0;
  private boolean detectiveDead;
  private boolean murdererLocatorReceived;
  private boolean hideChances;
  private boolean goldVisuals = false;
  private final Map<CharacterType, Player> gameCharacters = new EnumMap<>(CharacterType.class);
  private MapRestorerManager mapRestorerManager;
  private ArmorStandHologram bowHologram;

  public Arena(String id) {
    super(id);
    setPluginValues();
    setScoreboardManager(new ScoreboardManager(this));
    mapRestorerManager = new MapRestorerManager(this);
    setMapRestorerManager(mapRestorerManager);
    addGameStateHandler(ArenaState.IN_GAME, new InGameState());
    addGameStateHandler(ArenaState.RESTARTING, new RestartingState());
    addGameStateHandler(ArenaState.STARTING, new StartingState());
  }

  public static void init(Main plugin) {
    Arena.plugin = plugin;
  }

  @Override
  public Main getPlugin() {
    return plugin;
  }


  @Override
  public PluginMapRestorerManager getMapRestorerManager() {
    return mapRestorerManager;
  }


  private void setPluginValues() {
  }

  public void addCorpse(Corpse corpse) {
    if(plugin.getHookManager().isFeatureEnabled(HookManager.HookFeature.CORPSES)) {
      corpses.add(corpse);
    }
  }

  public List<Corpse> getCorpses() {
    return corpses;
  }

  public List<Stand> getStands() {
    return stands;
  }

  public void addHead(Stand stand) {
    stands.add(stand);
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

  public boolean isMurdererLocatorReceived() {
    return murdererLocatorReceived;
  }

  public void setMurdererLocatorReceived(boolean murdererLocatorReceived) {
    this.murdererLocatorReceived = murdererLocatorReceived;
  }

  public Map<CharacterType, Player> getGameCharacters() {
    return gameCharacters;
  }

  public boolean isHideChances() {
    return hideChances;
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

  public void startGoldVisuals() {
    if(visualTask != null) {
      return;
    }
    visualTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
      if(!goldVisuals || !plugin.isEnabled() || goldSpawnPoints.isEmpty() || getArenaState() != ArenaState.WAITING_FOR_PLAYERS) {
        //we need to cancel it that way as the arena class is an task
        visualTask.cancel();
        return;
      }

      for(Location goldLocations : goldSpawnPoints) {
        Location goldLocation = goldLocations.clone();
        goldLocation.add(0, 0.4, 0);
        java.util.Iterator<? extends Player> iterator = Bukkit.getOnlinePlayers().iterator();
        if(iterator.hasNext()) {
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

  public void loadSpecialBlock(SpecialBlock block) {
    if(!specialBlocks.contains(block)) {
      specialBlocks.add(block);
    }

    switch(block.getSpecialBlockType()) {
      case MYSTERY_CAULDRON:
        block.setArmorStandHologram(new ArmorStandHologram(plugin.getBukkitHelper().getBlockCenter(block.getLocation()), new MessageBuilder(plugin.getLanguageManager().getLanguageMessage("In-Game.Messages.Arena.Playing.Special-Blocks.Cauldron.Hologram")).build()));
        break;
      case PRAISE_DEVELOPER:
        ArmorStandHologram prayer = new ArmorStandHologram(plugin.getBukkitHelper().getBlockCenter(block.getLocation()));
        for(String str : plugin.getLanguageManager().getLanguageMessage("In-Game.Messages.Arena.Playing.Special-Blocks.Pray.Hologram").split(";")) {
          prayer.appendLine(new MessageBuilder(str).build());
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

  public int getTotalRoleChances(Role role) {
    int totalRoleChances = 0;

    for(Player p : getPlayers()) {
      User user = getPlugin().getUserManager().getUser(p);
      totalRoleChances += getContributorValue(role, user);
    }
    return totalRoleChances;
  }

  public boolean isCharacterSet(Arena.CharacterType type) {
    return gameCharacters.containsKey(type);
  }

  public void setCharacter(Arena.CharacterType type, Player player) {
    gameCharacters.put(type, player);
  }

  public void setCharacter(Role role, Player player) {
    gameCharacters.put(role == Role.MURDERER ? CharacterType.MURDERER : CharacterType.DETECTIVE, player);
  }

  @Nullable
  public Player getCharacter(Arena.CharacterType type) {
    return gameCharacters.get(type);
  }

  public void addToDetectiveList(Player player) {
    detectives.add(player);
  }

  public boolean lastAliveDetective() {
    return aliveDetective() <= 1;
  }

  public int aliveDetective() {
    int alive = 0;
    for(Player player : getPlayersLeft()) {
      if(Role.isRole(Role.ANY_DETECTIVE, plugin.getUserManager().getUser(player), this) && isDetectiveAlive(player)) {
        alive++;
      }
    }
    return alive;
  }

  public boolean isDetectiveAlive(Player player) {
    for(Player p : getPlayersLeft()) {
      if(p == player && detectives.contains(p)) {
        return true;
      }
    }
    return false;
  }

  public List<Player> getDetectiveList() {
    return detectives;
  }

  public void addToMurdererList(Player player) {
    murderers.add(player);
  }

  public void removeFromMurdererList(Player player) {
    murderers.remove(player);
  }


  public boolean lastAliveMurderer() {
    return aliveMurderer() == 1;
  }

  public int aliveMurderer() {
    int alive = 0;
    for(Player player : getPlayersLeft()) {
      if(Role.isRole(Role.MURDERER, plugin.getUserManager().getUser(player), this) && isMurderAlive(player)) {
        alive++;
      }
    }
    return alive;
  }

  public boolean isMurderAlive(Player player) {
    for(Player p : getPlayersLeft()) {
      if(p == player && murderers.contains(p)) {
        return true;
      }
    }
    return false;
  }

  public List<Player> getMurdererList() {
    return murderers;
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

  public List<Player> getDeaths() {
    return deaths;
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

  public List<Location> getPlayerSpawnPoints() {
    return playerSpawnPoints;
  }

  public int getSpawnGoldTime() {
    return spawnGoldTime;
  }

  public int getSpawnGoldTimer() {
    return spawnGoldTimer;
  }

  public void setSpawnGoldTimer(int spawnGoldTimer) {
    this.spawnGoldTimer = spawnGoldTimer;
  }


  public void setPlayerSpawnPoints(@NotNull List<Location> playerSpawnPoints) {
    this.playerSpawnPoints = playerSpawnPoints;
  }

  public void adjustContributorValue(Role role, User user, int number) {
    user.adjustStatistic("CONTRIBUTION_" + role.name(), number);
  }

  private Map<User, Integer> murdererContributions = new HashMap<>();
  private Map<User, Integer> detectiveContributions = new HashMap<>();

  public Map<User, Integer> getMurdererContributions() {
    return murdererContributions;
  }

  public Map<User, Integer> getDetectiveContributions() {
    return detectiveContributions;
  }

  public int getContributorValue(Role role, User user) {
    if(role == Role.MURDERER && murdererContributions.containsKey(user)) {
      return murdererContributions.get(user);
    } else if(detectiveContributions.containsKey(user)) {
      return detectiveContributions.get(user);
    }
    Player player = user.getPlayer();
    int contributor = user.getStatistic("CONTRIBUTION_" + role.name());
    int increase = plugin.getPermissionsManager().getPermissionCategoryValue(role.name() + "_BOOSTER", player);
    int multiplicator = plugin.getPermissionsManager().getPermissionCategoryValue("CHANCES_BOOSTER", player);
    int calculatedContributor = (contributor + increase) * multiplicator;
    if(role == Role.MURDERER) {
      murdererContributions.put(user, calculatedContributor);
    } else {
      detectiveContributions.put(user, calculatedContributor);
    }
    return calculatedContributor;
  }

  public void resetContributorValue(Role role, User user) {
    user.setStatistic("CONTRIBUTION_" + role.name(), 1);
  }

  public enum CharacterType {
    MURDERER, DETECTIVE, FAKE_DETECTIVE, HERO
  }
}