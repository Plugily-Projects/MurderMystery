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

import org.bukkit.configuration.file.FileConfiguration;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.utils.Debugger;
import plugily.projects.murdermystery.utils.services.ServiceRegistry;
import plugily.projects.murdermystery.utils.services.locale.Locale;
import plugily.projects.murdermystery.utils.services.locale.LocaleRegistry;
import plugily.projects.murdermystery.utils.services.locale.LocaleService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Plajer
 * <p>
 * Created at 03.08.2018
 */
public class LanguageManager {

  private static final Properties properties = new Properties();
  private static Main plugin;
  private static Locale pluginLocale;
  private static FileConfiguration languageConfig;
  private static FileConfiguration defaultLanguageConfig;

  private LanguageManager() {
  }

  public static void init(Main plugin) {
    LanguageManager.plugin = plugin;
    if (!new File(LanguageManager.plugin.getDataFolder() + File.separator + "language.yml").exists()) {
      plugin.saveResource("language.yml", false);
    }
    //auto update
    plugin.saveResource("locales/language_default.yml", true);

    new LanguageMigrator(plugin);
    languageConfig = ConfigUtils.getConfig(plugin, "language");
    defaultLanguageConfig = ConfigUtils.getConfig(plugin, "locales/language_default");
    registerLocales();
    setupLocale();
  }

  private static void registerLocales() {
    Stream.of(
      new Locale("Afrikaans", "Afrikaans", "af_ZA", "POEditor contributors", Arrays.asList("afrika", "af", "afr")),
      new Locale("Chinese (Simplified)", "简体中文", "zh_CN", "POEditor contributors", Arrays.asList("简体中文", "中文", "chinese", "chinese_simplified", "cn")),
      new Locale("Chinese (Traditional)", "繁體中文", "zh_TW", "POEditor contributors", Arrays.asList("中文(繁體)", "繁體中文", "chinese_traditional", "zh_tw")),
      new Locale("Czech", "Český", "cs_CZ", "POEditor contributors", Arrays.asList("czech", "cesky", "český", "cs")),
      new Locale("Danish", "Dänemark", "da_DK", "POEditor contributors", Arrays.asList("dänisch", "da", "dk")),
      new Locale("Dutch", "Nederlands", "nl_NL", "POEditor contributors", Arrays.asList("dutch", "nederlands", "nl")),
      new Locale("English", "English", "en_GB", "Plajer", Arrays.asList("default", "english", "en")),
      new Locale("French", "Français", "fr_FR", "POEditor contributors", Arrays.asList("french", "francais", "français", "fr")),
      new Locale("German", "Deutsch", "de_DE", "Tigerkatze and POEditor contributors", Arrays.asList("deutsch", "german", "de")),
      new Locale("Hungarian", "Magyar", "hu_HU", "POEditor contributors", Arrays.asList("hungarian", "magyar", "hu")),
      new Locale("Indonesian", "Indonesia", "id_ID", "POEditor contributors", Arrays.asList("indonesian", "indonesia", "id")),
      new Locale("Italian", "Italiano", "it_IT", "POEditor contributors", Arrays.asList("italian", "italiano", "it")),
      new Locale("Korean", "한국의", "ko_KR", "POEditor contributors", Arrays.asList("korean", "한국의", "kr")),
      new Locale("Polish", "Polski", "pl_PL", "Plajer", Arrays.asList("polish", "polski", "pl")),
      new Locale("Portuguese (BR)", "Português (Brasil)", "pt_BR", "POEditor contributors", Arrays.asList("portuguese br", "português br", "português brasil", "pt_br")),
      new Locale("Romanian", "Românesc", "ro_RO", "POEditor contributors", Arrays.asList("romanian", "romanesc", "românesc", "ro")),
      new Locale("Russian", "Pусский", "ru_RU", "POEditor contributors", Arrays.asList("russian", "pусский", "pyccknn", "russkiy", "ru")),
      new Locale("Slovak", "Slovenský", "sk_SK", "POEditor contributors", Arrays.asList("slovak", "slovenský", "slovensky", "sk")),
      new Locale("Spanish", "Español", "es_ES", "POEditor contributors", Arrays.asList("spanish", "espanol", "español", "es")),
      new Locale("Thai", "Thai", "th_TH", "POEditor contributors", Arrays.asList("thai", "th")),
      new Locale("Turkish", "Türkçe", "tr_TR", "POEditor contributors", Arrays.asList("turkish", "türkçe", "turkce", "tr")),
      new Locale("Ukrainian", "Ukraine", "uk_UA", "POEditor contributors", Arrays.asList("ukraine", "ua", "uk")),
      new Locale("Vietnamese", "Việt", "vn_VN", "POEditor contributors", Arrays.asList("vietnamese", "viet", "việt", "vn")))
      .forEach(LocaleRegistry::registerLocale);
  }

