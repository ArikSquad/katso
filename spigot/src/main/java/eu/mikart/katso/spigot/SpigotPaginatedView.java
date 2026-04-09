package eu.mikart.katso.spigot;

import eu.mikart.katso.PaginatedView;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class SpigotPaginatedView<T, S extends PaginatedView.PaginatedState<T>>
        extends PaginatedView<T, S, Player, ItemStack>
        implements SpigotView<S> {
}
