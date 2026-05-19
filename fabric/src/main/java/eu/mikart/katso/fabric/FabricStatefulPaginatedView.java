package eu.mikart.katso.fabric;

import eu.mikart.katso.pagination.PaginatedState;
import eu.mikart.katso.pagination.StatefulPaginatedView;

public abstract class FabricStatefulPaginatedView<T, S extends PaginatedState<T>, P, I>
        extends StatefulPaginatedView<T, S, P, I>
        implements FabricStatefulView<S, P, I> {
}
