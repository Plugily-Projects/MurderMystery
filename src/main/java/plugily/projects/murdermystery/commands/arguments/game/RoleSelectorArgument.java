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

package plugily.projects.murdermystery.commands.arguments.game;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import plugily.projects.minigamesbox.api.user.IUser;
import plugily.projects.minigamesbox.classic.PluginMain;
import plugily.projects.minigamesbox.classic.commands.arguments.data.CommandArgument;
import plugily.projects.minigamesbox.classic.commands.arguments.data.LabelData;
import plugily.projects.minigamesbox.classic.commands.arguments.data.LabeledCommandArgument;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.user.User;
import plugily.projects.minigamesbox.classic.utils.helper.ItemBuilder;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XMaterial;
import plugily.projects.minigamesbox.inventory.common.item.SimpleClickableItem;
import plugily.projects.minigamesbox.inventory.normal.NormalFastInv;
import plugily.projects.murdermystery.arena.role.Role;
import plugily.projects.murdermystery.commands.arguments.ArgumentsRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RoleSelectorArgument implements Listener {

  public RoleSelectorArgument(ArgumentsRegistry registry) {
    registry.mapArgument("murdermystery", new LabeledCommandArgument("roleselector", "murdermystery.command.roleselector", CommandArgument.ExecutorType.PLAYER,
      new LabelData("/mm roleselector", "/mm roleselector", "&7Select a role\n&6Permission: &7murdermystery.command.roleselector")) {
      @Override
      public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if(registry.getPlugin().getBukkitHelper().checkIsInGameInstance(player)) {
          openRolePassMenu(player, registry.getPlugin());
        }
      }
    });
  }

  public static void openRolePassMenu(Player player, PluginMain plugin) {
    NormalFastInv gui = new NormalFastInv(plugin.getBukkitHelper().serializeInt(Role.values().length), new MessageBuilder("IN_GAME_MESSAGES_ARENA_PASS_NAME").asKey().build());
    List<String> descriptionMurderer = new ArrayList<>();
    plugin.getLanguageManager().getLanguageListFromKey("IN_GAME_MESSAGES_ARENA_PASS_ROLE_MURDERER_LORE").forEach(string -> descriptionMurderer.add(new MessageBuilder(string).integer(plugin.getUserManager().getUser(player).getStatistic("PASS_MURDERER")).build()));
    gui.addItem(new SimpleClickableItem(new ItemBuilder(XMaterial.IRON_SWORD.parseMaterial())
      .name(new MessageBuilder("IN_GAME_MESSAGES_ARENA_PASS_ROLE_MURDERER_NAME").asKey().build())
      .lore(descriptionMurderer)
      .build(), event -> {
      IUser user = plugin.getUserManager().getUser(player);
      if(user.getStatistic("PASS_MURDERER") <= 0) {
        new MessageBuilder("IN_GAME_MESSAGES_ARENA_PASS_FAIL").asKey().player(player).value(Role.MURDERER.name()).sendPlayer();
        return;
      }
      user.adjustStatistic("PASS_MURDERER", -1);
      user.adjustStatistic("CONTRIBUTION_MURDERER", 999999999);
      new MessageBuilder("IN_GAME_MESSAGES_ARENA_PASS_SUCCESS").asKey().player(player).value(Role.MURDERER.name()).sendPlayer();
    }));
    List<String> descriptionDetective = new ArrayList<>();
    plugin.getLanguageManager().getLanguageListFromKey("IN_GAME_MESSAGES_ARENA_PASS_ROLE_DETECTIVE_LORE").forEach(string -> descriptionDetective.add(new MessageBuilder(string).integer(plugin.getUserManager().getUser(player).getStatistic("PASS_DETECTIVE")).build()));
    gui.addItem(new SimpleClickableItem(new ItemBuilder(XMaterial.BOW.parseMaterial())
      .name(new MessageBuilder("IN_GAME_MESSAGES_ARENA_PASS_ROLE_DETECTIVE_NAME").asKey().build())
      .lore(descriptionDetective)
      .build(), event -> {
      IUser user = plugin.getUserManager().getUser(player);
      if(user.getStatistic("PASS_DETECTIVE") <= 0) {
        new MessageBuilder("IN_GAME_MESSAGES_ARENA_PASS_FAIL").asKey().player(player).value(Role.DETECTIVE.name()).sendPlayer();
        return;
      }
      user.adjustStatistic("PASS_DETECTIVE", -1);
      user.adjustStatistic("CONTRIBUTION_DETECTIVE", 999999999);
      new MessageBuilder("IN_GAME_MESSAGES_ARENA_PASS_SUCCESS").asKey().player(player).value(Role.DETECTIVE.name()).sendPlayer();
    }));
    gui.refresh();
    gui.open(player);
  }

}
