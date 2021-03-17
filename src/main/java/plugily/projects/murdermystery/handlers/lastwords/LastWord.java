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
package plugily.projects.murdermystery.handlers.lastwords;

/**
 * @author 2Wild4You, Tigerpanzer_02
 * <p>
 * Created at 19.02.2021
 */
public class LastWord {

  private final String message;
  private final String permission;

  public LastWord(String message, String permission) {
    this.message = message;
    this.permission = permission;
  }

  public String getMessage() {
    return message;
  }

  public String getPermission() {
    return permission;
  }

  public boolean hasPermission() {
    return !permission.isEmpty();
  }

}
