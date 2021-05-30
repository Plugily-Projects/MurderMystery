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

package plugily.projects.murdermystery.handlers.party;

import de.simonsator.partyandfriends.api.pafplayers.OnlinePAFPlayer;
import de.simonsator.partyandfriends.api.party.PartyManager;
import de.simonsator.partyandfriends.api.party.PlayerParty;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @author Plajer
 * <p>
 * Created at 09.02.2020
 */
public class PAFSPartyHandlerImpl implements PartyHandler {

  @Override
  public boolean isPlayerInParty(Player player) {
    return PartyManager.getInstance().getParty(player.getUniqueId()) != null;
  }

  @Override
  public GameParty getParty(Player player) {
    PartyManager api = PartyManager.getInstance();
    PlayerParty party = api.getParty(player.getUniqueId());

    java.util.List<Player> players = new java.util.ArrayList<>();

    for (OnlinePAFPlayer localPlayer : party.getAllPlayers()) {
      Player pl = Bukkit.getPlayer(localPlayer.getUniqueId());

      if (pl != null)
        players.add(pl);
    }

    return new GameParty(players, Bukkit.getPlayer(party.getLeader().getUniqueId()));
  }

  @Override
  public boolean partiesSupported() {
    return true;
  }

  @Override
  public PartyPluginType getPartyPluginType() {
    return PartyPluginType.PAFSpigot;
  }
}
