package eu.mikart.katso.fabric;

import eu.mikart.katso.packetevents.PacketEventsViewManager;

public class FabricViewManager<P, I> extends PacketEventsViewManager<P, I> {

    public FabricViewManager(FabricViewPlatform<P, I> platform) {
        super(platform);
    }

    @Override
    public FabricViewPlatform<P, I> platform() {
        return (FabricViewPlatform<P, I>) super.platform();
    }
}
