package eu.mikart.katso.sponge;

import eu.mikart.katso.view.DefaultState;
import eu.mikart.katso.view.StatelessView;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public abstract class SpongeStatelessView
        extends StatelessView<ServerPlayer, ItemStackSnapshot>
        implements SpongeView<DefaultState> {
}
