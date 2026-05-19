package eu.mikart.katso.fabric;

import eu.mikart.katso.view.DefaultState;
import eu.mikart.katso.view.StatelessView;

public abstract class FabricStatelessView<P, I>
        extends StatelessView<P, I>
        implements FabricView<DefaultState, P, I> {
}
