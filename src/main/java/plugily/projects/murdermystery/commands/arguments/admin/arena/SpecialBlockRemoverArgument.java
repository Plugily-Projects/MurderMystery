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

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import plugily.projects.minigamesbox.classic.arena.PluginArena;
import plugily.projects.minigamesbox.classic.commands.arguments.data.CommandArgument;
import plugily.projects.minigamesbox.classic.commands.arguments.data.LabelData;
import plugily.projects.minigamesbox.classic.commands.arguments.data.LabeledCommandArgument;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.utils.configuration.ConfigUtils;
import plugily.projects.minigamesbox.classic.utils.serialization.LocationSerializer;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XMaterial;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.arena.special.SpecialBlock;
import plugily.projects.murdermystery.commands.arguments.ArgumentsRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tigerpanzer_02
 * <p>Created at 22.10.2020
 */
public class SpecialBlockRemoverArgument {
  public SpecialBlockRemoverArgument(ArgumentsRegistry registry) {
    registry.mapArgument(
        "murdermysteryadmin",
        new LabeledCommandArgument(
            "removeblock",
            "murdermystery.admin.removeblock",
            CommandArgument.ExecutorType.PLAYER,
            new LabelData(
                "/mma removeblock",
                "/mma removeblock",
                "&7Removes the special block you are looking at \n&6Permission: &7murdermystery.admin.removeblock")) {
          @Override
          public void execute(CommandSender sender, String[] args) {
            // no need for check as argument is only for players
            Player player = (Player) sender;
            Block targetBlock = player.getTargetBlock(null, 7);
            if(targetBlock.getType() == Material.CAULDRON
                || targetBlock.getType() == XMaterial.ENCHANTING_TABLE.parseMaterial()) {
              for(PluginArena arena : registry.getPlugin().getArenaRegistry().getArenas()) {

                Arena pluginArena =
                    (Arena) registry.getPlugin().getArenaRegistry().getArena(player);
                if(arena == null) {
                  return;
                }

                // do not check arenas that could not be the case
                if(pluginArena.getSpecialBlocks().isEmpty()) {
                  continue;
                }
                if(pluginArena.getPlayerSpawnPoints().get(0).getWorld() != player.getWorld()) {
                  continue;
                }
                // get all special blocks
                for(SpecialBlock specialBlock : new ArrayList<>(pluginArena.getSpecialBlocks())) {
                  // check if targetBlock is specialblock
                  if(specialBlock.getLocation().getBlock().equals(targetBlock)) {
                    // get special blocks from config
                    FileConfiguration config =
                        ConfigUtils.getConfig(registry.getPlugin(), "arenas");
                    // remove special block from arena
                    pluginArena.getSpecialBlocks().remove(specialBlock);
                    // remove hologram
                    if(specialBlock.getArmorStandHologram() != null) {
                      specialBlock.getArmorStandHologram().delete();
                    }
                    // remove special block from arena file
                    String path =
                        targetBlock.getType() == Material.CAULDRON
                            ? ".mystery-cauldrons"
                            : ".confessionals";
                    String serializedLoc =
                        LocationSerializer.locationToString(specialBlock.getLocation());
                    List<String> specialBlocksType =
                        config.getStringList("instances." + arena.getId() + path);
                    specialBlocksType.remove(serializedLoc);
                    config.set("instances." + arena.getId() + path, specialBlocksType);
                    // save arena config after removing special block
                    ConfigUtils.saveConfig(registry.getPlugin(), config, "arenas");
                    new MessageBuilder("&cRemoved special block at loc "
                        + serializedLoc
                        + " from arena "
                        + arena.getId()).player(player).sendPlayer();
                    return;
                  }
                }
              }
            }
            new MessageBuilder("&cPlease target an special block to continue!").player(player).sendPlayer();
          }
        });
  }
}
