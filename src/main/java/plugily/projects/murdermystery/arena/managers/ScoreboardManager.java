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

package plugily.projects.murdermystery.arena.managers;

import me.clip.placeholderapi.PlaceholderAPI;
import me.tigerhix.lib.scoreboard.ScoreboardLib;
import me.tigerhix.lib.scoreboard.common.EntryBuilder;
import me.tigerhix.lib.scoreboard.type.Entry;
import me.tigerhix.lib.scoreboard.type.Scoreboard;
import me.tigerhix.lib.scoreboard.type.ScoreboardHandler;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import plugily.projects.commonsbox.string.StringFormatUtils;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.api.StatsStorage;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.arena.ArenaState;
import plugily.projects.murdermystery.arena.role.Role;
import plugily.projects.murdermystery.handlers.ChatManager;
import plugily.projects.murdermystery.handlers.language.LanguageManager;
import plugily.projects.murdermystery.user.User;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Plajer
 * <p>
 * Created at 24.03.2019
 */
public class ScoreboardManager {

  private static final Main plugin = JavaPlugin.getPlugin(Main.class);
  private final List<Scoreboard> scoreboards = new ArrayList<>();
  private final Arena arena;

  public ScoreboardManager(Arena arena) {
    this.arena = arena;
  }

  /**
   * Creates arena scoreboard for target user
   *
   * @param user user that represents game player
   * @see User
   */
  public void createScoreboard(User user) {
    Player player = user.getPlayer();
    final String boardTitle = plugin.getChatManager().colorMessage("Scoreboard.Title");
    Scoreboard scoreboard = ScoreboardLib.createScoreboard(player).setHandler(new ScoreboardHandler() {
      @Override
      public String getTitle(Player player) {
        return boardTitle;
      }

      @Override
      public List<Entry> getEntries(Player player) {
        return formatScoreboard(user);
      }
    });
    scoreboard.activate();
    scoreboards.add(scoreboard);
  }

  /**
   * Removes scoreboard of user
   *
   * @param user user that represents game player
   * @see User
   */
  public void removeScoreboard(User user) {
    for(Scoreboard board : scoreboards) {
      if(board.getHolder().equals(user.getPlayer())) {
        scoreboards.remove(board);
        board.deactivate();
        return;
      }
    }
  }

  /**
   * Forces all scoreboards to deactivate.
   */
  public void stopAllScoreboards() {
    scoreboards.forEach(Scoreboard::deactivate);
    scoreboards.clear();
  }

  private List<Entry> formatScoreboard(User user) {
    EntryBuilder builder = new EntryBuilder();
    List<String> lines;
    if(arena.getArenaState() == ArenaState.IN_GAME) {
      lines = LanguageManager.getLanguageList("Scoreboard.Content.Playing");
      if(Role.isRole(Role.MURDERER, user.getPlayer())) {
        lines = LanguageManager.getLanguageList("Scoreboard.Content.Playing-Murderer");
      }
    } else {
      //apply fix
      if(arena.getArenaState() == ArenaState.ENDING) {
        lines = LanguageManager.getLanguageList("Scoreboard.Content.Playing");
        if(Role.isRole(Role.MURDERER, user.getPlayer())) {
          lines = LanguageManager.getLanguageList("Scoreboard.Content.Playing-Murderer");
        }
      } else {
        lines = LanguageManager.getLanguageList("Scoreboard.Content." + arena.getArenaState().getFormattedName());
      }
    }
    for(String line : lines) {
      builder.next(formatScoreboardLine(line, user));
    }
    return builder.build();
  }

  private String formatScoreboardLine(String line, User user) {
    String formattedLine = line;

    int timer = arena.getTimer();

    formattedLine = StringUtils.replace(formattedLine, "%TIME%", Integer.toString(timer + 1));
    formattedLine = StringUtils.replace(formattedLine, "%FORMATTED_TIME%", StringFormatUtils.formatIntoMMSS(timer + 1));
    formattedLine = StringUtils.replace(formattedLine, "%MAPNAME%", arena.getMapName());

    boolean havePlayer = false;
    int innocents = 0;

    Player player = user.getPlayer();

    for(Player p : arena.getPlayersLeft()) {
      if (p == player) {
        havePlayer = true;
      }

      if(!Role.isRole(Role.MURDERER, p)) {
        innocents++;
      }
    }

    ChatManager chatManager = plugin.getChatManager();

    if(!havePlayer) {
      formattedLine = StringUtils.replace(formattedLine, "%ROLE%", chatManager.colorMessage("Scoreboard.Roles.Dead"));
    } else {
      if(Role.isRole(Role.MURDERER, player)) {
        formattedLine = StringUtils.replace(formattedLine, "%ROLE%", chatManager.colorMessage("Scoreboard.Roles.Murderer"));
      } else if(Role.isRole(Role.ANY_DETECTIVE, player)) {
        formattedLine = StringUtils.replace(formattedLine, "%ROLE%", chatManager.colorMessage("Scoreboard.Roles.Detective"));
      } else {
        formattedLine = StringUtils.replace(formattedLine, "%ROLE%", chatManager.colorMessage("Scoreboard.Roles.Innocent"));
      }
    }
    formattedLine = StringUtils.replace(formattedLine, "%INNOCENTS%", Integer.toString(innocents));
    formattedLine = StringUtils.replace(formattedLine, "%PLAYERS%", Integer.toString(arena.getPlayers().size()));
    formattedLine = StringUtils.replace(formattedLine, "%MAX_PLAYERS%", Integer.toString(arena.getMaximumPlayers()));
    formattedLine = StringUtils.replace(formattedLine, "%MIN_PLAYERS%", Integer.toString(arena.getMinimumPlayers()));

    if (!arena.isHideChances()) {
      int totalMurderer = 0;
      int totalDetective = 0;

      for(Player p : arena.getPlayers()) {
        User us = plugin.getUserManager().getUser(p);
        totalMurderer += us.getStat(StatsStorage.StatisticType.CONTRIBUTION_MURDERER);
        totalDetective += us.getStat(StatsStorage.StatisticType.CONTRIBUTION_DETECTIVE);
      }

      formattedLine = arena.formatRoleChance(formattedLine, user, totalMurderer, totalDetective);
    }

    if (arena.isDetectiveDead()) {
      if(!arena.isCharacterSet(Arena.CharacterType.FAKE_DETECTIVE)) {
        formattedLine = StringUtils.replace(formattedLine, "%DETECTIVE_STATUS%", chatManager.colorMessage("Scoreboard.Detective-Died-No-Bow"));
      } else {
        formattedLine = StringUtils.replace(formattedLine, "%DETECTIVE_STATUS%", chatManager.colorMessage("Scoreboard.Detective-Died-Bow"));
      }
    } else {
      formattedLine = StringUtils.replace(formattedLine, "%DETECTIVE_STATUS%", chatManager.colorMessage("Scoreboard.Detective-Status-Normal"));
    }

    //should be for murderer only
    formattedLine = StringUtils.replace(formattedLine, "%KILLS%", Integer.toString(user.getStat(StatsStorage.StatisticType.LOCAL_KILLS)));
    formattedLine = StringUtils.replace(formattedLine, "%SCORE%", Integer.toString(user.getStat(StatsStorage.StatisticType.LOCAL_SCORE)));

    if(plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
      formattedLine = PlaceholderAPI.setPlaceholders(player, formattedLine);
    }

    return chatManager.colorRawMessage(formattedLine);
  }

}
