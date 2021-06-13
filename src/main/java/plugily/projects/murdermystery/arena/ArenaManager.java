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

import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import pl.plajerlair.commonsbox.minecraft.compat.VersionUtils;
import pl.plajerlair.commonsbox.minecraft.compat.xseries.XMaterial;
import pl.plajerlair.commonsbox.minecraft.item.ItemBuilder;
import pl.plajerlair.commonsbox.minecraft.misc.MiscUtils;
import pl.plajerlair.commonsbox.minecraft.serialization.InventorySerializer;
import plugily.projects.murdermystery.ConfigPreferences;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.api.StatsStorage;
import plugily.projects.murdermystery.api.events.game.MMGameJoinAttemptEvent;
import plugily.projects.murdermystery.api.events.game.MMGameLeaveAttemptEvent;
import plugily.projects.murdermystery.api.events.game.MMGameStopEvent;
import plugily.projects.murdermystery.arena.role.Role;
import plugily.projects.murdermystery.arena.special.SpecialBlock;
import plugily.projects.murdermystery.handlers.ChatManager;
import plugily.projects.murdermystery.handlers.PermissionsManager;
import plugily.projects.murdermystery.handlers.items.SpecialItem;
import plugily.projects.murdermystery.handlers.language.LanguageManager;
import plugily.projects.murdermystery.handlers.party.GameParty;
import plugily.projects.murdermystery.handlers.rewards.Reward;
import plugily.projects.murdermystery.user.User;
import plugily.projects.murdermystery.utils.Debugger;
import plugily.projects.murdermystery.utils.ItemPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Plajer
 * <p>
 * Created at 13.05.2018
 */
public class ArenaManager {

  private static final Main plugin = JavaPlugin.getPlugin(Main.class);
  private static final ChatManager chatManager = plugin.getChatManager();

  private ArenaManager() {
  }

