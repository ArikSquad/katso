package eu.mikart.katso.view;

public interface StatefulView<S, P, I> extends View<S, P, I> {

    S initialState();
}
