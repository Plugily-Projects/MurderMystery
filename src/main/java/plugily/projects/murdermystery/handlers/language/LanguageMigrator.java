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

package plugily.projects.murdermystery.handlers.language;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;
import pl.plajerlair.commonsbox.minecraft.migrator.MigratorUtils;
import plugily.projects.murdermystery.Main;

import java.io.File;

/*
  NOTE FOR CONTRIBUTORS - Please do not touch this class if you don't now how it works! You can break migrator modyfing these values!
 */
@SuppressWarnings("deprecation")
public class LanguageMigrator {

  public static final int CONFIG_FILE_VERSION = 19;
  public static final int LANGUAGE_FILE_VERSION = 4;
  private final Main plugin;

  public LanguageMigrator(Main plugin) {
    this.plugin = plugin;

    //initializes migrator to update files with latest values
    configUpdate();
    languageFileUpdate();
  }

  private void configUpdate() {
    if (plugin.getConfig().getInt("Version") == CONFIG_FILE_VERSION) {
      return;
    }
    Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[Murder Mystery] System notify >> Your config file is outdated! Updating...");
    File file = new File(plugin.getDataFolder() + "/config.yml");
    File bungeefile = new File(plugin.getDataFolder() + "/bungee.yml");

    int version = plugin.getConfig().getInt("Version", CONFIG_FILE_VERSION - 1);

    for (int i = version; i < CONFIG_FILE_VERSION; i++) {
      switch (i) {
        case 1:
          MigratorUtils.addNewLines(file, "\r\n# How many blocks per tick sword thrown by murderer should fly\r\n" +
            "# Please avoid high values as it might look like the sword is\r\n" +
            "# blinking each tick\r\n" +
            "Murderer-Sword-Speed: 0.65\r\n");
          break;
        case 2:
          MigratorUtils.addNewLines(file, "\r\n# Should players' name tags in game be hidden?\r\n" +
            "Nametags-Hidden: true\r\n");
          break;
        case 3:
          MigratorUtils.addNewLines(file, "\r\n# Lobby waiting time set when lobby max players number is reached, used to start game quicker.\r\n" +
            "Start-Time-On-Full-Lobby: 15\r\n");
          break;
        case 4:
          MigratorUtils.addNewLines(file, "\r\n# Should players get no fall damage?\r\n" +
            "Disable-Fall-Damage: false\r\n");
          break;
        case 5:
          MigratorUtils.addNewLines(file, "\r\n#How long should be the sword attack after throw cooldown in seconds?\r\n" +
            "#Its normal lower than Murderer-Sword-Fly-Cooldown!\r\n" +
            "Murderer-Sword-Attack-Cooldown: 1\r\n" +
            "\r\n" +
            "#How long should be the sword fly cooldown in seconds?\r\n" +
            "Murderer-Sword-Fly-Cooldown: 5\r\n" +
            "\r\n" +
            "#How long should be the bow shoot cooldown in seconds?\r\n" +
            "Detective-Bow-Cooldown: 5\r\n");
          break;
        case 6:
          MigratorUtils.addNewLines(file, "\r\n# Which item should be your Murderer sword?\r\n" +
            "Murderer-Sword-Material: IRON_SWORD\r\n");
          break;
        case 7:
          MigratorUtils.addNewLines(file, "\r\n#How much arrows should a player with bow gets when he pick up a gold ingot?\r\n" +
            "Detective-Gold-Pick-Up-Arrows: 1\r\n" +
            "\r\n" +
            "#How much arrows should the detective gets on game start or when a player get a bow?\r\n" +
            "Detective-Default-Arrows: 3\r\n" +
            "\r\n" +
            "#How much arrows should the player get when the prayer gives a bow to him?\r\n" +
            "Detective-Prayer-Arrows: 2\r\n");
          break;
        case 8:
          MigratorUtils.removeLineFromFile(bungeefile, "# This is useful for bungee game systems.");
          MigratorUtils.removeLineFromFile(bungeefile, "# Game state will be visible at MOTD.");
          MigratorUtils.removeLineFromFile(bungeefile, "MOTD-manager: false");
          MigratorUtils.removeLineFromFile(bungeefile, "MOTD-manager: true");
          MigratorUtils.addNewLines(bungeefile, "\r\n# This is useful for bungee game systems.\r\n" +
            "# %state% - Game state will be visible at MOTD.\r\n" +
            "MOTD:\r\n" +
            "  Manager: false\r\n" +
            "  Message: \"The actual game state of mm is %state%\"\r\n" +
            "  Game-States:\r\n" +
            "    Inactive: \"&lInactive...\"\r\n" +
            "    In-Game: \"&lIn-game\"\r\n" +
            "    Starting: \"&e&lStarting\"\r\n" +
            "    Full-Game: \"&4&lFULL\"\r\n" +
            "    Ending: \"&lEnding\"\r\n" +
            "    Restarting: \"&c&lRestarting\"\r\n");
          break;
        case 9:
          MigratorUtils.addNewLines(file, "\r\n" +
            "# Should we enable short commands such as /start and /leave\r\n" +
            "Enable-Short-Commands: false\r\n");
          break;
        case 10:
          MigratorUtils.addNewLines(file, "\r\n" +
            "#Should Detectives be killed if they kill a innocent?\r\n" +
            "Enable-Kill-Detective-If-Innocent-Killed: true\r\n");
          break;
        case 11:
          MigratorUtils.addNewLines(file, "\r\n#Should the murderer get speed effect?\r\n" +
            "Speed-Effect-Murderer:\r\n" +
            "  Enabled: true\r\n" +
            "  #Enter a multiplier (min 2, max 10)\r\n" +
            "  Speed: 3\r\n");
          break;
        case 12:
          MigratorUtils.addNewLines(file, "\r\n" +
            "#Should we change spawner mode to spawn on all spawners instant of random one\r\n" +
            "Change-Gold-Spawner-Mode-To-All: false\r\n");
          MigratorUtils.addNewLines(file, "\r\n" +
            "#Should we disable the gold spawn limit (It does not spawn more gold than spawner locations)\r\n" +
            "Disable-Gold-Limiter: false\r\n");
          break;
        case 13:
          MigratorUtils.addNewLines(file, "\r\n" +
            "# How many blocks should the sword fly\r\n" +
            "Murderer-Sword-Fly-Range: 20\r\n" +
            "\r\n" +
            "# In what radius should we hit the players\r\n" +
            "Murderer-Sword-Fly-Hit-Range: 0.5\r\n");
          break;
        case 14:
          MigratorUtils.addNewLines(file, "\r\n" +
            "#How much gold should a player need to get a bow\r\n" +
            "Gold-For-Bow: 10\r\n");
          break;
        case 15:
          MigratorUtils.addNewLines(file, "\r\n" +
            "#How much arrows should the fake detective get? (Cause: Player pick up bow after detective died)\r\n" +
            "Detective-Fake-Arrows: 3\r\n" +
            "\r\n" +
            "#How much arrows should the player get? (Cause: Bow because enough gold collected)\r\n" +
            "Gold-Bow-Arrows: 3\r\n");
          break;
        case 16:
          MigratorUtils.addNewLines(file, "\r\n" +
            "# Should we disable all chat related stuff?\r\n" +
            "# It will disable the separated chat, for example\r\n" +
            "Disable-Separate-Chat: false\r\n");
          break;
        case 17:
          MigratorUtils.addNewLines(file, "\r\n" +
            "#Disable Party features of external party plugins (such as PAF, Parties ...)\r\n" +
            "Disable-Parties: true\r\n");
          break;
        case 18:
          MigratorUtils.addNewLines(file, "\r\n" +
            "#Should there be a innocent locator\r\n" +
            "Enable-Innocent-Locator: true\r\n");
          break;
        default:
          break;
      }
      i++;
    }
    updateConfigVersionControl(version);
    plugin.reloadConfig();
    Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Murder Mystery] [System notify] Config updated, no comments were removed :)");
    Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Murder Mystery] [System notify] You're using latest config file version! Nice!");
  }

  private void languageFileUpdate() {
    FileConfiguration config = ConfigUtils.getConfig(plugin, "language");
    if (config.getString("File-Version-Do-Not-Edit", "").equals(String.valueOf(LANGUAGE_FILE_VERSION))) {
      return;
    }
    Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[Murder Mystery] [System notify] Your language file is outdated! Updating...");

    int version = LANGUAGE_FILE_VERSION - 1;
    if (NumberUtils.isNumber(config.getString("File-Version-Do-Not-Edit"))) {
      version = Integer.parseInt(config.getString("File-Version-Do-Not-Edit"));
    } else {
      Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Murder Mystery] [System notify] Failed to parse language file version!");
    }
    updateLanguageVersionControl(version);

    File file = new File(plugin.getDataFolder() + "/language.yml");

    for (int i = version; i < LANGUAGE_FILE_VERSION; i++) {
      switch (version) {
        case 1:
          MigratorUtils.insertAfterLine(file, "Lobby-Messages:", "      Not-Enough-Space-For-Party: \"&cYour party is bigger than free places on the arena %ARENANAME%\"");
          MigratorUtils.insertAfterLine(file, "In-Game:", "  Join-As-Party-Member: \"&cYou joined %ARENANAME% because the party leader joined it!\"");
          break;
        case 2:
          MigratorUtils.addNewLines(file, "Arena-Selector:\r\n" +
            "  Inv-Title: \"Arena selector\"\r\n" +
            "  Item:\r\n" +
            "    Lore:\r\n" +
            "      - \"&4Murder Mystery &f- &e%mapname%\"\r\n" +
            "      - \" \"\r\n" +
            "      - \" \"\r\n" +
            "      - \"  &fOnline: %playersize%/%maxplayers%\"\r\n" +
            "      - \"  &fState: %state%\"\r\n" +
            "      - \" \"\r\n" +
            "      - \" \"\r\n" +
            "      - \"&eClick to join this arena\"\r\n");
          break;
        case 3:
          MigratorUtils.insertAfterLine(file, "In-Game:", "  Game-Death-Format: \"&7[&4☠&7] &r\"");
          break;
        default:
          break;
      }
      version++;
    }
    Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Murder Mystery] [System notify] Language file updated! Nice!");
    Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Murder Mystery] [System notify] You're using latest language file version! Nice!");
  }

  private void updateConfigVersionControl(int oldVersion) {
    File file = new File(plugin.getDataFolder() + "/config.yml");
    MigratorUtils.removeLineFromFile(file, "# Don't modify");
    MigratorUtils.removeLineFromFile(file, "Version: " + oldVersion);
    MigratorUtils.removeLineFromFile(file, "# No way! You've reached the end! But... where's the dragon!?");
    MigratorUtils.addNewLines(file, "# Don't modify\r\nVersion: " + CONFIG_FILE_VERSION + "\r\n# No way! You've reached the end! But... where's the dragon!?");
  }

  private void updateLanguageVersionControl(int oldVersion) {
    File file = new File(plugin.getDataFolder() + "/language.yml");
    MigratorUtils.removeLineFromFile(file, "# Don't edit it. But who's stopping you? It's your server!");
    MigratorUtils.removeLineFromFile(file, "# Really, don't edit ;p");
    MigratorUtils.removeLineFromFile(file, "File-Version-Do-Not-Edit: " + oldVersion);
    MigratorUtils.addNewLines(file, "# Don't edit it. But who's stopping you? It's your server!\r\n# Really, don't edit ;p\r\nFile-Version-Do-Not-Edit: " + LANGUAGE_FILE_VERSION + "\r\n");
  }
}
