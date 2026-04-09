package eu.mikart.katso.minestom;

import eu.mikart.katso.view.StatefulView;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;

public interface MinestomStatefulView<S> extends StatefulView<S, Player, ItemStack>, MinestomView<S> {
}
