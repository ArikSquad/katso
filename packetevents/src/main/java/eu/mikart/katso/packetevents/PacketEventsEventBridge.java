package eu.mikart.katso.packetevents;

import eu.mikart.katso.context.ViewClick;

import java.util.List;
import java.util.Objects;

/**
 * Helper that turns platform packet callbacks into Katso manager calls.
 */
public final class PacketEventsEventBridge<P, I, C> {

    private final PacketEventsViewManager<P, I> manager;
    private final PacketEventsClickMapper<C> clickMapper;

    public PacketEventsEventBridge(PacketEventsViewManager<P, I> manager, PacketEventsClickMapper<C> clickMapper) {
        this.manager = Objects.requireNonNull(manager, "manager");
        this.clickMapper = Objects.requireNonNull(clickMapper, "clickMapper");
    }

    public PacketEventsResult topClick(P player, Object inventoryHandle, int slot, C clickType, int hotbarButton) {
        ViewClick click = clickMapper.toClick(clickType, hotbarButton);
        return manager.onTopClick(player, inventoryHandle, slot, click) ? PacketEventsResult.ALLOW : PacketEventsResult.CANCEL;
    }

    public PacketEventsResult topDrag(P player, Object inventoryHandle, List<Integer> topSlots, C dragType) {
        ViewClick click = clickMapper.toDragClick(dragType, topSlots);
        return manager.onTopDrag(player, inventoryHandle, topSlots, click) ? PacketEventsResult.ALLOW : PacketEventsResult.CANCEL;
    }

    public PacketEventsResult bottomClick(P player, Object topInventoryHandle, int slot, C clickType, int hotbarButton) {
        ViewClick click = clickMapper.toClick(clickType, hotbarButton);
        return manager.onBottomClick(player, topInventoryHandle, slot, click) ? PacketEventsResult.ALLOW : PacketEventsResult.CANCEL;
    }

    public void close(P player, Object inventoryHandle) {
        manager.onInventoryClosedByPlayer(player, inventoryHandle);
    }

    public void disconnect(P player) {
        manager.onPlayerDisconnected(player);
    }
}
