package eu.mikart.katso;

public record TopClickDecision(boolean allowInventoryChange, boolean dispatchClickHandler) {

    public static TopClickDecision allowEdit() {
        return new TopClickDecision(true, false);
    }

    public static TopClickDecision cancel() {
        return new TopClickDecision(false, false);
    }

    public static TopClickDecision cancelAndDispatch() {
        return new TopClickDecision(false, true);
    }
}
