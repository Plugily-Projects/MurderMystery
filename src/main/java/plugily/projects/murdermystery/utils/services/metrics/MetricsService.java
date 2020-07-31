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

package plugily.projects.murdermystery.utils.services.metrics;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import plugily.projects.murdermystery.utils.services.ServiceRegistry;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

/**
 * Metrics service for sending usage data
 */
public class MetricsService {

  private JavaPlugin plugin;

  public MetricsService(JavaPlugin plugin) {
    if (ServiceRegistry.getRegisteredService() == null || !ServiceRegistry.getRegisteredService().equals(plugin)) {
      throw new IllegalArgumentException("MetricsService cannot be used without registering service via ServiceRegistry first!");
    }
    if (!ServiceRegistry.isServiceEnabled()) {
      return;
    }
    this.plugin = plugin;
    metricsSchedulerTask();
  }

  private void metricsSchedulerTask() {
    Timer timer = new Timer(true);
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        if (!plugin.isEnabled()) {
          timer.cancel();
          return;
        }
        Bukkit.getScheduler().runTask(plugin, () -> {
          try {
            final byte[] post = ("pass=metricsservice&type=" + plugin.getName() + "&pluginversion=" + plugin.getDescription().getVersion() +
              "&serverversion=" + plugin.getServer().getBukkitVersion() + "&ip=" + InetAddress.getLocalHost().getHostAddress() + ":" + plugin.getServer().getPort() +
              "&playersonline=" + Bukkit.getOnlinePlayers().size()).getBytes(StandardCharsets.UTF_8);
            new Thread(() -> {
              try {
                plugin.getLogger().log(Level.FINE, "Metrics data sent!");
                //todo /v2/
                URL url = new URL("https://api.plajer.xyz/metrics/receiver.php");
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("User-Agent", "PLMetrics/1.0");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(post);
                os.flush();
                os.close();
                StringBuilder content;

                try (BufferedReader in = new BufferedReader(
                  new InputStreamReader(conn.getInputStream()))) {

                  String line;
                  content = new StringBuilder();

                  while ((line = in.readLine()) != null) {
                    content.append(line);
                    content.append(System.lineSeparator());
                  }
                }

                plugin.getLogger().log(Level.FINE, "Metrics response: " + content.toString());
              } catch (IOException ignored) {
              }
            }).start();
          } catch (IOException ignored) {/*cannot connect or there is a problem*/}
        });
      }
    }, 1000 * 60 * 5, 1000 * 60 * 30);
  }

}
