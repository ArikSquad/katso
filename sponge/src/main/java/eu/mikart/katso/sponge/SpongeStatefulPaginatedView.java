package eu.mikart.katso.sponge;

import eu.mikart.katso.pagination.PaginatedState;
import eu.mikart.katso.pagination.StatefulPaginatedView;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public abstract class SpongeStatefulPaginatedView<T, S extends PaginatedState<T>>
        extends StatefulPaginatedView<T, S, ServerPlayer, ItemStackSnapshot>
        implements SpongeStatefulView<S> {
}
