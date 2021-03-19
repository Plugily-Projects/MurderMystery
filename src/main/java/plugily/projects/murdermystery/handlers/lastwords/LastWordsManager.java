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
package plugily.projects.murdermystery.handlers.lastwords;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.utils.Debugger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @author 2Wild4You, Tigerpanzer_02
 * <p>
 * Created at 19.02.2021
 */
public class LastWordsManager {

  private final List<LastWord> registeredLastWords = new ArrayList<>();

  public LastWordsManager(Main plugin) {
    registerLastWords(plugin);
  }

  public void registerLastWords(Main plugin) {
    FileConfiguration config = ConfigUtils.getConfig(plugin, "language");
    ConfigurationSection section = config.getConfigurationSection("In-Game.Messages.Last-Words");
    if(section == null) {
      //use old formatting under v1.7.5
      addLastWord(new LastWord(plugin.getChatManager().colorMessage("In-Game.Messages.Last-Words.Meme"), "murdermystery.lastwords.meme"));
      addLastWord(new LastWord(plugin.getChatManager().colorMessage("In-Game.Messages.Last-Words.Rage"), "murdermystery.lastwords.rage"));
      addLastWord(new LastWord(plugin.getChatManager().colorMessage("In-Game.Messages.Last-Words.Pro"), "murdermystery.lastwords.pro"));
      addLastWord(new LastWord(plugin.getChatManager().colorMessage("In-Game.Messages.Last-Words.Default"), ""));
      Debugger.sendConsoleMsg("[MurderMystery] Please check your language.yml and update it to the new last words design that can be found on the latest language.yml");
      return;
    }
    String path = "In-Game.Messages.Last-Words.";
    for(String id : section.getKeys(false)) {
      addLastWord(new LastWord(plugin.getChatManager().colorMessage(path + id + ".Message"), config.getString(path + id + ".Permission", "")));
    }
  }

  public List<LastWord> getRegisteredLastWords() {
    return registeredLastWords;
  }

  public void addLastWord(LastWord lastWord) {
    registeredLastWords.add(lastWord);
  }

  public String getRandomLastWord(Player player) {
    //check perms
    List<LastWord> perms = registeredLastWords.stream().filter(lastWord -> player.hasPermission(lastWord.getPermission())).collect(Collectors.toList());
    if(!perms.isEmpty()) {
      return perms.get(ThreadLocalRandom.current().nextInt(perms.size())).getMessage();
    }
    //check default
    List<LastWord> noPerms = registeredLastWords.stream().filter(lastWord -> !lastWord.hasPermission()).collect(Collectors.toList());
    if(!noPerms.isEmpty()) {
      return noPerms.get(ThreadLocalRandom.current().nextInt(noPerms.size())).getMessage();
    }
    //fallback
    return registeredLastWords.get(0).getMessage();
  }
}
