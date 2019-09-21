/*
 * MurderMystery - Find the murderer, kill him and survive!
 * Copyright (C) 2019  Plajer's Lair - maintained by Tigerpanzer_02, Plajer and contributors
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

package pl.plajer.murdermystery.utils;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.utils.services.exception.ReportedException;

/**
 * @author Plajer
 * <p>
 * Created at 24.03.2019
 */
public class ExceptionLogHandler extends Handler {

  //these classes if found in stacktraces won't be reported
  //to the Error Service
  private List<String> blacklistedClasses = Arrays.asList("pl.plajer.murdermystery.user.data.MysqlManager", "pl.plajer.murdermystery.plajerlair.commonsbox.database.MysqlDatabase");

  public ExceptionLogHandler() {
    Bukkit.getLogger().addHandler(this);
  }

  @Override
  public void close() throws SecurityException {
  }

  @Override
  public void flush() {
  }

  @Override
  public void publish(LogRecord record) {
    Throwable throwable = record.getThrown();
    if (!(throwable instanceof Exception) || !throwable.getClass().getSimpleName().contains("Exception")) {
      return;
    }
    if (throwable.getStackTrace().length == 0
      || !throwable.getStackTrace()[0].getClassName().contains("pl.plajer.murdermystery")) {
      return;
    }
    if (containsBlacklistedClass(throwable)) {
      return;
    }
    new ReportedException(JavaPlugin.getPlugin(Main.class), (Exception) throwable);
  }

  private boolean containsBlacklistedClass(Throwable throwable) {
    for (StackTraceElement element : throwable.getStackTrace()) {
      for (String blacklist : blacklistedClasses) {
        if (element.getClassName().contains(blacklist)) {
          return true;
        }
      }
    }
    return false;
  }

}
