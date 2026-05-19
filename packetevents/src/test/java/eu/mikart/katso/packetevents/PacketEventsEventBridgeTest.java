package eu.mikart.katso.packetevents;

import eu.mikart.katso.context.ViewClick;
import eu.mikart.katso.context.ViewClickType;
import eu.mikart.katso.platform.ScheduledTask;
import eu.mikart.katso.platform.ViewInventory;
import eu.mikart.katso.view.ViewType;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PacketEventsEventBridgeTest {

    @Test
    void mapsAndDelegatesActions() {
        RecordingManager manager = new RecordingManager();
        PacketEventsEventBridge<String, String, String> bridge = new PacketEventsEventBridge<>(manager, new StringMapper());

        PacketEventsResult top = bridge.topClick("p", "inv", 5, "LEFT", -1);
        PacketEventsResult drag = bridge.topDrag("p", "inv", List.of(1, 2), "DRAG");
        PacketEventsResult bottom = bridge.bottomClick("p", "inv", 9, "RIGHT", -1);
        bridge.close("p", "inv");
        bridge.disconnect("p");

        assertEquals(PacketEventsResult.ALLOW, top);
        assertEquals(PacketEventsResult.CANCEL, drag);
        assertEquals(PacketEventsResult.ALLOW, bottom);
        assertEquals(5, manager.calls);
    }

    private static final class RecordingManager extends PacketEventsViewManager<String, String> {
        private int calls;

        RecordingManager() {
            super(new NoopPlatform());
        }

        @Override
        public boolean onTopClick(String player, Object inventoryHandle, int slot, ViewClick click) {
            calls++;
            return true;
        }

        @Override
        public boolean onTopDrag(String player, Object inventoryHandle, List<Integer> topSlots, ViewClick dragClick) {
            calls++;
            return false;
        }

        @Override
        public boolean onBottomClick(String player, Object topInventoryHandle, int slot, ViewClick click) {
            calls++;
            return true;
        }

        @Override
        public void onInventoryClosedByPlayer(String player, Object inventoryHandle) {
            calls++;
        }

        @Override
        public void onPlayerDisconnected(String player) {
            calls++;
        }
    }

    private static final class StringMapper implements PacketEventsClickMapper<String> {
        @Override
        public ViewClick toClick(String clickType, int hotbarButton) {
            return ViewClick.of("RIGHT".equals(clickType) ? ViewClickType.RIGHT : ViewClickType.LEFT);
        }

        @Override
        public ViewClick toDragClick(String dragType, Collection<Integer> draggedSlots) {
            return ViewClick.drag(ViewClickType.DRAG_LEFT, draggedSlots);
        }
    }

    private static final class NoopPlatform implements PacketEventsViewPlatform<String, String> {
        @Override
        public UUID playerId(String player) { return UUID.randomUUID(); }
        @Override
        public ViewInventory<String> createInventory(String player, ViewType type, Component title) { throw new UnsupportedOperationException(); }
        @Override
        public void openInventory(String player, ViewInventory<String> inventory) {}
        @Override
        public void closeInventory(String player) {}
        @Override
        public void sendMessage(String player, Component message) {}
        @Override
        public String emptyItem() { return ""; }
        @Override
        public String copyItem(String item) { return item; }
        @Override
        public boolean isEmpty(String item) { return item == null || item.isEmpty(); }
        @Override
        public boolean itemsEqual(String first, String second) { return first == null ? second == null : first.equals(second); }
        @Override
        public ScheduledTask scheduleNextTick(Runnable action) { return () -> {}; }
        @Override
        public ScheduledTask scheduleRepeating(Duration interval, Runnable action) { return () -> {}; }
    }
}
