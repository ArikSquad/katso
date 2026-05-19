package eu.mikart.katso.sponge;

import eu.mikart.katso.context.ViewClick;
import eu.mikart.katso.session.TopClickDecision;
import eu.mikart.katso.session.ViewManager;
import eu.mikart.katso.session.ViewNavigator;
import eu.mikart.katso.session.ViewSession;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.List;
import java.util.Map;

public class SpongeViewManager extends ViewManager<ServerPlayer, ItemStackSnapshot> {

    public SpongeViewManager(SpongeViewPlatform platform) {
        super(platform);
    }

    @Override
    public SpongeViewPlatform platform() {
        return (SpongeViewPlatform) super.platform();
    }

    public boolean onTopClick(ServerPlayer player, Object inventoryHandle, int slot, ViewClick click) {
        ViewSession<?, ServerPlayer, ItemStackSnapshot> session = currentSession(player);
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

    public boolean onTopDrag(ServerPlayer player, Object inventoryHandle, List<Integer> topSlots, ViewClick dragClick) {
        ViewSession<?, ServerPlayer, ItemStackSnapshot> session = currentSession(player);
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

    public boolean onBottomClick(ServerPlayer player, Object topInventoryHandle, int slot, ViewClick click) {
        ViewSession<?, ServerPlayer, ItemStackSnapshot> session = currentSession(player);
        if (session == null || !session.belongsToInventory(topInventoryHandle)) {
            return true;
        }

        if (!session.dispatchBottomClick(slot, click)) {
            return false;
        }

        scheduleEditableSnapshot(session);
        return true;
    }

    public void onInventoryClosedByPlayer(ServerPlayer player, Object inventoryHandle) {
        ViewSession<?, ServerPlayer, ItemStackSnapshot> session = currentSession(player);
        if (session != null && session.belongsToInventory(inventoryHandle)) {
            session.close(ViewSession.CloseReason.PLAYER_EXITED);
        }
    }

    public void onPlayerDisconnected(ServerPlayer player) {
        removeNavigator(player);
    }

    protected ViewSession<?, ServerPlayer, ItemStackSnapshot> currentSession(ServerPlayer player) {
        return findNavigator(player).map(ViewNavigator::currentSession).orElse(null);
    }

    protected void scheduleEditableSnapshot(ViewSession<?, ServerPlayer, ItemStackSnapshot> session) {
        Map<Integer, ItemStackSnapshot> snapshot = session.captureEditableSnapshot();
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
