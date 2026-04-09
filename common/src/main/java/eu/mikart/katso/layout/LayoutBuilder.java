package eu.mikart.katso.layout;

import eu.mikart.katso.context.ClickContext;
import eu.mikart.katso.context.ViewContext;
import eu.mikart.katso.view.ViewType;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

public final class LayoutBuilder<S, P, I> {

    private final Map<Integer, ViewComponent<S, P, I>> components = new HashMap<>();
    private final ViewType type;
    private boolean allowHotbarSwap;

    public LayoutBuilder(ViewType type) {
        this.type = Objects.requireNonNull(type, "type");
    }

    public Map<Integer, ViewComponent<S, P, I>> components() {
        return Collections.unmodifiableMap(components);
    }

    public ViewType type() {
        return type;
    }

    public boolean allowHotbarSwap() {
        return allowHotbarSwap;
    }

    public void allowHotbarSwap(boolean allowHotbarSwap) {
        this.allowHotbarSwap = allowHotbarSwap;
    }

    public void slot(
            int slot,
            BiFunction<S, ViewContext<S, P, I>, I> render,
            BiConsumer<ClickContext<S, P, I>, ViewContext<S, P, I>> onClick
    ) {
        validateSlot(slot);
        components.put(slot, new ViewComponent<>(slot, render, onClick));
    }

    public void slot(int slot, BiFunction<S, ViewContext<S, P, I>, I> render) {
        slot(slot, render, (click, context) -> {
        });
    }

    public void slot(int slot, I item, BiConsumer<ClickContext<S, P, I>, ViewContext<S, P, I>> onClick) {
        validateSlot(slot);
        components.put(slot, new ViewComponent<>(slot, (state, context) -> item, onClick));
    }

    public void slot(int slot, I item, UnaryOperator<S> stateUpdater) {
        validateSlot(slot);
        components.put(slot, new ViewComponent<>(slot, (state, context) -> item,
                (click, context) -> context.session().update(stateUpdater)));
    }

    public void slot(int slot, I item) {
        validateSlot(slot);
        components.put(slot, ViewComponent.staticItem(slot, item));
    }

    public void slots(Collection<Integer> slots,
                      BiFunction<S, ViewContext<S, P, I>, I> render,
                      BiConsumer<ClickContext<S, P, I>, ViewContext<S, P, I>> onClick) {
        slots.forEach(slot -> slot(slot, render, onClick));
    }

    public void slots(Collection<Integer> slots, BiFunction<S, ViewContext<S, P, I>, I> render) {
        slots.forEach(slot -> slot(slot, render));
    }

    public void editable(int slot, BiFunction<S, ViewContext<S, P, I>, I> render, SlotChangeHandler<S, I> onChange) {
        validateSlot(slot);
        components.put(slot, ViewComponent.editable(slot, render, onChange));
    }

    public void editable(int slot, I item, SlotChangeHandler<S, I> onChange) {
        validateSlot(slot);
        components.put(slot, ViewComponent.editable(slot, item, onChange));
    }

    public void editable(int slot) {
        validateSlot(slot);
        components.put(slot, new ViewComponent<>(slot,
                (state, context) -> context.manager().platform().emptyItem(),
                (click, context) -> {
                },
                SlotBehavior.EDITABLE,
                (changedSlot, oldItem, newItem, state) -> {
                },
                null));
    }

    public void editable(int slot, SlotChangeHandler<S, I> onChange) {
        editable(slot, (state, context) -> context.manager().platform().emptyItem(), onChange);
    }

    public void editableSlots(Collection<Integer> slots, SlotChangeHandler<S, I> onChange) {
        slots.forEach(slot -> editable(slot, onChange));
    }

    public void editableSlots(Collection<Integer> slots) {
        slots.forEach(this::editable);
    }

    public void editableGrid(int startSlot, int endSlot, SlotChangeHandler<S, I> onChange) {
        editableSlots(Layouts.rectangle(startSlot, endSlot), onChange);
    }

    public void autoUpdating(int slot, BiFunction<S, ViewContext<S, P, I>, I> render, Duration updateInterval) {
        validateSlot(slot);
        components.put(slot, ViewComponent.autoUpdating(slot, render, updateInterval));
    }

    public void autoUpdating(int slot,
                             BiFunction<S, ViewContext<S, P, I>, I> render,
                             BiConsumer<ClickContext<S, P, I>, ViewContext<S, P, I>> onClick,
                             Duration updateInterval) {
        validateSlot(slot);
        components.put(slot, ViewComponent.autoUpdating(slot, render, onClick, updateInterval));
    }

    public void filler(Collection<Integer> slots, I item) {
        slots.forEach(slot -> {
            validateSlot(slot);
            components.put(slot, ViewComponent.staticItem(slot, item));
        });
    }

    public void filler(Collection<Integer> slots, BiFunction<S, ViewContext<S, P, I>, I> render) {
        slots.forEach(slot -> {
            validateSlot(slot);
            components.put(slot, new ViewComponent<>(slot, render, (click, context) -> {
            }));
        });
    }

    public void filler(I item) {
        IntStream.range(0, type.size()).forEach(slot -> components.put(slot, ViewComponent.staticItem(slot, item)));
    }

    public SlotBehavior behaviorAt(int slot) {
        ViewComponent<S, P, I> component = components.get(slot);
        return component != null ? component.behavior() : SlotBehavior.NO_RENDER;
    }

    public boolean isEditable(int slot) {
        return behaviorAt(slot) == SlotBehavior.EDITABLE;
    }

    public Set<Integer> editableSlots() {
        Set<Integer> editable = new HashSet<>();
        components.forEach((slot, component) -> {
            if (component.behavior() == SlotBehavior.EDITABLE) {
                editable.add(slot);
            }
        });
        return editable;
    }

    private void validateSlot(int slot) {
        if (slot < 0 || slot >= type.size()) {
            throw new IllegalArgumentException("Slot " + slot + " is outside inventory size " + type.size());
        }
    }
}
