package eu.mikart.katso.minestom;

import eu.mikart.katso.PaginatedView;
import eu.mikart.katso.StatefulPaginatedView;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;

public abstract class MinestomStatefulPaginatedView<T, S extends PaginatedView.PaginatedState<T>>
        extends StatefulPaginatedView<T, S, Player, ItemStack>
        implements MinestomStatefulView<S> {
}
