package eu.mikart.katso;

public interface StatefulView<S, P, I> extends View<S, P, I> {

    S initialState();
}
