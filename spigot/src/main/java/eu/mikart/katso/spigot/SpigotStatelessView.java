package eu.mikart.katso.spigot;

import eu.mikart.katso.DefaultState;
import eu.mikart.katso.StatelessView;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class SpigotStatelessView extends StatelessView<Player, ItemStack> implements SpigotView<DefaultState> {
}
