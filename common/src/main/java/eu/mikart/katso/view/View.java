package eu.mikart.katso.view;

import eu.mikart.katso.context.ClickContext;
import eu.mikart.katso.context.ViewContext;
import eu.mikart.katso.layout.LayoutBuilder;
import eu.mikart.katso.session.ViewSession;

public interface View<S, P, I> {

    ViewConfig<S, P, I> config();

    void render(LayoutBuilder<S, P, I> layout, ViewContext<S, P, I> context);

    default void onOpen(ViewContext<S, P, I> context) {
    }

    default void onClose(ViewContext<S, P, I> context, ViewSession.CloseReason reason) {
    }

    default void onClick(ClickContext<S, P, I> click) {
    }

    default void onRefresh(ViewContext<S, P, I> context) {
    }

    default boolean onBottomClick(ClickContext<S, P, I> click) {
        return false;
    }
}
