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

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * A utility class to assist in checking for updates for plugins uploaded to
 * <a href="https://spigotmc.org/resources/">SpigotMC</a>. Before any members of this
 * class are accessed, {@link #init(JavaPlugin, int)} must be invoked by the plugin,
 * preferrably in its {@link JavaPlugin#onEnable()} method, though that is not a
 * requirement.
 * <p>
 * This class performs asynchronous queries to <a href="https://spiget.org">SpiGet</a>,
 * an REST server which is updated periodically. If the results of {@link #requestUpdateCheck()}
 * are inconsistent with what is published on SpigotMC, it may be due to SpiGet's cache.
 * Results will be updated in due time.
 *
 * @author Parker Hawke - 2008Choco
 */
public final class UpdateChecker {

  private static final String USER_AGENT = "CHOCO-update-checker";
  private static final String UPDATE_URL = "https://api.spiget.org/v2/resources/%d/versions?size=1&sort=-releaseDate";
  private static final Pattern DECIMAL_SCHEME_PATTERN = Pattern.compile("\\d+(?:\\.\\d+)*");
  public static final VersionScheme VERSION_SCHEME_DECIMAL = (first, second) -> {
    String[] firstSplit = splitVersionInfo(first), secondSplit = splitVersionInfo(second);
    if (firstSplit == null || secondSplit == null) {
      return null;
    }

    for (int i = 0; i < Math.min(firstSplit.length, secondSplit.length); i++) {
      int currentValue = NumberUtils.toInt(firstSplit[i]), newestValue = NumberUtils.toInt(secondSplit[i]);

      if (newestValue > currentValue) {
        return second;
      } else if (newestValue < currentValue) {
        return first;
      }
    }

    return (secondSplit.length > firstSplit.length) ? second : first;
  };
  private static UpdateChecker instance;
  private final JavaPlugin plugin;
  private final int pluginID;
  private final VersionScheme versionScheme;
  private UpdateResult lastResult = null;

  private UpdateChecker(JavaPlugin plugin, int pluginID, VersionScheme versionScheme) {
    this.plugin = plugin;
    this.pluginID = pluginID;
    this.versionScheme = versionScheme;
  }

  private static String[] splitVersionInfo(String version) {
    Matcher matcher = DECIMAL_SCHEME_PATTERN.matcher(version);
    if (!matcher.find()) {
      return null;
    }

    return matcher.group().split("\\.");
  }

  /**
   * Initialize this update checker with the specified values and return its instance. If an instance
   * of UpdateChecker has already been initialized, this method will act similarly to {@link #get()}
   * (which is recommended after initialization).
   *
   * @param plugin        the plugin for which to check updates. Cannot be null
   * @param pluginID      the ID of the plugin as identified in the SpigotMC resource link. For example,
   *                      "https://www.spigotmc.org/resources/veinminer.<b>12038</b>/" would expect "12038" as a value. The
   *                      value must be greater than 0
   * @param versionScheme a custom version scheme parser. Cannot be null
   * @return the UpdateChecker instance
   */
  public static UpdateChecker init(JavaPlugin plugin, int pluginID, VersionScheme versionScheme) {
    Preconditions.checkArgument(plugin != null, "Plugin cannot be null");
    Preconditions.checkArgument(pluginID > 0, "Plugin ID must be greater than 0");
    Preconditions.checkArgument(versionScheme != null, "null version schemes are unsupported");

    return (instance == null) ? instance = new UpdateChecker(plugin, pluginID, versionScheme) : instance;
  }

  /**
   * Initialize this update checker with the specified values and return its instance. If an instance
   * of UpdateChecker has already been initialized, this method will act similarly to {@link #get()}
   * (which is recommended after initialization).
   *
   * @param plugin   the plugin for which to check updates. Cannot be null
   * @param pluginID the ID of the plugin as identified in the SpigotMC resource link. For example,
   *                 "https://www.spigotmc.org/resources/veinminer.<b>12038</b>/" would expect "12038" as a value. The
   *                 value must be greater than 0
   * @return the UpdateChecker instance
   */
  public static UpdateChecker init(JavaPlugin plugin, int pluginID) {
    return init(plugin, pluginID, VERSION_SCHEME_DECIMAL);
  }

  /**
   * Get the initialized instance of UpdateChecker. If {@link #init(JavaPlugin, int)} has not yet been
   * invoked, this method will throw an exception.
   *
   * @return the UpdateChecker instance
   */
  public static UpdateChecker get() {
    Preconditions.checkState(instance != null, "Instance has not yet been initialized. Be sure #init() has been invoked");
    return instance;
  }

  /**
   * Check whether the UpdateChecker has been initialized or not (if {@link #init(JavaPlugin, int)}
   * has been invoked) and {@link #get()} is safe to use.
   *
   * @return true if initialized, false otherwise
   */
  public static boolean isInitialized() {
    return instance != null;
  }

  /**
   * Request an update check to SpiGet. This request is asynchronous and may not complete
   * immediately as an HTTP GET request is published to the SpiGet API.
   *
   * @return a future update result
   */
  public CompletableFuture<UpdateResult> requestUpdateCheck() {
    return CompletableFuture.supplyAsync(() -> {
      int responseCode = -1;
      try {
        URL url = new URL(String.format(UPDATE_URL, pluginID));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("User-Agent", USER_AGENT);

        InputStreamReader reader = new InputStreamReader(connection.getInputStream());
        responseCode = connection.getResponseCode();

        JsonElement element = new JsonParser().parse(reader);
        if (!element.isJsonArray()) {
          return new UpdateResult(UpdateReason.INVALID_JSON);
        }

        reader.close();

        JsonObject versionObject = element.getAsJsonArray().get(0).getAsJsonObject();
        String current = plugin.getDescription().getVersion(), newest = versionObject.get("name").getAsString();
        String latest = versionScheme.compareVersions(current, newest);

        if (latest == null) {
          return new UpdateResult(UpdateReason.UNSUPPORTED_VERSION_SCHEME);
        } else if (latest.equals(current)) {
          return new UpdateResult(current.equals(newest) ? UpdateReason.UP_TO_DATE : UpdateReason.UNRELEASED_VERSION);
        } else if (latest.equals(newest)) {
          return new UpdateResult(UpdateReason.NEW_UPDATE, latest);
        }
      } catch (IOException e) {
        return new UpdateResult(UpdateReason.COULD_NOT_CONNECT);
      } catch (JsonSyntaxException e) {
        return new UpdateResult(UpdateReason.INVALID_JSON);
      }

      return new UpdateResult(responseCode == 401 ? UpdateReason.UNAUTHORIZED_QUERY : UpdateReason.UNKNOWN_ERROR);
    });
  }

  /**
   * Get the last update result that was queried by {@link #requestUpdateCheck()}. If no update
   * check was performed since this class' initialization, this method will return null.
   *
   * @return the last update check result. null if none.
   */
  public UpdateResult getLastResult() {
    return lastResult;
  }


  /**
   * A constant reason for the result of {@link UpdateResult}.
   */
  public enum UpdateReason {

    /**
     * A new update is available for download on SpigotMC.
     */
    NEW_UPDATE, // The only reason that requires an update

    /**
     * A successful connection to the SpiGet API could not be established.
     */
    COULD_NOT_CONNECT,

    /**
     * The JSON retrieved from SpiGet was invalid or malformed.
     */
    INVALID_JSON,

    /**
     * A 401 error was returned by the SpiGet API.
     */
    UNAUTHORIZED_QUERY,

    /**
     * The version of the plugin installed on the server is greater than the one uploaded
     * to SpigotMC's resources section.
     */
    UNRELEASED_VERSION,

    /**
     * An unknown error occurred.
     */
    UNKNOWN_ERROR,

    /**
     * The plugin uses an unsupported version scheme, therefore a proper comparison between
     * versions could not be made.
     */
    UNSUPPORTED_VERSION_SCHEME,

    /**
     * The plugin is up to date with the version released on SpigotMC's resources section.
     */
    UP_TO_DATE

  }

  /**
   * A functional interface to compare two version Strings with similar version schemes.
   */
  @FunctionalInterface
  public interface VersionScheme {

    /**
     * Compare two versions and return the higher of the two. If null is returned, it is assumed
     * that at least one of the two versions are unsupported by this version scheme parser.
     *
     * @param first  the first version to check
     * @param second the second version to check
     * @return the greater of the two versions. null if unsupported version schemes
     */
    String compareVersions(String first, String second);

  }

  /**
   * Represents a result for an update query performed by {@link UpdateChecker#requestUpdateCheck()}.
   */
  public final class UpdateResult {

    private final UpdateReason reason;
    private final String newestVersion;

    { // An actual use for initializer blocks. This is madness!
      UpdateChecker.this.lastResult = this;
    }

    private UpdateResult(UpdateReason reason, String newestVersion) {
      this.reason = reason;
      this.newestVersion = newestVersion;
    }

    private UpdateResult(UpdateReason reason) {
      Preconditions.checkArgument(reason != UpdateReason.NEW_UPDATE, "Reasons that require updates must also provide the latest version String");
      this.reason = reason;
      this.newestVersion = plugin.getDescription().getVersion();
    }

    /**
     * Get the constant reason of this result.
     *
     * @return the reason
     */
    public UpdateReason getReason() {
      return reason;
    }

    /**
     * Check whether or not this result requires the user to update.
     *
     * @return true if requires update, false otherwise
     */
    public boolean requiresUpdate() {
      return reason == UpdateReason.NEW_UPDATE;
    }

    /**
     * Get the latest version of the plugin. This may be the currently installed version, it
     * may not be. This depends entirely on the result of the update.
     *
     * @return the newest version of the plugin
     */
    public String getNewestVersion() {
      return newestVersion;
    }

  }

}
