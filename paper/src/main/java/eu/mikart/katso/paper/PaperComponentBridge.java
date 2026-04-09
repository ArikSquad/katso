package eu.mikart.katso.paper;

import eu.mikart.katso.spigot.SpigotComponentBridge;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

public interface PaperComponentBridge extends SpigotComponentBridge {

    static PaperComponentBridge nativeAdventure() {
        return new PaperComponentBridge() {
            private final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();

            @Override
            public String serializeTitle(Component component) {
                return serializer.serialize(component);
            }

            @Override
            public void sendMessage(Player player, Component component) {
                player.sendMessage(component);
            }
        };
    }
}
