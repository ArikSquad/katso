package eu.mikart.katso;

public abstract class StatefulPaginatedView<T, S extends PaginatedView.PaginatedState<T>, P, I>
        extends PaginatedView<T, S, P, I>
        implements StatefulView<S, P, I> {
}
