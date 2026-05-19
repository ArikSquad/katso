package eu.mikart.katso.velocity;

import eu.mikart.katso.packetevents.PacketEventsViewManager;

public class VelocityViewManager<P, I> extends PacketEventsViewManager<P, I> {

    public VelocityViewManager(VelocityViewPlatform<P, I> platform) {
        super(platform);
    }
}
