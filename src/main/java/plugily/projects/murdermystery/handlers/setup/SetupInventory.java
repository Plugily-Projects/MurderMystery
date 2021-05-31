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

package plugily.projects.murdermystery.handlers.setup;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.handlers.ChatManager;
import plugily.projects.murdermystery.handlers.setup.components.ArenaRegisterComponent;
import plugily.projects.murdermystery.handlers.setup.components.MiscComponents;
import plugily.projects.murdermystery.handlers.setup.components.PlayerAmountComponents;
import plugily.projects.murdermystery.handlers.setup.components.SpawnComponents;
import plugily.projects.murdermystery.handlers.setup.components.SpecialBlocksComponents;

import java.util.Random;

/**
 * @author Plajer
 * <p>
 * Created at 25.05.2019
 */
public class SetupInventory {

  public static final String VIDEO_LINK = "https://tutorial.plugily.xyz";
  private static final Random random = new Random();
  private static final Main plugin = JavaPlugin.getPlugin(Main.class);
  private final FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
  private final Arena arena;
  private final Player player;
  private ChestGui gui;
  private final SetupUtilities setupUtilities;

  public SetupInventory(Arena arena, Player player) {
    this.arena = arena;
    this.player = player;
    this.setupUtilities = new SetupUtilities(config, arena, plugin.getChatManager());
    prepareGui();
  }

  private void prepareGui() {
    this.gui = new ChestGui(4, "Murder Mystery Arena Setup");
    this.gui.setOnGlobalClick(e -> e.setCancelled(true));
    StaticPane pane = new StaticPane(9, 4);
    this.gui.addPane(pane);

    prepareComponents(pane);
  }

  private void prepareComponents(StaticPane pane) {
    SpawnComponents spawnComponents = new SpawnComponents();
    spawnComponents.prepare(this);
    spawnComponents.injectComponents(pane);

    PlayerAmountComponents playerAmountComponents = new PlayerAmountComponents();
    playerAmountComponents.prepare(this);
    playerAmountComponents.injectComponents(pane);

    MiscComponents miscComponents = new MiscComponents();
    miscComponents.prepare(this);
    miscComponents.injectComponents(pane);

    ArenaRegisterComponent arenaRegisterComponent = new ArenaRegisterComponent();
    arenaRegisterComponent.prepare(this);
    arenaRegisterComponent.injectComponents(pane);

    SpecialBlocksComponents specialBlocksComponents = new SpecialBlocksComponents();
    specialBlocksComponents.prepare(this);
    specialBlocksComponents.injectComponents(pane);
  }

  private void sendProTip(Player p) {
    ChatManager chatManager = plugin.getChatManager();

    switch(random.nextInt(16 + 1)) {
      case 0:
        p.sendMessage(chatManager.colorRawMessage("&e&lTIP: &7Help us translating plugin to your language here: https://translate.plugily.xyz"));
        break;
      case 1:
        p.sendMessage(chatManager.colorRawMessage("&e&lTIP: &7PlaceholderApi plugin is supported with our plugin! Check here: https://wiki.plugily.xyz/murdermystery/placeholders/placeholderapi"));
        break;
      case 2:
        p.sendMessage(chatManager.colorRawMessage("&e&lTIP: &7We are open source! You can always help us by contributing! Check https://github.com/Plugily-Projects/MurderMystery"));
        break;
      case 3:
        p.sendMessage(chatManager.colorRawMessage("&e&lTIP: &7Need help? Check wiki &8https://wiki.plugily.xyz/minecraft/murdermystery &7or discord https://discord.gg/UXzUdTP"));
        break;
      case 4:
        p.sendMessage(chatManager.colorRawMessage("&e&lTIP: &7Suggest new ideas for the plugin or vote on current ones! https://app.feedbacky.net/b/MurderMystery"));
        break;
      case 5:
        p.sendMessage(plugin.getChatManager().colorRawMessage("&e&lTIP: &7Achievements, ranks and replay ability are things available in our paid addon for this minigame! https://wiki.plugily.xyz/murdermystery/addon/overview"));
        break;
      default:
        break;
    }
  }

  public void openInventory() {
    sendProTip(player);
    gui.show(player);
  }

  public Main getPlugin() {
    return plugin;
  }

  public FileConfiguration getConfig() {
    return config;
  }

  public Arena getArena() {
    return arena;
  }

  public Player getPlayer() {
    return player;
  }

  public ChestGui getGui() {
    return gui;
  }

  public SetupUtilities getSetupUtilities() {
    return setupUtilities;
  }

}
