package eu.mikart.katso.view;

import eu.mikart.katso.context.ViewContext;
import net.kyori.adventure.text.Component;

import java.util.Objects;

public final class ViewConfig<S, P, I> {

    private final ViewType type;
    private final Title<S, P, I> title;

    private ViewConfig(ViewType type, Title<S, P, I> title) {
        this.type = Objects.requireNonNull(type, "type");
        this.title = Objects.requireNonNull(title, "title");
    }

    public static <S, P, I> Builder<S, P, I> builder(ViewType type) {
        return new Builder<>(type);
    }

    public static <S, P, I> ViewConfig<S, P, I> of(ViewType type, Component title) {
        return ViewConfig.<S, P, I>builder(type).title(title).build();
    }

    public static <S, P, I> ViewConfig<S, P, I> of(ViewType type, String title) {
        return ViewConfig.<S, P, I>builder(type).title(title).build();
    }

    public ViewType type() {
        return type;
    }

    public Component title(ViewContext<S, P, I> context) {
        return title.resolve(context);
    }

    @FunctionalInterface
    public interface Title<S, P, I> {

        Component resolve(ViewContext<S, P, I> context);
    }

    public static final class Builder<S, P, I> {

        private final ViewType type;
        private Title<S, P, I> title = context -> Component.empty();

        private Builder(ViewType type) {
            this.type = Objects.requireNonNull(type, "type");
        }

        public Builder<S, P, I> title(Component title) {
            Objects.requireNonNull(title, "title");
            this.title = context -> title;
            return this;
        }

        public Builder<S, P, I> title(String title) {
            Objects.requireNonNull(title, "title");
            this.title = context -> Component.text(title);
            return this;
        }

        public Builder<S, P, I> title(Title<S, P, I> title) {
            this.title = Objects.requireNonNull(title, "title");
            return this;
        }

        public ViewConfig<S, P, I> build() {
            return new ViewConfig<>(type, title);
        }
    }
}
