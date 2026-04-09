package eu.mikart.katso.spigot;

import eu.mikart.katso.pagination.PaginatedState;
import eu.mikart.katso.pagination.StatefulPaginatedView;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class SpigotStatefulPaginatedView<T, S extends PaginatedState<T>>
        extends StatefulPaginatedView<T, S, Player, ItemStack>
        implements SpigotStatefulView<S> {
}
