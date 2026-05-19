package eu.mikart.katso.fabric;

import eu.mikart.katso.pagination.PaginatedState;
import eu.mikart.katso.pagination.PaginatedView;

public abstract class FabricPaginatedView<T, S extends PaginatedState<T>, P, I>
        extends PaginatedView<T, S, P, I>
        implements FabricView<S, P, I> {
}
