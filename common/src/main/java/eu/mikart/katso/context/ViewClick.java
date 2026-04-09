package eu.mikart.katso.context;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public record ViewClick(ViewClickType type, int hotbarButton, Set<Integer> draggedSlots) {

    public ViewClick {
        type = type == null ? ViewClickType.UNKNOWN : type;
        hotbarButton = Math.max(-1, hotbarButton);
        draggedSlots = Set.copyOf(draggedSlots == null ? Set.of() : new LinkedHashSet<>(draggedSlots));
    }

    public static ViewClick of(ViewClickType type) {
        return new ViewClick(type, -1, Set.of());
    }

    public static ViewClick numberKey(int hotbarButton) {
        return new ViewClick(ViewClickType.NUMBER_KEY, hotbarButton, Set.of());
    }

    public static ViewClick drag(ViewClickType type, Collection<Integer> draggedSlots) {
        return new ViewClick(type, -1, new LinkedHashSet<>(draggedSlots));
    }

    public boolean isShiftClick() {
        return type == ViewClickType.SHIFT_LEFT || type == ViewClickType.SHIFT_RIGHT;
    }

    public boolean isDrag() {
        return type == ViewClickType.DRAG_LEFT || type == ViewClickType.DRAG_RIGHT;
    }

    public boolean isHotbarSwap() {
        return type == ViewClickType.NUMBER_KEY;
    }

    public boolean isLeftClick() {
        return type == ViewClickType.LEFT
                || type == ViewClickType.SHIFT_LEFT
                || type == ViewClickType.DRAG_LEFT;
    }
}
