package eu.mikart.katso.paper;

import eu.mikart.katso.spigot.SpigotViewManager;
import org.bukkit.plugin.Plugin;

public class PaperViewManager extends SpigotViewManager {

    public PaperViewManager(Plugin plugin) {
        super(plugin, PaperComponentBridge.nativeAdventure());
    }

    public PaperViewManager(Plugin plugin, PaperComponentBridge componentBridge) {
        super(plugin, componentBridge);
    }
}
