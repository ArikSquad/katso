package eu.mikart.katso.spigot;

import eu.mikart.katso.StatefulView;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface SpigotStatefulView<S> extends StatefulView<S, Player, ItemStack>, SpigotView<S> {
}
