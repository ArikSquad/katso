package eu.mikart.katso.spigot;

import eu.mikart.katso.context.ViewClick;
import eu.mikart.katso.context.ViewClickType;
import eu.mikart.katso.session.TopClickDecision;
import eu.mikart.katso.session.ViewManager;
import eu.mikart.katso.session.ViewNavigator;
import eu.mikart.katso.session.ViewSession;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.DragType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;

public class SpigotViewManager extends ViewManager<Player, ItemStack> implements Listener {

    public SpigotViewManager(Plugin plugin) {
        this(plugin, SpigotComponentBridge.legacySection());
    }

    public SpigotViewManager(Plugin plugin, SpigotComponentBridge componentBridge) {
        super(new SpigotViewPlatform(plugin, componentBridge));
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ViewSession<?, Player, ItemStack> session = currentSession(player);
        if (session == null || !session.belongsToInventory(event.getView().getTopInventory())) {
            return;
        }

        int topSize = event.getView().getTopInventory().getSize();
        if (event.getRawSlot() >= 0 && event.getRawSlot() < topSize) {
            handleTopClick(event, session);
            return;
        }

        if (event.getClickedInventory() == null) {
            return;
        }

        ViewClick click = toViewClick(event.getClick(), event.getHotbarButton());
        if (!session.dispatchBottomClick(event.getSlot(), click)) {
            event.setCancelled(true);
            return;
        }

        scheduleEditableSnapshot(session);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ViewSession<?, Player, ItemStack> session = currentSession(player);
        if (session == null || !session.belongsToInventory(event.getView().getTopInventory())) {
            return;
        }

        List<Integer> topSlots = event.getRawSlots().stream()
                .filter(rawSlot -> rawSlot >= 0 && rawSlot < event.getView().getTopInventory().getSize())
                .sorted()
                .toList();
        if (topSlots.isEmpty()) {
            return;
        }

        ViewClick click = ViewClick.drag(
                event.getType() == DragType.EVEN ? ViewClickType.DRAG_LEFT : ViewClickType.DRAG_RIGHT,
                topSlots
        );
        TopClickDecision decision = session.decideTopClick(topSlots.getFirst(), click, topSlots);
        if (!decision.allowInventoryChange()) {
            event.setCancelled(true);
            return;
        }

        scheduleEditableSnapshot(session);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        ViewSession<?, Player, ItemStack> session = currentSession(player);
        if (session == null || !session.belongsToInventory(event.getInventory())) {
            return;
        }

        session.close(ViewSession.CloseReason.PLAYER_EXITED);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removeNavigator(event.getPlayer());
    }

    protected ViewClick toViewClick(ClickType clickType, int hotbarButton) {
        return switch (clickType) {
            case LEFT -> ViewClick.of(ViewClickType.LEFT);
            case RIGHT -> ViewClick.of(ViewClickType.RIGHT);
            case SHIFT_LEFT -> ViewClick.of(ViewClickType.SHIFT_LEFT);
            case SHIFT_RIGHT -> ViewClick.of(ViewClickType.SHIFT_RIGHT);
            case MIDDLE -> ViewClick.of(ViewClickType.MIDDLE);
            case NUMBER_KEY -> ViewClick.numberKey(hotbarButton);
            case DROP -> ViewClick.of(ViewClickType.DROP);
            case CONTROL_DROP -> ViewClick.of(ViewClickType.CONTROL_DROP);
            case DOUBLE_CLICK -> ViewClick.of(ViewClickType.DOUBLE_CLICK);
            default -> ViewClick.of(ViewClickType.UNKNOWN);
        };
    }

    protected ViewSession<?, Player, ItemStack> currentSession(Player player) {
        return findNavigator(player)
                .map(ViewNavigator::currentSession)
                .orElse(null);
    }

    protected void scheduleEditableSnapshot(ViewSession<?, Player, ItemStack> session) {
        Map<Integer, ItemStack> snapshot = session.captureEditableSnapshot();
        if (snapshot.isEmpty()) {
            return;
        }

        platform().scheduleNextTick(() -> {
            if (!session.closed()) {
                session.applyEditableSnapshot(snapshot);
            }
        });
    }

    private void handleTopClick(InventoryClickEvent event, ViewSession<?, Player, ItemStack> session) {
        int slot = event.getRawSlot();
        ViewClick click = toViewClick(event.getClick(), event.getHotbarButton());
        TopClickDecision decision = session.decideTopClick(slot, click, List.of());

        if (decision.dispatchClickHandler()) {
            session.dispatchTopClick(slot, click);
        }

        if (!decision.allowInventoryChange()) {
            event.setCancelled(true);
            return;
        }

        scheduleEditableSnapshot(session);
    }
}
