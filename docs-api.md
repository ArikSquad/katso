# Katso API Guide

Katso builds Minecraft inventory UIs from three concepts:

1. **A view** describes one screen.
2. **A layout** writes slots each render.
3. **A navigator** opens, replaces, and returns between views for a player.

```java
viewManager.navigator(player).push(new SearchView());
```

## Configure a menu

Use `Views` or `MenuTypes` to avoid remembering constructor details:

```java
@Override
public ViewConfig<SearchState, Player, ItemStack> config() {
    return Views.<SearchState, Player, ItemStack>configure(MenuTypes.ANVIL)
            .title(context -> Component.text("Search: " + context.state().query()))
            .build();
}
```

Chest rows are available with `Views.chest(rows)` or constants such as `MenuTypes.CHEST_3_ROW`.
Fixed-size menus include hopper, dispenser, dropper, anvil, furnaces, brewing stand, enchanting table,
crafting table, cartography table, grindstone, loom, stonecutter, smithing table, and beacon.

## Render slots

```java
@Override
public void render(LayoutBuilder<SearchState, Player, ItemStack> layout,
                   ViewContext<SearchState, Player, ItemStack> context) {
    layout.slot(0, icon(Material.PAPER, "Type in the anvil bar"));
    layout.slot(2, icon(Material.LIME_WOOL, "Confirm"), (click, ctx) -> runSearch(ctx.state().query()));
}
```

`layout.editable(slot)` opts a slot into normal inventory behavior. Non-editable top slots are cancelled and dispatched to your click handlers.

## Anvil text input

Anvil menus expose the rename bar as text input on platforms that can read it. Override `onTextInput` and update state.

```java
public record SearchState(String query) {}

@Override
public SearchState initialState() {
    return new SearchState("");
}

@Override
public void onTextInput(ViewContext<SearchState, Player, ItemStack> context, String text) {
    context.session().setState(new SearchState(text));
}
```

This makes the anvil bar suitable for search boxes, filters, command palettes, rename prompts, and confirmation dialogs.

## Navigation

```java
viewManager.navigator(player).push(new RootMenu());      // keep current view in back stack
viewManager.navigator(player).replace(new DetailsMenu()); // swap without adding history
viewManager.navigator(player).pop();                     // return or close
```

## Shared editable menus

Use shared contexts when multiple players should see and edit the same menu state:

```java
var shared = viewManager.getOrCreateSharedContext("team-dispenser", new TeamState());
viewManager.navigator(player).pushShared(new TeamDispenserView(), shared);
```
