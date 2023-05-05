
package plugily.projects.murdermystery.boot;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import plugily.projects.minigamesbox.classic.arena.PluginArena;
import plugily.projects.minigamesbox.number.NumberUtils;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.handlers.placeholder.Placeholder;
import plugily.projects.minigamesbox.classic.handlers.placeholder.PlaceholderManager;
import plugily.projects.minigamesbox.classic.user.User;
import plugily.projects.minigamesbox.classic.user.UserManager;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.arena.ArenaRegistry;
import plugily.projects.murdermystery.arena.role.Role;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 15.10.2022
 */
public class PlaceholderInitializer {

  private final Main plugin;

  public PlaceholderInitializer(Main plugin) {
    this.plugin = plugin;
    registerPlaceholders();
  }

  private void registerPlaceholders() {
    getPlaceholderManager()
      .registerPlaceholder(
        new Placeholder(
          "detective_list",
          Placeholder.PlaceholderType.ARENA,
          Placeholder.PlaceholderExecutor.ALL) {
          @Override
          public String getValue(Player player, PluginArena arena) {
            Arena pluginArena = getArenaRegistry().getArena(arena.getId());
            if(pluginArena == null) {
              return null;
            }

            StringBuilder detectives = new StringBuilder();
            for(Player p : pluginArena.getDetectiveList()) {
              detectives.append(p.getName()).append(", ");
            }

            int index = detectives.length() - 2;
            if(index > 0 && index < detectives.length()) {
              detectives.deleteCharAt(index);
            }

            return (pluginArena.isDetectiveDead() ? ChatColor.STRIKETHROUGH : "")
              + detectives.toString();
          }
        });

    getPlaceholderManager()
      .registerPlaceholder(
        new Placeholder(
          "murderer_list",
          Placeholder.PlaceholderType.ARENA,
          Placeholder.PlaceholderExecutor.ALL) {
          @Override
          public String getValue(Player player, PluginArena arena) {
            Arena pluginArena = getArenaRegistry().getArena(arena.getId());
            if(pluginArena == null) {
              return null;
            }

            StringBuilder murders = new StringBuilder();
            for(Player p : pluginArena.getMurdererList()) {
              User user = getUserManager().getUser(p);
              int localKills = user.getStatistic("LOCAL_KILLS");
              murders.append(p.getName());
              if(pluginArena.getMurdererList().size() > 1) {
                murders.append(" (").append(localKills).append("), ");
              }
            }
            if(pluginArena.getMurdererList().size() > 1) {
              murders.deleteCharAt(murders.length() - 2);
            }

            return (pluginArena.aliveMurderer() == 1 ? "" : ChatColor.STRIKETHROUGH)
              + murders.toString();
          }
        });

    getPlaceholderManager()
      .registerPlaceholder(
        new Placeholder(
          "murderer_kills",
          Placeholder.PlaceholderType.ARENA,
          Placeholder.PlaceholderExecutor.ALL) {
          @Override
          public String getValue(Player player, PluginArena arena) {
            Arena pluginArena = getArenaRegistry().getArena(arena.getId());
            if(pluginArena == null) {
              return null;
            }
            int murdererKills = 0;
            for(Player p : pluginArena.getMurdererList()) {
              User user = getUserManager().getUser(p);
              int localKills = user.getStatistic("LOCAL_KILLS");
              murdererKills += localKills;
            }
            return Integer.toString(murdererKills);
          }
        });

    getPlaceholderManager()
      .registerPlaceholder(
        new Placeholder(
          "hero", Placeholder.PlaceholderType.ARENA, Placeholder.PlaceholderExecutor.ALL) {
          @Override
          public String getValue(Player player, PluginArena arena) {
            Arena pluginArena = getArenaRegistry().getArena(arena.getId());
            if(pluginArena == null) {
              return null;
            }
            Player hero = pluginArena.getCharacter(Arena.CharacterType.HERO);
            return hero != null
              ? hero.getName()
              : new MessageBuilder("IN_GAME_MESSAGES_GAME_END_PLACEHOLDERS_NOBODY")
              .asKey()
              .build();
          }
        });

    getPlaceholderManager().registerPlaceholder(new Placeholder("murderer_chance", Placeholder.PlaceholderType.ARENA, Placeholder.PlaceholderExecutor.ALL) {
      @Override
      public String getValue(Player player, PluginArena arena) {
        Arena pluginArena = getArenaRegistry().getArena(arena.getId());
        if(pluginArena == null) {
          return null;
        }

        User user = getUserManager().getUser(player);
        return NumberUtils.round(((double) pluginArena.getContributorValue(Role.MURDERER, user) / (double) pluginArena.getTotalRoleChances(Role.MURDERER)) * 100.0, 2) + "%";
      }
    });

    getPlaceholderManager().registerPlaceholder(new Placeholder("detective_chance", Placeholder.PlaceholderType.ARENA, Placeholder.PlaceholderExecutor.ALL) {
      @Override
      public String getValue(Player player, PluginArena arena) {
        Arena pluginArena = getArenaRegistry().getArena(arena.getId());
        if(pluginArena == null) {
          return null;
        }

        User user = getUserManager().getUser(player);
        return NumberUtils.round(((double) pluginArena.getContributorValue(Role.DETECTIVE, user) / (double) pluginArena.getTotalRoleChances(Role.DETECTIVE)) * 100.0, 2) + "%";
      }
    });

    getPlaceholderManager()
      .registerPlaceholder(
        new Placeholder(
          "detective_status",
          Placeholder.PlaceholderType.ARENA,
          Placeholder.PlaceholderExecutor.ALL) {
          @Override
          public String getValue(Player player, PluginArena arena) {
            Arena pluginArena = getArenaRegistry().getArena(arena.getId());
            if(pluginArena == null) {
              return null;
            }

            if(pluginArena.isDetectiveDead()) {
              if(!pluginArena.isCharacterSet(Arena.CharacterType.FAKE_DETECTIVE)) {
                return new MessageBuilder("SCOREBOARD_DETECTIVE_BOW_DROPPED").asKey().build();
              } else {
                return new MessageBuilder("SCOREBOARD_DETECTIVE_BOW_PICKED").asKey().build();
              }
            } else {
              return new MessageBuilder("SCOREBOARD_DETECTIVE_ALIVE").asKey().build();
            }
          }
        });

    getPlaceholderManager()
      .registerPlaceholder(
        new Placeholder(
          "innocent_size",
          Placeholder.PlaceholderType.ARENA,
          Placeholder.PlaceholderExecutor.ALL) {
          @Override
          public String getValue(Player player, PluginArena arena) {
            Arena pluginArena = getArenaRegistry().getArena(arena.getId());
            if(pluginArena == null) {
              return null;
            }
            int innocents = 0;
            for(Player p : arena.getPlayersLeft()) {
              if(!Role.isRole(Role.MURDERER, getUserManager().getUser(p))) {
                innocents++;
              }
            }
            return Integer.toString(innocents);
          }
        });

    getPlaceholderManager()
      .registerPlaceholder(
        new Placeholder(
          "player_role", Placeholder.PlaceholderType.ARENA, Placeholder.PlaceholderExecutor.ALL) {
          @Override
          public String getValue(Player player, PluginArena arena) {
            Arena pluginArena = getArenaRegistry().getArena(arena.getId());
            if(pluginArena == null) {
              return null;
            }
            User user = getUserManager().getUser(player);
            String role;
            if(pluginArena.isDeathPlayer(player)) {
              role = new MessageBuilder("SCOREBOARD_ROLES_DEAD").asKey().build();
            } else if(Role.isRole(Role.MURDERER, user, arena)) {
              role = new MessageBuilder("SCOREBOARD_ROLES_MURDERER").asKey().build();
            } else if(Role.isRole(Role.ANY_DETECTIVE, user, arena)) {
              role = new MessageBuilder("SCOREBOARD_ROLES_DETECTIVE").asKey().build();
            } else {
              role = new MessageBuilder("SCOREBOARD_ROLES_INNOCENT").asKey().build();
            }
            return role;
          }
        });

    getPlaceholderManager()
      .registerPlaceholder(
        new Placeholder(
          "summary_player",
          Placeholder.PlaceholderType.ARENA,
          Placeholder.PlaceholderExecutor.ALL) {
          @Override
          public String getValue(Player player, PluginArena arena) {
            return getSummary(player, arena);
          }

          @Nullable
          private String getSummary(Player player, PluginArena arena) {
            Arena pluginArena = getArenaRegistry().getArena(arena.getId());
            if(pluginArena == null) {
              return null;
            }
            String summaryEnding;

            if(pluginArena.getMurdererList().containsAll(pluginArena.getPlayersLeft())
              && pluginArena.getMurdererList().contains(player)) {
              summaryEnding =
                new MessageBuilder("IN_GAME_MESSAGES_GAME_END_PLACEHOLDERS_WIN")
                  .asKey()
                  .arena(pluginArena)
                  .build();
            } else if(!pluginArena.getMurdererList().containsAll(pluginArena.getPlayersLeft())
              && !pluginArena.getMurdererList().contains(player)) {
              summaryEnding =
                new MessageBuilder("IN_GAME_MESSAGES_GAME_END_PLACEHOLDERS_WIN")
                  .asKey()
                  .arena(pluginArena)
                  .build();
            } else {
              summaryEnding =
                new MessageBuilder("IN_GAME_MESSAGES_GAME_END_PLACEHOLDERS_LOSE")
                  .asKey()
                  .arena(pluginArena)
                  .build();
            }
            return summaryEnding;
          }
        });
    getPlaceholderManager()
      .registerPlaceholder(
        new Placeholder(
          "summary", Placeholder.PlaceholderType.ARENA, Placeholder.PlaceholderExecutor.ALL) {
          @Override
          public String getValue(Player player, PluginArena arena) {
            return getSummary(arena);
          }

          @Override
          public String getValue(PluginArena arena) {
            return getSummary(arena);
          }

          @Nullable
          private String getSummary(PluginArena arena) {
            Arena pluginArena = getArenaRegistry().getArena(arena.getId());
            if(pluginArena == null) {
              return null;
            }
            String summaryEnding;

            if(pluginArena.getMurdererList().containsAll(pluginArena.getPlayersLeft())) {
              summaryEnding =
                new MessageBuilder(
                  "IN_GAME_MESSAGES_GAME_END_PLACEHOLDERS_MURDERER_KILLED_ALL")
                  .asKey()
                  .arena(pluginArena)
                  .build();
            } else {
              summaryEnding =
                new MessageBuilder("IN_GAME_MESSAGES_GAME_END_PLACEHOLDERS_MURDERER_STOPPED")
                  .asKey()
                  .arena(pluginArena)
                  .build();
            }
            return summaryEnding;
          }
        });
  }

  private PlaceholderManager getPlaceholderManager() {
    return plugin.getPlaceholderManager();
  }

  private ArenaRegistry getArenaRegistry() {
    return plugin.getArenaRegistry();
  }

  private UserManager getUserManager() {
    return plugin.getUserManager();
  }

}
