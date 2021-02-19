package plugily.projects.murdermystery.handlers.lastwords;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.utils.Debugger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class LastWordsManager {

  private final ArrayList<LastWord> registeredLastWords = new ArrayList<>();

  public LastWordsManager(Main plugin) {
    registerLastWords(plugin);
  }

  public void registerLastWords(Main plugin) {
    FileConfiguration config = ConfigUtils.getConfig(plugin, "language");
    ConfigurationSection section = config.getConfigurationSection("In-Game.Messages.Last-Words");
    String path = "In-Game.Messages.Last-Words.";
    if(section == null) {
      //use old formatting under v1.7.5
      addLastWord(new LastWord(plugin.getChatManager().colorMessage("In-Game.Messages.Last-Words.Meme"), "murdermystery.lastwords.meme"));
      addLastWord(new LastWord(plugin.getChatManager().colorMessage("In-Game.Messages.Last-Words.Rage"), "murdermystery.lastwords.rage"));
      addLastWord(new LastWord(plugin.getChatManager().colorMessage("In-Game.Messages.Last-Words.Pro"), "murdermystery.lastwords.pro"));
      addLastWord(new LastWord(plugin.getChatManager().colorMessage("In-Game.Messages.Last-Words.Default"), ""));
      Debugger.sendConsoleMsg("[MurderMystery] Please check your language.yml and update it to the new last words design that can be found on the latest language.yml");
      return;
    }
    for(String id : section.getKeys(false)) {
      addLastWord(new LastWord(plugin.getChatManager().colorMessage(path + id + ".Message"), config.getString(path + id + ".Permission", "")));
    }
  }

  public ArrayList<LastWord> getRegisteredLastWords() {
    return registeredLastWords;
  }

  public void addLastWord(LastWord lastWord) {
    registeredLastWords.add(lastWord);
  }

  public String getRandomLastWord(Player player) {
    //check perms
    List<LastWord> perms = registeredLastWords.stream().filter(lastWord -> player.hasPermission(lastWord.getPermission())).collect(Collectors.toList());
    if(perms.size() > 0) {
      return perms.get(ThreadLocalRandom.current().nextInt(perms.size() - 1)).getMessage();
    }
    //check default
    List<LastWord> noPerms = registeredLastWords.stream().filter(lastWord -> !lastWord.hasPermission()).collect(Collectors.toList());
    if(noPerms.size() > 0) {
      return noPerms.get(ThreadLocalRandom.current().nextInt(noPerms.size() - 1)).getMessage();
    }
    //fallback
    return registeredLastWords.get(0).getMessage();
  }
}
