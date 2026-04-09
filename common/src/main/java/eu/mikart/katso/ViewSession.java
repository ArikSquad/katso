package eu.mikart.katso;

import net.kyori.adventure.text.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class ViewSession<S, P, I> {

    private final ViewManager<P, I> manager;
    private final ViewNavigator<P, I> navigator;
    private final View<S, P, I> view;
    private final P player;
    private final SharedContext<S, P, I> sharedContext;
    private final ViewInventory<I> inventory;
    private final ViewContext<P, I> context;
    private final Map<Integer, ScheduledTask> autoUpdateTasks = new HashMap<>();
    private final Set<ScheduledTask> managedTasks = new HashSet<>();

    private S state;
    private ViewLayout<S, P, I> cachedLayout;
    private boolean layoutDirty = true;
    private Consumer<CloseReason> onCloseHandler;
    private boolean closed;

    ViewSession(ViewManager<P, I> manager,
                ViewNavigator<P, I> navigator,
                View<S, P, I> view,
                P player,
                S initialState,
                SharedContext<S, P, I> sharedContext) {
        this.manager = Objects.requireNonNull(manager, "manager");
        this.navigator = Objects.requireNonNull(navigator, "navigator");
        this.view = Objects.requireNonNull(view, "view");
        this.player = Objects.requireNonNull(player, "player");
        this.sharedContext = sharedContext;
        this.state = sharedContext != null ? sharedContext.state() : initialState;
        this.inventory = manager.platform().createInventory(player, view.configuration().type(),
                view.configuration().titleFunction().apply(state, temporaryContext()));
        this.context = new ViewContext<>(manager, navigator, player, inventory, this);
        if (sharedContext != null) {
            sharedContext.registerSession(this);
        }
    }

    public View<S, P, I> view() {
        return view;
    }

    public P player() {
        return player;
    }

    public ViewInventory<I> inventory() {
        return inventory;
    }

    public SharedContext<S, P, I> sharedContext() {
        return sharedContext;
    }

    public ViewContext<P, I> context() {
        return context;
    }

    public S state() {
        return state;
    }

    public boolean closed() {
        return closed;
    }

    void open() {
        render();
        manager.platform().openInventory(player, inventory);
        view.onOpen(state, context);
    }

    public boolean belongsToInventory(Object inventoryHandle) {
        return Objects.equals(inventory.handle(), inventoryHandle);
    }

    public TopClickDecision decideTopClick(int slot, ViewClick click, Collection<Integer> affectedSlots) {
        if (closed || cachedLayout == null) {
            return TopClickDecision.cancel();
        }

        if (affectedSlots != null && !affectedSlots.isEmpty()) {
            if (click.isHotbarSwap() && !cachedLayout.allowHotkey()) {
                return TopClickDecision.cancel();
            }
            return affectedSlots.stream().allMatch(cachedLayout::isEditable)
                    ? TopClickDecision.allowEdit()
                    : TopClickDecision.cancel();
        }

        if (cachedLayout.isEditable(slot)) {
            if (click.isHotbarSwap() && !cachedLayout.allowHotkey()) {
                return TopClickDecision.cancel();
            }
            return TopClickDecision.allowEdit();
        }

        return TopClickDecision.cancelAndDispatch();
    }

    public void dispatchTopClick(int slot, ViewClick click) {
        if (closed || cachedLayout == null) {
            return;
        }

        ClickContext<S, P> clickContext = new ClickContext<>(slot, click, player, state);
        ViewComponent<S, P, I> component = cachedLayout.components().get(slot);
        if (component == null) {
            view.onClick(clickContext, context);
            return;
        }
        component.onClick().accept(clickContext, context);
    }

    public boolean dispatchBottomClick(int slot, ViewClick click) {
        return view.onBottomClick(new ClickContext<>(slot, click, player, state), context);
    }

    public Map<Integer, I> captureEditableSnapshot() {
        if (cachedLayout == null) {
            return Map.of();
        }

        Map<Integer, I> snapshot = new HashMap<>();
        for (int slot : cachedLayout.editableSlots()) {
            snapshot.put(slot, manager.platform().copyItem(inventory.getItem(slot)));
        }
        return snapshot;
    }

    public void applyEditableSnapshot(Map<Integer, I> snapshot) {
        if (closed || snapshot.isEmpty() || cachedLayout == null) {
            return;
        }

        Map<Integer, I> changedSlots = new HashMap<>();
        for (Map.Entry<Integer, I> entry : snapshot.entrySet()) {
            int slot = entry.getKey();
            I oldItem = manager.platform().copyItem(entry.getValue());
            I newItem = manager.platform().copyItem(inventory.getItem(slot));
            if (!cachedLayout.isEditable(slot) || manager.platform().itemsEqual(oldItem, newItem)) {
                continue;
            }

            ViewComponent<S, P, I> component = cachedLayout.components().get(slot);
            if (component != null && component.changeHandler() != null) {
                component.changeHandler().onChange(slot, oldItem, newItem, state);
            }
            changedSlots.put(slot, newItem);
        }

        if (changedSlots.isEmpty()) {
            return;
        }

        if (sharedContext != null) {
            sharedContext.applySlotChanges(changedSlots);
            return;
        }

        render();
    }

    public void refresh() {
        layoutDirty = true;
        render();
    }

    public void render() {
        if (closed) {
            return;
        }

        boolean rebuildLayout = cachedLayout == null || layoutDirty;
        if (rebuildLayout) {
            cachedLayout = new ViewLayout<>(view.configuration().type());
            view.layout(cachedLayout, state, context);
            layoutDirty = false;
        }

        Component title = view.configuration().titleFunction().apply(state, context);
        inventory.setTitle(title);

        Set<Integer> renderedSlots = new HashSet<>();
        for (Map.Entry<Integer, ViewComponent<S, P, I>> entry : cachedLayout.components().entrySet()) {
            int slot = entry.getKey();
            ViewComponent<S, P, I> component = entry.getValue();
            renderedSlots.add(slot);

            if (component.behavior() == SlotBehavior.EDITABLE) {
                renderEditableSlot(slot, component);
            } else {
                renderSlot(slot, component);
            }
        }

        if (rebuildLayout) {
            clearRemovedSlots(renderedSlots);
            rescheduleAutoUpdates();
        }

        view.onRefresh(state, context);
    }

    public void setState(S newState) {
        state = newState;
        layoutDirty = true;

        if (sharedContext != null) {
            sharedContext.setState(newState);
            return;
        }

        render();
    }

    public void setStateQuiet(S newState) {
        state = newState;
        layoutDirty = true;
        render();
    }

    void setStateFromShared(S newState) {
        state = newState;
        layoutDirty = true;
        render();
    }

    void renderFromShared() {
        render();
    }

    public void update(UnaryOperator<S> updater) {
        setState(updater.apply(state));
    }

    public void updateQuiet(UnaryOperator<S> updater) {
        setStateQuiet(updater.apply(state));
    }

    @SuppressWarnings("unchecked")
    public <T> void updateUnchecked(Function<T, T> updater) {
        setState((S) updater.apply((T) state));
    }

    public ViewSession<S, P, I> onClose(Consumer<CloseReason> handler) {
        onCloseHandler = handler;
        return this;
    }

    public ViewSession<S, P, I> refreshEvery(Duration interval) {
        ScheduledTask task = manager.platform().scheduleRepeating(interval, this::render);
        managedTasks.add(task);
        return this;
    }

    public void close(CloseReason reason) {
        if (closed) {
            return;
        }

        closed = true;

        autoUpdateTasks.values().forEach(ScheduledTask::cancel);
        autoUpdateTasks.clear();
        managedTasks.forEach(ScheduledTask::cancel);
        managedTasks.clear();

        if (sharedContext != null) {
            sharedContext.unregisterSession(this);
        }

        view.onClose(state, context, reason);
        if (onCloseHandler != null) {
            onCloseHandler.accept(reason);
        }
        navigator.onSessionClosed(this, reason);

        if (reason == CloseReason.SERVER_EXITED) {
            manager.platform().closeInventory(player);
        }
    }

    private ViewContext<P, I> temporaryContext() {
        return new ViewContext<>(manager, navigator, player, null, this);
    }

    private void renderEditableSlot(int slot, ViewComponent<S, P, I> component) {
        if (sharedContext != null) {
            if (!sharedContext.hasSlotItem(slot)) {
                sharedContext.initializeSlotItem(slot, component.render().apply(state, context));
            }
            I targetItem = sharedContext.getSlotItem(slot);
            if (!manager.platform().itemsEqual(inventory.getItem(slot), targetItem)) {
                inventory.setItem(slot, targetItem);
            }
            return;
        }

        I targetItem = component.render().apply(state, context);
        if (!manager.platform().itemsEqual(inventory.getItem(slot), targetItem)) {
            inventory.setItem(slot, targetItem);
        }
    }

    private void renderSlot(int slot, ViewComponent<S, P, I> component) {
        I item = component.render().apply(state, context);
        if (!manager.platform().itemsEqual(inventory.getItem(slot), item)) {
            inventory.setItem(slot, item);
        }
    }

    private void clearRemovedSlots(Set<Integer> renderedSlots) {
        for (int slot = 0; slot < inventory.size(); slot++) {
            if (renderedSlots.contains(slot)) {
                continue;
            }
            I emptyItem = manager.platform().emptyItem();
            if (!manager.platform().itemsEqual(inventory.getItem(slot), emptyItem)) {
                inventory.setItem(slot, emptyItem);
            }
        }
    }

    private void rescheduleAutoUpdates() {
        autoUpdateTasks.values().forEach(ScheduledTask::cancel);
        autoUpdateTasks.clear();

        for (Map.Entry<Integer, ViewComponent<S, P, I>> entry : cachedLayout.components().entrySet()) {
            ViewComponent<S, P, I> component = entry.getValue();
            if (component.updateInterval() == null) {
                continue;
            }

            int slot = entry.getKey();
            ScheduledTask task = manager.platform().scheduleRepeating(component.updateInterval(), () -> {
                if (closed) {
                    return;
                }
                renderSlot(slot, component);
            });
            autoUpdateTasks.put(slot, task);
        }
    }

    public enum CloseReason {
        PLAYER_EXITED,
        SERVER_EXITED,
        REPLACED
    }
}
