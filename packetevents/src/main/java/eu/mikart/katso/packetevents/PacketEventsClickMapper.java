package eu.mikart.katso.packetevents;

import eu.mikart.katso.context.ViewClick;

import java.util.Collection;

/**
 * Maps protocol/platform-specific click metadata into Katso's normalized {@link ViewClick} representation.
 */
public interface PacketEventsClickMapper<C> {

    ViewClick toClick(C clickType, int hotbarButton);

    ViewClick toDragClick(C dragType, Collection<Integer> draggedSlots);
}
