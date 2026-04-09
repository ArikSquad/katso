package eu.mikart.katso.platform;

import net.kyori.adventure.text.Component;

public interface ViewInventory<I> {

    int size();

    I getItem(int slot);

    void setItem(int slot, I item);

    void setTitle(Component title);

    Object handle();
}
