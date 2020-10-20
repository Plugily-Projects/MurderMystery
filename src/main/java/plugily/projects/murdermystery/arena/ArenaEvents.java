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

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.spigotmc.event.entity.EntityDismountEvent;
import pl.plajerlair.commonsbox.minecraft.compat.XMaterial;
import pl.plajerlair.commonsbox.minecraft.item.ItemBuilder;
import plugily.projects.murdermystery.ConfigPreferences;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.api.StatsStorage;
import plugily.projects.murdermystery.arena.role.Role;
import plugily.projects.murdermystery.arena.special.pray.PrayerRegistry;
import plugily.projects.murdermystery.handlers.ChatManager;
import plugily.projects.murdermystery.handlers.items.SpecialItemManager;
import plugily.projects.murdermystery.handlers.rewards.Reward;
import plugily.projects.murdermystery.user.User;
import plugily.projects.murdermystery.utils.ItemPosition;
import plugily.projects.murdermystery.utils.Utils;

/**
 * @author Plajer
 * <p>
 * Created at 13.03.2018
 */
public class ArenaEvents implements Listener {

  private final Main plugin;
  private final ChatManager chatManager;

  public ArenaEvents(Main plugin) {
    this.plugin = plugin;
    chatManager = plugin.getChatManager();
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void onArmorStandEject(EntityDismountEvent e) {
    if (!(e.getEntity() instanceof ArmorStand) || !"MurderMysteryArmorStand".equals(e.getEntity().getCustomName())) {
      return;
    }
    if (!(e.getDismounted() instanceof Player)) {
      return;
    }
    if (e.getDismounted().isDead()) {
      e.getEntity().remove();
    }
    //we could use setCancelled here but for 1.12 support we cannot (no api)
    e.getDismounted().addPassenger(e.getEntity());
  }

  @EventHandler
  public void onFallDamage(EntityDamageEvent e) {
    if (!(e.getEntity() instanceof Player)) {
      return;
    }
    Player victim = (Player) e.getEntity();
    Arena arena = ArenaRegistry.getArena(victim);
    if (arena == null) {
      return;
    }
    if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
      if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DISABLE_FALL_DAMAGE)) {
        if (e.getDamage() >= 20.0) {
          //kill the player for suicidal death, else do not
          victim.damage(1000.0);
        }
      }
      e.setCancelled(true);
    }
    //kill the player and move to the spawn point
    if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
      victim.damage(1000.0);
      victim.teleport(arena.getPlayerSpawnPoints().get(0));
    }
  }

  @EventHandler
  public void onBowShot(EntityShootBowEvent e) {
    if (!(e.getEntity() instanceof Player)) {
      return;
    }
    if (!Role.isRole(Role.ANY_DETECTIVE, (Player) e.getEntity())) {
      return;
    }
    User user = plugin.getUserManager().getUser((Player) e.getEntity());
    if (user.getCooldown("bow_shot") == 0) {
      user.setCooldown("bow_shot", plugin.getConfig().getInt("Detective-Bow-Cooldown", 5));
      Player player = (Player) e.getEntity();
      Utils.applyActionBarCooldown(player, plugin.getConfig().getInt("Detective-Bow-Cooldown", 5));
      e.getBow().setDurability((short) 0);
    } else {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onArrowPickup(PlayerPickupArrowEvent e) {
    if (ArenaRegistry.isInArena(e.getPlayer())) {
      e.getItem().remove();
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onItemPickup(EntityPickupItemEvent e) {
    if (!(e.getEntity() instanceof Player)) {
      return;
    }
    Player player = (Player) e.getEntity();
    Arena arena = ArenaRegistry.getArena(player);
    if (arena == null) {
      return;
    }
    e.setCancelled(true);
    if (arena.getBowHologram() != null && e.getItem().equals(arena.getBowHologram().getEntityItem())) {
      if (plugin.getUserManager().getUser(player).isSpectator()) {
        return;
      }

      if (Role.isRole(Role.INNOCENT, player)) {
        player.playSound(player.getLocation(), Sound.BLOCK_LAVA_POP, 1F, 2F);
        arena.removeBowHolo();
        e.getItem().remove();

        for (Player loopPlayer : arena.getPlayersLeft()) {
          if (Role.isRole(Role.INNOCENT, loopPlayer)) {
            ItemPosition.setItem(loopPlayer, ItemPosition.BOW_LOCATOR, new ItemStack(Material.AIR, 1));
          }
        }

        arena.setCharacter(Arena.CharacterType.FAKE_DETECTIVE, player);
        ItemPosition.setItem(player, ItemPosition.BOW, new ItemStack(Material.BOW, 1));
        ItemPosition.setItem(player, ItemPosition.INFINITE_ARROWS, new ItemStack(Material.ARROW, plugin.getConfig().getInt("Detective-Fake-Arrows", 3)));
        chatManager.broadcast(arena, chatManager.colorMessage("In-Game.Messages.Bow-Messages.Pickup-Bow-Message", player));
      }

      return;
    }

    if (e.getItem().getItemStack().getType() != Material.GOLD_INGOT) {
      return;
    }

    User user = plugin.getUserManager().getUser(player);
    if (user.isSpectator() || arena.getArenaState() != ArenaState.IN_GAME) {
      return;
    }

    if (PrayerRegistry.getBan().contains(player)){
      e.setCancelled(true);
      return;
    }

    e.getItem().remove();

    player.playSound(player.getLocation(), Sound.BLOCK_LAVA_POP, 1, 1);
    arena.getGoldSpawned().remove(e.getItem());

    ItemStack stack = new ItemStack(Material.GOLD_INGOT, e.getItem().getItemStack().getAmount());
    if (PrayerRegistry.getRush().contains(player)) {
      stack.setAmount(3 * e.getItem().getItemStack().getAmount());
    }

    ItemPosition.addItem(player, ItemPosition.GOLD_INGOTS, stack);
    user.addStat(StatsStorage.StatisticType.LOCAL_GOLD, stack.getAmount());
    ArenaUtils.addScore(user, ArenaUtils.ScoreAction.GOLD_PICKUP, stack.getAmount());

    player.sendMessage(chatManager.colorMessage("In-Game.Messages.Picked-Up-Gold", player));
    plugin.getRewardsHandler().performReward(player, Reward.RewardType.GOLD_PICKUP);

    if (Role.isRole(Role.ANY_DETECTIVE, player)) {
      ItemPosition.addItem(player, ItemPosition.ARROWS, new ItemStack(Material.ARROW, e.getItem().getItemStack().getAmount() * plugin.getConfig().getInt("Detective-Gold-Pick-Up-Arrows", 3)));
      return;
    }

    if (user.getStat(StatsStorage.StatisticType.LOCAL_GOLD) >= plugin.getConfig().getInt("Gold-For-Bow", 10)) {
      user.setStat(StatsStorage.StatisticType.LOCAL_GOLD, 0);
      player.sendTitle(chatManager.colorMessage("In-Game.Messages.Bow-Messages.Bow-Shot-For-Gold", player),
        chatManager.colorMessage("In-Game.Messages.Bow-Messages.Bow-Shot-Subtitle", player), 5, 40, 5);
      ItemPosition.setItem(player, ItemPosition.BOW, new ItemStack(Material.BOW, 1));
      ItemPosition.addItem(player, ItemPosition.ARROWS, new ItemStack(Material.ARROW, plugin.getConfig().getInt("Gold-Bow-Arrows", 3)));
      player.getInventory().setItem(/* same for all roles */ ItemPosition.GOLD_INGOTS.getOtherRolesItemPosition(), new ItemStack(Material.GOLD_INGOT, 0));
    }
  }

  @EventHandler
  public void onMurdererDamage(EntityDamageByEntityEvent e) {
    if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof Player)) {
      return;
    }
    Player attacker = (Player) e.getDamager();
    Player victim = (Player) e.getEntity();
    if (!ArenaUtils.areInSameArena(attacker, victim)) {
      return;
    }
    //we are killing player via damage() method so event can be cancelled safely, will work for detective damage murderer and others
    e.setCancelled(true);

    //better check this for future even if anyone else cannot use sword
    if (!Role.isRole(Role.MURDERER, attacker)) {
      return;
    }

    //check if victim is murderer
    if (Role.isRole(Role.MURDERER, victim)) {
      return;
    }

    //todo support for skins later
    //just don't kill user if item isn't murderer sword
    if (attacker.getInventory().getItemInMainHand().getType() != plugin.getConfigPreferences().getMurdererSword().getType()) {
      return;
    }

    //check if sword has cooldown
    if (attacker.hasCooldown(plugin.getConfigPreferences().getMurdererSword().getType())) {
      return;
    }

    if (Role.isRole(Role.MURDERER, victim)) {
      plugin.getRewardsHandler().performReward(attacker, Reward.RewardType.MURDERER_KILL);
    } else if (Role.isRole(Role.ANY_DETECTIVE, victim)) {
      plugin.getRewardsHandler().performReward(attacker, Reward.RewardType.DETECTIVE_KILL);
    }

    victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_DEATH, 50, 1);
    victim.damage(100.0);

    User user = plugin.getUserManager().getUser(attacker);

    user.addStat(StatsStorage.StatisticType.KILLS, 1);
    user.addStat(StatsStorage.StatisticType.LOCAL_KILLS, 1);
    ArenaUtils.addScore(user, ArenaUtils.ScoreAction.KILL_PLAYER, 0);

    Arena arena = ArenaRegistry.getArena(attacker);
    if (Role.isRole(Role.ANY_DETECTIVE, victim) && arena.lastAliveDetective()) {
      //if already true, no effect is done :)
      arena.setDetectiveDead(true);
      if (Role.isRole(Role.FAKE_DETECTIVE, victim)) {
        arena.setCharacter(Arena.CharacterType.FAKE_DETECTIVE, null);
      }
      ArenaUtils.dropBowAndAnnounce(arena, victim);
    }
  }

  @EventHandler
  public void onArrowDamage(EntityDamageByEntityEvent e) {
    if (!(e.getDamager() instanceof Arrow && e.getEntity() instanceof Player)) {
      return;
    }
    if (!(((Arrow) e.getDamager()).getShooter() instanceof Player)) {
      return;
    }
    Player attacker = (Player) ((Arrow) e.getDamager()).getShooter();
    Player victim = (Player) e.getEntity();
    if (!ArenaUtils.areInSameArena(attacker, victim)) {
      return;
    }
    //we won't allow to suicide
    if (attacker.equals(victim)) {
      e.setCancelled(true);
      return;
    }
    //dont kill murderer on bow damage if attacker is murderer
    if (Role.isRole(Role.MURDERER, attacker) && Role.isRole(Role.MURDERER, victim)) {
      e.setCancelled(true);
      return;
    }
    Arena arena = ArenaRegistry.getArena(attacker);
    //we need to set it before the victim die, because of hero character
    if (Role.isRole(Role.MURDERER, victim)) {
      arena.setCharacter(Arena.CharacterType.HERO, attacker);
    }
    victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_DEATH, 50, 1);
    victim.damage(100.0);

    User user = plugin.getUserManager().getUser(attacker);

    user.addStat(StatsStorage.StatisticType.KILLS, 1);
    if (Role.isRole(Role.MURDERER, attacker)) {
      user.addStat(StatsStorage.StatisticType.LOCAL_KILLS, 1);
      ArenaUtils.addScore(user, ArenaUtils.ScoreAction.KILL_PLAYER, 0);
    }

    victim.sendTitle(chatManager.colorMessage("In-Game.Messages.Game-End-Messages.Titles.Died", victim), null, 5, 40, 50);

    if (Role.isRole(Role.MURDERER, victim)) {
      ArenaUtils.addScore(plugin.getUserManager().getUser(attacker), ArenaUtils.ScoreAction.KILL_MURDERER, 0);
    } else if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.ENABLE_KILL_DETECTIVE_IF_INNOCENT_KILLED) && Role.isRole(Role.INNOCENT, victim)) {
      if (Role.isRole(Role.MURDERER, attacker)) {
        victim.sendTitle(null, chatManager.colorMessage("In-Game.Messages.Game-End-Messages.Subtitles.Murderer-Killed-You", victim), 5, 40, 5);
      } else {
        victim.sendTitle(null, chatManager.colorMessage("In-Game.Messages.Game-End-Messages.Subtitles.Player-Killed-You", victim), 5, 40, 5);
      }

      //if else, murderer killed, so don't kill him :)
      if (Role.isRole(Role.ANY_DETECTIVE, attacker) || Role.isRole(Role.INNOCENT, attacker)) {
        attacker.sendTitle(chatManager.colorMessage("In-Game.Messages.Game-End-Messages.Titles.Died", attacker),
          chatManager.colorMessage("In-Game.Messages.Game-End-Messages.Subtitles.Killed-Innocent", attacker), 5, 40, 5);
        attacker.damage(100.0);
        ArenaUtils.addScore(plugin.getUserManager().getUser(attacker), ArenaUtils.ScoreAction.INNOCENT_KILL, 0);
        plugin.getRewardsHandler().performReward(attacker, Reward.RewardType.DETECTIVE_KILL);
        if (Role.isRole(Role.ANY_DETECTIVE, attacker) && arena.lastAliveDetective()) {
          arena.setDetectiveDead(true);
          if (Role.isRole(Role.FAKE_DETECTIVE, attacker)) {
            arena.setCharacter(Arena.CharacterType.FAKE_DETECTIVE, null);
          }
          ArenaUtils.dropBowAndAnnounce(arena, victim);
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onPlayerDie(PlayerDeathEvent e) {
    Arena arena = ArenaRegistry.getArena(e.getEntity());
    if (arena == null) {
      return;
    }
    e.setDeathMessage("");
    e.getDrops().clear();
    e.setDroppedExp(0);
    plugin.getCorpseHandler().spawnCorpse(e.getEntity(), arena);
    e.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 3 * 20, 0));
    Player player = e.getEntity();
    if (arena.getArenaState() == ArenaState.STARTING) {
      return;
    } else if (arena.getArenaState() == ArenaState.ENDING || arena.getArenaState() == ArenaState.RESTARTING) {
      player.getInventory().clear();
      player.setFlying(false);
      player.setAllowFlight(false);
      plugin.getUserManager().getUser(player).setStat(StatsStorage.StatisticType.LOCAL_GOLD, 0);
      return;
    }
    if (Role.isRole(Role.MURDERER, player) && arena.lastAliveMurderer()) {
      ArenaUtils.onMurdererDeath(arena);
    }
    if (Role.isRole(Role.ANY_DETECTIVE, player) && arena.lastAliveDetective()) {
      arena.setDetectiveDead(true);
      if (Role.isRole(Role.FAKE_DETECTIVE, player)) {
        arena.setCharacter(Arena.CharacterType.FAKE_DETECTIVE, null);
      }
      ArenaUtils.dropBowAndAnnounce(arena, player);
    }
    User user = plugin.getUserManager().getUser(player);
    user.addStat(StatsStorage.StatisticType.DEATHS, 1);
    user.setSpectator(true);
    player.setCollidable(false);
    player.setGameMode(GameMode.SURVIVAL);
    user.setStat(StatsStorage.StatisticType.LOCAL_GOLD, 0);
    ArenaUtils.hidePlayer(player, arena);
    player.setAllowFlight(true);
    player.setFlying(true);
    player.getInventory().clear();
    chatManager.broadcastAction(arena, player, ChatManager.ActionType.DEATH);
    //we must call it ticks later due to instant respawn bug
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      e.getEntity().spigot().respawn();
      player.getInventory().setItem(0, new ItemBuilder(XMaterial.COMPASS.parseItem()).name(chatManager.colorMessage("In-Game.Spectator.Spectator-Item-Name", player)).build());
      player.getInventory().setItem(4, new ItemBuilder(XMaterial.COMPARATOR.parseItem()).name(chatManager.colorMessage("In-Game.Spectator.Settings-Menu.Item-Name", player)).build());
      player.getInventory().setItem(8, SpecialItemManager.getSpecialItem("Leave").getItemStack());
    }, 5);
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onRespawn(PlayerRespawnEvent e) {
    Player player = e.getPlayer();
    Arena arena = ArenaRegistry.getArena(player);
    if (arena == null) {
      return;
    }
    if (arena.getArenaState() == ArenaState.STARTING || arena.getArenaState() == ArenaState.WAITING_FOR_PLAYERS) {
      e.setRespawnLocation(arena.getLobbyLocation());
      return;
    } else if (arena.getArenaState() == ArenaState.ENDING || arena.getArenaState() == ArenaState.RESTARTING) {
      e.setRespawnLocation(arena.getEndLocation());
      return;
    }
    if (arena.getPlayers().contains(player)) {
      User user = plugin.getUserManager().getUser(player);
      if (player.getLocation().getWorld().equals(arena.getPlayerSpawnPoints().get(0).getWorld())) {
        e.setRespawnLocation(player.getLocation());
      } else {
        e.setRespawnLocation(arena.getPlayerSpawnPoints().get(0));
      }
      player.setAllowFlight(true);
      player.setFlying(true);
      user.setSpectator(true);
      ArenaUtils.hidePlayer(player, arena);
      player.setCollidable(false);
      player.setGameMode(GameMode.SURVIVAL);
      player.removePotionEffect(PotionEffectType.NIGHT_VISION);
      user.setStat(StatsStorage.StatisticType.LOCAL_GOLD, 0);
      plugin.getRewardsHandler().performReward(player, Reward.RewardType.DEATH);
    }
  }

  @EventHandler
  public void onItemMove(InventoryClickEvent e) {
    if (e.getWhoClicked() instanceof Player && ArenaRegistry.isInArena((Player) e.getWhoClicked())) {
      e.setResult(Event.Result.DENY);
    }
  }

  @EventHandler
  public void playerCommandExecution(PlayerCommandPreprocessEvent e) {
    if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.ENABLE_SHORT_COMMANDS)) {
      Player player = e.getPlayer();
      if (e.getMessage().equalsIgnoreCase("/start")) {
        player.performCommand("mma forcestart");
        e.setCancelled(true);
        return;
      }
      if (e.getMessage().equalsIgnoreCase("/leave")) {
        player.performCommand("mm leave");
        e.setCancelled(true);
      }
    }
  }

  @EventHandler
  public void locatorDistanceUpdate(PlayerMoveEvent e) {
    Player player = e.getPlayer();
    Arena arena = ArenaRegistry.getArena(player);
    if (arena == null) {
      return;
    }
    //skip spectators
    if (plugin.getUserManager().getUser(player).isSpectator()) {
      return;
    }
    if (arena.getArenaState() == ArenaState.IN_GAME) {
      if (Role.isRole(Role.INNOCENT, player)) {
        if (player.getInventory().getItem(ItemPosition.BOW_LOCATOR.getOtherRolesItemPosition()) != null) {
          ItemStack bowLocator = new ItemStack(Material.COMPASS, 1);
          ItemMeta bowMeta = bowLocator.getItemMeta();
          bowMeta.setDisplayName(chatManager.colorMessage("In-Game.Bow-Locator-Item-Name", player) + " §7| §a" + (int) Math.round(player.getLocation().distance(player.getCompassTarget())));
          bowLocator.setItemMeta(bowMeta);
          ItemPosition.setItem(player, ItemPosition.BOW_LOCATOR, bowLocator);
          return;
        }
      }
      if (arena.isMurdererLocatorReceived() && Role.isRole(Role.MURDERER, player) && arena.isMurderAlive(player)) {
        ItemStack innocentLocator = new ItemStack(Material.COMPASS, 1);
        ItemMeta innocentMeta = innocentLocator.getItemMeta();
        for (Player p : arena.getPlayersLeft()) {
          if (Role.isRole(Role.INNOCENT, p) || Role.isRole(Role.ANY_DETECTIVE, p)) {
            innocentMeta.setDisplayName(chatManager.colorMessage("In-Game.Innocent-Locator-Item-Name", player) + " §7| §a" + (int) Math.round(player.getLocation().distance(p.getLocation())));
            innocentLocator.setItemMeta(innocentMeta);
            ItemPosition.setItem(player, ItemPosition.INNOCENTS_LOCATOR, innocentLocator);
          }
        }
      }
    }
  }

}
