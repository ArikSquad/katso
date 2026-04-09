package eu.mikart.katso.minestom;

import eu.mikart.katso.view.DefaultState;
import eu.mikart.katso.view.StatelessView;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;

public abstract class MinestomStatelessView extends StatelessView<Player, ItemStack> implements MinestomView<DefaultState> {
}
