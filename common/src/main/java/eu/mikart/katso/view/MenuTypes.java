package eu.mikart.katso.view;

/** Convenience factory and constants for common Minecraft menus. */
public final class MenuTypes {
    public static final ViewType CHEST_1_ROW = ViewType.chest(1);
    public static final ViewType CHEST_2_ROW = ViewType.chest(2);
    public static final ViewType CHEST_3_ROW = ViewType.chest(3);
    public static final ViewType CHEST_4_ROW = ViewType.chest(4);
    public static final ViewType CHEST_5_ROW = ViewType.chest(5);
    public static final ViewType CHEST_6_ROW = ViewType.chest(6);
    public static final ViewType HOPPER = ViewType.menu(MenuKind.HOPPER);
    public static final ViewType DISPENSER = ViewType.menu(MenuKind.DISPENSER);
    public static final ViewType DROPPER = ViewType.menu(MenuKind.DROPPER);
    public static final ViewType ANVIL = ViewType.menu(MenuKind.ANVIL);
    public static final ViewType FURNACE = ViewType.menu(MenuKind.FURNACE);
    public static final ViewType BLAST_FURNACE = ViewType.menu(MenuKind.BLAST_FURNACE);
    public static final ViewType SMOKER = ViewType.menu(MenuKind.SMOKER);
    public static final ViewType BREWING_STAND = ViewType.menu(MenuKind.BREWING_STAND);
    public static final ViewType ENCHANTING_TABLE = ViewType.menu(MenuKind.ENCHANTING_TABLE);
    public static final ViewType CRAFTING_TABLE = ViewType.menu(MenuKind.CRAFTING_TABLE);
    public static final ViewType CARTOGRAPHY_TABLE = ViewType.menu(MenuKind.CARTOGRAPHY_TABLE);
    public static final ViewType GRINDSTONE = ViewType.menu(MenuKind.GRINDSTONE);
    public static final ViewType LOOM = ViewType.menu(MenuKind.LOOM);
    public static final ViewType STONECUTTER = ViewType.menu(MenuKind.STONECUTTER);
    public static final ViewType SMITHING_TABLE = ViewType.menu(MenuKind.SMITHING_TABLE);
    public static final ViewType BEACON = ViewType.menu(MenuKind.BEACON);

    private MenuTypes() {}

    public static ViewType chest(int rows) { return ViewType.chest(rows); }
    public static ViewType menu(MenuKind kind) { return ViewType.menu(kind); }
}
