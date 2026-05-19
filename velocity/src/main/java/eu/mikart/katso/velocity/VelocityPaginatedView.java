package eu.mikart.katso.velocity;

import com.velocitypowered.api.proxy.Player;
import eu.mikart.katso.pagination.PaginatedState;
import eu.mikart.katso.pagination.PaginatedView;

public abstract class VelocityPaginatedView<T, S extends PaginatedState<T>, I>
        extends PaginatedView<T, S, Player, I>
        implements VelocityView<S, I> {
}