  private static void loadProperties() {
    LocaleService service = ServiceRegistry.getLocaleService(plugin);
    if (service == null) {
      Debugger.sendConsoleMsg("&c[Murder Mystery] Locales cannot be downloaded because API website is unreachable, locales will be disabled.");
      pluginLocale = LocaleRegistry.getByName("English");
      return;
    }
    if (service.isValidVersion()) {
      LocaleService.DownloadStatus status = service.demandLocaleDownload(pluginLocale);
      if (status == LocaleService.DownloadStatus.FAIL) {
        pluginLocale = LocaleRegistry.getByName("English");
        Debugger.sendConsoleMsg("&c[Murder Mystery] Locale service couldn't download latest locale for plugin! English locale will be used instead!");
        return;
      } else if (status == LocaleService.DownloadStatus.SUCCESS) {
        Debugger.sendConsoleMsg("&c[Murder Mystery] Downloaded locale " + pluginLocale.getPrefix() + " properly!");
      } else if (status == LocaleService.DownloadStatus.LATEST) {
        Debugger.sendConsoleMsg("&c[Murder Mystery] Locale " + pluginLocale.getPrefix() + " is latest! Awesome!");
      }
    } else {
      pluginLocale = LocaleRegistry.getByName("English");
      Debugger.sendConsoleMsg("&c[Murder Mystery] Your plugin version is too old to use latest locale! Please update plugin to access latest updates of locale!");
      return;
    }
    try (InputStreamReader reader = new InputStreamReader(new FileInputStream(plugin.getDataFolder() + "/locales/"
      + pluginLocale.getPrefix() + ".properties"), StandardCharsets.UTF_8)) {
      properties.load(reader);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void setupLocale() {
    String localeName = plugin.getConfig().getString("locale", "default").toLowerCase();
    for (Locale locale : LocaleRegistry.getRegisteredLocales()) {
      if (locale.getPrefix().equalsIgnoreCase(localeName)) {
        pluginLocale = locale;
        break;
      }
      for (String alias : locale.getAliases()) {
        if (alias.equals(localeName)) {
          pluginLocale = locale;
          break;
        }
      }
    }
    if (pluginLocale == null) {
      Debugger.sendConsoleMsg("&c[Murder Mystery] Plugin locale is invalid! Using default one...");
      pluginLocale = LocaleRegistry.getByName("English");
    }
    Debugger.sendConsoleMsg("&a[Murder Mystery] Loaded locale " + pluginLocale.getName() + " (" + pluginLocale.getOriginalName() + " ID: "
      + pluginLocale.getPrefix() + ") by " + pluginLocale.getAuthor());
    loadProperties();
  }

  public static boolean isDefaultLanguageUsed() {
    return pluginLocale.getName().equals("English");
  }

  public static String getLanguageMessage(String path) {
    if (isDefaultLanguageUsed()) {
      return getString(path);
    }
    String prop = properties.getProperty(path);
    if (prop == null){
      return getString(path);
    }
    if (getString(path).equalsIgnoreCase(defaultLanguageConfig.getString(path, "not found"))){
      return prop;
    }
    return getString(path);
  }

  public static List<String> getLanguageList(String path) {
    if (isDefaultLanguageUsed()) {
      return getStrings(path);
    }
    String prop = properties.getProperty(path);
    if (prop == null) {
      return getStrings(path);
    }
    if (getString(path).equalsIgnoreCase(defaultLanguageConfig.getString(path, "not found"))){
      return Arrays.asList(plugin.getChatManager().colorRawMessage(prop).split(";"));
    }
    return getStrings(path);
  }


  private static List<String> getStrings(String path) {
    //check normal language if nothing found in specific language
    if (!languageConfig.isSet(path)) {
      //send normal english message - User can change this translation on his own
      Debugger.sendConsoleMsg("&c[Murder Mystery] Game message not found in your locale! Added it to your language.yml");
      Debugger.sendConsoleMsg("&c[Murder Mystery] Path: " + path + " | Language not found. Report it to the author on Discord!");
    }
    List<String> list = languageConfig.getStringList(path);
    list = list.stream().map(string -> org.bukkit.ChatColor.translateAlternateColorCodes('&', string)).collect(Collectors.toList());
    return list;
  }


  private static String getString(String path) {
    //check normal language if nothing found in specific language
    if (!languageConfig.isSet(path)) {
      //send normal english message - User can change this translation on his own
      Debugger.sendConsoleMsg("&c[Murder Mystery] Game message not found in your locale! Added it to your language.yml");
      Debugger.sendConsoleMsg("&c[Murder Mystery] Path: " + path + " | Language not found. Report it to the author on Discord!");
    }
    return languageConfig.getString(path, "Not found");
  }

  public static void reloadConfig() {
    languageConfig = ConfigUtils.getConfig(plugin, "language");
  }

  public static Locale getPluginLocale() {
    return pluginLocale;
  }
}
