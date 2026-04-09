package eu.mikart.katso.paper;

import eu.mikart.katso.PaginatedView;
import eu.mikart.katso.spigot.SpigotStatefulPaginatedView;

public abstract class PaperStatefulPaginatedView<T, S extends PaginatedView.PaginatedState<T>>
        extends SpigotStatefulPaginatedView<T, S>
        implements PaperStatefulView<S> {
}
