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

package plugily.projects.murdermystery.arena.special;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import plugily.projects.minigamesbox.classic.arena.ArenaState;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.user.User;
import plugily.projects.minigamesbox.classic.utils.helper.ItemBuilder;
import plugily.projects.minigamesbox.classic.utils.helper.ItemUtils;
import plugily.projects.minigamesbox.classic.utils.misc.complement.ComplementAccessor;
import plugily.projects.minigamesbox.classic.utils.version.ServerVersion;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XMaterial;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.arena.special.mysterypotion.MysteryPotion;
import plugily.projects.murdermystery.arena.special.mysterypotion.MysteryPotionRegistry;
import plugily.projects.murdermystery.arena.special.pray.PrayerRegistry;
import plugily.projects.murdermystery.utils.ItemPosition;


/**
 * @author Plajer
 * <p>
 * Created at 16.10.2018
 */
public class SpecialBlockEvents implements Listener {

  private final Main plugin;

  public SpecialBlockEvents(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void onSpecialBlockClick(PlayerInteractEvent event) {
    if(event.getClickedBlock() == null)
      return;

    if(ServerVersion.Version.isCurrentEqualOrHigher(ServerVersion.Version.v1_11_R1) && event.getHand() == org.bukkit.inventory.EquipmentSlot.OFF_HAND) {
      return;
    }

    Arena arena = plugin.getArenaRegistry().getArena(event.getPlayer());
    if(arena == null) {
      return;
    }

    if(arena.getArenaState() != ArenaState.IN_GAME || plugin.getUserManager().getUser(event.getPlayer()).isSpectator()) {
      return;
    }

    for(SpecialBlock specialBlock : arena.getSpecialBlocks()) {
      if(event.getClickedBlock().getType() == XMaterial.LEVER.parseMaterial() && plugin.getBukkitHelper().getNearbyBlocks(specialBlock.getLocation(), 3).contains(event.getClickedBlock())) {
        onPrayLeverClick(event);
        return;
      }
      if(specialBlock.getLocation().getBlock().equals(event.getClickedBlock())) {
        switch(specialBlock.getSpecialBlockType()) {
          case MYSTERY_CAULDRON:
            onCauldronClick(event);
            return;
          case PRAISE_DEVELOPER:
            onPrayerClick(event);
            return;
          case HORSE_PURCHASE:
          case RAPID_TELEPORTATION:
            //not yet implemented
          default:
            break;
        }
      }
    }
  }

  private void onCauldronClick(PlayerInteractEvent event) {
    if(event.getClickedBlock().getType() != Material.CAULDRON) {
      return;
    }

    if(event.getPlayer().getInventory().getItem(/* same for all roles */ ItemPosition.POTION.getOtherRolesItemPosition()) != null) {
      new MessageBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_SPECIAL_BLOCKS_CAULDRON_POTION").asKey().player(event.getPlayer()).sendPlayer();
      return;
    }

    User user = plugin.getUserManager().getUser(event.getPlayer());

    int localGold = user.getStatistic("LOCAL_GOLD");
    if(localGold < 1) {
      new MessageBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_SPECIAL_BLOCKS_NOT_ENOUGH_GOLD").asKey().player(event.getPlayer()).integer(1).sendPlayer();
      return;
    }

    org.bukkit.Location blockLoc = event.getClickedBlock().getLocation();

    VersionUtils.sendParticles("FIREWORKS_SPARK", event.getPlayer(), blockLoc, 10);
    Item item = blockLoc.getWorld().dropItemNaturally(blockLoc.clone().add(0, 1, 0), new ItemStack(Material.POTION, 1));
    item.setPickupDelay(10000);
    Bukkit.getScheduler().runTaskLater(plugin, item::remove, 20);
    user.adjustStatistic("LOCAL_GOLD", 1);
    ItemPosition.addItem(user, ItemPosition.GOLD_INGOTS, new ItemStack(Material.GOLD_INGOT, -1));
    ItemPosition.setItem(user, ItemPosition.POTION, new ItemBuilder(XMaterial.POTION.parseItem()).name(MysteryPotionRegistry.getRandomPotion().getName()).build());
  }

  private void onPrayerClick(PlayerInteractEvent event) {
    if(event.getClickedBlock().getType() != XMaterial.ENCHANTING_TABLE.parseMaterial()) {
      return;
    }

    event.setCancelled(true);

    User user = plugin.getUserManager().getUser(event.getPlayer());
    int localGold = user.getStatistic("LOCAL_GOLD");

    if(localGold < 1) {
      new MessageBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_SPECIAL_BLOCKS_NOT_ENOUGH_GOLD").asKey().player(event.getPlayer()).integer(1).sendPlayer();
      return;
    }
    new MessageBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_SPECIAL_BLOCKS_PRAY_CHAT").asKey().player(event.getPlayer()).sendPlayer();
    user.adjustStatistic("LOCAL_PRAISES", 1);
    VersionUtils.sendParticles("FIREWORKS_SPARK", event.getPlayer(), event.getClickedBlock().getLocation(), 10);
    user.adjustStatistic("LOCAL_GOLD", 1);
    ItemPosition.addItem(user, ItemPosition.GOLD_INGOTS, new ItemStack(Material.GOLD_INGOT, -1));
  }

  private void onPrayLeverClick(PlayerInteractEvent event) {
    User user = plugin.getUserManager().getUser(event.getPlayer());
    if(user.getStatistic("LOCAL_PRAISES") < 1) {
      new MessageBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_SPECIAL_BLOCKS_PRAY_PAY").asKey().player(event.getPlayer()).sendPlayer();
      return;
    }
    PrayerRegistry.applyRandomPrayer(user);
    user.setStatistic("LOCAL_PRAISES", 0);
  }

  @EventHandler
  public void onMysteryPotionDrink(PlayerItemConsumeEvent event) {
    ItemStack item = event.getItem();
    if(item.getType() != XMaterial.POTION.parseMaterial() || !ItemUtils.isItemStackNamed(item)) {
      return;
    }

    if(plugin.getArenaRegistry().getArena(event.getPlayer()) == null) {
      return;
    }

    String itemDisplayName = ComplementAccessor.getComplement().getDisplayName(item.getItemMeta());
    User user = plugin.getUserManager().getUser(event.getPlayer());
    for(MysteryPotion potion : MysteryPotionRegistry.getMysteryPotions()) {
      if(itemDisplayName.equals(potion.getName())) {
        event.setCancelled(true);
        event.getPlayer().sendMessage(potion.getSubtitle());
        VersionUtils.sendTitles(event.getPlayer(), "", potion.getSubtitle(), 5, 40, 5);
        ItemPosition.setItem(user, ItemPosition.POTION, null);
        event.getPlayer().addPotionEffect(potion.getPotionEffect());
        return;
      }
    }
  }

}
