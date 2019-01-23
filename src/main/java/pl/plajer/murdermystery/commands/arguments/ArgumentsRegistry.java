/*
 * MurderMystery - Find the murderer, kill him and survive!
 * Copyright (C) 2019  Plajer's Lair - maintained by Plajer and Tigerpanzer
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

package pl.plajer.murdermystery.commands.arguments;

/**
 * @author Plajer
 * <p>
 * Created at 11.01.2019
 */
//todo
  /*
public class ArgumentsRegistry implements CommandExecutor {

  private Main plugin;
  private Map<String, List<CommandArgument>> mappedArguments = new HashMap<>();

  public ArgumentsRegistry(Main plugin) {
    this.plugin = plugin;
    TabCompletion completion = new TabCompletion(this);
    plugin.getCommand("murdermystery").setExecutor(this);
    plugin.getCommand("murdermystery").setTabCompleter(completion);
    plugin.getCommand("murdermysteryadmin").setExecutor(this);
    plugin.getCommand("murdermysteryadmin").setTabCompleter(completion);
    
    //todo arguments
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    try {
      for (String mainCommand : mappedArguments.keySet()) {
        if (cmd.getName().equalsIgnoreCase(mainCommand)) {
          if (cmd.getName().equalsIgnoreCase("murdermystery")) {
            if (args.length == 0) {
              sender.sendMessage(ChatManager.colorMessage("Commands.Main-Command.Header"));
              sender.sendMessage(ChatManager.colorMessage("Commands.Main-Command.Description"));
              if (sender.hasPermission("murdermystery.admin")) {
                sender.sendMessage(ChatManager.colorMessage("Commands.Main-Command.Admin-Bonus-Description"));
              }
              sender.sendMessage(ChatManager.colorMessage("Commands.Main-Command.Footer"));
              return true;
            }
            if (args.length > 1 && args[1].equalsIgnoreCase("edit")) {
              if (!checkSenderIsExecutorType(sender, CommandArgument.ExecutorType.PLAYER)
                  || !Utils.hasPermission(sender, "murdermystery.admin.create")) {
                return true;
              }
              if (ArenaRegistry.getArena(args[0]) == null) {
                sender.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("Commands.No-Arena-Like-That"));
                return true;
              }

              SetupInventory.sendProTip((Player) sender);
              new SetupInventory(ArenaRegistry.getArena(args[0])).openInventory((Player) sender);
            }
          }
          if (cmd.getName().equalsIgnoreCase("murdermysteryadmin")) {
            if (args.length == 0) {
              if (!sender.hasPermission("murdermystery.admin")) {
                return true;
              }
              sender.sendMessage(ChatColor.GREEN + "  " + ChatColor.BOLD + "Murder Mystery " + ChatColor.GRAY + plugin.getDescription().getVersion());
              sender.sendMessage(ChatColor.RED + " []" + ChatColor.GRAY + " = optional  " + ChatColor.GOLD + "<>" + ChatColor.GRAY + " = required");
              if (sender instanceof Player) {
                sender.sendMessage(ChatColor.GRAY + "Hover command to see more, click command to suggest it.");
              }
              List<LabelData> data = mappedArguments.get("murdermysteryadmin").stream().filter(arg -> arg instanceof LabeledCommandArgument)
                  .map(arg -> ((LabeledCommandArgument) arg).getLabelData()).collect(Collectors.toList());
              data.add(new LabelData("/mm &6<arena>&f edit", "/mm <arena> edit",
                  "&7Edit existing arena\n&6Permission: &7murdermystery.admin.edit"));
              data.addAll(mappedArguments.get("murdermystery").stream().filter(arg -> arg instanceof LabeledCommandArgument)
                  .map(arg -> ((LabeledCommandArgument) arg).getLabelData()).collect(Collectors.toList()));
              for (LabelData labelData : data) {
                TextComponent component;
                if (sender instanceof Player) {
                  component = new TextComponent(labelData.getText());
                } else {
                  //more descriptive for console - split at \n to show only basic description
                  component = new TextComponent(labelData.getText() + " - " + labelData.getDescription().split("\n")[0]);
                }
                component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, labelData.getCommand()));
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(labelData.getDescription()).create()));
                sender.spigot().sendMessage(component);
              }
              return true;
            }
          }
          for (CommandArgument argument : mappedArguments.get(mainCommand)) {
            if (argument.getArgumentName().equalsIgnoreCase(args[0])) {
              boolean hasPerm = false;
              for (String perm : argument.getPermissions()) {
                if (perm.equals("")) {
                  hasPerm = true;
                  break;
                }
                if (sender.hasPermission(perm)) {
                  hasPerm = true;
                  break;
                }
              }
              if (!hasPerm) {
                return true;
              }
              if (checkSenderIsExecutorType(sender, argument.getValidExecutors())) {
                argument.execute(sender, args);
              }
              //return true even if sender is not good executor or hasn't got permission
              return true;
            }
          }

          //sending did you mean help
          List<StringMatcher.Match> matches = StringMatcher.match(args[0], mappedArguments.get(cmd.getName().toLowerCase()).stream().map(CommandArgument::getArgumentName).collect(Collectors.toList()));
          if (!matches.isEmpty()) {
            sender.sendMessage(ChatManager.colorMessage("Commands.Did-You-Mean").replace("%command%", label + " " + matches.get(0).getMatch()));
            return true;
          }
        }
      }
    } catch (Exception ex) {
      new ReportedException(plugin, ex);
    }
    return false;
  }

  private boolean checkSenderIsExecutorType(CommandSender sender, CommandArgument.ExecutorType type) {
    switch (type) {
      case BOTH:
        return sender instanceof ConsoleCommandSender || sender instanceof Player;
      case CONSOLE:
        return sender instanceof ConsoleCommandSender;
      case PLAYER:
        if (sender instanceof Player) {
          return true;
        }
        sender.sendMessage(ChatManager.colorMessage("Commands.Only-By-Player"));
        return false;
      default:
        return false;
    }
  }

  /**
   * Maps new argument to the main command
   *
   * @param mainCommand mother command ex. /mm
   * @param argument    argument to map ex. leave (for /mm leave)
   */
  /*public void mapArgument(String mainCommand, CommandArgument argument) {
    List<CommandArgument> args = mappedArguments.getOrDefault(mainCommand, new ArrayList<>());
    args.add(argument);
    mappedArguments.put(mainCommand, args);
  }

  public Map<String, List<CommandArgument>> getMappedArguments() {
    return mappedArguments;
  }

  public Main getPlugin() {
    return plugin;
  }

  public SpyChatArgument getSpyChat() {
    return spyChat;
  }
}*/
