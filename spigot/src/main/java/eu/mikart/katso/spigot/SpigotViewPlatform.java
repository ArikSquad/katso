package eu.mikart.katso.spigot;

import eu.mikart.katso.platform.ScheduledTask;
import eu.mikart.katso.platform.ViewInventory;
import eu.mikart.katso.platform.ViewPlatform;
import eu.mikart.katso.view.ViewType;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class SpigotViewPlatform implements ViewPlatform<Player, ItemStack> {

    private final Plugin plugin;
    private final SpigotComponentBridge componentBridge;

    public SpigotViewPlatform(Plugin plugin, SpigotComponentBridge componentBridge) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.componentBridge = Objects.requireNonNull(componentBridge, "componentBridge");
    }

    @Override
    public UUID playerId(Player player) {
        return player.getUniqueId();
    }

    @Override
    public ViewInventory<ItemStack> createInventory(Player player, ViewType type, Component title) {
        Inventory inventory = type.kind() == eu.mikart.katso.view.MenuKind.CHEST
                ? Bukkit.createInventory(null, type.size(), componentBridge.serializeTitle(title))
                : Bukkit.createInventory(null, bukkitType(type), componentBridge.serializeTitle(title));
        return new SpigotInventory(player, inventory);
    }

    @Override
    public void openInventory(Player player, ViewInventory<ItemStack> inventory) {
        player.openInventory((Inventory) inventory.handle());
    }

    @Override
    public void closeInventory(Player player) {
        player.closeInventory();
    }

    @Override
    public void sendMessage(Player player, Component message) {
        componentBridge.sendMessage(player, message);
    }

    @Override
    public Optional<String> readTextInput(Player player, ViewInventory<ItemStack> inventory) {
        if (!(((Inventory) inventory.handle()) instanceof org.bukkit.inventory.AnvilInventory anvil)) {
            return Optional.empty();
        }
        try {
            Method method = anvil.getClass().getMethod("getRenameText");
            Object value = method.invoke(anvil);
            return value instanceof String text ? Optional.of(text) : Optional.empty();
        } catch (ReflectiveOperationException ignored) {
            return Optional.empty();
        }
    }

    private org.bukkit.event.inventory.InventoryType bukkitType(ViewType type) {
        String[] names = switch (type.kind()) {
            case HOPPER -> new String[]{"HOPPER"};
            case DISPENSER -> new String[]{"DISPENSER"};
            case DROPPER -> new String[]{"DROPPER", "DISPENSER"};
            case ANVIL -> new String[]{"ANVIL"};
            case FURNACE -> new String[]{"FURNACE"};
            case BLAST_FURNACE -> new String[]{"BLAST_FURNACE"};
            case SMOKER -> new String[]{"SMOKER"};
            case BREWING_STAND -> new String[]{"BREWING"};
            case ENCHANTING_TABLE -> new String[]{"ENCHANTING"};
            case CRAFTING_TABLE -> new String[]{"WORKBENCH"};
            case CARTOGRAPHY_TABLE -> new String[]{"CARTOGRAPHY"};
            case GRINDSTONE -> new String[]{"GRINDSTONE"};
            case LOOM -> new String[]{"LOOM"};
            case STONECUTTER -> new String[]{"STONECUTTER"};
            case SMITHING_TABLE -> new String[]{"SMITHING", "SMITHING_NEW"};
            case BEACON -> new String[]{"BEACON"};
            case CHEST -> throw new IllegalArgumentException("Chest menus are created by size");
        };
        for (String name : names) {
            try { return org.bukkit.event.inventory.InventoryType.valueOf(name); } catch (IllegalArgumentException ignored) { }
        }
        throw new IllegalArgumentException("Bukkit does not expose an inventory type for " + type.kind());
    }

    @Override
    public ItemStack emptyItem() {
        return new ItemStack(Material.AIR);
    }

    @Override
    public ItemStack copyItem(ItemStack item) {
        return item == null ? emptyItem() : item.clone();
    }

    @Override
    public boolean isEmpty(ItemStack item) {
        return item == null || item.getType().isAir();
    }

    @Override
    public boolean itemsEqual(ItemStack first, ItemStack second) {
        if (isEmpty(first) && isEmpty(second)) {
            return true;
        }
        return Objects.equals(first, second);
    }

    @Override
    public ScheduledTask scheduleNextTick(Runnable action) {
        BukkitTask task = Bukkit.getScheduler().runTask(plugin, action);
        return task::cancel;
    }

    @Override
    public ScheduledTask scheduleRepeating(Duration interval, Runnable action) {
        long ticks = Math.max(1L, (long) Math.ceil(interval.toMillis() / 50.0D));
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, action, ticks, ticks);
        return task::cancel;
    }

    private final class SpigotInventory implements ViewInventory<ItemStack> {

        private final Player player;
        private final Inventory inventory;

        private SpigotInventory(Player player, Inventory inventory) {
            this.player = player;
            this.inventory = inventory;
        }

        @Override
        public int size() {
            return inventory.getSize();
        }

        @Override
        public ItemStack getItem(int slot) {
            return copyItem(inventory.getItem(slot));
        }

        @Override
        public void setItem(int slot, ItemStack item) {
            inventory.setItem(slot, isEmpty(item) ? null : copyItem(item));
        }

        @Override
        public void setTitle(Component title) {
            if (player.getOpenInventory().getTopInventory().equals(inventory)) {
                player.getOpenInventory().setTitle(componentBridge.serializeTitle(title));
            }
        }

        @Override
        public Object handle() {
            return inventory;
        }
    }
}
