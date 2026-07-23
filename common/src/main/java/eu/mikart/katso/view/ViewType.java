package eu.mikart.katso.view;

import java.util.Map;
import java.util.Objects;

public record ViewType(MenuKind kind, int rows, int size) {

    private static final Map<MenuKind, Integer> FIXED_SIZES = Map.ofEntries(
            Map.entry(MenuKind.HOPPER, 5),
            Map.entry(MenuKind.DISPENSER, 9),
            Map.entry(MenuKind.DROPPER, 9),
            Map.entry(MenuKind.ANVIL, 3),
            Map.entry(MenuKind.FURNACE, 3),
            Map.entry(MenuKind.BLAST_FURNACE, 3),
            Map.entry(MenuKind.SMOKER, 3),
            Map.entry(MenuKind.BREWING_STAND, 5),
            Map.entry(MenuKind.ENCHANTING_TABLE, 2),
            Map.entry(MenuKind.CRAFTING_TABLE, 10),
            Map.entry(MenuKind.CARTOGRAPHY_TABLE, 3),
            Map.entry(MenuKind.GRINDSTONE, 3),
            Map.entry(MenuKind.LOOM, 4),
            Map.entry(MenuKind.STONECUTTER, 2),
            Map.entry(MenuKind.SMITHING_TABLE, 4),
            Map.entry(MenuKind.BEACON, 1)
    );

    public static final ViewType CHEST_1_ROW = chest(1);
    public static final ViewType CHEST_2_ROW = chest(2);
    public static final ViewType CHEST_3_ROW = chest(3);
    public static final ViewType CHEST_4_ROW = chest(4);
    public static final ViewType CHEST_5_ROW = chest(5);
    public static final ViewType CHEST_6_ROW = chest(6);
    public static final ViewType HOPPER = menu(MenuKind.HOPPER);
    public static final ViewType DISPENSER = menu(MenuKind.DISPENSER);
    public static final ViewType ANVIL = menu(MenuKind.ANVIL);

    public ViewType {
        Objects.requireNonNull(kind, "kind");
        if (kind == MenuKind.CHEST) {
            if (rows < 1 || rows > 6) throw new IllegalArgumentException("Chest rows must be between 1 and 6");
            if (size != rows * 9) throw new IllegalArgumentException("Chest size must equal rows * 9");
        } else {
            if (rows != 0) throw new IllegalArgumentException("Only chest views accept rows");
            int expected = FIXED_SIZES.getOrDefault(kind, -1);
            if (size != expected) throw new IllegalArgumentException(kind + " size must be " + expected);
        }
    }

    public static ViewType chest(int rows) {
        return new ViewType(MenuKind.CHEST, rows, rows * 9);
    }

    public static ViewType menu(MenuKind kind) {
        if (kind == MenuKind.CHEST) throw new IllegalArgumentException("Use chest(rows) for chest menus");
        Integer size = FIXED_SIZES.get(kind);
        if (size == null) throw new IllegalArgumentException("Unsupported fixed menu: " + kind);
        return new ViewType(kind, 0, size);
    }

    /** Backwards-compatible name for older Katso integrations. */
    @Deprecated(forRemoval = false)
    public Kind legacyKind() { return Kind.valueOf(kind.name()); }

    @Deprecated(forRemoval = false)
    public enum Kind { CHEST, HOPPER, DISPENSER }
}
