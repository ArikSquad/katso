package eu.mikart.katso.velocity;

import com.velocitypowered.api.proxy.Player;
import eu.mikart.katso.packetevents.PacketEventsViewManager;

public class VelocityViewManager<I> extends PacketEventsViewManager<Player, I> {

    public VelocityViewManager(VelocityViewPlatform<I> platform) {
        super(platform);
    }

    @Override
    public VelocityViewPlatform<I> platform() {
        return (VelocityViewPlatform<I>) super.platform();
    }
}
