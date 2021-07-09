package plugily.projects.murdermystery.commands.arguments.admin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import plugily.projects.commonsbox.number.NumberUtils;
import plugily.projects.murdermystery.api.StatsStorage;
import plugily.projects.murdermystery.arena.role.Role;
import plugily.projects.murdermystery.commands.arguments.ArgumentsRegistry;
import plugily.projects.murdermystery.commands.arguments.data.CommandArgument;
import plugily.projects.murdermystery.commands.arguments.data.LabelData;
import plugily.projects.murdermystery.commands.arguments.data.LabeledCommandArgument;
import plugily.projects.murdermystery.handlers.ChatManager;
import plugily.projects.murdermystery.user.User;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 13.06.2021
 */
public class RolePassArgument {

  public RolePassArgument(ArgumentsRegistry registry, ChatManager chatManager) {
    registry.mapArgument("murdermysteryadmin", new LabeledCommandArgument("rolepass", "murdermystery.admin.rolepass", CommandArgument.ExecutorType.BOTH,
        new LabelData("/mma rolepass <add/set/remove> <role> <amount> [player]", "/mma rolepass <add/remove> <amount> [player] ", "&7Add or remove rolepass\n&6Permission: &7murdermystery.admin.rolepass")) {
      @Override
      public void execute(CommandSender sender, String[] args) {
        if(args.length < 3) {
          sender.sendMessage(chatManager.getPrefix() + ChatColor.RED + "Command: /mma rolepass <add/set/remove> <role> <amount> [player]");
          return;
        }
        //add/remove
        String addOrRemove = args[0];
        if(!addOrRemove.equalsIgnoreCase("add") && !addOrRemove.equalsIgnoreCase("remove") && !addOrRemove.equalsIgnoreCase("set")) {
          sender.sendMessage(chatManager.getPrefix() + ChatColor.RED + "Command: /mma rolepass <add/set/remove> <role> <amount> [player]");
          return;
        }
        String roleArg = args[1];
        Role role = Role.MURDERER;
        try {
          role = Role.valueOf(roleArg);
        } catch(IllegalArgumentException exception) {
          sender.sendMessage(chatManager.getPrefix() + ChatColor.RED + "Command: /mma rolepass <add/set/remove> <role> <amount> [player]");
          return;
        }
        if(role != Role.MURDERER && role != Role.DETECTIVE) {
          sender.sendMessage(chatManager.getPrefix() + ChatColor.RED + "Command: /mma rolepass <add/set/remove> <role> <amount> [player]");
          return;
        }
        java.util.Optional<Integer> opt = NumberUtils.parseInt(args[2]);
        if(!opt.isPresent()) {
          sender.sendMessage(chatManager.getPrefix() + ChatColor.RED + "Command: /mma rolepass <add/set/remove> <role> <amount> [player]");
          return;
        }
        int amount = opt.orElse(0);
        //player
        Player player = args.length == 4 ? Bukkit.getPlayerExact(args[3]) : (Player) sender;
        if(player == null) {
          sender.sendMessage(chatManager.getPrefix() + chatManager.colorMessage("Commands.Admin-Commands.Player-Not-Found"));
          return;
        }
        User user = registry.getPlugin().getUserManager().getUser(player);
        switch(addOrRemove.toLowerCase()) {
          case "add":
            if(role == Role.MURDERER) {
              user.addStat(StatsStorage.StatisticType.MURDERER_PASS, amount);
            } else {
              user.addStat(StatsStorage.StatisticType.DETECTIVE_PASS, amount);
            }
            break;
          case "remove":
            if(role == Role.MURDERER) {
              user.addStat(StatsStorage.StatisticType.MURDERER_PASS, -amount);
            } else {
              user.addStat(StatsStorage.StatisticType.DETECTIVE_PASS, -amount);
            }
            break;
          case "set":
            if(role == Role.MURDERER) {
              user.setStat(StatsStorage.StatisticType.MURDERER_PASS, amount);
            } else {
              user.setStat(StatsStorage.StatisticType.DETECTIVE_PASS, amount);
            }
            break;
          default:
            break;
        }
        if(role == Role.MURDERER) {
          sender.sendMessage(chatManager.getPrefix() + chatManager.colorMessage("In-Game.Role-Pass.Change").replace("%amount%", args[2]).replace("%role%", Role.MURDERER.name()));
        } else {
          sender.sendMessage(chatManager.getPrefix() + chatManager.colorMessage("In-Game.Role-Pass.Change").replace("%amount%", args[2]).replace("%role%", Role.DETECTIVE.name()));
        }
      }
    });
  }

}
