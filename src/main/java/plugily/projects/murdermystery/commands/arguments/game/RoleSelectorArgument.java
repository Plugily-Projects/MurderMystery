package plugily.projects.murdermystery.commands.arguments.game;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import plugily.projects.commonsbox.minecraft.compat.xseries.XMaterial;
import plugily.projects.commonsbox.minecraft.item.ItemBuilder;
import plugily.projects.inventoryframework.gui.GuiItem;
import plugily.projects.inventoryframework.gui.type.ChestGui;
import plugily.projects.inventoryframework.pane.OutlinePane;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.api.StatsStorage;
import plugily.projects.murdermystery.arena.role.Role;
import plugily.projects.murdermystery.commands.arguments.ArgumentsRegistry;
import plugily.projects.murdermystery.commands.arguments.data.CommandArgument;
import plugily.projects.murdermystery.commands.arguments.data.LabelData;
import plugily.projects.murdermystery.commands.arguments.data.LabeledCommandArgument;
import plugily.projects.murdermystery.handlers.ChatManager;
import plugily.projects.murdermystery.handlers.language.LanguageManager;
import plugily.projects.murdermystery.user.User;
import plugily.projects.murdermystery.utils.Utils;

public class RoleSelectorArgument implements Listener {

  private final ChatManager chatManager;

  public RoleSelectorArgument(ArgumentsRegistry registry, ChatManager chatManager) {
    this.chatManager = chatManager;
    registry.mapArgument("murdermystery", new LabeledCommandArgument("arenas", "murdermystery.roleselector", CommandArgument.ExecutorType.PLAYER,
        new LabelData("/mm roleselector", "/mm roleselector", "&7Select a role\n&6Permission: &7murdermystery.roleselector")) {
      @Override
      public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if(!Utils.checkIsInGameInstance(player)) {
          return;
        }
        openRolePassMenu((Player) sender, registry.getPlugin());
      }
    });
  }

  public static void openRolePassMenu(Player player, Main plugin) {
    int rows = Utils.serializeInt(Role.values().length) / 9;
    ChestGui gui = new ChestGui(rows, plugin.getChatManager().colorMessage("In-Game.Role-Pass.Menu-Name"));
    gui.setOnGlobalClick(event -> event.setCancelled(true));
    OutlinePane pane = new OutlinePane(9, rows);
    gui.addPane(pane);

    pane.addItem(new GuiItem(new ItemBuilder(XMaterial.IRON_SWORD.parseMaterial())
        .name(plugin.getChatManager().colorMessage("In-Game.Role-Pass.Role.Murderer.Name"))
        .lore(LanguageManager.getLanguageList("In-Game.Role-Pass.Role.Murderer.Lore"))
        .build(), event -> {
      event.setCancelled(true);
      User user = plugin.getUserManager().getUser(player);
      if(user.getStat(StatsStorage.StatisticType.MURDERER_PASS) <= 0) {
        player.sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("In-Game.Role-Pass.Fail").replace("%role%", Role.MURDERER.name()));
        return;
      }
      user.addStat(StatsStorage.StatisticType.MURDERER_PASS, -1);
      user.addStat(StatsStorage.StatisticType.CONTRIBUTION_MURDERER, 999);
      player.sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("In-Game.Role-Pass.Success").replace("%role%", Role.MURDERER.name()));
    }));

    pane.addItem(new GuiItem(new ItemBuilder(XMaterial.BOW.parseMaterial())
        .name(plugin.getChatManager().colorMessage("In-Game.Role-Pass.Role.Detective.Name"))
        .lore(LanguageManager.getLanguageList("In-Game.Role-Pass.Role.Detective.Lore"))
        .build(), event -> {
      event.setCancelled(true);
      User user = plugin.getUserManager().getUser(player);
      if(user.getStat(StatsStorage.StatisticType.DETECTIVE_PASS) <= 0) {
        player.sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("In-Game.Role-Pass.Fail").replace("%role%", Role.DETECTIVE.name()));
        return;
      }
      user.addStat(StatsStorage.StatisticType.DETECTIVE_PASS, -1);
      user.addStat(StatsStorage.StatisticType.CONTRIBUTION_DETECTIVE, 999);
      player.sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("In-Game.Role-Pass.Success").replace("%role%", Role.DETECTIVE.name()));
    }));
    gui.show(player);
  }

}
