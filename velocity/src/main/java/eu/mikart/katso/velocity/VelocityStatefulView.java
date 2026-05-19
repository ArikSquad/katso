package eu.mikart.katso.velocity;

import com.velocitypowered.api.proxy.Player;
import eu.mikart.katso.view.StatefulView;

public interface VelocityStatefulView<S, I> extends StatefulView<S, Player, I>, VelocityView<S, I> {
}
