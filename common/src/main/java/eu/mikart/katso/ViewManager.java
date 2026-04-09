package eu.mikart.katso;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ViewManager<P, I> {

    private final ViewPlatform<P, I> platform;
    private final Map<UUID, ViewNavigator<P, I>> navigators = new ConcurrentHashMap<>();
    private final Map<String, SharedContext<?, P, I>> sharedContexts = new ConcurrentHashMap<>();

    public ViewManager(ViewPlatform<P, I> platform) {
        this.platform = Objects.requireNonNull(platform, "platform");
    }

    public ViewPlatform<P, I> platform() {
        return platform;
    }

    public ViewNavigator<P, I> navigator(P player) {
        return navigators.computeIfAbsent(platform.playerId(player), ignored -> new ViewNavigator<>(this, player));
    }

    public Optional<ViewNavigator<P, I>> findNavigator(P player) {
        return Optional.ofNullable(navigators.get(platform.playerId(player)));
    }

    public void removeNavigator(P player) {
        ViewNavigator<P, I> navigator = navigators.remove(platform.playerId(player));
        if (navigator != null) {
            navigator.clear();
        }
    }

    public boolean hasSharedContext(String id) {
        return sharedContexts.containsKey(id);
    }

    public <S> SharedContext<S, P, I> createSharedContext(String id, S initialState) {
        SharedContext<S, P, I> context = new SharedContext<>(this, id, initialState);
        SharedContext<?, P, I> previous = sharedContexts.putIfAbsent(id, context);
        if (previous != null) {
            throw new IllegalArgumentException("Shared context already exists: " + id);
        }
        return context;
    }

    @SuppressWarnings("unchecked")
    public <S> SharedContext<S, P, I> getOrCreateSharedContext(String id, S initialState) {
        return (SharedContext<S, P, I>) sharedContexts.computeIfAbsent(id,
                ignored -> new SharedContext<>(this, id, initialState));
    }

    @SuppressWarnings("unchecked")
    public <S> Optional<SharedContext<S, P, I>> findSharedContext(String id) {
        return Optional.ofNullable((SharedContext<S, P, I>) sharedContexts.get(id));
    }

    public void removeSharedContext(String id) {
        SharedContext<?, P, I> context = sharedContexts.remove(id);
        if (context != null) {
            context.closeAll();
        }
    }

    void removeSharedContext(String id, SharedContext<?, P, I> context) {
        sharedContexts.remove(id, context);
    }
}
