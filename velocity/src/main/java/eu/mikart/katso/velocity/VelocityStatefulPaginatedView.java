package eu.mikart.katso.velocity;

import com.velocitypowered.api.proxy.Player;
import eu.mikart.katso.pagination.PaginatedState;
import eu.mikart.katso.pagination.StatefulPaginatedView;

public abstract class VelocityStatefulPaginatedView<T, S extends PaginatedState<T>, I>
        extends StatefulPaginatedView<T, S, Player, I>
        implements VelocityStatefulView<S, I> {
}
