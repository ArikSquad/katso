package eu.mikart.katso.sponge;

import eu.mikart.katso.session.ViewManager;

/**
 * Sponge platform manager.
 */
public class SpongeViewManager<P, I> extends ViewManager<P, I> {

    public SpongeViewManager(SpongeViewPlatform<P, I> platform) {
        super(platform);
    }
}
