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

package plugily.projects.murdermystery.handlers.setup.components;

import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import plugily.projects.murdermystery.ConfigPreferences;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.handlers.ChatManager;
import plugily.projects.murdermystery.handlers.setup.SetupInventory;
import plugily.projects.murdermystery.handlers.sign.ArenaSign;
import plugily.projects.murdermystery.utils.conversation.SimpleConversationBuilder;
import pl.plajerlair.commonsbox.minecraft.compat.XMaterial;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;
import pl.plajerlair.commonsbox.minecraft.item.ItemBuilder;
import pl.plajerlair.commonsbox.minecraft.serialization.LocationSerializer;

/**
 * @author Plajer
 * <p>
 * Created at 25.05.2019
 */
public class MiscComponents implements SetupComponent {

  private SetupInventory setupInventory;

  @Override
  public void prepare(SetupInventory setupInventory) {
    this.setupInventory = setupInventory;
  }

  @Override
  public void injectComponents(StaticPane pane) {
    Player player = setupInventory.getPlayer();
    FileConfiguration config = setupInventory.getConfig();
    Arena arena = setupInventory.getArena();
    Main plugin = setupInventory.getPlugin();
    ItemStack bungeeItem;
    if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
      // Material.getMaterial("SIGN") doesn't work anymore in 1.16.1 so use Material.OAK_SIGN instead.
      Material sign = Material.getMaterial("SIGN");
      if (sign == null) sign = Material.OAK_SIGN;
      bungeeItem = new ItemBuilder(sign)
        .name(ChatManager.colorRawMessage("&e&lAdd Game Sign"))
        .lore(ChatColor.GRAY + "Target a sign and click this.")
        .lore(ChatColor.DARK_GRAY + "(this will set target sign as game sign)")
        .build();
    } else {
      bungeeItem = new ItemBuilder(Material.BARRIER)
        .name(ChatManager.colorRawMessage("&c&lAdd Game Sign"))
        .lore(ChatColor.GRAY + "Option disabled in bungee cord mode.")
        .lore(ChatColor.DARK_GRAY + "Bungee mode is meant to be one arena per server")
        .lore(ChatColor.DARK_GRAY + "If you wish to have multi arena, disable bungee in config!")
        .build();
    }
    pane.addItem(new GuiItem(bungeeItem, e -> {
      if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
        return;
      }
      e.getWhoClicked().closeInventory();
      Location location = player.getTargetBlock(null, 10).getLocation();
      if (!(location.getBlock().getState() instanceof Sign)) {
        player.sendMessage(ChatManager.colorMessage("Commands.Look-Sign"));
        return;
      }
      if (location.distance(e.getWhoClicked().getWorld().getSpawnLocation()) <= Bukkit.getServer().getSpawnRadius()
        && e.getClick() != ClickType.SHIFT_LEFT) {
        e.getWhoClicked().sendMessage(ChatManager.colorRawMessage("&c&l✖ &cWarning | Server spawn protection is set to &6" + Bukkit.getServer().getSpawnRadius()
          + " &cand sign you want to place is in radius of this protection! &c&lNon opped players won't be able to interact with this sign and can't join the game so."));
        e.getWhoClicked().sendMessage(ChatManager.colorRawMessage("&cYou can ignore this warning and add sign with Shift + Left Click, but for now &c&loperation is cancelled"));
        return;
      }
      plugin.getSignManager().getArenaSigns().add(new ArenaSign((Sign) location.getBlock().getState(), arena));
      player.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("Signs.Sign-Created"));
      String signLoc = location.getBlock().getWorld().getName() + "," + location.getBlock().getX() + "," + location.getBlock().getY() + "," + location.getBlock().getZ() + ",0.0,0.0";
      List<String> locs = config.getStringList("instances." + arena.getId() + ".signs");
      locs.add(signLoc);
      config.set("instances." + arena.getId() + ".signs", locs);
      ConfigUtils.saveConfig(plugin, config, "arenas");
    }), 5, 0);

    pane.addItem(new GuiItem(new ItemBuilder(Material.NAME_TAG)
      .name(ChatManager.colorRawMessage("&e&lSet Map Name"))
      .lore(ChatColor.GRAY + "Click to set arena map name")
      .lore("", ChatManager.colorRawMessage("&a&lCurrently: &e" + config.getString("instances." + arena.getId() + ".mapname")))
      .build(), e -> {
      e.getWhoClicked().closeInventory();
      new SimpleConversationBuilder().withPrompt(new StringPrompt() {
        @Override
        public String getPromptText(ConversationContext context) {
          return ChatManager.colorRawMessage(ChatManager.PLUGIN_PREFIX + "&ePlease type in chat arena name! You can use color codes.");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
          String name = ChatManager.colorRawMessage(input);
          player.sendRawMessage(ChatManager.colorRawMessage("&e✔ Completed | &aName of arena " + arena.getId() + " set to " + name));
          arena.setMapName(name);
          config.set("instances." + arena.getId() + ".mapname", arena.getMapName());
          ConfigUtils.saveConfig(plugin, config, "arenas");

          new SetupInventory(arena, player).openInventory();
          return Prompt.END_OF_CONVERSATION;
        }
      }).buildFor(player);
    }), 6, 0);

    pane.addItem(new GuiItem(new ItemBuilder(Material.GOLD_INGOT)
      .name(ChatManager.colorRawMessage("&e&lAdd Gold Spawn"))
      .lore(ChatColor.GRAY + "Add new gold spawn")
      .lore(ChatColor.GRAY + "on the place you're standing at.")
      .lore("", setupInventory.getSetupUtilities().isOptionDoneList("instances." + arena.getId() + ".goldspawnpoints", 4))
      .lore("", ChatManager.colorRawMessage("&8Shift + Right Click to remove all spawns"))
      .build(), e -> {
      e.getWhoClicked().closeInventory();
      if (e.getClick() == ClickType.SHIFT_RIGHT) {
        config.set("instances." + arena.getId() + ".goldspawnpoints", new ArrayList<>());
        arena.setGoldSpawnPoints(new ArrayList<>());
        player.sendMessage(ChatManager.colorRawMessage("&eDone | &aGold spawn points deleted, you can add them again now!"));
        arena.setReady(false);
        ConfigUtils.saveConfig(plugin, config, "arenas");
        return;
      }
      List<String> goldSpawns = config.getStringList("instances." + arena.getId() + ".goldspawnpoints");
      goldSpawns.add(LocationSerializer.locationToString(player.getLocation()));
      config.set("instances." + arena.getId() + ".goldspawnpoints", goldSpawns);
      String goldProgress = goldSpawns.size() >= 4 ? "&e✔ Completed | " : "&c✘ Not completed | ";
      player.sendMessage(ChatManager.colorRawMessage(goldProgress + "&aGold spawn added! &8(&7" + goldSpawns.size() + "/4&8)"));
      if (goldSpawns.size() == 4) {
        player.sendMessage(ChatManager.colorRawMessage("&eInfo | &aYou can add more than 4 gold spawns! Four is just a minimum!"));
      }
      List<Location> spawns = new ArrayList<>(arena.getGoldSpawnPoints());
      spawns.add(player.getLocation());
      arena.setGoldSpawnPoints(spawns);
      ConfigUtils.saveConfig(plugin, config, "arenas");
    }), 7, 0);

    pane.addItem(new GuiItem(new ItemBuilder(XMaterial.GOLD_NUGGET.parseItem())
      .amount(config.getInt("instances." + arena.getId() + "." + "spawngoldtime", 3))
      .name(ChatManager.colorRawMessage("&e&lSet gold spawn time in seconds"))
      .lore(ChatColor.GRAY + "LEFT click to decrease")
      .lore(ChatColor.GRAY + "RIGHT click to increase")
      .lore(ChatColor.DARK_GRAY + "How much gold should be spawned? ")
      .lore(ChatColor.DARK_GRAY + "That means 1 gold spawned every ... seconds")
      .lore(ChatColor.DARK_GRAY + "Default: 5")
      .lore(ChatColor.DARK_GRAY + "Every 5 seconds it will spawn 1 gold")
      .lore("", setupInventory.getSetupUtilities().isOptionDone("instances." + arena.getId() + ".spawngoldtime"))
      .build(), e -> {
      if (e.getClick().isRightClick()) {
        e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() + 1);
      }
      if (e.getClick().isLeftClick() && e.getCurrentItem().getAmount() > 1) {
        e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() - 1);
      }
      if (e.getInventory().getItem(e.getSlot()).getAmount() < 1) {
        e.getWhoClicked().sendMessage(ChatManager.colorRawMessage("&c&l✖ &cWarning | Please do not set amount lower than 1! Game is not designed without gold!"));
        e.getInventory().getItem(e.getSlot()).setAmount(1);
      }
      config.set("instances." + arena.getId() + ".spawngoldtime", e.getCurrentItem().getAmount());
      arena.setSpawnGoldTime(e.getCurrentItem().getAmount());
      ConfigUtils.saveConfig(plugin, config, "arenas");
      new SetupInventory(arena, setupInventory.getPlayer()).openInventory();
    }), 7, 1);

    pane.addItem(new GuiItem(new ItemBuilder(XMaterial.FILLED_MAP.parseItem())
      .name(ChatManager.colorRawMessage("&e&lView Setup Video"))
      .lore(ChatColor.GRAY + "Having problems with setup or wanna")
      .lore(ChatColor.GRAY + "know some useful tips? Click to get video link!")
      .build(), e -> {
      e.getWhoClicked().closeInventory();
      player.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorRawMessage("&6Check out this video: " + SetupInventory.VIDEO_LINK));
    }), 8, 1);
  }

}
