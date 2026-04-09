package eu.mikart.katso;

import java.util.Objects;

public record ViewType(Kind kind, int rows) {

    public static final ViewType CHEST_1_ROW = chest(1);
    public static final ViewType CHEST_2_ROW = chest(2);
    public static final ViewType CHEST_3_ROW = chest(3);
    public static final ViewType CHEST_4_ROW = chest(4);
    public static final ViewType CHEST_5_ROW = chest(5);
    public static final ViewType CHEST_6_ROW = chest(6);
    public static final ViewType HOPPER = new ViewType(Kind.HOPPER, 0);
    public static final ViewType DISPENSER = new ViewType(Kind.DISPENSER, 0);

    public ViewType {
        Objects.requireNonNull(kind, "kind");
        if (kind == Kind.CHEST && (rows < 1 || rows > 6)) {
            throw new IllegalArgumentException("Chest rows must be between 1 and 6");
        }
        if (kind != Kind.CHEST && rows != 0) {
            throw new IllegalArgumentException("Only chest views accept rows");
        }
    }

    public int size() {
        return switch (kind) {
            case CHEST -> rows * 9;
            case HOPPER -> 5;
            case DISPENSER -> 9;
        };
    }

    public static ViewType chest(int rows) {
        return new ViewType(Kind.CHEST, rows);
    }

    public enum Kind {
        CHEST,
        HOPPER,
        DISPENSER
    }
}
