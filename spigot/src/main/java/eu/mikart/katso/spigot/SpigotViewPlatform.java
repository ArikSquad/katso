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
import java.util.Objects;
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
        Inventory inventory = switch (type.kind()) {
            case CHEST -> Bukkit.createInventory(null, type.size(), componentBridge.serializeTitle(title));
            case HOPPER -> Bukkit.createInventory(null, org.bukkit.event.inventory.InventoryType.HOPPER,
                    componentBridge.serializeTitle(title));
            case DISPENSER -> Bukkit.createInventory(null, org.bukkit.event.inventory.InventoryType.DISPENSER,
                    componentBridge.serializeTitle(title));
        };
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
