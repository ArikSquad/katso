package eu.mikart.katso.minestom;

import eu.mikart.katso.ScheduledTask;
import eu.mikart.katso.ViewInventory;
import eu.mikart.katso.ViewPlatform;
import eu.mikart.katso.ViewType;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

public class MinestomViewPlatform implements ViewPlatform<Player, ItemStack> {

    @Override
    public UUID playerId(Player player) {
        return player.getUuid();
    }

    @Override
    public ViewInventory<ItemStack> createInventory(Player player, ViewType type, Component title) {
        InventoryType inventoryType = switch (type.kind()) {
            case CHEST -> switch (type.rows()) {
                case 1 -> InventoryType.CHEST_1_ROW;
                case 2 -> InventoryType.CHEST_2_ROW;
                case 3 -> InventoryType.CHEST_3_ROW;
                case 4 -> InventoryType.CHEST_4_ROW;
                case 5 -> InventoryType.CHEST_5_ROW;
                case 6 -> InventoryType.CHEST_6_ROW;
                default -> throw new IllegalArgumentException("Unsupported chest rows: " + type.rows());
            };
            case HOPPER -> InventoryType.HOPPER;
            case DISPENSER -> InventoryType.WINDOW_3X3;
        };

        Inventory inventory = new Inventory(inventoryType, title);
        return new MinestomInventory(inventory);
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
        player.sendMessage(message);
    }

    @Override
    public ItemStack emptyItem() {
        return ItemStack.AIR;
    }

    @Override
    public ItemStack copyItem(ItemStack item) {
        return item == null ? ItemStack.AIR : item;
    }

    @Override
    public boolean isEmpty(ItemStack item) {
        return item == null || item.isAir();
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
        Task task = MinecraftServer.getSchedulerManager().submitTask(() -> {
            action.run();
            return TaskSchedule.stop();
        });
        return task::cancel;
    }

    @Override
    public ScheduledTask scheduleRepeating(Duration interval, Runnable action) {
        Task task = MinecraftServer.getSchedulerManager().submitTask(() -> {
            action.run();
            return TaskSchedule.duration(interval);
        });
        return task::cancel;
    }

    private final class MinestomInventory implements ViewInventory<ItemStack> {

        private final Inventory inventory;

        private MinestomInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        @Override
        public int size() {
            return inventory.getSize();
        }

        @Override
        public ItemStack getItem(int slot) {
            return inventory.getItemStack(slot);
        }

        @Override
        public void setItem(int slot, ItemStack item) {
            inventory.setItemStack(slot, isEmpty(item) ? ItemStack.AIR : item);
        }

        @Override
        public void setTitle(Component title) {
            inventory.setTitle(title);
        }

        @Override
        public Object handle() {
            return inventory;
        }
    }
}
