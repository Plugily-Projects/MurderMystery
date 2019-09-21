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

package pl.plajer.murdermystery.arena.special;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.api.StatsStorage;
import pl.plajer.murdermystery.arena.Arena;
import pl.plajer.murdermystery.arena.ArenaRegistry;
import pl.plajer.murdermystery.arena.ArenaState;
import pl.plajer.murdermystery.arena.special.mysterypotion.MysteryPotion;
import pl.plajer.murdermystery.arena.special.mysterypotion.MysteryPotionRegistry;
import pl.plajer.murdermystery.arena.special.pray.PrayerRegistry;
import pl.plajer.murdermystery.handlers.ChatManager;
import pl.plajer.murdermystery.user.User;
import pl.plajer.murdermystery.utils.ItemPosition;
import pl.plajer.murdermystery.utils.Utils;
import pl.plajerlair.commonsbox.minecraft.compat.XMaterial;
import pl.plajerlair.commonsbox.minecraft.item.ItemBuilder;

/**
 * @author Plajer
 * <p>
 * Created at 16.10.2018
 */
public class SpecialBlockEvents implements Listener {

  private Main plugin;

  public SpecialBlockEvents(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void onSpecialBlockClick(PlayerInteractEvent e) {
    Arena arena = ArenaRegistry.getArena(e.getPlayer());
    if (arena == null || e.getClickedBlock() == null || e.getClickedBlock().getType() == null) {
      return;
    }
    if (arena.getArenaState() != ArenaState.IN_GAME || plugin.getUserManager().getUser(e.getPlayer()).isSpectator()) {
      return;
    }
    boolean leverBlock = false;
    if (e.getClickedBlock().getType() == XMaterial.LEVER.parseMaterial()) {
      leverBlock = true;
    }
    for (SpecialBlock specialBlock : arena.getSpecialBlocks()) {
      if (leverBlock) {
        if (Utils.getNearbyBlocks(specialBlock.getLocation(), 3).contains(e.getClickedBlock())) {
          onPrayLeverClick(e);
          return;
        }
      }
      if (e.getClickedBlock().getLocation().equals(specialBlock.getLocation())) {
        switch (specialBlock.getSpecialBlockType()) {
          case HORSE_PURCHASE:
            return;
          case MYSTERY_CAULDRON:
            onCauldronClick(e);
            return;
          case PRAISE_DEVELOPER:
            onPrayerClick(e);
            return;
          case RAPID_TELEPORTATION:
            return;
          default:
            break;
        }
      }
    }
  }

  private void onCauldronClick(PlayerInteractEvent e) {
    if (e.getClickedBlock().getType() != XMaterial.CAULDRON.parseMaterial()) {
      return;
    }
    User user = plugin.getUserManager().getUser(e.getPlayer());
    if (e.getPlayer().getInventory().getItem(/* same for all roles */ ItemPosition.POTION.getOtherRolesItemPosition()) != null) {
      e.getPlayer().sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("In-Game.Messages.Special-Blocks.Cauldron-Drink-Potion"));
      return;
    }
    if (user.getStat(StatsStorage.StatisticType.LOCAL_GOLD) < 1) {
      e.getPlayer().sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("In-Game.Messages.Special-Blocks.Not-Enough-Gold").replace("%amount%", String.valueOf(1)));
      return;
    }
    e.getClickedBlock().getWorld().spawnParticle(Particle.FIREWORKS_SPARK, e.getClickedBlock().getLocation(), 10, 0.5, 0.5, 0.5);
    Item item = e.getClickedBlock().getWorld().dropItemNaturally(e.getClickedBlock().getLocation().clone().add(0, 1, 0), new ItemStack(Material.POTION, 1));
    item.setPickupDelay(10000);
    Bukkit.getScheduler().runTaskLater(plugin, item::remove, 20);
    user.setStat(StatsStorage.StatisticType.LOCAL_GOLD, user.getStat(StatsStorage.StatisticType.LOCAL_GOLD) - 1);
    ItemPosition.addItem(e.getPlayer(), ItemPosition.GOLD_INGOTS, new ItemStack(Material.GOLD_INGOT, -1));
    ItemPosition.setItem(e.getPlayer(), ItemPosition.POTION, new ItemBuilder(XMaterial.POTION.parseItem()).name(MysteryPotionRegistry.getRandomPotion().getName()).build());
  }

  private void onPrayerClick(PlayerInteractEvent e) {
    if (e.getClickedBlock().getType() != XMaterial.ENCHANTING_TABLE.parseMaterial()) {
      return;
    }
    e.setCancelled(true);
    User user = plugin.getUserManager().getUser(e.getPlayer());
    if (user.getStat(StatsStorage.StatisticType.LOCAL_GOLD) < 1) {
      e.getPlayer().sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("In-Game.Messages.Special-Blocks.Not-Enough-Gold").replace("%amount%", String.valueOf(1)));
      return;
    }
    e.getPlayer().sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("In-Game.Messages.Special-Blocks.Prayed-Message"));
    user.setStat(StatsStorage.StatisticType.LOCAL_PRAISES, user.getStat(StatsStorage.StatisticType.LOCAL_PRAISES) + 1);
    e.getClickedBlock().getWorld().spawnParticle(Particle.FIREWORKS_SPARK, e.getClickedBlock().getLocation(), 10, 0.5, 0.5, 0.5, 1);
    user.setStat(StatsStorage.StatisticType.LOCAL_GOLD, user.getStat(StatsStorage.StatisticType.LOCAL_GOLD) - 1);
    ItemPosition.addItem(e.getPlayer(), ItemPosition.GOLD_INGOTS, new ItemStack(Material.GOLD_INGOT, -1));
  }

  private void onPrayLeverClick(PlayerInteractEvent e) {
    User user = plugin.getUserManager().getUser(e.getPlayer());
    if (user.getStat(StatsStorage.StatisticType.LOCAL_PRAISES) < 1) {
      e.getPlayer().sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("In-Game.Messages.Special-Blocks.No-Money-No-Pray"));
      return;
    }
    PrayerRegistry.applyRandomPrayer(user);
    user.setStat(StatsStorage.StatisticType.LOCAL_PRAISES, 0);
  }

  @EventHandler
  public void onMysteryPotionDrink(PlayerItemConsumeEvent e) {
    if (e.getItem().getType() != XMaterial.POTION.parseMaterial() || !Utils.isNamed(e.getItem())) {
      return;
    }
    Arena arena = ArenaRegistry.getArena(e.getPlayer());
    if (arena == null) {
      return;
    }
    for (MysteryPotion potion : MysteryPotionRegistry.getMysteryPotions()) {
      if (e.getItem().getItemMeta().getDisplayName().equals(potion.getName())) {
        e.setCancelled(true);
        e.getPlayer().sendMessage(potion.getSubtitle());
        e.getPlayer().sendTitle("", potion.getSubtitle(), 5, 40, 5);
        ItemPosition.setItem(e.getPlayer(), ItemPosition.POTION, null);
        e.getPlayer().addPotionEffect(potion.getPotionEffect());
        return;
      }
    }
  }

}
