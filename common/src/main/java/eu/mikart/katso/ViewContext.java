package eu.mikart.katso;

public record ViewContext<P, I>(
        ViewManager<P, I> manager,
        ViewNavigator<P, I> navigator,
        P player,
        ViewInventory<I> inventory,
        ViewSession<?, P, I> rawSession
) {

    @SuppressWarnings("unchecked")
    public <S> ViewSession<S, P, I> session() {
        return (ViewSession<S, P, I>) rawSession;
    }

    public <S> ViewSession<S, P, I> push(View<S, P, I> view, S state) {
        return navigator.push(view, state);
    }

    public <S> ViewSession<S, P, I> push(View<S, P, I> view) {
        return navigator.push(view);
    }

    public boolean pop() {
        return navigator.pop();
    }

    public <S> ViewSession<S, P, I> replace(View<S, P, I> view, S state) {
        return navigator.replace(view, state);
    }

    public <S> ViewSession<S, P, I> replace(View<S, P, I> view) {
        return navigator.replace(view);
    }

    public <S> ViewSession<S, P, I> pushShared(View<S, P, I> view, String contextId, S initialState) {
        return navigator.pushShared(view, contextId, initialState);
    }

    public <S> ViewSession<S, P, I> joinShared(View<S, P, I> view, String contextId) {
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
