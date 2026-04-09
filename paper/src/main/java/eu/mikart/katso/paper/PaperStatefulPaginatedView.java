package eu.mikart.katso.paper;

import eu.mikart.katso.pagination.PaginatedState;
import eu.mikart.katso.spigot.SpigotStatefulPaginatedView;

public abstract class PaperStatefulPaginatedView<T, S extends PaginatedState<T>>
        extends SpigotStatefulPaginatedView<T, S>
        implements PaperStatefulView<S> {
}