  /**
   * Attempts player to join arena.
   * Calls MMGameJoinAttemptEvent.
   * Can be cancelled only via above-mentioned event
   *
   * @param player player to join
   * @see MMGameJoinAttemptEvent
   */
  public static void joinAttempt(Player player, Arena arena) {
    Debugger.debug("[{0}] Initial join attempt for {1}", arena.getId(), player.getName());
    long start = System.currentTimeMillis();
    MMGameJoinAttemptEvent gameJoinAttemptEvent = new MMGameJoinAttemptEvent(player, arena);
    Bukkit.getPluginManager().callEvent(gameJoinAttemptEvent);

    if(!arena.isReady()) {
      player.sendMessage(chatManager.getPrefix() + chatManager.colorMessage("In-Game.Arena-Not-Configured"));
      return;
    }
    if(gameJoinAttemptEvent.isCancelled()) {
      player.sendMessage(chatManager.getPrefix() + chatManager.colorMessage("In-Game.Join-Cancelled-Via-API"));
      return;
    }
    if(ArenaRegistry.isInArena(player)) {
      player.sendMessage(chatManager.getPrefix() + chatManager.colorMessage("In-Game.Already-Playing"));
      return;
    }

    //check if player is in party and send party members to the game
    if(plugin.getPartyHandler().isPlayerInParty(player)) {
      Debugger.debug("{0} is in a party", player.getName());
      GameParty party = plugin.getPartyHandler().getParty(player);
      if(party.getLeader().equals(player)) {
        Debugger.debug("{0} is partyleader", player.getName());
        if(arena.getMaximumPlayers() - arena.getPlayers().size() >= party.getPlayers().size()) {
          for(Player partyPlayer : party.getPlayers()) {
            if(player.equals(partyPlayer)) {
              continue;
            }
            Arena partyArena = ArenaRegistry.getArena(partyPlayer);
            if(partyArena != null) {
              if(partyArena.getArenaState() == ArenaState.IN_GAME) {
                continue;
              }
              Debugger.debug("[Partyleader: {0}] Found party member {1} on other arena", player.getName(), partyPlayer.getName());
              leaveAttempt(partyPlayer, partyArena);
            }
            partyPlayer.sendMessage(chatManager.getPrefix() + chatManager.formatMessage(arena, chatManager.colorMessage("In-Game.Join-As-Party-Member"), partyPlayer));
            Debugger.debug("Let party member {0} join the arena of {1}", partyPlayer.getName(), player.getName());
            joinAttempt(partyPlayer, arena);
          }
        } else {
          Debugger.debug("[Partyleader: {0}] The arena got not enough space for the party", player.getName());
          player.sendMessage(chatManager.getPrefix() + chatManager.formatMessage(arena, chatManager.colorMessage("In-Game.Messages.Lobby-Messages.Not-Enough-Space-For-Party"), player));
          return;
        }
      }
    }

    if(!(plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)
        || player.hasPermission(PermissionsManager.getJoinPerm().replace("<arena>", "*"))
        || player.hasPermission(PermissionsManager.getJoinPerm().replace("<arena>", arena.getId())))) {
      player.sendMessage(chatManager.getPrefix() + chatManager.colorMessage("In-Game.Join-No-Permission").replace("%permission%",
          PermissionsManager.getJoinPerm().replace("<arena>", arena.getId())));
      return;
    }
    if(arena.getArenaState() == ArenaState.RESTARTING) {
      return;
    }
    if(arena.getArenaState() == ArenaState.STARTING && arena.getPlayers().size() >= arena.getMaximumPlayers()) {
      if(!player.hasPermission(PermissionsManager.getJoinFullGames())) {
        player.sendMessage(chatManager.getPrefix() + chatManager.colorMessage("In-Game.Full-Game-No-Permission"));
        return;
      }
      boolean foundSlot = false;
      for(Player loopPlayer : arena.getPlayers()) {
        if(loopPlayer.hasPermission(PermissionsManager.getJoinFullGames())) {
          continue;
        }
        leaveAttempt(loopPlayer, arena);
        loopPlayer.sendMessage(chatManager.getPrefix() + chatManager.colorMessage("In-Game.Messages.Lobby-Messages.You-Were-Kicked-For-Premium-Slot"));
        chatManager.broadcast(arena, chatManager.formatMessage(arena, chatManager.colorMessage("In-Game.Messages.Lobby-Messages.Kicked-For-Premium-Slot"), loopPlayer));
        foundSlot = true;
        break;
      }
      if(!foundSlot) {
        player.sendMessage(chatManager.getPrefix() + chatManager.colorMessage("In-Game.No-Slots-For-Premium"));
        return;
      }
    }

    Debugger.debug("[{0}] Checked join attempt for {1} initialized", arena.getId(), player.getName());

    User user = plugin.getUserManager().getUser(player);
    user.lastBoard = player.getScoreboard();

    //reset scoreboard
    player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

    arena.getScoreboardManager().createScoreboard(user);

    if(plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
      InventorySerializer.saveInventoryToFile(plugin, player);
    }

    int murderIncrease = player.getEffectivePermissions().stream().filter(permAttach -> permAttach.getPermission().startsWith("murdermystery.role.murderer."))
        .mapToInt(pai -> Integer.parseInt(pai.getPermission().substring(28 /* remove the permission node to obtain the number*/))).max().orElse(0);
    int detectiveIncrease = player.getEffectivePermissions().stream().filter(permAttach -> permAttach.getPermission().startsWith("murdermystery.role.detective."))
        .mapToInt(pai -> Integer.parseInt(pai.getPermission().substring(29 /* remove the permission node to obtain the number*/))).max().orElse(0);
    user.addStat(StatsStorage.StatisticType.CONTRIBUTION_MURDERER, murderIncrease);
    user.addStat(StatsStorage.StatisticType.CONTRIBUTION_DETECTIVE, detectiveIncrease);

