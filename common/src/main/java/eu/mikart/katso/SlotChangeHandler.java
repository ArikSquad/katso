package eu.mikart.katso;

@FunctionalInterface
public interface SlotChangeHandler<S, I> {

    void onChange(int slot, I oldItem, I newItem, S state);
}
