package eu.mikart.katso.platform;

import eu.mikart.katso.view.ViewType;
import net.kyori.adventure.text.Component;

import java.time.Duration;
import java.util.UUID;

public interface ViewPlatform<P, I> {

    UUID playerId(P player);

    ViewInventory<I> createInventory(P player, ViewType type, Component title);

    void openInventory(P player, ViewInventory<I> inventory);

    void closeInventory(P player);

    void sendMessage(P player, Component message);

    I emptyItem();

    I copyItem(I item);

    boolean isEmpty(I item);

    boolean itemsEqual(I first, I second);

    ScheduledTask scheduleNextTick(Runnable action);

    ScheduledTask scheduleRepeating(Duration interval, Runnable action);
}
