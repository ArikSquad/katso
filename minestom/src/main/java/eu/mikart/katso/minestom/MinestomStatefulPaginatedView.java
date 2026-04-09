package eu.mikart.katso.minestom;

import eu.mikart.katso.pagination.PaginatedState;
import eu.mikart.katso.pagination.StatefulPaginatedView;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;

public abstract class MinestomStatefulPaginatedView<T, S extends PaginatedState<T>>
        extends StatefulPaginatedView<T, S, Player, ItemStack>
        implements MinestomStatefulView<S> {
}
