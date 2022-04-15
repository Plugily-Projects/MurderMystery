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

package plugily.projects.murdermystery.handlers.setup;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import plugily.projects.minigamesbox.classic.PluginMain;
import plugily.projects.minigamesbox.classic.arena.PluginArena;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.handlers.setup.PluginSetupInventory;
import plugily.projects.minigamesbox.classic.handlers.setup.SetupUtilities;
import plugily.projects.minigamesbox.classic.handlers.setup.items.CountItem;
import plugily.projects.minigamesbox.classic.handlers.setup.items.LocationItem;
import plugily.projects.minigamesbox.classic.utils.configuration.ConfigUtils;
import plugily.projects.minigamesbox.classic.utils.conversation.SimpleConversationBuilder;
import plugily.projects.minigamesbox.classic.utils.dimensional.Cuboid;
import plugily.projects.minigamesbox.classic.utils.helper.ItemBuilder;
import plugily.projects.minigamesbox.classic.utils.serialization.LocationSerializer;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XMaterial;
import plugily.projects.minigamesbox.inventory.common.item.ClickableItem;
import plugily.projects.minigamesbox.inventory.common.item.SimpleClickableItem;
import plugily.projects.minigamesbox.inventory.normal.NormalFastInv;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.arena.special.SpecialBlock;

import java.util.ArrayList;
import java.util.List;

public class SetupInventory extends PluginSetupInventory {


  private final Main plugin;
  private Arena arena;
  private final Player player;


  public SetupInventory(Main plugin, @Nullable PluginArena arena, Player player) {
    super(plugin, arena, player);
    this.plugin = plugin;
    this.player = player;
    setArena(player, arena);
    open();
  }

  public SetupInventory(Main plugin, @Nullable PluginArena arena, Player player, SetupUtilities.InventoryStage inventoryStage) {
    super(plugin, arena, player, inventoryStage);
    this.plugin = plugin;
    this.player = player;
    setArena(player, arena);
    open();
  }

  @Override
  public void setArena(Player player, PluginArena arena) {
    if(arena == null && plugin.getSetupUtilities().getArena(player) != null) {
      this.arena = plugin.getArenaRegistry().getArena(plugin.getSetupUtilities().getArena(player).getId());
      setInventoryStage(SetupUtilities.InventoryStage.PAGED_GUI);
    } else if(arena != null) {
      this.arena = plugin.getArenaRegistry().getArena(arena.getId());
    } else {
      this.arena = null;
    }
    setArena(this.arena);
  }

