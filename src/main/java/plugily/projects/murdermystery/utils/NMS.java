package plugily.projects.murdermystery.utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import pl.plajerlair.commonsbox.minecraft.compat.ServerVersion.Version;
import plugily.projects.murdermystery.Main;

@SuppressWarnings("deprecation")
public abstract class NMS {

	private static final Main PLUGIN = JavaPlugin.getPlugin(Main.class);

	public static void setDurability(ItemStack item, short durability) {
		if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
			ItemMeta meta = item.getItemMeta();
			if (meta != null) {
				((Damageable) meta).setDamage(durability);
			}
		} else {
			item.setDurability(durability);
		}
	}


	public static void hidePlayer(Player to, Player p) {
		if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
			to.hidePlayer(PLUGIN, p);
		} else {
			to.hidePlayer(p);
		}
	}

	public static void showPlayer(Player to, Player p) {
		if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
			to.showPlayer(PLUGIN, p);
		} else {
			to.showPlayer(p);
		}
	}
}
