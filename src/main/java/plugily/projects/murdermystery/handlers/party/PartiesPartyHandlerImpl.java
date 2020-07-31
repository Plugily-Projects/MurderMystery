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

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.alessiodp.parties.api.interfaces.Party;
import com.alessiodp.parties.api.interfaces.PartyPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

/**
 * @author Plajer
 * <p>
 * Created at 09.02.2020
 */
public class PartiesPartyHandlerImpl implements PartyHandler {

  @Override
  public boolean isPlayerInParty(Player player) {
    PartiesAPI api = Parties.getApi();
    PartyPlayer partyPlayer = api.getPartyPlayer(player.getUniqueId());
    if (partyPlayer == null) return false;
    Party party = api.getParty(partyPlayer.getPartyName());
    return party != null && party.getMembers().size() > 1;
  }

  @Override
  public GameParty getParty(Player player) {
    PartiesAPI api = Parties.getApi();
    PartyPlayer partyPlayer = api.getPartyPlayer(player.getUniqueId());
    Party party = api.getParty(partyPlayer.getPartyName());
    return new GameParty(party.getOnlineMembers(true).stream().map(localPlayer -> Bukkit.getPlayer(localPlayer.getPlayerUUID())).collect(Collectors.toList()), Bukkit.getPlayer(party.getLeader()));
  }

  @Override
  public boolean partiesSupported() {
    return true;
  }

  @Override
  public PartyPluginType getPartyPluginType() {
    return PartyPluginType.PARTIES;
  }
}
