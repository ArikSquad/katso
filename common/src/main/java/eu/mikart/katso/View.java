package eu.mikart.katso;

public interface View<S, P, I> {

    ViewConfiguration<S, P, I> configuration();

    void layout(ViewLayout<S, P, I> layout, S state, ViewContext<P, I> context);

    default void onOpen(S state, ViewContext<P, I> context) {
    }

    default void onClose(S state, ViewContext<P, I> context, ViewSession.CloseReason reason) {
    }

    default void onClick(ClickContext<S, P> click, ViewContext<P, I> context) {
    }

    default void onRefresh(S state, ViewContext<P, I> context) {
    }

    default boolean onBottomClick(ClickContext<S, P> click, ViewContext<P, I> context) {
        return false;
    }
}
