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

package plugily.projects.murdermystery.commands.arguments;

import plugily.projects.minigamesbox.classic.commands.arguments.PluginArgumentsRegistry;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.commands.arguments.admin.RolePassArgument;
import plugily.projects.murdermystery.commands.arguments.admin.arena.SpecialBlockRemoverArgument;
import plugily.projects.murdermystery.commands.arguments.game.RoleSelectorArgument;
import plugily.projects.murdermystery.commands.arguments.game.SwordSkinsArgument;

/**
 * @author Plajer
 * <p>
 * Created at 11.01.2019
 */
public class ArgumentsRegistry extends PluginArgumentsRegistry {

  public ArgumentsRegistry(Main plugin) {
    super(plugin);
    //register basic arugments
    new RoleSelectorArgument(this);
    new SwordSkinsArgument(this);

    //register admin related arguments
    new SpecialBlockRemoverArgument(this);
    new RolePassArgument(this);
  }

}
