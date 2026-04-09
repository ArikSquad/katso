package eu.mikart.katso.minestom;

import eu.mikart.katso.PaginatedView;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;

public abstract class MinestomPaginatedView<T, S extends PaginatedView.PaginatedState<T>>
        extends PaginatedView<T, S, Player, ItemStack>
        implements MinestomView<S> {
}
