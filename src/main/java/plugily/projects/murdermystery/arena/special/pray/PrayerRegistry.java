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

package plugily.projects.murdermystery.arena.special.pray;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import plugily.projects.commonsbox.minecraft.misc.MiscUtils;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.api.StatsStorage;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.arena.ArenaRegistry;
import plugily.projects.murdermystery.arena.ArenaState;
import plugily.projects.murdermystery.arena.role.Role;
import plugily.projects.murdermystery.handlers.ChatManager;
import plugily.projects.murdermystery.handlers.language.LanguageManager;
import plugily.projects.murdermystery.user.User;
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
  private static ChatManager chatManager;
  private static final List<Prayer> prayers = new ArrayList<>();
  private static final List<Player> ban = new ArrayList<>(), rush = new ArrayList<>();

  private PrayerRegistry() {
  }

  public static void init(Main plugin) {
    PrayerRegistry.plugin = plugin;
    chatManager = plugin.getChatManager();
    //good prayers
    prayers.add(new Prayer(Prayer.PrayerType.DETECTIVE_REVELATION, true, chatManager.colorMessage("In-Game.Messages.Special-Blocks.Praises.Gifts.Detective-Revelation")));
    prayers.add(new Prayer(Prayer.PrayerType.GOLD_RUSH, true, chatManager.colorMessage("In-Game.Messages.Special-Blocks.Praises.Gifts.Gold-Rush")));
    prayers.add(new Prayer(Prayer.PrayerType.SINGLE_COMPENSATION, true, chatManager.colorMessage("In-Game.Messages.Special-Blocks.Praises.Gifts.Single-Compensation")));
    prayers.add(new Prayer(Prayer.PrayerType.BOW_TIME, true, chatManager.colorMessage("In-Game.Messages.Special-Blocks.Praises.Gifts.Bow-Time")));

    //bad prayers
    prayers.add(new Prayer(Prayer.PrayerType.SLOWNESS_CURSE, false, chatManager.colorMessage("In-Game.Messages.Special-Blocks.Praises.Curses.Slowness-Curse")));
    prayers.add(new Prayer(Prayer.PrayerType.BLINDNESS_CURSE, false, chatManager.colorMessage("In-Game.Messages.Special-Blocks.Praises.Curses.Blindness-Curse")));
    prayers.add(new Prayer(Prayer.PrayerType.GOLD_BAN, false, chatManager.colorMessage("In-Game.Messages.Special-Blocks.Praises.Curses.Gold-Ban")));
    prayers.add(new Prayer(Prayer.PrayerType.INCOMING_DEATH, false, chatManager.colorMessage("In-Game.Messages.Special-Blocks.Praises.Curses.Incoming-Death")));
  }

  public static Prayer getRandomPray() {
    return prayers.get(ThreadLocalRandom.current().nextInt(prayers.size()));
  }

  public static List<Prayer> getPrayers() {
    return prayers;
  }

  public static void applyRandomPrayer(User user) {
    Prayer prayer = getRandomPray();

    user.setStat(StatsStorage.StatisticType.LOCAL_CURRENT_PRAY, prayer.getPrayerType().ordinal());

    Player player = user.getPlayer();
    Arena arena = ArenaRegistry.getArena(player);
    List<String> prayMessage = LanguageManager.getLanguageList("In-Game.Messages.Special-Blocks.Praises.Message");

    String feeling = chatManager.colorMessage("In-Game.Messages.Special-Blocks.Praises.Feelings." + (prayer.isGoodPray() ? "Blessed" : "Cursed"), player);
    int praySize = prayMessage.size();

    for (int a = 0; a < praySize; a++) {
      prayMessage.set(a, prayMessage.get(a).replace("%feeling%", feeling).replace("%praise%", prayer.getPrayerDescription()));
    }

    switch(prayer.getPrayerType()) {
      case BLINDNESS_CURSE:
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0, false, false));
        break;
      case BOW_TIME:
        if(!Role.isRole(Role.ANY_DETECTIVE, player, arena)) {
          ItemPosition.addItem(player, ItemPosition.BOW, new ItemStack(Material.BOW, 1));
        }
        ItemPosition.setItem(player, ItemPosition.ARROWS, new ItemStack(Material.ARROW, plugin.getConfig().getInt("Detective-Prayer-Arrows", 2)));
        break;
      case DETECTIVE_REVELATION:
        Player characterType = null;

        if (arena != null) {
          characterType = arena.getCharacter(Arena.CharacterType.DETECTIVE);

          if (characterType == null) {
            characterType = arena.getCharacter(Arena.CharacterType.FAKE_DETECTIVE);
          }
        }

        String charName = characterType == null ? "????" : characterType.getName();

        for (int a = 0; a < praySize; a++) {
          prayMessage.set(a, prayMessage.get(a).replace("%detective%", charName));
        }

        break;
      case INCOMING_DEATH:
        new BukkitRunnable() {
          int time = 60;

          @Override
          public void run() {
            if(arena == null || arena.getArenaState() != ArenaState.IN_GAME || !arena.getPlayersLeft().contains(player)) {
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
        ItemPosition.addItem(player, ItemPosition.GOLD_INGOTS, new ItemStack(Material.GOLD_INGOT, 5));
        user.setStat(StatsStorage.StatisticType.LOCAL_GOLD, user.getStat(StatsStorage.StatisticType.LOCAL_GOLD) + 5);
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
