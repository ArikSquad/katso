package eu.mikart.katso.context;

import eu.mikart.katso.session.ViewSession;

import java.util.Objects;

public record ClickContext<S, P, I>(int slot, ViewClick click, ViewContext<S, P, I> context) {

    public ClickContext {
        Objects.requireNonNull(click, "click");
        Objects.requireNonNull(context, "context");
    }

    public P player() {
        return context.player();
    }

    public S state() {
        return context.state();
    }

    public ViewSession<S, P, I> session() {
        return context.session();
    }
}
