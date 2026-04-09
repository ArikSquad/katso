package eu.mikart.katso.context;

import eu.mikart.katso.platform.ViewInventory;
import eu.mikart.katso.session.ViewManager;
import eu.mikart.katso.session.ViewNavigator;
import eu.mikart.katso.session.ViewSession;
import eu.mikart.katso.view.View;

import java.util.Objects;

public record ViewContext<S, P, I>(
        ViewManager<P, I> manager,
        ViewNavigator<P, I> navigator,
        P player,
        ViewInventory<I> inventory,
        ViewSession<S, P, I> session
) {

    public ViewContext {
        Objects.requireNonNull(manager, "manager");
        Objects.requireNonNull(navigator, "navigator");
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(session, "session");
    }

    public S state() {
        return session.state();
    }

    public View<S, P, I> view() {
        return session.view();
    }

    public <T> ViewSession<T, P, I> push(View<T, P, I> view, T state) {
        return navigator.push(view, state);
    }

    public <T> ViewSession<T, P, I> push(View<T, P, I> view) {
        return navigator.push(view);
    }

    public boolean pop() {
        return navigator.pop();
    }

    public <T> ViewSession<T, P, I> replace(View<T, P, I> view, T state) {
        return navigator.replace(view, state);
    }

    public <T> ViewSession<T, P, I> replace(View<T, P, I> view) {
        return navigator.replace(view);
    }

    public <T> ViewSession<T, P, I> pushShared(View<T, P, I> view, String contextId, T initialState) {
        return navigator.pushShared(view, contextId, initialState);
    }

    public <T> ViewSession<T, P, I> joinShared(View<T, P, I> view, String contextId) {
        return navigator.joinShared(view, contextId);
    }

    public boolean hasBackStack() {
        return navigator.hasStack();
    }

    public int stackDepth() {
        return navigator.depth();
    }

    public void backOrClose() {
        if (!pop()) {
            manager.platform().closeInventory(player);
        }
    }
}
