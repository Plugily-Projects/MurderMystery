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

import java.util.ArrayList;
import java.util.List;

/**
 * Registry class for holding locales
 *
 * @since 1.2.0
 */
public class LocaleRegistry {

  private static List<Locale> registeredLocales = new ArrayList<>();

  /**
   * Register new locale into registry
   *
   * @param locale locale to register
   * @throws IllegalArgumentException if same locale is registered twice
   */
  public static void registerLocale(Locale locale) {
    if (registeredLocales.contains(locale)) {
      throw new IllegalArgumentException("Cannot register same locale twice!");
    }
    registeredLocales.add(locale);
  }

  /**
   * Get all registered locales
   *
   * @return all registered locales
   */
  public static List<Locale> getRegisteredLocales() {
    return registeredLocales;
  }

  /**
   * Get locale by its name
   *
   * @param name name to search
   * @return locale by name or locale "Undefined" when not found (null is not returned)
   * @since 1.2.2
   */
  public static Locale getByName(String name) {
    for (Locale locale : registeredLocales) {
      if (locale.getName().equals(name)) {
        return locale;
      }
    }
    return new Locale("Undefined", "Undefined", "", "System", new ArrayList<>());
  }
}
