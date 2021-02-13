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

package plugily.projects.murdermystery.commands.arguments.admin.arena;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import pl.plajerlair.commonsbox.minecraft.compat.xseries.XMaterial;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;
import pl.plajerlair.commonsbox.minecraft.serialization.LocationSerializer;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.arena.ArenaRegistry;
import plugily.projects.murdermystery.arena.special.SpecialBlock;
import plugily.projects.murdermystery.commands.arguments.ArgumentsRegistry;
import plugily.projects.murdermystery.commands.arguments.data.CommandArgument;
import plugily.projects.murdermystery.commands.arguments.data.LabelData;
import plugily.projects.murdermystery.commands.arguments.data.LabeledCommandArgument;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 22.10.2020
 */

public class SpecialBlockRemoverArgument {
  public SpecialBlockRemoverArgument(ArgumentsRegistry registry) {
    registry.mapArgument("murdermysteryadmin", new LabeledCommandArgument("removeblock", "murdermystery.admin.removeblock", CommandArgument.ExecutorType.PLAYER,
      new LabelData("/mma removeblock", "/mma removeblock", "&7Removes the special block you are looking at \n&6Permission: &7murdermystery.admin.removeblock")) {
      @Override
      public void execute(CommandSender sender, String[] args) {
        //no need for check as argument is only for players
        Player player = (Player) sender;
        Block targetBlock = player.getTargetBlock(null, 7);
        if(targetBlock.getType() == Material.CAULDRON || targetBlock.getType() == XMaterial.ENCHANTING_TABLE.parseMaterial()) {
          for(Arena arena : ArenaRegistry.getArenas()) {
            //do not check arenas that could not be the case
            if(arena.getSpecialBlocks().isEmpty()) {
              continue;
            }
            if(arena.getPlayerSpawnPoints().get(0).getWorld() != player.getWorld()) {
              continue;
            }
            //get all special blocks
            for(SpecialBlock specialBlock : arena.getSpecialBlocks()) {
              //check if targetBlock is specialblock
              if(specialBlock.getLocation().getBlock().equals(targetBlock)) {
                //get special blocks from config
                FileConfiguration config = ConfigUtils.getConfig(registry.getPlugin(), "arenas");
                //remove special block from arena
                arena.getSpecialBlocks().remove(specialBlock);
                //remove hologram
                if(specialBlock.getArmorStandHologram() != null) {
                  specialBlock.getArmorStandHologram().delete();
                }
                //remove special block from arena file
                String path = targetBlock.getType() == Material.CAULDRON ? ".mystery-cauldrons" : ".confessionals";
                List<String> specialBlocksType = new ArrayList<>(config.getStringList("instances." + arena.getId() + path));
                specialBlocksType.remove(LocationSerializer.locationToString(specialBlock.getLocation()));
                config.set("instances." + arena.getId() + path, specialBlocksType);
                //save arena config after removing special block
                ConfigUtils.saveConfig(registry.getPlugin(), config, "arenas");

                player.sendMessage(ChatColor.RED + "Removed special block at loc " + LocationSerializer.locationToString(specialBlock.getLocation()) + " from arena " + arena.getId());
                return;
              }
            }
          }
        }
        player.sendMessage(ChatColor.RED + "Please target an special block to continue!");
      }
    });
  }
}
