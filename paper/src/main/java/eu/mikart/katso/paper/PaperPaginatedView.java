package eu.mikart.katso.paper;

import eu.mikart.katso.PaginatedView;
import eu.mikart.katso.spigot.SpigotPaginatedView;

public abstract class PaperPaginatedView<T, S extends PaginatedView.PaginatedState<T>>
        extends SpigotPaginatedView<T, S>
        implements PaperView<S> {
}
