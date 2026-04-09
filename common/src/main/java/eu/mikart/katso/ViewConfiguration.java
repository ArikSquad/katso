package eu.mikart.katso;

import net.kyori.adventure.text.Component;

import java.util.Objects;
import java.util.function.BiFunction;

public final class ViewConfiguration<S, P, I> {

    private final BiFunction<S, ViewContext<P, I>, Component> titleFunction;
    private final ViewType type;

    public ViewConfiguration(Component title, ViewType type) {
        this((BiFunction<S, ViewContext<P, I>, Component>) (state, context) -> title, type);
    }

    public ViewConfiguration(String title, ViewType type) {
        this((BiFunction<S, ViewContext<P, I>, Component>) (state, context) -> Component.text(title), type);
    }

    public ViewConfiguration(Title<S, P, I> title, ViewType type) {
        this((BiFunction<S, ViewContext<P, I>, Component>) title::title, type);
    }

    public ViewConfiguration(StringTitle<S, P, I> title, ViewType type) {
        this((BiFunction<S, ViewContext<P, I>, Component>) (state, context) -> Component.text(title.title(state, context)), type);
    }

    private ViewConfiguration(BiFunction<S, ViewContext<P, I>, Component> titleFunction, ViewType type) {
        this.titleFunction = Objects.requireNonNull(titleFunction, "titleFunction");
        this.type = Objects.requireNonNull(type, "type");
    }

    public BiFunction<S, ViewContext<P, I>, Component> titleFunction() {
        return titleFunction;
    }

    public ViewType type() {
        return type;
    }

    public static <S, P, I> ViewConfiguration<S, P, I> withTitle(Title<S, P, I> title, ViewType type) {
        return new ViewConfiguration<>(title, type);
    }

    public static <S, P, I> ViewConfiguration<S, P, I> withString(StringTitle<S, P, I> title, ViewType type) {
        return new ViewConfiguration<>(title, type);
    }

    @FunctionalInterface
    public interface Title<S, P, I> {

        Component title(S state, ViewContext<P, I> context);
    }

    @FunctionalInterface
    public interface StringTitle<S, P, I> {

        String title(S state, ViewContext<P, I> context);
    }
}
