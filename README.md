# Katso

Katso is an inventory GUI view library for Minecraft server platforms. The codebase is structured around a 
small common core, thin platform adapters, and a typed view API that favors clear state flow over backwards compatibility.

## Modules

- `common`: platform-agnostic view API, layout DSL, session lifecycle, shared state, and pagination support.
- `spigot`: Spigot inventory/event adapter.
- `paper`: Paper convenience wrappers on top of the Spigot adapter.
- `minestom`: Minestom inventory/event adapter.
- `packetevents`: Platform-agnostic click/drag/close lifecycle manager for packet-level integrations.
- `velocity`: Velocity adapter built on the PacketEvents module.
- `fabric`: Fabric adapter built on the PacketEvents module.
- `sponge`: Sponge adapter entrypoint.

## Design Goals

- Typed view context instead of raw session casts.
- Clear separation between view API, layout DSL, session lifecycle, platform abstraction, and pagination.
- Small platform modules that adapt the common engine instead of reimplementing it.
- Breaking changes are acceptable when they improve the API or internal design.

## Quick Example

```java
import eu.mikart.katso.context.ViewContext;
import eu.mikart.katso.layout.LayoutBuilder;
import eu.mikart.katso.paper.PaperStatefulView;
import eu.mikart.katso.view.ViewConfig;
import eu.mikart.katso.view.ViewType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import net.kyori.adventure.text.Component;

public final class CounterView implements PaperStatefulView<Integer> {

    @Override
    public Integer initialState() {
        return 0;
    }

    @Override
    public ViewConfig<Integer, Player, ItemStack> config() {
        return ViewConfig.<Integer, Player, ItemStack>builder(ViewType.CHEST_3_ROW)
                .title(context -> Component.text("Count: " + context.state()))
                .build();
    }

    @Override
    public void render(LayoutBuilder<Integer, Player, ItemStack> layout,
                       ViewContext<Integer, Player, ItemStack> context) {
        layout.slot(11, new ItemStack(Material.RED_WOOL), state -> state - 1);
        layout.slot(13, new ItemStack(Material.PAPER));
        layout.slot(15, new ItemStack(Material.LIME_WOOL), state -> state + 1);
    }
}
```

Open views through the platform manager:

```java
PaperViewManager viewManager = new PaperViewManager(plugin);
viewManager.navigator(player).push(new CounterView());
```

## Platform Notes

- **Velocity** support is intentionally PacketEvents-first because Velocity itself does not provide an inventory GUI API.
- **Fabric** support is exposed via a PacketEvents-backed manager design intended for multi-version integrations (including 1.21.11/26.1.2 environments).
- **Sponge** support is exposed via a dedicated platform interface and manager.
- Each platform module now also exposes the same typed view surface as the established adapters: `*View`, `*StatefulView`,
  `*StatelessView`, `*PaginatedView`, and `*StatefulPaginatedView`.


### PacketEvents integration flow

For Velocity/Fabric (and any other packet-first platform), wire your packet listeners to `PacketEventsEventBridge`:

1. Construct a `PacketEventsViewManager` platform adapter (or `VelocityViewManager` / `FabricViewManager`).
2. Implement a `PacketEventsClickMapper` that maps protocol click types to `ViewClick`.
3. Forward packet callbacks to `topClick`, `topDrag`, `bottomClick`, `close`, and `disconnect`.

The bridge returns `ALLOW`/`CANCEL` to make packet cancellation straightforward in host plugins.

## Compatibility

Katso does not target source or binary compatibility across releases. The API is allowed to change when the replacement is cleaner.
