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

package plugily.projects.murdermystery.arena.special.mysterypotion;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;
import plugily.projects.murdermystery.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Plajer
 * <p>
 * Created at 15.10.2018
 */
public class MysteryPotionRegistry {

  private static final List<MysteryPotion> mysteryPotions = new ArrayList<>();

  public static void init(Main plugin) {
    FileConfiguration config = ConfigUtils.getConfig(plugin, "specialblocks");
    org.bukkit.configuration.ConfigurationSection section = config.getConfigurationSection("Special-Blocks.Cauldron-Potions");
    if(section == null) {
      return;
    }

    for(String key : section.getKeys(false)) {
      PotionEffectType effectType = PotionEffectType.getByName(section.getString(key + ".Type", "").toUpperCase());

      if (effectType == null) {
        effectType = PotionEffectType.HEAL;
      }

      //amplifiers are counted from 0 so -1
      PotionEffect effect = new PotionEffect(effectType,
          section.getInt(key + ".Duration") * 20, section.getInt(key + ".Amplifier") - 1, false, false);

      mysteryPotions.add(new MysteryPotion(plugin.getChatManager().colorRawMessage(section.getString(key + ".Name")),
        plugin.getChatManager().colorRawMessage(section.getString(key + ".Subtitle")), effect));
    }
  }

  public static MysteryPotion getRandomPotion() {
    return mysteryPotions.get(mysteryPotions.size() == 1 ? 0 : ThreadLocalRandom.current().nextInt(mysteryPotions.size()));
  }

  public static List<MysteryPotion> getMysteryPotions() {
    return mysteryPotions;
  }
}
