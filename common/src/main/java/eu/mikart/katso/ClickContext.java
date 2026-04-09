package eu.mikart.katso;

public record ClickContext<S, P>(int slot, ViewClick click, P player, S state) {
}
