package eu.mikart.katso.view;

import net.kyori.adventure.text.Component;

/** Small entry points that make the public API read naturally at call sites. */
public final class Views {
    private Views() {}

    public static ViewType chest(int rows) { return ViewType.chest(rows); }
    public static ViewType menu(MenuKind kind) { return ViewType.menu(kind); }

    public static <S, P, I> ViewConfig.Builder<S, P, I> configure(ViewType type) {
        return ViewConfig.builder(type);
    }

    public static <S, P, I> ViewConfig<S, P, I> titled(ViewType type, String title) {
        return ViewConfig.of(type, title);
    }

    public static <S, P, I> ViewConfig<S, P, I> titled(ViewType type, Component title) {
        return ViewConfig.of(type, title);
    }
}
