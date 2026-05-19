package eu.mikart.katso.sponge;

import eu.mikart.katso.view.View;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public interface SpongeView<S> extends View<S, ServerPlayer, ItemStackSnapshot> {
}
