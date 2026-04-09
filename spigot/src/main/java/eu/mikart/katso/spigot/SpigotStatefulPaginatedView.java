package eu.mikart.katso.spigot;

import eu.mikart.katso.PaginatedView;
import eu.mikart.katso.StatefulPaginatedView;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class SpigotStatefulPaginatedView<T, S extends PaginatedView.PaginatedState<T>>
        extends StatefulPaginatedView<T, S, Player, ItemStack>
        implements SpigotStatefulView<S> {
}
