package eu.mikart.katso.pagination;

import eu.mikart.katso.view.StatefulView;

public abstract class StatefulPaginatedView<T, S extends PaginatedState<T>, P, I>
        extends PaginatedView<T, S, P, I>
        implements StatefulView<S, P, I> {
}
