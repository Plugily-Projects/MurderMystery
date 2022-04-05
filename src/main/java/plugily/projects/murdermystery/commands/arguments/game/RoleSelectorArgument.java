package plugily.projects.murdermystery.commands.arguments.game;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
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

import java.util.stream.Collectors;

public class RoleSelectorArgument implements Listener {

  public RoleSelectorArgument(ArgumentsRegistry registry) {
    registry.mapArgument("murdermystery", new LabeledCommandArgument("roleselector", "murdermystery.roleselector", CommandArgument.ExecutorType.PLAYER,
        new LabelData("/mm roleselector", "/mm roleselector", "&7Select a role\n&6Permission: &7murdermystery.roleselector")) {
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

    gui.addItem(new SimpleClickableItem(new ItemBuilder(XMaterial.IRON_SWORD.parseMaterial())
        .name(new MessageBuilder("IN_GAME_MESSAGES_ARENA_PASS_ROLE_MURDERER_NAME").asKey().build())
        .lore(plugin.getLanguageManager().getLanguageListFromKey("IN_GAME_MESSAGES_ARENA_PASS_ROLE_MURDERER_LORE").stream().map(string -> string.replace("%amount%", plugin.getUserManager().getUser(player).getStatistic("DETECTIVE_PASS") + "")).collect(Collectors.toList()))
        .build(), event -> {
      User user = plugin.getUserManager().getUser(player);
      if(user.getStatistic("MURDERER_PASS") <= 0) {
        new MessageBuilder("IN_GAME_MESSAGES_ARENA_PASS_FAIL").asKey().player(player).value(Role.MURDERER.name()).sendPlayer();
        return;
      }
      user.adjustStatistic("MURDERER_PASS", -1);
      user.adjustStatistic("CONTRIBUTION_MURDERER", 999);
      new MessageBuilder("IN_GAME_MESSAGES_ARENA_PASS_SUCCESS").asKey().player(player).value(Role.MURDERER.name()).sendPlayer();
    }));

    gui.addItem(new SimpleClickableItem(new ItemBuilder(XMaterial.BOW.parseMaterial())
        .name(new MessageBuilder("IN_GAME_MESSAGES_ARENA_PASS_ROLE_DETECTIVE_NAME").asKey().build())
        .lore(plugin.getLanguageManager().getLanguageListFromKey("IN_GAME_MESSAGES_ARENA_PASS_ROLE_DETECTIVE_LORE").stream().map(string -> string.replace("%amount%", plugin.getUserManager().getUser(player).getStatistic("DETECTIVE_PASS") + "")).collect(Collectors.toList()))
        .build(), event -> {
      User user = plugin.getUserManager().getUser(player);
      if(user.getStatistic("DETECTIVE_PASS") <= 0) {
        new MessageBuilder("IN_GAME_MESSAGES_ARENA_PASS_FAIL").asKey().player(player).value(Role.DETECTIVE.name()).sendPlayer();
        return;
      }
      user.adjustStatistic("DETECTIVE_PASS", -1);
      user.adjustStatistic("CONTRIBUTION_DETECTIVE", 999);
      new MessageBuilder("IN_GAME_MESSAGES_ARENA_PASS_SUCCESS").asKey().player(player).value(Role.DETECTIVE.name()).sendPlayer();
    }));

    gui.open(player);
  }

}
