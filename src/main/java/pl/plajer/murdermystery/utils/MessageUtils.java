/*
 * MurderMystery - Find the murderer, kill him and survive!
 * Copyright (C) 2019  Plajer's Lair - maintained by Plajer and contributors
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

/*
 * Murder Mystery is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Murder Mystery is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Murder Mystery.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.plajer.murdermystery.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import pl.plajer.murdermystery.Main;

/**
 * @author Plajer
 * <p>
 * Created at 03.08.2018
 */
public class MessageUtils {

  private static Main plugin = JavaPlugin.getPlugin(Main.class);

  @Deprecated
  public static void sendTitle(Player player, String text) {
    if (plugin.is1_9_R1() || plugin.is1_10_R1()) {
      player.sendTitle(text, null);
    } else {
      player.sendTitle(text, null, 5, 40, 5);
    }
  }

  @Deprecated
  public static void sendSubTitle(Player player, String text) {
    if (plugin.is1_9_R1() || plugin.is1_10_R1()) {
      player.sendTitle(null, text);
    } else {
      player.sendTitle(null, text, 5, 40, 5);
    }
  }

  public static void thisVersionIsNotSupported() {
    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "  _   _           _                                                    _                _ ");
    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + " | \\ | |   ___   | |_     ___   _   _   _ __    _ __     ___    _ __  | |_    ___    __| |");
    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + " |  \\| |  / _ \\  | __|   / __| | | | | | '_ \\  | '_ \\   / _ \\  | '__| | __|  / _ \\  / _` |");
    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + " | |\\  | | (_) | | |_    \\__ \\ | |_| | | |_) | | |_) | | (_) | | |    | |_  |  __/ | (_| |");
    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + " |_| \\_|  \\___/   \\__|   |___/  \\__,_| | .__/  | .__/   \\___/  |_|     \\__|  \\___|  \\__,_|");
    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "                                       |_|     |_|                                        ");
  }

  public static void errorOccurred() {
    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "  _____                                                                                  _   _ ");
    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + " | ____|  _ __   _ __    ___    _ __      ___     ___    ___   _   _   _ __    ___    __| | | |");
    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + " |  _|   | '__| | '__|  / _ \\  | '__|    / _ \\   / __|  / __| | | | | | '__|  / _ \\  / _` | | |");
    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + " | |___  | |    | |    | (_) | | |      | (_) | | (__  | (__  | |_| | | |    |  __/ | (_| | |_|");
    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + " |_____| |_|    |_|     \\___/  |_|       \\___/   \\___|  \\___|  \\__,_| |_|     \\___|  \\__,_| (_)");
    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "                                                                                               ");
  }

  public static void updateIsHere() {
    Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "  _   _               _           _          ");
    Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + " | | | |  _ __     __| |   __ _  | |_    ___ ");
    Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + " | | | | | '_ \\   / _` |  / _` | | __|  / _ \\");
    Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + " | |_| | | |_) | | (_| | | (_| | | |_  |  __/");
    Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "  \\___/  | .__/   \\__,_|  \\__,_|  \\__|  \\___|");
    Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "         |_|                                 ");
  }

  public static void info() {
    Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "  _____        __        _ ");
    Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + " |_   _|      / _|      | |");
    Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "   | |  _ __ | |_ ___   | |");
    Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "   | | | '_ \\|  _/ _ \\  | |");
    Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "  _| |_| | | | || (_) | |_|");
    Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + " |_____|_| |_|_| \\___/  (_)");
  }

}
