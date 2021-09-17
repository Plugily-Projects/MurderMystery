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

import de.simonsator.partyandfriends.spigot.api.pafplayers.PAFPlayer;
import de.simonsator.partyandfriends.spigot.api.pafplayers.PAFPlayerManager;
import de.simonsator.partyandfriends.spigot.api.party.PartyManager;
import de.simonsator.partyandfriends.spigot.api.party.PlayerParty;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @author Plajer
 * <p>
 * Created at 09.02.2020
 */
public class PAFBPartyHandlerImpl implements PartyHandler {

  @Override
  public GameParty getParty(Player player) {
    PAFPlayer partyPlayer = PAFPlayerManager.getInstance().getPlayer(player.getUniqueId());
    if (partyPlayer == null)
      return null;

    PlayerParty party = PartyManager.getInstance().getParty(partyPlayer);
    if (party == null)
      return null;

    Player leader = Bukkit.getPlayer(party.getLeader().getUniqueId());
    if (leader == null)
      return null;

    java.util.List<Player> allMembers = party.getAllPlayers().stream()
        .map(localPlayer -> Bukkit.getPlayer(localPlayer.getUniqueId()))
        .filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toList());

    return new GameParty(allMembers, leader);
  }

  @Override
  public boolean partiesSupported() {
    return true;
  }

  @Override
  public PartyPluginType getPartyPluginType() {
    return PartyPluginType.PAFBungee;
  }
}
