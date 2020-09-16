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

package plugily.projects.murdermystery.handlers.sign;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import pl.plajerlair.commonsbox.minecraft.compat.ServerVersion;
import plugily.projects.murdermystery.arena.Arena;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;

/**
 * Created for 1.14 compatibility purposes, it will cache block behind sign that will be
 * accessed via reflection on 1.14 which is expensive
 */
public class ArenaSign {

  private final Sign sign;
  private Block behind;
  private final Arena arena;

  public ArenaSign(Sign sign, Arena arena) {
    this.sign = sign;
    this.arena = arena;
    setBehindBlock();
  }

  private void setBehindBlock() {
    this.behind = null;
    if (sign.getBlock().getType() == Material.getMaterial("WALL_SIGN")) {
      this.behind = ServerVersion.Version.isCurrentEqualOrHigher(ServerVersion.Version.v1_14_R1) ? getBlockBehind() : getBlockBehindLegacy();
    }
  }

  private Block getBlockBehind() {
    try {
      Object blockData = sign.getBlock().getState().getClass().getMethod("getBlockData").invoke(sign.getBlock().getState());
      BlockFace face = (BlockFace) blockData.getClass().getMethod("getFacing").invoke(blockData);

      Location loc = sign.getLocation();
      Location location = new Location(sign.getWorld(), loc.getBlockX() - face.getModX(), loc.getBlockY() - face.getModY(),
        loc.getBlockZ() - face.getModZ());
      return location.getBlock();
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
      return null;
    }
  }

  private Block getBlockBehindLegacy() {
    return sign.getBlock().getRelative(((org.bukkit.material.Sign) sign.getData()).getAttachedFace());
  }

  public Sign getSign() {
    return sign;
  }

  @Nullable
  public Block getBehind() {
    return behind;
  }

  public Arena getArena() {
    return arena;
  }

}
