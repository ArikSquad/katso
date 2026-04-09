package eu.mikart.katso.minestom;

import eu.mikart.katso.context.ViewClick;
import eu.mikart.katso.context.ViewClickType;
import eu.mikart.katso.session.TopClickDecision;
import eu.mikart.katso.session.ViewManager;
import eu.mikart.katso.session.ViewNavigator;
import eu.mikart.katso.session.ViewSession;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MinestomViewManager extends ViewManager<Player, ItemStack> {

    private final Map<java.util.UUID, Map<Integer, ItemStack>> pendingSnapshots = new ConcurrentHashMap<>();

    public MinestomViewManager() {
        super(new MinestomViewPlatform());
    }

    public void handlePreClick(InventoryPreClickEvent event) {
        Player player = event.getPlayer();
        ViewSession<?, Player, ItemStack> session = currentSession(player);
        if (session == null) {
            return;
        }

        if (event.getInventory() instanceof PlayerInventory) {
            if (!session.belongsToInventory(player.getOpenInventory())) {
                return;
            }

            ViewClick click = toViewClick(event.getClick());
            if (!session.dispatchBottomClick(event.getSlot(), click)) {
                event.setCancelled(true);
                return;
            }
            captureSnapshot(player, session);
            return;
        }

        if (!session.belongsToInventory(event.getInventory())) {
            return;
        }

        ViewClick click = toViewClick(event.getClick());
        List<Integer> draggedSlots = click.isDrag() ? click.draggedSlots().stream().sorted().toList() : List.of();
        TopClickDecision decision = session.decideTopClick(event.getSlot(), click, draggedSlots);

        if (decision.dispatchClickHandler()) {
            session.dispatchTopClick(event.getSlot(), click);
        }

        if (!decision.allowInventoryChange()) {
            event.setCancelled(true);
            return;
        }

        captureSnapshot(player, session);
    }

    public void handlePostClick(InventoryClickEvent event) {
        Player player = event.getPlayer();
        ViewSession<?, Player, ItemStack> session = currentSession(player);
        if (session == null) {
            pendingSnapshots.remove(player.getUuid());
            return;
        }

        Map<Integer, ItemStack> snapshot = pendingSnapshots.remove(player.getUuid());
        if (snapshot == null || !session.belongsToInventory(event.getInventory())) {
            return;
        }

        session.applyEditableSnapshot(snapshot);
    }

    public void handleClose(InventoryCloseEvent event) {
        Player player = event.getPlayer();
        ViewSession<?, Player, ItemStack> session = currentSession(player);
        pendingSnapshots.remove(player.getUuid());
        if (session == null || !session.belongsToInventory(event.getInventory())) {
            return;
        }

        session.close(ViewSession.CloseReason.PLAYER_EXITED);
    }

    public void handleDisconnect(Player player) {
        pendingSnapshots.remove(player.getUuid());
        removeNavigator(player);
    }

    protected ViewClick toViewClick(Click click) {
        if (click instanceof Click.Left) {
            return ViewClick.of(ViewClickType.LEFT);
        }
        if (click instanceof Click.Right) {
            return ViewClick.of(ViewClickType.RIGHT);
        }
        if (click instanceof Click.LeftShift) {
            return ViewClick.of(ViewClickType.SHIFT_LEFT);
        }
        if (click instanceof Click.RightShift) {
            return ViewClick.of(ViewClickType.SHIFT_RIGHT);
        }
        if (click instanceof Click.Middle) {
            return ViewClick.of(ViewClickType.MIDDLE);
        }
        if (click instanceof Click.DropSlot dropSlot) {
            return ViewClick.of(dropSlot.all() ? ViewClickType.CONTROL_DROP : ViewClickType.DROP);
        }
        if (click instanceof Click.DropCursor) {
            return ViewClick.of(ViewClickType.DROP);
        }
        if (click instanceof Click.Double) {
            return ViewClick.of(ViewClickType.DOUBLE_CLICK);
        }
        if (click instanceof Click.HotbarSwap hotbarSwap) {
            return ViewClick.numberKey(hotbarSwap.hotbarSlot());
        }
        if (click instanceof Click.LeftDrag leftDrag) {
            return ViewClick.drag(ViewClickType.DRAG_LEFT, leftDrag.slots());
        }
        if (click instanceof Click.RightDrag rightDrag) {
            return ViewClick.drag(ViewClickType.DRAG_RIGHT, rightDrag.slots());
        }
        if (click instanceof Click.MiddleDrag middleDrag) {
            return ViewClick.drag(ViewClickType.DRAG_LEFT, middleDrag.slots());
        }
        return ViewClick.of(ViewClickType.UNKNOWN);
    }

    protected ViewSession<?, Player, ItemStack> currentSession(Player player) {
        return findNavigator(player)
                .map(ViewNavigator::currentSession)
                .orElse(null);
    }

    private void captureSnapshot(Player player, ViewSession<?, Player, ItemStack> session) {
        Map<Integer, ItemStack> snapshot = session.captureEditableSnapshot();
        if (snapshot.isEmpty()) {
            pendingSnapshots.remove(player.getUuid());
            return;
        }
        pendingSnapshots.put(player.getUuid(), snapshot);
    }
}
