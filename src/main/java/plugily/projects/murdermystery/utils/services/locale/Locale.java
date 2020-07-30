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

package plugily.projects.murdermystery.utils.services.locale;

import java.util.List;

/**
 * Class for locales
 *
 * @since 1.2.0
 */
public class Locale {

  private String name;
  private String originalName;
  private String prefix;
  private String author;
  private List<String> aliases;

  public Locale(String name, String originalName, String prefix, String author, List<String> aliases) {
    this.prefix = prefix;
    this.name = name;
    this.originalName = originalName;
    this.author = author;
    this.aliases = aliases;
  }

  /**
   * Gets name of locale, ex. English or German
   *
   * @return name of locale
   */
  public String getName() {
    return name;
  }

  /**
   * Gets original name of locale ex. for German it will return Deutsch, Polish returns Polski etc.
   *
   * @return name of locale in its language
   */
  public String getOriginalName() {
    return originalName;
  }

  /**
   * @return authors of locale
   */
  public String getAuthor() {
    return author;
  }

  /**
   * Language code ex. en_GB, de_DE, pl_PL etc.
   *
   * @return language code of locale
   */
  public String getPrefix() {
    return prefix;
  }

  /**
   * Valid aliases of locale ex. for German - deutsch, de, german; Polish - polski, pl, polish etc.
   *
   * @return aliases for locale
   */
  public List<String> getAliases() {
    return aliases;
  }

}