  @Override
  public void addExternalItems(NormalFastInv inv) {
    switch (getInventoryStage()) {
      case SETUP_GUI:
        break;
      case ARENA_LIST:
        break;
      case PAGED_GUI:
        break;
      case PAGED_VALUES:
        break;
      case PAGED_BOOLEAN:
        inv.setItem(10, new SimpleClickableItem(new ItemBuilder(XMaterial.REDSTONE.parseItem())
            .name(new MessageBuilder(arena.isGoldVisuals() ? "&c&lDisable Gold Visuals" : "&a&lEnable Gold Visuals").build())
            .lore(ChatColor.GRAY + "Enables gold visuals to spawn")
            .lore(ChatColor.GRAY + "some particle effects above gold locations")
            .build(), event -> {
          arena.toggleGoldVisuals();
          plugin.getSetupUtilities().getConfig().set("instances." + arena.getId() + ".goldvisuals", arena.isGoldVisuals());
          ConfigUtils.saveConfig(plugin, plugin.getSetupUtilities().getConfig(), "arenas");
          plugin.openSetupInventory(arena, player, SetupUtilities.InventoryStage.PAGED_BOOLEAN);
        }));
        break;
      case PAGED_COUNTABLE:
        inv.setItem(11, new CountItem(new ItemBuilder(Material.REDSTONE_TORCH)
            .amount(plugin.getSetupUtilities().getMinimumValueHigherThanZero("spawngoldtime", this))
            .name(new MessageBuilder("&e&lSet gold spawn time in seconds").build())
            .lore(ChatColor.GRAY + "LEFT click to decrease")
            .lore(ChatColor.GRAY + "RIGHT click to increase")
            .lore(ChatColor.DARK_GRAY + "How much gold should be spawned? ")
            .lore(ChatColor.DARK_GRAY + "That means 1 gold spawned every ... seconds")
            .lore(ChatColor.DARK_GRAY + "Default: 5")
            .lore(ChatColor.DARK_GRAY + "Every 5 seconds it will spawn 1 gold")
            .lore("", plugin.getSetupUtilities().isOptionDone("spawngoldtime", this))
            .build(), e -> {
          ItemStack currentItem = e.getCurrentItem();
          if(currentItem == null) {
            return;
          }
          plugin.getSetupUtilities().getConfig().set("instances." + arena.getId() + ".spawngoldtime", e.getCurrentItem().getAmount());
          arena.setSpawnGoldTime(e.getCurrentItem().getAmount());
          ConfigUtils.saveConfig(plugin, plugin.getSetupUtilities().getConfig(), "arenas");
          inv.refresh();
        }));


        inv.setItem(12, new CountItem(new ItemBuilder(Material.IRON_SWORD)
            .amount(plugin.getSetupUtilities().getMinimumValueHigherThanZero("playerpermurderer", this))
            .name(new MessageBuilder("&e&lSet Player Per Murderer Amount").build())
            .lore(ChatColor.GRAY + "LEFT click to decrease")
            .lore(ChatColor.GRAY + "RIGHT click to increase")
            .lore(ChatColor.DARK_GRAY + "How many murderer should be ingame? This means ")
            .lore(ChatColor.DARK_GRAY + "one murderer for that amount of players. Default: ")
            .lore(ChatColor.DARK_GRAY + "5 players are 1 murderer, that means if we have ")
            .lore(ChatColor.DARK_GRAY + "14 Players it will calculate 2 murderer! ")
            .lore(ChatColor.DARK_GRAY + "Set it to 1 if you want only one murderer ")
            .lore("", plugin.getSetupUtilities().isOptionDone("playerpermurderer", this))
            .build(), e -> {
          ItemStack currentItem = e.getCurrentItem();
          if(currentItem == null) {
            return;
          }
          plugin.getSetupUtilities().getConfig().set("instances." + arena.getId() + ".playerpermurderer", e.getCurrentItem().getAmount());
          arena.setArenaOption("MURDERER_DIVIDER", e.getCurrentItem().getAmount());
          ConfigUtils.saveConfig(plugin, plugin.getSetupUtilities().getConfig(), "arenas");
          inv.refresh();
        }));
        inv.setItem(13, new CountItem(new ItemBuilder(Material.BOW)
            .amount(plugin.getSetupUtilities().getMinimumValueHigherThanZero("playerperdetective", this))
            .name(new MessageBuilder("&e&lSet Player Per Detective Amount").build())
            .lore(ChatColor.GRAY + "LEFT click to decrease")
            .lore(ChatColor.GRAY + "RIGHT click to increase")
            .lore(ChatColor.DARK_GRAY + "How many detectives should be ingame? This means ")
            .lore(ChatColor.DARK_GRAY + "one detective for that amount of players. Default: ")
            .lore(ChatColor.DARK_GRAY + "7 players are 1 detective, that means if we have ")
            .lore(ChatColor.DARK_GRAY + "18 Players it will calculate 2 detectives! ")
            .lore(ChatColor.DARK_GRAY + "Set it to 1 if you want only one detectives ")
            .lore("", plugin.getSetupUtilities().isOptionDone("playerperdetective", this))
            .build(), e -> {
          ItemStack currentItem = e.getCurrentItem();
          if(currentItem == null) {
            return;
          }
          plugin.getSetupUtilities().getConfig().set("instances." + arena.getId() + ".playerperdetective", e.getCurrentItem().getAmount());
          arena.setArenaOption("DETECTIVE_DIVIDER", e.getCurrentItem().getAmount());
          ConfigUtils.saveConfig(plugin, plugin.getSetupUtilities().getConfig(), "arenas");
          inv.refresh();
        }));

        break;
      case PAGED_LOCATIONS:
        inv.setItem(22, new LocationItem(new ItemBuilder(XMaterial.GOLD_INGOT.parseMaterial())
            .name(new MessageBuilder("&e&lAdd Gold Spawn").build())
            .lore(ChatColor.GRAY + "Add new gold spawn")
            .lore(ChatColor.GRAY + "on the place you're standing at.")
            .lore("", plugin.getSetupUtilities().isOptionDoneSection("goldspawnpoints", 4,this))
            .build(), e -> {
          if(e.getClick() == ClickType.SHIFT_RIGHT) {
            plugin.getSetupUtilities().getConfig().set("instances." + arena.getId() + ".goldspawnpoints", new ArrayList<>());
            arena.setGoldSpawnPoints(new ArrayList<>());
            new MessageBuilder("&eDone | &aGold spawn points deleted, you can add them again now!").player(player).sendPlayer();
            arena.setReady(false);
            ConfigUtils.saveConfig(plugin, plugin.getSetupUtilities().getConfig(), "arenas");
            return;
          }

          List<String> goldSpawns = plugin.getSetupUtilities().getConfig().getStringList("instances." + arena.getId() + ".goldspawnpoints");
          goldSpawns.add(LocationSerializer.locationToString(player.getLocation()));
          plugin.getSetupUtilities().getConfig().set("instances." + arena.getId() + ".goldspawnpoints", goldSpawns);
          String goldProgress = goldSpawns.size() >= 4 ? "&e✔ Completed | " : "&c✘ Not completed | ";
          new MessageBuilder(goldProgress + "&aGold spawn added! &8(&7" + goldSpawns.size() + "/4&8)").player(player).sendPlayer();
          if(goldSpawns.size() == 4) {
            new MessageBuilder("&eInfo | &aYou can add more than 4 gold spawns! Four is just a minimum!").player(player).sendPlayer();
          }
          List<Location> spawns = new ArrayList<>(arena.getGoldSpawnPoints());
          spawns.add(player.getLocation());
          arena.setGoldSpawnPoints(spawns);

          new MessageBuilder("&e✔ Completed | &aGold spawn location for arena " + arena.getId() + " set at your location!").player(player).sendPlayer();
          ConfigUtils.saveConfig(plugin, plugin.getSetupUtilities().getConfig(), "arenas");
        }, event -> {
          new MessageBuilder("&cNot supported!").prefix().player(player).sendPlayer();
        }, false, false, false));

        inv.setItem(23, new LocationItem(new ItemBuilder(XMaterial.EMERALD_BLOCK.parseMaterial())
            .name(new MessageBuilder("&e&lAdd Starting Location").build())
            .lore(ChatColor.GRAY + "Click to add the starting location")
            .lore(ChatColor.GRAY + "on the place where you are standing.")
            .lore(ChatColor.DARK_GRAY + "(locations where players will be")
            .lore(ChatColor.DARK_GRAY + "teleported when game starts)")
            .lore("", plugin.getSetupUtilities().isOptionDoneSection("playerspawnpoints", 4,this))
            .build(), e -> {
          if(e.getClick() == ClickType.SHIFT_RIGHT) {
            plugin.getSetupUtilities().getConfig().set("instances." + arena.getId() + ".playerspawnpoints", new ArrayList<>());
            arena.setPlayerSpawnPoints(new ArrayList<>());
            new MessageBuilder("&eDone | &aPlayerSpawnPoints spawn points deleted, you can add them again now!").player(player).sendPlayer();
            arena.setReady(false);
            ConfigUtils.saveConfig(plugin, plugin.getSetupUtilities().getConfig(), "arenas");
            return;
          }

          List<String> startingPoints = plugin.getSetupUtilities().getConfig().getStringList("instances." + arena.getId() + ".playerspawnpoints");
          startingPoints.add(LocationSerializer.locationToString(player.getLocation()));
          plugin.getSetupUtilities().getConfig().set("instances." + arena.getId() + ".playerspawnpoints", startingPoints);
          String startingProgress = startingPoints.size() >= 4 ? "&e✔ Completed | " : "&c✘ Not completed | ";
          new MessageBuilder(startingProgress + "&aPlayer spawn added! &8(&7" + startingPoints.size() + "/4&8)").player(player).sendPlayer();
          if(startingPoints.size() == 4) {
            new MessageBuilder("&eInfo | &aYou can add more than 4 PlayerSpawnPoints locations! Four is just a minimum!").player(player).sendPlayer();
          }
          List<Location> spawns = new ArrayList<>(arena.getGoldSpawnPoints());
          spawns.add(player.getLocation());
          arena.setPlayerSpawnPoints(spawns);

          new MessageBuilder("&e✔ Completed | &aPlayerSpawnPoints location for arena " + arena.getId() + " set at your location!").player(player).sendPlayer();
          ConfigUtils.saveConfig(plugin, plugin.getSetupUtilities().getConfig(), "arenas");
        }, event -> {
          new MessageBuilder("&cNot supported!").prefix().player(player).sendPlayer();
        }, false, false, false));

        inv.setItem(30, new LocationItem(new ItemBuilder(Material.ENDER_CHEST)
            .name(new MessageBuilder("&e&lAdd Mystery Cauldron").build())
            .lore(ChatColor.GRAY + "Target a cauldron and add it to the game")
            .lore(ChatColor.GRAY + "it will cost 1 gold per potion!")
            .lore(ChatColor.GRAY + "Configure cauldron potions in special_blocks.yml file!")
            .build(), e -> {
          e.getWhoClicked().closeInventory();
          Block targetBlock = e.getWhoClicked().getTargetBlock(null, 7);
          if(targetBlock.getType() != Material.CAULDRON) {
            e.getWhoClicked().sendMessage(ChatColor.RED + "Please target cauldron to continue!");
            return;
          }
          arena.loadSpecialBlock(new SpecialBlock(targetBlock.getLocation(),
              SpecialBlock.SpecialBlockType.MYSTERY_CAULDRON));
          List<String> cauldrons = new ArrayList<>(plugin.getSetupUtilities().getConfig().getStringList("instances." + arena.getId() + ".mystery-cauldrons"));
          cauldrons.add(LocationSerializer.locationToString(targetBlock.getLocation()));
          plugin.getSetupUtilities().getConfig().set("instances." + arena.getId() + ".mystery-cauldrons", cauldrons);
          new MessageBuilder("&e✔ Completed | &aAdded Cauldron special block for arena " + arena.getId() + " set at your location!").player(player).sendPlayer();
          ConfigUtils.saveConfig(plugin, plugin.getSetupUtilities().getConfig(), "arenas");
        }, event -> {
          new MessageBuilder("&cNot supported!").prefix().player(player).sendPlayer();
        }, false, false, false));
        inv.setItem(31, new LocationItem(new ItemBuilder(Material.ENDER_CHEST)
            .name(new MessageBuilder("&e&lAdd Confessional").build())
            .lore(ChatColor.GRAY + "Target enchanting table and")
            .lore(ChatColor.GRAY + "add praise to the developer")
            .lore(ChatColor.GRAY + "confessional, gift for")
            .lore(ChatColor.GRAY + "the developer costs 1 gold!")
            .lore(ChatColor.GOLD + "Add some levers in radius")
            .lore(ChatColor.GOLD + "of 3 blocks near the enchant table")
            .lore(ChatColor.GOLD + "to allow users to pray there!")
            .lore(ChatColor.RED + "You can either get gifts")
            .lore(ChatColor.RED + "or curses from prayer!")
            .build(), e -> {
          e.getWhoClicked().closeInventory();
          Block targetBlock = e.getWhoClicked().getTargetBlock(null, 7);
          if(targetBlock.getType() != XMaterial.ENCHANTING_TABLE.parseMaterial()) {
            e.getWhoClicked().sendMessage(ChatColor.RED + "Please target enchanting table to continue!");
            return;
          }
          arena.loadSpecialBlock(new SpecialBlock(targetBlock.getLocation(),
              SpecialBlock.SpecialBlockType.PRAISE_DEVELOPER));
          List<String> confessionals = new ArrayList<>(plugin.getSetupUtilities().getConfig().getStringList("instances." + arena.getId() + ".confessionals"));
          confessionals.add(LocationSerializer.locationToString(targetBlock.getLocation()));
          plugin.getSetupUtilities().getConfig().set("instances." + arena.getId() + ".confessionals", confessionals);
          new MessageBuilder("&e✔ Completed | &aAdded Confessional special block for arena " + arena.getId() + " set at your location!").player(player).sendPlayer();
          new MessageBuilder("&eInfo | &aRemember to place any lever in radius of 3 near enchant table!").player(player).sendPlayer();
          ConfigUtils.saveConfig(plugin, plugin.getSetupUtilities().getConfig(), "arenas");
        }, event -> {
          new MessageBuilder("&cNot supported!").prefix().player(player).sendPlayer();
        }, false, false, false));

        break;
      default:
        break;
    }
    inv.refresh();
}

