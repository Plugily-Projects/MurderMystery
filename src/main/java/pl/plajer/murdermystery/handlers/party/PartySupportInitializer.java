/*
 * MurderMystery - Find the murderer, kill him and survive!
 * Copyright (C) 2020  Plajer's Lair - maintained by Tigerpanzer_02, Plajer and contributors
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

package pl.plajer.murdermystery.handlers.party;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @author Plajer
 * <p>
 * Created at 09.02.2020
 */
public class PartySupportInitializer {

  public PartyHandler initialize() {
    PartyHandler partyHandler;
    if (Bukkit.getServer().getPluginManager().getPlugin("Parties") != null) {
      partyHandler = new PartiesPartyHandlerImpl();
    } else {
      partyHandler = new PartyHandler() {
        @Override
        public boolean isPlayerInParty(Player player) {
          return false;
        }

        @Override
        public GameParty getParty(Player player) {
          return null;
        }

        @Override
        public boolean partiesSupported() {
          return false;
        }

        @Override
        public PartyPluginType getPartyPluginType() {
          return PartyPluginType.NONE;
        }
      };
    }
    return partyHandler;
  }

}
