package eu.mikart.katso.session;

import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

public final class SharedContext<S, P, I> {

    private final ViewManager<P, I> manager;
    private final String id;
    private volatile S state;
    private final Map<Integer, I> slotItems = new ConcurrentHashMap<>();
    private final Set<ViewSession<S, P, I>> sessions = new CopyOnWriteArraySet<>();
    private final List<Consumer<SlotChange<I>>> slotChangeListeners = new CopyOnWriteArrayList<>();

    SharedContext(ViewManager<P, I> manager, String id, S initialState) {
        this.manager = Objects.requireNonNull(manager, "manager");
        this.id = Objects.requireNonNull(id, "id");
        this.state = initialState;
    }

    public String id() {
        return id;
    }

    public S state() {
        return state;
    }

    public void setState(S newState) {
        state = newState;
        broadcastState();
    }

    public boolean hasSlotItem(int slot) {
        return slotItems.containsKey(slot);
    }

    public I getSlotItem(int slot) {
        return slotItems.getOrDefault(slot, manager.platform().emptyItem());
    }

    public Map<Integer, I> getAllSlotItems() {
        return new HashMap<>(slotItems);
    }

    public void setSlotItem(int slot, I item) {
        applySlotChanges(Map.of(slot, item));
    }

    public void setSlotItems(Map<Integer, I> items) {
        applySlotChanges(items);
    }

    public void clearSlotItems() {
        Map<Integer, I> cleared = new HashMap<>();
        for (Integer slot : slotItems.keySet()) {
            cleared.put(slot, manager.platform().emptyItem());
        }
        applySlotChanges(cleared);
    }

    public SharedContext<S, P, I> onSlotChange(Consumer<SlotChange<I>> listener) {
        slotChangeListeners.add(listener);
        return this;
    }

    public int sessionCount() {
        return sessions.size();
    }

    public Set<P> viewers() {
        Set<P> viewers = new HashSet<>();
        for (ViewSession<S, P, I> session : sessions) {
            if (!session.closed()) {
                viewers.add(session.player());
            }
        }
        return viewers;
    }

    public void broadcastMessage(Component message) {
        viewers().forEach(player -> manager.platform().sendMessage(player, message));
    }

    public void closeAll() {
        for (ViewSession<S, P, I> session : new ArrayList<>(sessions)) {
            session.close(ViewSession.CloseReason.SERVER_EXITED);
        }
    }

    void initializeSlotItem(int slot, I item) {
        slotItems.putIfAbsent(slot, manager.platform().copyItem(item));
    }

    void registerSession(ViewSession<S, P, I> session) {
        sessions.add(session);
        slotItems.forEach(session.inventory()::setItem);
        session.renderFromShared();
    }

    void unregisterSession(ViewSession<S, P, I> session) {
        sessions.remove(session);
        if (sessions.isEmpty()) {
            manager.removeSharedContext(id, this);
            return;
        }
        broadcastRender();
    }

    void broadcastState() {
        for (ViewSession<S, P, I> session : sessions) {
            if (!session.closed()) {
                session.setStateFromShared(state);
            }
        }
    }

    void broadcastRender() {
        for (ViewSession<S, P, I> session : sessions) {
            if (!session.closed()) {
                session.renderFromShared();
            }
        }
    }

    void applySlotChanges(Map<Integer, I> changes) {
        if (changes.isEmpty()) {
            return;
        }

        Map<Integer, SlotChange<I>> appliedChanges = new HashMap<>();
        for (Map.Entry<Integer, I> entry : changes.entrySet()) {
            int slot = entry.getKey();
            I newItem = entry.getValue();
            I oldItem = slotItems.getOrDefault(slot, manager.platform().emptyItem());

            if (manager.platform().itemsEqual(oldItem, newItem)) {
                continue;
            }

            I storedItem = manager.platform().copyItem(newItem);
            slotItems.put(slot, storedItem);
            appliedChanges.put(slot, new SlotChange<>(slot, oldItem, storedItem));
        }

        if (appliedChanges.isEmpty()) {
            return;
        }

        for (SlotChange<I> change : appliedChanges.values()) {
            slotChangeListeners.forEach(listener -> listener.accept(change));
        }

        for (ViewSession<S, P, I> session : sessions) {
            if (session.closed()) {
                continue;
            }
            for (SlotChange<I> change : appliedChanges.values()) {
                session.inventory().setItem(change.slot(), change.newItem());
            }
        }

        broadcastRender();
    }

    public record SlotChange<I>(int slot, I oldItem, I newItem) {
    }
}
