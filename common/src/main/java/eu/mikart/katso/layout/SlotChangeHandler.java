package eu.mikart.katso.layout;

@FunctionalInterface
public interface SlotChangeHandler<S, I> {

    void onChange(int slot, I oldItem, I newItem, S state);
}
