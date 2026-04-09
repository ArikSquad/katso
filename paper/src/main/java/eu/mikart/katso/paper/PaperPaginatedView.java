package eu.mikart.katso.paper;

import eu.mikart.katso.pagination.PaginatedState;
import eu.mikart.katso.spigot.SpigotPaginatedView;

public abstract class PaperPaginatedView<T, S extends PaginatedState<T>>
        extends SpigotPaginatedView<T, S>
        implements PaperView<S> {
}
