package eu.mikart.katso.sponge;

import eu.mikart.katso.pagination.PaginatedState;
import eu.mikart.katso.pagination.PaginatedView;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public abstract class SpongePaginatedView<T, S extends PaginatedState<T>>
        extends PaginatedView<T, S, ServerPlayer, ItemStackSnapshot>
        implements SpongeView<S> {
}
