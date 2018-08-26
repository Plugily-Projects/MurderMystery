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

import java.util.Arrays;
import java.util.List;

/**
 * @author Plajer
 * <p>
 * Created at 03.08.2018
 */
public enum Locale {

  CHINESE_SIMPLIFIED("简体中文", "zh_Hans", "POEditor contributors (壁灯)", Arrays.asList("简体中文", "中文", "chinese", "zh")),
  CZECH("Český", "cs_CZ", "POEditor contributors", Arrays.asList("czech", "cesky", "český", "cs")),
  ENGLISH("English", "en_GB", "Plajer", Arrays.asList("default", "english", "en")),
  FRENCH("Français", "fr_FR", "POEditor contributors (Dianox)", Arrays.asList("french", "francais", "français", "fr")),
  GERMAN("Deutsch", "de_DE", "Tigerkatze", Arrays.asList("deutsch", "german", "de")),
  HUNGARIAN("Magyar", "hu_HU", "POEditor contributors", Arrays.asList("hungarian", "magyar", "hu")),
  INDONESIAN("Indonesia", "id_ID", "POEditor contributors", Arrays.asList("indonesian", "indonesia", "id")),
  KOREAN("한국의", "ko_KR", "POEditor contributors (human0324)", Arrays.asList("korean", "한국의", "kr")),
  POLISH("Polski", "pl_PL", "Plajer & POEditor contributors (Owen Port)", Arrays.asList("polish", "polski", "pl"));

  String formattedName;
  String prefix;
  String author;
  List<String> aliases;

  Locale(String formattedName, String prefix, String author, List<String> aliases) {
    this.prefix = prefix;
    this.formattedName = formattedName;
    this.author = author;
    this.aliases = aliases;
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

  public List<String> getAliases() {
    return aliases;
  }

}
