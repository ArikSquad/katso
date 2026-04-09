package eu.mikart.katso.session;

import eu.mikart.katso.view.StatefulView;
import eu.mikart.katso.view.View;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;

public final class ViewNavigator<P, I> {

    private final ViewManager<P, I> manager;
    private final P player;
    private final Deque<NavigationEntry<?, P, I>> stack = new ArrayDeque<>();
    private ViewSession<?, P, I> currentSession;

    ViewNavigator(ViewManager<P, I> manager, P player) {
        this.manager = Objects.requireNonNull(manager, "manager");
        this.player = Objects.requireNonNull(player, "player");
    }

    public P player() {
        return player;
    }

    public ViewSession<?, P, I> currentSession() {
        return currentSession;
    }

    public <S> ViewSession<S, P, I> push(View<S, P, I> view, S state) {
        if (currentSession != null && !currentSession.closed()) {
            pushCurrentToStack();
            currentSession.close(ViewSession.CloseReason.REPLACED);
        }

        ViewSession<S, P, I> session = openSession(view, state, null);
        currentSession = session;
        return session;
    }

    public <S> ViewSession<S, P, I> push(View<S, P, I> view) {
        return push(view, resolveInitialState(view));
    }

    public <S> ViewSession<S, P, I> pushShared(View<S, P, I> view, String contextId, S initialState) {
        return pushShared(view, manager.getOrCreateSharedContext(contextId, initialState));
    }

    public <S> ViewSession<S, P, I> joinShared(View<S, P, I> view, String contextId) {
        SharedContext<S, P, I> context = manager.<S>findSharedContext(contextId)
                .orElseThrow(() -> new IllegalArgumentException("Shared context not found: " + contextId));
        return pushShared(view, context);
    }

    public <S> ViewSession<S, P, I> pushShared(View<S, P, I> view, SharedContext<S, P, I> sharedContext) {
        if (currentSession != null && !currentSession.closed()) {
            pushCurrentToStack();
            currentSession.close(ViewSession.CloseReason.REPLACED);
        }

        ViewSession<S, P, I> session = openSession(view, sharedContext.state(), sharedContext);
        currentSession = session;
        return session;
    }

    public <S> ViewSession<S, P, I> replace(View<S, P, I> view, S state) {
        if (currentSession != null && !currentSession.closed()) {
            currentSession.close(ViewSession.CloseReason.REPLACED);
        }

        ViewSession<S, P, I> session = openSession(view, state, null);
        currentSession = session;
        return session;
    }

    public <S> ViewSession<S, P, I> replace(View<S, P, I> view) {
        return replace(view, resolveInitialState(view));
    }

    public boolean pop() {
        if (stack.isEmpty()) {
            if (currentSession != null && !currentSession.closed()) {
                currentSession.close(ViewSession.CloseReason.PLAYER_EXITED);
            }
            currentSession = null;
            return false;
        }

        if (currentSession != null && !currentSession.closed()) {
            currentSession.close(ViewSession.CloseReason.REPLACED);
        }

        NavigationEntry<?, P, I> entry = stack.pop();
        currentSession = openEntry(entry);
        return true;
    }

    public void popTo(int levels) {
        for (int index = 0; index < levels - 1 && !stack.isEmpty(); index++) {
            stack.pop();
        }
        pop();
    }

    public void popToRoot() {
        while (stack.size() > 1) {
            stack.pop();
        }
        pop();
    }

    public void clear() {
        stack.clear();
        if (currentSession != null && !currentSession.closed()) {
            currentSession.close(ViewSession.CloseReason.SERVER_EXITED);
        }
        currentSession = null;
    }

    public boolean hasStack() {
        return !stack.isEmpty();
    }

    public int depth() {
        return stack.size();
    }

    @SuppressWarnings("unchecked")
    public <S> Optional<NavigationEntry<S, P, I>> peekPrevious() {
        return Optional.ofNullable((NavigationEntry<S, P, I>) stack.peek());
    }

    void onSessionClosed(ViewSession<?, P, I> session, ViewSession.CloseReason reason) {
        if (currentSession != session) {
            return;
        }
        if (reason == ViewSession.CloseReason.PLAYER_EXITED || reason == ViewSession.CloseReason.SERVER_EXITED) {
            stack.clear();
        }
        currentSession = null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void pushCurrentToStack() {
        if (currentSession == null) {
            return;
        }
        stack.push(new NavigationEntry(currentSession.view(), currentSession.state(), currentSession.sharedContext()));
    }

    @SuppressWarnings("unchecked")
    private ViewSession<?, P, I> openEntry(NavigationEntry<?, P, I> entry) {
        if (entry.sharedContext() != null) {
            return openSession((View<Object, P, I>) entry.view(), entry.state(), (SharedContext<Object, P, I>) entry.sharedContext());
        }
        return openSession((View<Object, P, I>) entry.view(), entry.state(), null);
    }

    private <S> ViewSession<S, P, I> openSession(View<S, P, I> view, S state, SharedContext<S, P, I> sharedContext) {
        ViewSession<S, P, I> session = new ViewSession<>(manager, this, view, player, state, sharedContext);
        session.open();
        return session;
    }

    @SuppressWarnings("unchecked")
    private <S> S resolveInitialState(View<S, P, I> view) {
        if (view instanceof StatefulView<?, ?, ?> stateful) {
            return ((StatefulView<S, P, I>) stateful).initialState();
        }
        return null;
    }

    public record NavigationEntry<S, P, I>(View<S, P, I> view, S state, SharedContext<S, P, I> sharedContext) {
    }
}
