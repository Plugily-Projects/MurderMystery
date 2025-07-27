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

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plugily.projects.minigamesbox.api.user.IUser;
import plugily.projects.minigamesbox.classic.commands.arguments.data.CommandArgument;
import plugily.projects.minigamesbox.classic.commands.arguments.data.LabelData;
import plugily.projects.minigamesbox.classic.commands.arguments.data.LabeledCommandArgument;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.commands.arguments.ArgumentsRegistry;
import plugily.projects.murdermystery.handlers.skins.sword.SwordSkin;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author postyizhan
 * <p>
 * Created at 30.06.2025
 */
public class SwordSkinsArgument {

  public SwordSkinsArgument(ArgumentsRegistry registry) {
    registry.mapArgument("murdermystery", new LabeledCommandArgument("skins", "murdermystery.skins.use", CommandArgument.ExecutorType.PLAYER,
        new LabelData("/mm skins sword <皮肤名>", "/mm skins sword <skin_name>", "&7切换杀手剑皮肤\n&6权限: &7murdermystery.skins.use")) {
      @Override
      public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Main plugin = (Main) registry.getPlugin();
        
        if(args.length < 2) {
          // 显示帮助信息
          player.sendMessage(ChatColor.RED + "用法: /mm skins sword <皮肤名>");
          player.sendMessage(ChatColor.YELLOW + "可用皮肤:");
          
          List<SwordSkin> availableSkins = plugin.getSwordSkinManager().getRegisteredSwordSkins().stream()
              .filter(skin -> !skin.hasPermission() || player.hasPermission(skin.getPermission()))
              .collect(Collectors.toList());
          
          for(SwordSkin skin : availableSkins) {
            String skinName = plugin.getSwordSkinManager().getSkinNameByItemStack(skin.getItemStack());
            if(skinName != null) {
              player.sendMessage(ChatColor.GREEN + "- " + skinName);
            }
          }
          return;
        }
        
        if(!args[1].equalsIgnoreCase("sword")) {
          player.sendMessage(ChatColor.RED + "目前只支持剑皮肤！用法: /mm skins sword <皮肤名>");
          return;
        }
        
        if(args.length < 3) {
          player.sendMessage(ChatColor.RED + "请指定皮肤名称！用法: /mm skins sword <皮肤名>");
          return;
        }
        
        String skinName = args[2];
        
        // 检查皮肤是否存在
        SwordSkin selectedSkin = plugin.getSwordSkinManager().getSkinByName(skinName);
        if(selectedSkin == null) {
          player.sendMessage(new MessageBuilder("COMMANDS_SWORD_SKINS_SKIN_NOT_FOUND").asKey().value(skinName).build());
          return;
        }
        
        // 检查权限
        if(selectedSkin.hasPermission() && !player.hasPermission(selectedSkin.getPermission())) {
          player.sendMessage(new MessageBuilder("COMMANDS_SWORD_SKINS_NO_PERMISSION").asKey().value(skinName).build());
          return;
        }
        
        // 保存玩家选择的皮肤
        IUser user = plugin.getUserManager().getUser(player);
        // 将皮肤名称转换为哈希码存储（因为统计系统只支持整数）
        user.setStatistic("SELECTED_SWORD_SKIN", skinName.hashCode());
        
        player.sendMessage(new MessageBuilder("COMMANDS_SWORD_SKINS_SKIN_SELECTED").asKey().value(skinName).build());
      }
    });
  }
}
