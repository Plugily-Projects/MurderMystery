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

package plugily.projects.murdermystery.utils.services.exception;

import org.bukkit.plugin.java.JavaPlugin;
import plugily.projects.murdermystery.utils.Debugger;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 * Reporter service for reporting exceptions directly to website reporter panel
 */
public class ReporterService {

  private final JavaPlugin plugin;
  private final String pluginName;
  private final String pluginVersion;
  private final String serverVersion;
  private final String error;

  //don't create it outside core
  ReporterService(JavaPlugin plugin, String pluginName, String pluginVersion, String serverVersion, String error) {
    this.plugin = plugin;
    this.pluginName = pluginName;
    this.pluginVersion = pluginVersion;
    this.serverVersion = serverVersion;
    this.error = error;
  }

  public void reportException() {
    try {
      URL url = new URL("https://api.plugily.xyz/error/report.php");
      HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setRequestProperty("User-Agent", "PLService/1.0");
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      conn.setDoOutput(true);

      OutputStream os = conn.getOutputStream();
      os.write(("pass=servicereporter&type=" + pluginName + "&pluginversion=" + pluginVersion + "&serverversion=" + serverVersion + "&error=" + error).getBytes(StandardCharsets.UTF_8));
      os.flush();
      os.close();

      plugin.getLogger().log(Level.WARNING, "[Reporter service] Error reported!");
      Debugger.debug(Level.INFO, "[Reporter service] Code: {0} ({1})", conn.getResponseCode(), conn.getResponseMessage());
    } catch (IOException ignored) {/*cannot connect or there is a problem*/
    }
  }

}
