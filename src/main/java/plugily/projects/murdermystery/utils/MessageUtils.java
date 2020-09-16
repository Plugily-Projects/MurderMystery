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

package plugily.projects.murdermystery.utils;

/**
 * @author Plajer
 * <p>
 * Created at 03.08.2018
 */
public class MessageUtils {

  private MessageUtils() {
  }

  public static void thisVersionIsNotSupported() {
    Debugger.sendConsoleMsg("&c  _   _           _                                                    _                _ ");
    Debugger.sendConsoleMsg("&c | \\ | |   ___   | |_     ___   _   _   _ __    _ __     ___    _ __  | |_    ___    __| |");
    Debugger.sendConsoleMsg("&c |  \\| |  / _ \\  | __|   / __| | | | | | '_ \\  | '_ \\   / _ \\  | '__| | __|  / _ \\  / _` |");
    Debugger.sendConsoleMsg("&c | |\\  | | (_) | | |_    \\__ \\ | |_| | | |_) | | |_) | | (_) | | |    | |_  |  __/ | (_| |");
    Debugger.sendConsoleMsg("&c |_| \\_|  \\___/   \\__|   |___/  \\__,_| | .__/  | .__/   \\___/  |_|     \\__|  \\___|  \\__,_|");
    Debugger.sendConsoleMsg("&c                                       |_|     |_|                                        ");
  }

  public static void errorOccurred() {
    Debugger.sendConsoleMsg("&c  _____                                                                                  _   _ ");
    Debugger.sendConsoleMsg("&c | ____|  _ __   _ __    ___    _ __      ___     ___    ___   _   _   _ __    ___    __| | | |");
    Debugger.sendConsoleMsg("&c |  _|   | '__| | '__|  / _ \\  | '__|    / _ \\   / __|  / __| | | | | | '__|  / _ \\  / _` | | |");
    Debugger.sendConsoleMsg("&c | |___  | |    | |    | (_) | | |      | (_) | | (__  | (__  | |_| | | |    |  __/ | (_| | |_|");
    Debugger.sendConsoleMsg("&c |_____| |_|    |_|     \\___/  |_|       \\___/   \\___|  \\___|  \\__,_| |_|     \\___|  \\__,_| (_)");
    Debugger.sendConsoleMsg("&c                                                                                               ");
  }

  public static void updateIsHere() {
    Debugger.sendConsoleMsg("&a  _   _               _           _          ");
    Debugger.sendConsoleMsg("&a | | | |  _ __     __| |   __ _  | |_    ___ ");
    Debugger.sendConsoleMsg("&a | | | | | '_ \\   / _` |  / _` | | __|  / _ \\");
    Debugger.sendConsoleMsg("&a | |_| | | |_) | | (_| | | (_| | | |_  |  __/");
    Debugger.sendConsoleMsg("&a  \\___/  | .__/   \\__,_|  \\__,_|  \\__|  \\___|");
    Debugger.sendConsoleMsg("&a         |_|                                 ");
  }

  public static void info() {
    Debugger.sendConsoleMsg("&e  _____        __        _ ");
    Debugger.sendConsoleMsg("&e |_   _|      / _|      | |");
    Debugger.sendConsoleMsg("&e   | |  _ __ | |_ ___   | |");
    Debugger.sendConsoleMsg("&e   | | | '_ \\|  _/ _ \\  | |");
    Debugger.sendConsoleMsg("&e  _| |_| | | | || (_) | |_|");
    Debugger.sendConsoleMsg("&e |_____|_| |_|_| \\___/  (_)");
  }

}
