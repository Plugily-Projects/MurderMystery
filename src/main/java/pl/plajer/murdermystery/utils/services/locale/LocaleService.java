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

package pl.plajer.murdermystery.utils.services.locale;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import pl.plajer.murdermystery.utils.services.ServiceRegistry;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;

/**
 * Localization service used for fetching latest locales for minigames
 */
public class LocaleService {

  private JavaPlugin plugin;
  private FileConfiguration localeData;

  public LocaleService(JavaPlugin plugin) {
    if (ServiceRegistry.getRegisteredService() == null || !ServiceRegistry.getRegisteredService().equals(plugin)) {
      throw new IllegalArgumentException("LocaleService cannot be used without registering service via ServiceRegistry first!");
    }
    if (!ServiceRegistry.isServiceEnabled()) {
      return;
    }
    this.plugin = plugin;
    try (Scanner scanner = new Scanner(requestLocaleFetch(null), "UTF-8").useDelimiter("\\A")) {
      String data = scanner.hasNext() ? scanner.next() : "";
      Files.write(new File(plugin.getDataFolder().getPath() + "/locales/locale_data.yml").toPath(), data.getBytes());
      this.localeData = ConfigUtils.getConfig(plugin, "/locales/locale_data");
      plugin.getLogger().log(Level.INFO, "Fetched latest localization file from repository.");
    } catch (IOException ignored) {
      //ignore exceptions
      plugin.getLogger().log(Level.WARNING, "Couldn't access locale fetcher service or there is other problem! You should notify author!");
    }
  }

  private static String toReadable(String version) {
    String[] split = Pattern.compile(".", Pattern.LITERAL).split(version.replace("v", ""));
    StringBuilder versionBuilder = new StringBuilder();
    for (String s : split) {
      versionBuilder.append(String.format("%4s", s));
    }
    version = versionBuilder.toString();
    return version;
  }

  private InputStream requestLocaleFetch(Locale locale) {
    try {
      //todo /v2/
      URL url = new URL("https://api.plajer.xyz/locale/fetch.php");
      HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setRequestProperty("User-Agent", "PLLocale/1.0");
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      conn.setDoOutput(true);

      OutputStream os = conn.getOutputStream();
      if (locale == null) {
        os.write(("pass=localeservice&type=" + plugin.getName()).getBytes(StandardCharsets.UTF_8));
      } else {
        os.write(("pass=localeservice&type=" + plugin.getName() + "&locale=" + locale.getPrefix()).getBytes(StandardCharsets.UTF_8));
      }
      os.flush();
      os.close();
      return conn.getInputStream();
    } catch (Exception e) {
      e.printStackTrace();
      return new InputStream() {
        @Override
        public int read() {
          return -1;
        }
      };
    }
  }

  /**
   * Sends a demand request to download latest locale from Plajer-Lair/locale_storage repository
   * Whole repository can be seen here https://github.com/Plajer-Lair/locale_storage
   *
   * @param locale locale to download
   * @return SUCCESS for downloaded locale, FAIL for service fault, LATEST when locale is latest as one in repository
   */
  public DownloadStatus demandLocaleDownload(Locale locale) {
    //service fault
    if (localeData == null) {
      return DownloadStatus.FAIL;
    }
    File localeFile = new File(plugin.getDataFolder() + "/locales/" + locale.getPrefix() + ".properties");
    if (!localeFile.exists() || !isExact(locale, localeFile)) {
      return writeFile(locale);
    }
    return DownloadStatus.LATEST;
  }

  private DownloadStatus writeFile(Locale locale) {
    try (Scanner scanner = new Scanner(requestLocaleFetch(locale), "UTF-8").useDelimiter("\\A")) {
      String data = scanner.hasNext() ? scanner.next() : "";
      Files.write(new File(plugin.getDataFolder().getPath() + "/locales/" + locale.getPrefix() + ".properties").toPath(), data.getBytes());
      return DownloadStatus.SUCCESS;
    } catch (IOException ignored) {
      plugin.getLogger().log(Level.WARNING, "Demanded locale " + locale.getPrefix() + " cannot be downloaded! You should notify author!");
      return DownloadStatus.FAIL;
    }
  }

  /**
   * Checks if plugin version allows to update locale
   *
   * @return true if locale can be updated for this version else cannot
   */
  public boolean isValidVersion() {
    //service fault
    if (localeData == null) {
      return false;
    }
    return !checkHigher(plugin.getDescription().getVersion(), localeData.getString("locales.valid-version", plugin.getDescription().getVersion()));
  }

  private boolean isExact(Locale locale, File file) {
    try (Scanner scanner = new Scanner(requestLocaleFetch(locale), "UTF-8").useDelimiter("\\A")) {
      String onlineData = scanner.hasNext() ? scanner.next() : "";
      Scanner localScanner = new Scanner(file, "UTF-8").useDelimiter("\\A");
      String localData = localScanner.hasNext() ? localScanner.next() : "";
      localScanner.close();

      return onlineData.equals(localData);
    } catch (IOException ignored) {
      return false;
    }
  }

  private boolean checkHigher(String currentVersion, String newVersion) {
    String current = toReadable(currentVersion);
    String newVer = toReadable(newVersion);
    return current.compareTo(newVer) < 0;
  }

  /**
   * Download status enum for locale download demands
   */
  public enum DownloadStatus {
    SUCCESS, FAIL, LATEST
  }

}
