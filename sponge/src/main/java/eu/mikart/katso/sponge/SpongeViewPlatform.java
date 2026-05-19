package eu.mikart.katso.sponge;

import eu.mikart.katso.platform.ViewPlatform;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

/**
 * Marker platform type for Sponge integrations.
 */
public interface SpongeViewPlatform extends ViewPlatform<ServerPlayer, ItemStackSnapshot> {
}
