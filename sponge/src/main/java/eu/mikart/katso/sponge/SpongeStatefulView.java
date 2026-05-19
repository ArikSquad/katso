package eu.mikart.katso.sponge;

import eu.mikart.katso.view.StatefulView;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public interface SpongeStatefulView<S> extends StatefulView<S, ServerPlayer, ItemStackSnapshot>, SpongeView<S> {
}