  @Override
  public boolean addAdditionalArenaValidateValues(InventoryClickEvent event, PluginArena arena, PluginMain plugin, FileConfiguration config) {
    for(String s : new String[]{"goldspawnpoints", "playerspawnpoints"}) {
      if(!config.isSet("instances." + arena.getId() + "." + s) || config.getStringList("instances." + arena.getId() + "." + s).size() < 4) {
        new MessageBuilder("&c&l✘ &cArena validation failed! Please configure following spawns properly: "+ s + " (must be minimum 4 spawns)").send(event.getWhoClicked());
        return false;
      }
    }

    return true;
  }

  @Override
  public void addAdditionalArenaSetValues(PluginArena arena, FileConfiguration config) {
    Arena pluginArena = plugin.getArenaRegistry().getArena(arena.getId());
    if(pluginArena == null) {
      return;
    }
    List<Location> playerSpawnPoints = new ArrayList<>();
    for(String loc : config.getStringList("instances." + arena.getId() + ".playerspawnpoints")) {
      playerSpawnPoints.add(LocationSerializer.getLocation(loc));
    }
    pluginArena.setPlayerSpawnPoints(playerSpawnPoints);
    List<Location> goldSpawnPoints = new ArrayList<>();
    for(String loc : config.getStringList("instances." + arena.getId() + ".goldspawnpoints")) {
      goldSpawnPoints.add(LocationSerializer.getLocation(loc));
    }
    pluginArena.setGoldSpawnPoints(goldSpawnPoints);

    List<SpecialBlock> specialBlocks = new ArrayList<>();
    if(config.isSet("instances." + arena.getId() + ".mystery-cauldrons")) {
      for(String loc : config.getStringList("instances." + arena.getId() + ".mystery-cauldrons")) {
        specialBlocks.add(new SpecialBlock(LocationSerializer.getLocation(loc), SpecialBlock.SpecialBlockType.MYSTERY_CAULDRON));
      }
    }
    if(config.isSet("instances." + arena.getId() + ".confessionals")) {
      for(String loc : config.getStringList("instances." + arena.getId() + ".confessionals")) {
        specialBlocks.add(new SpecialBlock(LocationSerializer.getLocation(loc), SpecialBlock.SpecialBlockType.PRAISE_DEVELOPER));
      }
    }
    for(SpecialBlock specialBlock : specialBlocks) {
      if(!pluginArena.getSpecialBlocks().contains(specialBlock)) {
        pluginArena.loadSpecialBlock(specialBlock);
      }
    }
    pluginArena.setSpawnGoldTime(config.getInt("instances." + arena.getId() + ".spawngoldtime", 5));
    pluginArena.setHideChances(config.getBoolean("instances." + arena.getId() + ".hidechances", false));
    pluginArena.setArenaOption("MURDERER_DIVIDER", config.getInt("instances." + arena.getId() + ".playerpermurderer", 5));
    pluginArena.setArenaOption("DETECTIVE_DIVIDER",config.getInt("instances." + arena.getId() + ".playerperdetective", 7));
 }
}
