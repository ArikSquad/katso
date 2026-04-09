package eu.mikart.katso.spigot;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

public interface SpigotComponentBridge {

    String serializeTitle(Component component);

    void sendMessage(Player player, Component component);

    static SpigotComponentBridge legacySection() {
        return new SpigotComponentBridge() {
            private final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();

            @Override
            public String serializeTitle(Component component) {
                return serializer.serialize(component);
            }

            @Override
            public void sendMessage(Player player, Component component) {
                player.sendMessage(serializer.serialize(component));
            }
        };
    }
}
