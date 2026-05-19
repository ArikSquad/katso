package eu.mikart.katso.velocity;

import com.velocitypowered.api.proxy.Player;
import eu.mikart.katso.view.DefaultState;
import eu.mikart.katso.view.StatelessView;

public abstract class VelocityStatelessView<I> extends StatelessView<Player, I> implements VelocityView<DefaultState, I> {
}
