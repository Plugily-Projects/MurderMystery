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

package plugily.projects.murdermystery.arena.special.pray;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import plugily.projects.minigamesbox.api.arena.IArenaState;
import plugily.projects.minigamesbox.api.user.IUser;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.utils.misc.MiscUtils;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.arena.role.Role;
import plugily.projects.murdermystery.utils.ItemPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Plajer
 * <p>
 * Created at 16.10.2018
 */
public class PrayerRegistry {

  private static Main plugin;
  private static final List<Prayer> prayers = new ArrayList<>();
  private static final List<Player> ban = new ArrayList<>(), rush = new ArrayList<>();

  private PrayerRegistry() {
  }

  public static void init(Main plugin) {
    PrayerRegistry.plugin = plugin;
    //good prayers
    prayers.add(new Prayer(Prayer.PrayerType.DETECTIVE_REVELATION, true, new MessageBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_SPECIAL_BLOCKS_PRAY_PRAISE_GIFTS_DETECTIVE_REVELATION").asKey().build()));
    prayers.add(new Prayer(Prayer.PrayerType.GOLD_RUSH, true, new MessageBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_SPECIAL_BLOCKS_PRAY_PRAISE_GIFTS_GOLD_RUSH").asKey().build()));
    prayers.add(new Prayer(Prayer.PrayerType.SINGLE_COMPENSATION, true, new MessageBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_SPECIAL_BLOCKS_PRAY_PRAISE_GIFTS_SINGLE_COMPENSATION").asKey().build()));
    prayers.add(new Prayer(Prayer.PrayerType.BOW_TIME, true, new MessageBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_SPECIAL_BLOCKS_PRAY_PRAISE_GIFTS_BOW").asKey().build()));

    //bad prayers
    prayers.add(new Prayer(Prayer.PrayerType.SLOWNESS_CURSE, false, new MessageBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_SPECIAL_BLOCKS_PRAY_PRAISE_CURSES_SLOWNESS").asKey().build()));
    prayers.add(new Prayer(Prayer.PrayerType.BLINDNESS_CURSE, false, new MessageBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_SPECIAL_BLOCKS_PRAY_PRAISE_CURSES_BLINDNESS").asKey().build()));
    prayers.add(new Prayer(Prayer.PrayerType.GOLD_BAN, false, new MessageBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_SPECIAL_BLOCKS_PRAY_PRAISE_CURSES_GOLD").asKey().build()));
    prayers.add(new Prayer(Prayer.PrayerType.INCOMING_DEATH, false, new MessageBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_SPECIAL_BLOCKS_PRAY_PRAISE_CURSES_DEATH").asKey().build()));
  }

  public static Prayer getRandomPray() {
    return prayers.get(ThreadLocalRandom.current().nextInt(prayers.size()));
  }

  public static List<Prayer> getPrayers() {
    return prayers;
  }

  public static void applyRandomPrayer(IUser user) {
    Prayer prayer = getRandomPray();

    user.setStatistic("LOCAL_CURRENT_PRAY", prayer.getPrayerType().ordinal());

    Player player = user.getPlayer();
    Arena arena = plugin.getArenaRegistry().getArena(player);
    List<String> prayMessage = plugin.getLanguageManager().getLanguageList("In-Game.Messages.Arena.Playing.Special-Blocks.Pray.Praise.Heard");

    String feeling = plugin.getLanguageManager().getLanguageMessage("In-Game.Messages.Arena.Playing.Special-Blocks.Pray.Praise.Feeling." + (prayer.isGoodPray() ? "Blessed" : "Cursed"));
    int praySize = prayMessage.size();

    for(int a = 0; a < praySize; a++) {
      prayMessage.set(a, prayMessage.get(a).replace("%feeling%", feeling).replace("%praise%", prayer.getPrayerDescription()));
    }

    switch(prayer.getPrayerType()) {
      case BLINDNESS_CURSE:
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0, false, false));
        break;
      case BOW_TIME:
        if(!Role.isRole(Role.ANY_DETECTIVE, user, arena)) {
          ItemPosition.addItem(user, ItemPosition.BOW, new ItemStack(Material.BOW, 1));
        }
        ItemPosition.setItem(user, ItemPosition.ARROWS, new ItemStack(Material.ARROW, plugin.getConfig().getInt("Bow.Amount.Arrows.Prayer", 2)));
        break;
      case DETECTIVE_REVELATION:
        Player characterType = null;

        if(arena != null) {
          characterType = arena.getCharacter(Arena.CharacterType.DETECTIVE);

          if(characterType == null) {
            characterType = arena.getCharacter(Arena.CharacterType.FAKE_DETECTIVE);
          }
        }

        String charName = characterType == null ? "????" : characterType.getName();

        for(int a = 0; a < praySize; a++) {
          prayMessage.set(a, prayMessage.get(a).replace("%detective%", charName));
        }

        break;
      case INCOMING_DEATH:
        new BukkitRunnable() {
          int time = 60;

          @Override
          public void run() {
            if(arena == null || arena.getArenaState() != IArenaState.IN_GAME || !arena.getPlayersLeft().contains(player)) {
              cancel();
              return;
            }

            if(time-- == 0) {
              player.damage(1000);
              cancel();
            }
          }
        }.runTaskTimer(plugin, 20, 20);
        break;
      case SINGLE_COMPENSATION:
        ItemPosition.addItem(user, ItemPosition.GOLD_INGOTS, new ItemStack(Material.GOLD_INGOT, 5));
        user.adjustStatistic("LOCAL_GOLD", 5);
        break;
      case SLOWNESS_CURSE:
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 0, false, false));
        break;
      case GOLD_BAN:
        ban.add(player);
        break;
      case GOLD_RUSH:
        rush.add(player);
        break;
      default:
        break;
    }
    for(String msg : prayMessage) {
      MiscUtils.sendCenteredMessage(player, msg);
    }
  }

  public static List<Player> getBan() {
    return ban;
  }

  public static List<Player> getRush() {
    return rush;
  }
}
