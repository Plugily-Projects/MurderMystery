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
package plugily.projects.murdermystery.handlers.skins.sword;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import plugily.projects.minigamesbox.classic.utils.configuration.ConfigUtils;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XMaterial;
import plugily.projects.murdermystery.Main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @author 2Wild4You, Tigerpanzer_02
 *     <p>Created at 19.02.2021
 */
public class SwordSkinManager {

  private final List<SwordSkin> registeredSwordSkins = new ArrayList<>();
  private final Map<Player, ItemStack> murdererSwords = new HashMap<>();

  public SwordSkinManager(Main plugin) {
    registerSwordSkins(plugin);
  }

  public void registerSwordSkins(Main plugin) {
    FileConfiguration config = ConfigUtils.getConfig(plugin, "skins");
    ConfigurationSection section = config.getConfigurationSection("Skins.Sword");
    String path = "Skins.Sword.";
    for (String id : section.getKeys(false)) {
      addSwordSkin(
          new SwordSkin(
              XMaterial.matchXMaterial(config.getString(path + id + ".Material", "BEDROCK"))
                  .orElse(XMaterial.BEDROCK)
                  .parseItem(),
              config.getString(path + id + ".Permission", "")));
    }
  }

  public List<SwordSkin> getRegisteredSwordSkins() {
    return registeredSwordSkins;
  }

  public void addSwordSkin(SwordSkin lastWord) {
    registeredSwordSkins.add(lastWord);
  }

  public ItemStack getRandomSwordSkin(Player player) {
    // check perms
    List<SwordSkin> perms =
        registeredSwordSkins.stream()
            .filter(swordSkin -> player.hasPermission(swordSkin.getPermission()))
            .collect(Collectors.toList());
    if (!perms.isEmpty()) {
      ItemStack itemStack =
          perms.get(ThreadLocalRandom.current().nextInt(perms.size())).getItemStack();
      murdererSwords.put(player, itemStack);
      return itemStack;
    }
    // check default
    List<SwordSkin> noPerms =
        registeredSwordSkins.stream()
            .filter(swordSkin -> !swordSkin.hasPermission())
            .collect(Collectors.toList());
    if (!noPerms.isEmpty()) {
      ItemStack itemStack =
          noPerms.get(ThreadLocalRandom.current().nextInt(noPerms.size())).getItemStack();
      murdererSwords.put(player, itemStack);
      return itemStack;
    }
    // fallback
    ItemStack itemStack = registeredSwordSkins.get(0).getItemStack();
    murdererSwords.put(player, itemStack);
    return itemStack;
  }

  public void removeMurdererSword(Player player) {
    murdererSwords.remove(player);
  }

  public ItemStack getMurdererSword(Player player) {
    return murdererSwords.get(player);
  }
}
