package eu.mikart.katso.packetevents;

import eu.mikart.katso.context.ViewClick;
import eu.mikart.katso.session.TopClickDecision;
import eu.mikart.katso.session.ViewManager;
import eu.mikart.katso.session.ViewNavigator;
import eu.mikart.katso.session.ViewSession;

import java.util.List;
import java.util.Map;

public class PacketEventsViewManager<P, I> extends ViewManager<P, I> {

    public PacketEventsViewManager(PacketEventsViewPlatform<P, I> platform) {
        super(platform);
    }

    @Override
    public PacketEventsViewPlatform<P, I> platform() {
        return (PacketEventsViewPlatform<P, I>) super.platform();
    }

    public boolean onTopClick(P player, Object inventoryHandle, int slot, ViewClick click) {
        ViewSession<?, P, I> session = currentSession(player);
        if (session == null || !session.belongsToInventory(inventoryHandle)) {
            return true;
        }

        TopClickDecision decision = session.decideTopClick(slot, click, List.of());
        if (decision.dispatchClickHandler()) {
            session.dispatchTopClick(slot, click);
        }
        if (!decision.allowInventoryChange()) {
            return false;
        }

        scheduleEditableSnapshot(session);
        return true;
    }

    public boolean onTopDrag(P player, Object inventoryHandle, List<Integer> topSlots, ViewClick dragClick) {
        ViewSession<?, P, I> session = currentSession(player);
        if (session == null || !session.belongsToInventory(inventoryHandle) || topSlots.isEmpty()) {
            return true;
        }

        TopClickDecision decision = session.decideTopClick(topSlots.getFirst(), dragClick, topSlots);
        if (!decision.allowInventoryChange()) {
            return false;
        }

        scheduleEditableSnapshot(session);
        return true;
    }

    public boolean onBottomClick(P player, Object topInventoryHandle, int slot, ViewClick click) {
        ViewSession<?, P, I> session = currentSession(player);
        if (session == null || !session.belongsToInventory(topInventoryHandle)) {
            return true;
        }

        if (!session.dispatchBottomClick(slot, click)) {
            return false;
        }

        scheduleEditableSnapshot(session);
        return true;
    }

    public void onInventoryClosedByPlayer(P player, Object inventoryHandle) {
        ViewSession<?, P, I> session = currentSession(player);
        if (session != null && session.belongsToInventory(inventoryHandle)) {
            session.close(ViewSession.CloseReason.PLAYER_EXITED);
        }
    }

    public void onPlayerDisconnected(P player) {
        removeNavigator(player);
    }

    protected ViewSession<?, P, I> currentSession(P player) {
        return findNavigator(player).map(ViewNavigator::currentSession).orElse(null);
    }

    protected void scheduleEditableSnapshot(ViewSession<?, P, I> session) {
        Map<Integer, I> snapshot = session.captureEditableSnapshot();
        if (snapshot.isEmpty()) {
            return;
        }

        platform().scheduleNextTick(() -> {
            if (!session.closed()) {
                session.applyEditableSnapshot(snapshot);
            }
        });
    }
}
