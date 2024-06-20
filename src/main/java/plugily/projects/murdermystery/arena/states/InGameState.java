/*
 * MurderMystery - Find the murderer, kill him and survive!
 * Copyright (c) 2022  Plugily Projects - maintained by Tigerpanzer_02 and contributors
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

package plugily.projects.murdermystery.arena.states;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import plugily.projects.minigamesbox.api.user.IUser;
import plugily.projects.minigamesbox.classic.arena.PluginArena;
import plugily.projects.minigamesbox.classic.arena.states.PluginInGameState;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.handlers.language.TitleBuilder;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XSound;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.arena.ArenaUtils;
import plugily.projects.murdermystery.arena.role.Role;
import plugily.projects.murdermystery.utils.ItemPosition;

/**
 * @author Plajer
 * <p>Created at 03.06.2019
 */
public class InGameState extends PluginInGameState {

  @Override
  public void handleCall(PluginArena arena) {
    super.handleCall(arena);
    Arena pluginArena = (Arena) getPlugin().getArenaRegistry().getArena(arena.getId());
    if(pluginArena == null) {
      return;
    }

    // winner checks
    if(pluginArena.getTimer() <= 0) {
      getPlugin().getArenaManager().stopGame(false, pluginArena);
    }
    if(pluginArena.getPlayersLeft().size() == pluginArena.aliveMurderer()) {
      getPlugin().getArenaManager().stopGame(false, pluginArena);
    }

    distributeMurdererSword(pluginArena);
    //every 30 secs survive reward
    givePlayerSurviveReward(pluginArena);
    addInnocentLocator(pluginArena);
    if(pluginArena.getPlayersLeft().size() == pluginArena.aliveMurderer() + 1) {
      addMurdererSpeed(pluginArena);
    }
    spawnGold(pluginArena);
  }

  private void addMurdererSpeed(Arena pluginArena) {
    int multiplier = getPlugin().getConfig().getInt("Murderer.Speed", 3);
    if(multiplier > 1 && multiplier <= 10) {
      for(Player player : pluginArena.getMurdererList()) {
        if(pluginArena.isMurderAlive(player)) {
          // no potion because it adds particles which can be identified
          player.setWalkSpeed(0.1f * multiplier);
        }
      }
    }
  }

  private void distributeMurdererSword(Arena pluginArena) {
    int inGameLength = getPlugin().getConfig().getInt("Time-Manager.In-Game", 270);
    if(pluginArena.getTimer() <= (inGameLength - 10) && pluginArena.getTimer() > (inGameLength - 15)) {
      new MessageBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_SWORD_SOON").asKey().integer(pluginArena.getTimer() - (inGameLength - 15)).arena(pluginArena).sendArena();
      for(Player p : pluginArena.getPlayers()) {
        XSound.UI_BUTTON_CLICK.play(p.getLocation(), 1, 1);
      }

      if(pluginArena.getTimer() == (inGameLength - 14)) {
        if(pluginArena.getMurdererList().isEmpty()) getPlugin().getArenaManager().stopGame(false, pluginArena);

        for(Player p : pluginArena.getMurdererList()) {
          IUser murderer = getPlugin().getUserManager().getUser(p);

          if(murderer.isSpectator() || !p.isOnline())
            continue;

          p.getInventory().setHeldItemSlot(0);
          ItemPosition.setItem(murderer, ItemPosition.MURDERER_SWORD, pluginArena.getPlugin().getSwordSkinManager().getRandomSwordSkin(p));
        }
      }
    }
  }

  private void spawnGold(Arena pluginArena) {
    //don't spawn it every time
    if(pluginArena.getSpawnGoldTimer() == pluginArena.getSpawnGoldTime()) {
      spawnSomeGold(pluginArena);
      pluginArena.setSpawnGoldTimer(0);
    } else {
      pluginArena.setSpawnGoldTimer(pluginArena.getSpawnGoldTimer() + 1);
    }
  }

  private void givePlayerSurviveReward(Arena pluginArena) {
    if(pluginArena.getTimer() % 30 == 0) {
      new TitleBuilder("IN_GAME_MESSAGES_ARENA_PLAYING_TIME_LEFT").arena(pluginArena).sendArena();
      for(Player p : pluginArena.getPlayersLeft()) {
        IUser user = getPlugin().getUserManager().getUser(p);
        if(Role.isRole(Role.INNOCENT, user, pluginArena)) {
          ArenaUtils.addScore(user, ArenaUtils.ScoreAction.SURVIVE_TIME, 0);
        }
      }
    }
  }

  private void addInnocentLocator(Arena pluginArena) {
    if(pluginArena.getTimer() <= 30 || pluginArena.getPlayersLeft().size() == pluginArena.aliveMurderer() + 1) {
      if(getPlugin().getConfigPreferences().getOption("MURDERER_LOCATOR")) {
        ArenaUtils.updateInnocentLocator(pluginArena);
      }
    }
  }

  private void spawnSomeGold(Arena arena) {
    int spawnPointsSize = arena.getGoldSpawnPoints().size();

    if(spawnPointsSize == 0) {
      return;
    }
    //may users want to disable it and want much gold on their map xD
    if(!getPlugin().getConfigPreferences().getOption("GOLD_LIMITER")) {
      //do not exceed amount of gold per spawn
      if(arena.getGoldSpawned().size() >= spawnPointsSize) {
        return;
      }
    }
    if(getPlugin().getConfigPreferences().getOption("GOLD_SPAWNER_MODE_ALL")) {
      for(Location location : arena.getGoldSpawnPoints()) {
        dropGold(arena, location);
      }
    } else {
      Location loc = arena.getGoldSpawnPoints().get(getPlugin().getRandom().nextInt(spawnPointsSize));
      dropGold(arena, loc);
    }
  }

  private void dropGold(Arena arena, Location location) {
    arena.getGoldSpawned().add(location.getWorld().dropItem(location, new ItemStack(Material.GOLD_INGOT, 1)));
    getPlugin().getPowerupRegistry().spawnPowerup(location, arena);
  }
}
