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

package pl.plajer.murdermystery.handlers.language;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import pl.plajer.murdermystery.Main;
import pl.plajerlair.commonsbox.minecraft.migrator.MigratorUtils;

/*
  NOTE FOR CONTRIBUTORS - Please do not touch this class if you don't now how it works! You can break migrator modyfing these values!
 */
public class LanguageMigrator {

  public static final int CONFIG_FILE_VERSION = 10;
  private Main plugin;

  public LanguageMigrator(Main plugin) {
    this.plugin = plugin;

    //initializes migrator to update files with latest values
    configUpdate();
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

  private void updateConfigVersionControl(int oldVersion) {
    File file = new File(plugin.getDataFolder() + "/config.yml");
    MigratorUtils.removeLineFromFile(file, "# Don't modify");
    MigratorUtils.removeLineFromFile(file, "Version: " + oldVersion);
    MigratorUtils.removeLineFromFile(file, "# No way! You've reached the end! But... where's the dragon!?");
    MigratorUtils.addNewLines(file, "# Don't modify\r\nVersion: " + CONFIG_FILE_VERSION + "\r\n# No way! You've reached the end! But... where's the dragon!?");
  }

}
