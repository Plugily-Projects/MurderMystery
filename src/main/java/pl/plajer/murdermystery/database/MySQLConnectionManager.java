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

package pl.plajer.murdermystery.database;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

import java.sql.Connection;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import pl.plajer.murdermystery.utils.MessageUtils;
import pl.plajerlair.core.utils.ConfigUtils;

/**
 * @author Plajer
 * <p>
 * Created at 03.08.2018
 */
public class MySQLConnectionManager {

  private BoneCP connectionPool = null;
  private JavaPlugin plugin;

  public MySQLConnectionManager(JavaPlugin plugin) {
    this.plugin = plugin;
  }

  public void configureConnPool() {
    FileConfiguration databaseConfig = ConfigUtils.getConfig(plugin, "mysql");
    try {
      Class.forName("com.mysql.jdbc.Driver"); //also you need the MySQL driver
      plugin.getLogger().info("Creating BoneCP Configuration...");
      BoneCPConfig config = new BoneCPConfig();
      config.setJdbcUrl(databaseConfig.getString("address"));
      config.setUsername(databaseConfig.getString("user"));
      config.setPassword(databaseConfig.getString("password"));
      config.setMinConnectionsPerPartition(databaseConfig.getInt("min-connections")); //if you say 5 here, there will be 10 connection available
      config.setMaxConnectionsPerPartition(databaseConfig.getInt("max-connections"));
      config.setPartitionCount(2); //2*5 = 10 connection will be available
      //config.setLazyInit(true); //depends on the application usage you should chose lazy or not
      //setting Lazy true means BoneCP won't open any connections before you request a one from it.
      plugin.getLogger().info("Setting up MySQL Connection pool...");
      connectionPool = new BoneCP(config); // setup the connection pool
      plugin.getLogger().info("Connection pool successfully configured. ");
      plugin.getLogger().info("Total connections ==> " + connectionPool.getTotalCreatedConnections());
    } catch (Exception e) {
      e.printStackTrace();
      MessageUtils.errorOccured();
      Bukkit.getConsoleSender().sendMessage("Cannot save contents to MySQL database!");
      Bukkit.getConsoleSender().sendMessage("Check configuration of mysql.yml file or disable mysql option in config.yml");
    }
  }

  public void shutdownConnPool() {
    try {
      plugin.getLogger().info("Shutting down connection pool. Trying to close all connections.");
      if (connectionPool != null) {
        connectionPool.shutdown(); //this method must be called only once when the application stops.
        //you don't need to call it every time when you get a connection from the Connection Pool
        plugin.getLogger().info("Pool successfully shutdown. ");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Connection getConnection() {
    Connection conn = null;
    try {
      conn = getConnectionPool().getConnection();
      //will get a thread-safe connection from the BoneCP connection pool.
      //synchronization of the method will be done inside BoneCP source

    } catch (Exception e) {
      e.printStackTrace();
    }
    return conn;
  }

  public void closeConnection(Connection conn) {
    try {
      if (conn != null) {
        conn.close(); //release the connection - the name is tricky but connection is not closed it is released
        //and it will stay in pool
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public BoneCP getConnectionPool() {
    return connectionPool;
  }

}
