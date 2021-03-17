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
import org.bukkit.scheduler.BukkitRunnable;
import plugily.projects.murdermystery.utils.services.ServiceRegistry;

import java.util.logging.Level;

/**
 * Create reported exception with data sent to plugily.xyz reporter service
 */
public class ReportedException {

  private ReporterService reporterService;

  public ReportedException(JavaPlugin plugin, Exception e) {
    Exception exception = e.getCause() != null ? (Exception) e.getCause() : e;
    StringBuilder stacktrace = new StringBuilder(exception.getClass().getSimpleName());
    if(exception.getMessage() != null) {
      stacktrace.append(" (").append(exception.getMessage()).append(")");
    }
    stacktrace.append("\n");
    for(StackTraceElement str : exception.getStackTrace()) {
      stacktrace.append(str.toString()).append("\n");
    }

    plugin.getLogger().log(Level.WARNING, "[Reporter service] <<-----------------------------[START]----------------------------->>");
    plugin.getLogger().log(Level.WARNING, stacktrace.toString());
    plugin.getLogger().log(Level.WARNING, "[Reporter service] <<------------------------------[END]------------------------------>>");

    if(!ServiceRegistry.isServiceEnabled() || System.currentTimeMillis() - ServiceRegistry.getServiceCooldown() < 900000) {
      return;
    }
    ServiceRegistry.setServiceCooldown(System.currentTimeMillis());
    new BukkitRunnable() {
      @Override
      public void run() {
        reporterService = new ReporterService(plugin, plugin.getName(), plugin.getDescription().getVersion(), plugin.getServer().getBukkitVersion() + " " + plugin.getServer().getVersion(),
          stacktrace.toString());
        reporterService.reportException();
      }
    }.runTaskAsynchronously(plugin);
  }
}
