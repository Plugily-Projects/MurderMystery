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

package pl.plajer.murdermystery.handlers.setup.components;

import com.github.stefvanschie.inventoryframework.pane.StaticPane;

import pl.plajer.murdermystery.handlers.setup.SetupInventory;

/**
 * @author Plajer
 * <p>
 * Created at 25.05.2019
 */
public interface SetupComponent {

  void prepare(SetupInventory setupInventory);

  void injectComponents(StaticPane pane);

}
