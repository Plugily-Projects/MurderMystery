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
import plugily.projects.minigamesbox.classic.utils.version.ServerVersion;
import plugily.projects.murdermystery.Main;


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
  public void onSpecialBlockClick(PlayerInteractEvent e) {
    if (e.getClickedBlock() == null)
      return;

    if(ServerVersion.Version.isCurrentEqualOrHigher(ServerVersion.Version.v1_11_R1) && e.getHand() == org.bukkit.inventory.EquipmentSlot.OFF_HAND) {
      return;
    }

    Arena arena = ArenaRegistry.getArena(e.getPlayer());
    if(arena == null) {
      return;
    }

    if(arena.getArenaState() != ArenaState.IN_GAME || plugin.getUserManager().getUser(e.getPlayer()).isSpectator()) {
      return;
    }

    for(SpecialBlock specialBlock : arena.getSpecialBlocks()) {
      if(e.getClickedBlock().getType() == XMaterial.LEVER.parseMaterial() && Utils.getNearbyBlocks(specialBlock.getLocation(), 3).contains(e.getClickedBlock())) {
        onPrayLeverClick(e);
        return;
      }
      if(specialBlock.getLocation().getBlock().equals(e.getClickedBlock())) {
        switch(specialBlock.getSpecialBlockType()) {
          case MYSTERY_CAULDRON:
            onCauldronClick(e);
            return;
          case PRAISE_DEVELOPER:
            onPrayerClick(e);
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

  private void onCauldronClick(PlayerInteractEvent e) {
    if(e.getClickedBlock().getType() != Material.CAULDRON) {
      return;
    }

    if(e.getPlayer().getInventory().getItem(/* same for all roles */ ItemPosition.POTION.getOtherRolesItemPosition()) != null) {
      e.getPlayer().sendMessage(chatManager.getPrefix() + chatManager.colorMessage("In-Game.Messages.Special-Blocks.Cauldron-Drink-Potion", e.getPlayer()));
      return;
    }

    User user = plugin.getUserManager().getUser(e.getPlayer());

    int localGold = user.getStat(StatsStorage.StatisticType.LOCAL_GOLD);
    if(localGold < 1) {
      e.getPlayer().sendMessage(chatManager.getPrefix() + chatManager.colorMessage("In-Game.Messages.Special-Blocks.Not-Enough-Gold", e.getPlayer()).replace("%amount%", "1"));
      return;
    }

    org.bukkit.Location blockLoc = e.getClickedBlock().getLocation();

    VersionUtils.sendParticles("FIREWORKS_SPARK", e.getPlayer(), blockLoc, 10);
    Item item = blockLoc.getWorld().dropItemNaturally(blockLoc.clone().add(0, 1, 0), new ItemStack(Material.POTION, 1));
    item.setPickupDelay(10000);
    Bukkit.getScheduler().runTaskLater(plugin, item::remove, 20);
    user.setStat(StatsStorage.StatisticType.LOCAL_GOLD, localGold - 1);
    ItemPosition.addItem(e.getPlayer(), ItemPosition.GOLD_INGOTS, new ItemStack(Material.GOLD_INGOT, -1));
    ItemPosition.setItem(e.getPlayer(), ItemPosition.POTION, new ItemBuilder(XMaterial.POTION.parseItem()).name(MysteryPotionRegistry.getRandomPotion().getName()).build());
  }

  private void onPrayerClick(PlayerInteractEvent e) {
    if(e.getClickedBlock().getType() != XMaterial.ENCHANTING_TABLE.parseMaterial()) {
      return;
    }

    e.setCancelled(true);

    User user = plugin.getUserManager().getUser(e.getPlayer());
    int localGold = user.getStat(StatsStorage.StatisticType.LOCAL_GOLD);

    if(localGold < 1) {
      e.getPlayer().sendMessage(chatManager.getPrefix() + chatManager.colorMessage("In-Game.Messages.Special-Blocks.Not-Enough-Gold", e.getPlayer()).replace("%amount%", "1"));
      return;
    }

    e.getPlayer().sendMessage(chatManager.getPrefix() + chatManager.colorMessage("In-Game.Messages.Special-Blocks.Prayed-Message", e.getPlayer()));
    user.setStat(StatsStorage.StatisticType.LOCAL_PRAISES, user.getStat(StatsStorage.StatisticType.LOCAL_PRAISES) + 1);
    VersionUtils.sendParticles("FIREWORKS_SPARK", e.getPlayer(), e.getClickedBlock().getLocation(), 10);
    user.setStat(StatsStorage.StatisticType.LOCAL_GOLD, localGold - 1);
    ItemPosition.addItem(e.getPlayer(), ItemPosition.GOLD_INGOTS, new ItemStack(Material.GOLD_INGOT, -1));
  }

  private void onPrayLeverClick(PlayerInteractEvent e) {
    User user = plugin.getUserManager().getUser(e.getPlayer());
    if(user.getStat(StatsStorage.StatisticType.LOCAL_PRAISES) < 1) {
      e.getPlayer().sendMessage(chatManager.getPrefix() + chatManager.colorMessage("In-Game.Messages.Special-Blocks.No-Money-No-Pray", e.getPlayer()));
      return;
    }
    PrayerRegistry.applyRandomPrayer(user);
    user.setStat(StatsStorage.StatisticType.LOCAL_PRAISES, 0);
  }

  @EventHandler
  public void onMysteryPotionDrink(PlayerItemConsumeEvent e) {
    ItemStack item = e.getItem();
    if(item.getType() != XMaterial.POTION.parseMaterial() || !ItemUtils.isItemStackNamed(item)) {
      return;
    }

    if(ArenaRegistry.getArena(e.getPlayer()) == null) {
      return;
    }

    String itemDisplayName = ComplementAccessor.getComplement().getDisplayName(item.getItemMeta());

    for(MysteryPotion potion : MysteryPotionRegistry.getMysteryPotions()) {
      if(itemDisplayName.equals(potion.getName())) {
        e.setCancelled(true);
        e.getPlayer().sendMessage(potion.getSubtitle());
        VersionUtils.sendTitles(e.getPlayer(), "", potion.getSubtitle(), 5, 40, 5);
        ItemPosition.setItem(e.getPlayer(), ItemPosition.POTION, null);
        e.getPlayer().addPotionEffect(potion.getPotionEffect());
        return;
      }
    }
  }

}
