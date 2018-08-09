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

package pl.plajer.murdermystery.handlers.language;

/**
 * @author Plajer
 * <p>
 * Created at 03.08.2018
 */
public enum Locale {

  ENGLISH("English", "en_GB", "Plajer"),
  GERMAN("Deutsch", "de_DE", "Tigerkatze"),
  POLISH("Polski", "pl_PL", "Plajer");

  String formattedName;
  String prefix;
  String author;

  Locale(String formattedName, String prefix, String author) {
    this.prefix = prefix;
    this.formattedName = formattedName;
    this.author = author;
  }

  public String getFormattedName() {
    return formattedName;
  }

  public String getAuthor() {
    return author;
  }

  public String getPrefix() {
    return prefix;
  }

}