    arena.addPlayer(player);
    player.setLevel(0);
    player.setExp(1);
    player.setHealth(VersionUtils.getMaxHealth(player));
    player.setFoodLevel(20);
    if((arena.getArenaState() == ArenaState.IN_GAME || arena.getArenaState() == ArenaState.ENDING)) {
      arena.teleportToStartLocation(player);
      player.sendMessage(chatManager.colorMessage("In-Game.You-Are-Spectator"));
      player.getInventory().clear();

      for(SpecialItem item : plugin.getSpecialItemManager().getSpecialItems()) {
        if(item.getDisplayStage() != SpecialItem.DisplayStage.SPECTATOR) {
          continue;
        }
        player.getInventory().setItem(item.getSlot(), item.getItemStack());
      }

      player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
      player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0));
      ArenaUtils.hidePlayer(player, arena);

      user.setSpectator(true);

      arena.addSpectatorPlayer(player);
      VersionUtils.setCollidable(player, false);
      player.setGameMode(GameMode.SURVIVAL);
      player.setAllowFlight(true);
      player.setFlying(true);

      for(Player spectator : arena.getPlayers()) {
        if(!plugin.getUserManager().getUser(spectator).isSpectator()) {
          VersionUtils.hidePlayer(plugin, /*not spectator*/ spectator, /*joined spectator*/ player);
        }
        VersionUtils.showPlayer(plugin, player, spectator);
      }
      ArenaUtils.hidePlayersOutsideTheGame(player, arena);
      Debugger.debug("[{0}] Join attempt as spectator finished for {1} took {2}ms", arena.getId(), player.getName(), System.currentTimeMillis() - start);
      return;
    }
    arena.teleportToLobby(player);
    player.getInventory().setArmorContents(new ItemStack[]{new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
    player.setFlying(false);
    player.setAllowFlight(false);
    player.getInventory().clear();
    arena.doBarAction(Arena.BarAction.ADD, player);
    if(!user.isSpectator()) {
      chatManager.broadcastAction(arena, player, ChatManager.ActionType.JOIN);
    }
    if(arena.getArenaState() == ArenaState.STARTING || arena.getArenaState() == ArenaState.WAITING_FOR_PLAYERS) {
      for(SpecialItem item : plugin.getSpecialItemManager().getSpecialItems()) {
        if(item.getDisplayStage() != SpecialItem.DisplayStage.LOBBY) {
          continue;
        }
        player.getInventory().setItem(item.getSlot(), item.getItemStack());
      }
    }
    player.updateInventory();
    for(Player arenaPlayer : arena.getPlayers()) {
      ArenaUtils.showPlayer(arenaPlayer, arena);
    }
    arena.showPlayers();
    ArenaUtils.updateNameTagsVisibility(player);
    plugin.getSignManager().updateSigns();
    Debugger.debug("[{0}] Join attempt as player for {1} took {2}ms", arena.getId(), player.getName(), System.currentTimeMillis() - start);
  }

  /**
   * Attempts player to leave arena.
   * Calls MMGameLeaveAttemptEvent event.
   *
   * @param player player to join
   * @see MMGameLeaveAttemptEvent
   */
  public static void leaveAttempt(Player player, Arena arena) {
    Debugger.debug("[{0}] Initial leave attempt for {1}", arena.getId(), player.getName());
    long start = System.currentTimeMillis();

    Bukkit.getPluginManager().callEvent(new MMGameLeaveAttemptEvent(player, arena));
    User user = plugin.getUserManager().getUser(player);

    int localScore = user.getStat(StatsStorage.StatisticType.LOCAL_SCORE);
    if(localScore > user.getStat(StatsStorage.StatisticType.HIGHEST_SCORE)) {
      user.setStat(StatsStorage.StatisticType.HIGHEST_SCORE, localScore);
    }

    //todo change later
    int murderDecrease = player.getEffectivePermissions().stream().filter(permAttach -> permAttach.getPermission().startsWith("murdermystery.role.murderer."))
        .mapToInt(pai -> Integer.parseInt(pai.getPermission().substring(28 /* remove the permission node to obtain the number*/))).max().orElse(0);
    int detectiveDecrease = player.getEffectivePermissions().stream().filter(permAttach -> permAttach.getPermission().startsWith("murdermystery.role.detective."))
        .mapToInt(pai -> Integer.parseInt(pai.getPermission().substring(29 /* remove the permission node to obtain the number*/))).max().orElse(0);
    user.addStat(StatsStorage.StatisticType.CONTRIBUTION_MURDERER, -murderDecrease);
    if(user.getStat(StatsStorage.StatisticType.CONTRIBUTION_MURDERER) <= 0) {
      user.setStat(StatsStorage.StatisticType.CONTRIBUTION_MURDERER, 1);
    }
    user.addStat(StatsStorage.StatisticType.CONTRIBUTION_DETECTIVE, -detectiveDecrease);
    if(user.getStat(StatsStorage.StatisticType.CONTRIBUTION_DETECTIVE) <= 0) {
      user.setStat(StatsStorage.StatisticType.CONTRIBUTION_DETECTIVE, 1);
    }

    if(arena.getArenaState() == ArenaState.IN_GAME) {
      if(Role.isRole(Role.FAKE_DETECTIVE, player) || Role.isRole(Role.INNOCENT, player)) {
        user.setStat(StatsStorage.StatisticType.CONTRIBUTION_MURDERER, ThreadLocalRandom.current().nextInt(4) + 1);
        user.setStat(StatsStorage.StatisticType.CONTRIBUTION_DETECTIVE, ThreadLocalRandom.current().nextInt(4) + 1);
      }
    }

    user.removeScoreboard(arena);

    boolean playerHasMurdererRole = Role.isRole(Role.MURDERER, player);
    if(playerHasMurdererRole) {
      arena.removeFromMurdererList(player);
    }
    if(arena.getArenaState() == ArenaState.IN_GAME && !user.isSpectator()) {
      List<Player> playersLeft = arena.getPlayersLeft();

      //-1 cause we didn't remove player yet
      if(playersLeft.size() - 1 > 1) {
        if(playerHasMurdererRole) {
          if(arena.getMurdererList().isEmpty()) {
            List<Player> players = new ArrayList<>();
            for(Player gamePlayer : playersLeft) {
              if(gamePlayer == player || Role.isRole(Role.ANY_DETECTIVE, gamePlayer) || Role.isRole(Role.MURDERER, gamePlayer)) {
                continue;
              }
              players.add(gamePlayer);
            }
            Player newMurderer = players.get(players.size() == 1 ? 0 : ThreadLocalRandom.current().nextInt(players.size()));
            Debugger.debug("A murderer left the game. New murderer: {0}", newMurderer.getName());
            arena.setCharacter(Arena.CharacterType.MURDERER, newMurderer);
            arena.addToMurdererList(newMurderer);
            String title = chatManager.colorMessage("In-Game.Messages.Previous-Role-Left-Title", player).replace("%role%",
                chatManager.colorMessage("Scoreboard.Roles.Murderer", player));
            String subtitle = chatManager.colorMessage("In-Game.Messages.Previous-Role-Left-Subtitle", player).replace("%role%",
                chatManager.colorMessage("Scoreboard.Roles.Murderer", player));
            for(Player gamePlayer : arena.getPlayers()) {
              VersionUtils.sendTitles(gamePlayer, title, subtitle, 5, 40, 5);
            }
            VersionUtils.sendTitles(newMurderer, chatManager.colorMessage("In-Game.Messages.Role-Set.Murderer-Title", player),
                chatManager.colorMessage("In-Game.Messages.Role-Set.Murderer-Subtitle", player), 5, 40, 5);
            ItemPosition.setItem(newMurderer, ItemPosition.MURDERER_SWORD, plugin.getConfigPreferences().getMurdererSword());
            user.setStat(StatsStorage.StatisticType.CONTRIBUTION_MURDERER, 1);
          } else {
            Debugger.debug("No new murderer added as there are some");
          }
        } else if(Role.isRole(Role.ANY_DETECTIVE, player) && arena.lastAliveDetective()) {
          arena.setDetectiveDead(true);
          if(Role.isRole(Role.FAKE_DETECTIVE, player)) {
            arena.setCharacter(Arena.CharacterType.FAKE_DETECTIVE, null);
          } else {
            user.setStat(StatsStorage.StatisticType.CONTRIBUTION_DETECTIVE, 1);
          }
          ArenaUtils.dropBowAndAnnounce(arena, player);
        }
        plugin.getCorpseHandler().spawnCorpse(player, arena);
      } else {
        stopGame(false, arena);
      }
    }
    //the default fly speed
    player.setFlySpeed(0.1f);
    player.getInventory().clear();
    player.getInventory().setArmorContents(null);
    arena.removePlayer(player);

    if(!user.isSpectator() && !arena.isSpectatorPlayer(player)) {
      chatManager.broadcastAction(arena, player, ChatManager.ActionType.LEAVE);
    }

    VersionUtils.setGlowing(player, false);
    user.setSpectator(false);

    arena.removeDeathPlayer(player);
    arena.removeSpectatorPlayer(player);

    VersionUtils.setCollidable(player, true);
    user.removeScoreboard(arena);
    arena.doBarAction(Arena.BarAction.REMOVE, player);
    player.setHealth(VersionUtils.getMaxHealth(player));
    player.setFoodLevel(20);
    player.setLevel(0);
    player.setExp(0);
    player.setFlying(false);
    player.setAllowFlight(false);
    player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
    player.setWalkSpeed(0.2f);
    player.setFireTicks(0);
    if(arena.getArenaState() != ArenaState.WAITING_FOR_PLAYERS && arena.getArenaState() != ArenaState.STARTING && arena.getPlayers().isEmpty()) {
      arena.setArenaState(ArenaState.ENDING);
      arena.setTimer(0);
    }

    player.setGameMode(GameMode.SURVIVAL);
    for(Player players : plugin.getServer().getOnlinePlayers()) {
      if(!ArenaRegistry.isInArena(players)) {
        VersionUtils.showPlayer(plugin, players, player);
      }
      VersionUtils.showPlayer(plugin, player, players);
    }
    arena.teleportToEndLocation(player);
    if(!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)
        && plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
      InventorySerializer.loadInventory(plugin, player);
    }
    plugin.getUserManager().saveAllStatistic(user);
    plugin.getSignManager().updateSigns();
    Debugger.debug("[{0}] Game leave finished for {1} took{2}ms ", arena.getId(), player.getName(), System.currentTimeMillis() - start);
  }

  /**
   * Stops current arena. Calls MMGameStopEvent event
   *
   * @param quickStop should arena be stopped immediately? (use only in important cases)
   * @see MMGameStopEvent
   */
  public static void stopGame(boolean quickStop, Arena arena) {
    Debugger.debug("[{0}] Stop game event initialized with quickStop {1}", arena.getId(), quickStop);
    long start = System.currentTimeMillis();

    Bukkit.getPluginManager().callEvent(new MMGameStopEvent(arena));
    arena.setArenaState(ArenaState.ENDING);
    if(quickStop) {
      arena.setTimer(2);
      chatManager.broadcast(arena, chatManager.colorRawMessage("&cThe game has been force stopped by command"));
    } else {
      arena.setTimer(10);
    }

    for(SpecialBlock specialBlock : arena.getSpecialBlocks()) {
      if(specialBlock.getArmorStandHologram() != null) {
        specialBlock.getArmorStandHologram().delete();
      }
    }

    arena.removeBowHolo();

    List<String> summaryMessages = LanguageManager.getLanguageList("In-Game.Messages.Game-End-Messages.Summary-Message");
    arena.getScoreboardManager().stopAllScoreboards();

    boolean murderWon = arena.getPlayersLeft().size() == arena.aliveMurderer();

    for(final Player player : arena.getPlayers()) {
      User user = plugin.getUserManager().getUser(player);

      if(!quickStop && Role.isAnyRole(player)) {
        boolean hasDeathRole = Role.isRole(Role.DEATH, player);

        if(!hasDeathRole && !Role.isRole(Role.SPECTATOR, player)) {
          if(Role.isRole(Role.FAKE_DETECTIVE, player) || Role.isRole(Role.INNOCENT, player)) {
            user.setStat(StatsStorage.StatisticType.CONTRIBUTION_MURDERER, ThreadLocalRandom.current().nextInt(4) + 1);
            user.setStat(StatsStorage.StatisticType.CONTRIBUTION_DETECTIVE, ThreadLocalRandom.current().nextInt(4) + 1);
          }

          boolean hasMurdererRole = Role.isRole(Role.MURDERER, player);

          if((murderWon && hasMurdererRole) || !hasMurdererRole) {
            user.addStat(StatsStorage.StatisticType.WINS, 1);
            plugin.getRewardsHandler().performReward(player, Reward.RewardType.WIN);
          } else {
            user.addStat(StatsStorage.StatisticType.LOSES, 1);
            plugin.getRewardsHandler().performReward(player, Reward.RewardType.LOSE);
          }
        } else if(hasDeathRole) {
          user.addStat(StatsStorage.StatisticType.LOSES, 1);
          plugin.getRewardsHandler().performReward(player, Reward.RewardType.LOSE);
        }
      }
      //the default walk & fly speed
      player.setFlySpeed(0.1f);
      player.setWalkSpeed(0.2f);

      player.getInventory().clear();
      for(SpecialItem item : plugin.getSpecialItemManager().getSpecialItems()) {
        if(item.getDisplayStage() != SpecialItem.DisplayStage.SPECTATOR) {
          continue;
        }
        player.getInventory().setItem(item.getSlot(), item.getItemStack());
      }
      if(!quickStop) {
        for(String msg : summaryMessages) {
          MiscUtils.sendCenteredMessage(player, formatSummaryPlaceholders(msg, arena, player));
        }
      }
      user.removeScoreboard(arena);
      if(!quickStop && !user.isSpectator() && !user.isPermanentSpectator() && plugin.getConfig().getBoolean("Firework-When-Game-Ends", true)) {
        new BukkitRunnable() {
          int i = 0;

          @Override
          public void run() {
            if(i >= 4 || !arena.getPlayers().contains(player)) {
              cancel();
            }
            MiscUtils.spawnRandomFirework(player.getLocation());
            i++;
          }
        }.runTaskTimer(plugin, 30, 30);
      }
    }
    Debugger.debug("[{0}] Stop game event finished took{1}ms ", arena.getId(), System.currentTimeMillis() - start);
  }

  private static String formatSummaryPlaceholders(String msg, Arena arena, Player player) {
    String formatted = msg;

    StringBuilder murders = new StringBuilder(), detectives = new StringBuilder();
    int murdererKills = 0;
    for(Player p : arena.getMurdererList()) {
      User user = plugin.getUserManager().getUser(p);
      int localKills = user.getStat(StatsStorage.StatisticType.LOCAL_KILLS);

      if(arena.getMurdererList().size() > 1) {
        murders.append(p.getName()).append(" (").append(localKills).append("), ");
      } else {
        murders.append(p.getName());
      }

      murdererKills += localKills;
    }
    if(arena.getMurdererList().size() > 1) {
      murders.deleteCharAt(murders.length() - 2);
    }
    for(Player p : arena.getDetectiveList()) {
      detectives.append(p.getName()).append(", ");
    }

    detectives.deleteCharAt(detectives.length() - 2);

    int aliveMurderer = arena.aliveMurderer();

    if(arena.getPlayersLeft().size() == aliveMurderer) {
      formatted = StringUtils.replace(formatted, "%winner%", chatManager.colorMessage("In-Game.Messages.Game-End-Messages.Winners.Murderer"));
    } else {
      formatted = StringUtils.replace(formatted, "%winner%", chatManager.colorMessage("In-Game.Messages.Game-End-Messages.Winners.Players"));
    }

    formatted = StringUtils.replace(formatted, "%detective%", (arena.isDetectiveDead() ? ChatColor.STRIKETHROUGH : "") + detectives.toString());

    formatted = StringUtils.replace(formatted, "%murderer%", (aliveMurderer == 1 ? "" : ChatColor.STRIKETHROUGH) + murders.toString());

    formatted = StringUtils.replace(formatted, "%murderer_kills%", Integer.toString(murdererKills));

    Player hero = arena.getCharacter(Arena.CharacterType.HERO);
    formatted = StringUtils.replace(formatted, "%hero%", hero != null ? hero.getName() : chatManager.colorMessage("In-Game.Messages.Game-End-Messages.Winners.Nobody"));

    if(plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
      formatted = PlaceholderAPI.setPlaceholders(player, formatted);
    }

    return formatted;
  }

}
