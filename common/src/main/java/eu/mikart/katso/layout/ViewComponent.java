package eu.mikart.katso.layout;

import eu.mikart.katso.context.ClickContext;
import eu.mikart.katso.context.ViewContext;

import java.time.Duration;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public record ViewComponent<S, P, I>(
        int slot,
        BiFunction<S, ViewContext<S, P, I>, I> render,
        BiConsumer<ClickContext<S, P, I>, ViewContext<S, P, I>> onClick,
        SlotBehavior behavior,
        SlotChangeHandler<S, I> changeHandler,
        Duration updateInterval
) {

    public ViewComponent {
        render = Objects.requireNonNull(render, "render");
        onClick = Objects.requireNonNull(onClick, "onClick");
        behavior = Objects.requireNonNull(behavior, "behavior");
    }

    public ViewComponent(int slot, BiFunction<S, ViewContext<S, P, I>, I> render,
                         BiConsumer<ClickContext<S, P, I>, ViewContext<S, P, I>> onClick) {
        this(slot, render, onClick, SlotBehavior.UI, null, null);
    }

    public static <S, P, I> ViewComponent<S, P, I> staticItem(int slot, I item) {
        return new ViewComponent<>(slot, (state, context) -> item, (click, context) -> {
        }, SlotBehavior.UI, null, null);
    }

    public static <S, P, I> ViewComponent<S, P, I> autoUpdating(
            int slot,
            BiFunction<S, ViewContext<S, P, I>, I> render,
            Duration updateInterval
    ) {
        return new ViewComponent<>(slot, render, (click, context) -> {
        }, SlotBehavior.UI, null, updateInterval);
    }

    public static <S, P, I> ViewComponent<S, P, I> autoUpdating(
            int slot,
            BiFunction<S, ViewContext<S, P, I>, I> render,
            BiConsumer<ClickContext<S, P, I>, ViewContext<S, P, I>> onClick,
            Duration updateInterval
    ) {
        return new ViewComponent<>(slot, render, onClick, SlotBehavior.UI, null, updateInterval);
    }

    public static <S, P, I> ViewComponent<S, P, I> clickable(
            int slot,
            BiFunction<S, ViewContext<S, P, I>, I> render,
            BiConsumer<ClickContext<S, P, I>, ViewContext<S, P, I>> onClick
    ) {
        return new ViewComponent<>(slot, render, onClick, SlotBehavior.UI, null, null);
    }

    public static <S, P, I> ViewComponent<S, P, I> editable(
            int slot,
            BiFunction<S, ViewContext<S, P, I>, I> render,
            SlotChangeHandler<S, I> changeHandler
    ) {
        return new ViewComponent<>(slot, render, (click, context) -> {
        }, SlotBehavior.EDITABLE, changeHandler, null);
    }

    public static <S, P, I> ViewComponent<S, P, I> editable(int slot, I item, SlotChangeHandler<S, I> changeHandler) {
        return new ViewComponent<>(slot, (state, context) -> item, (click, context) -> {
        }, SlotBehavior.EDITABLE, changeHandler, null);
    }
}
