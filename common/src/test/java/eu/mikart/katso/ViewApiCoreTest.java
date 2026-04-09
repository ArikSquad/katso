package eu.mikart.katso;

import eu.mikart.katso.context.ClickContext;
import eu.mikart.katso.context.ViewClick;
import eu.mikart.katso.layout.LayoutBuilder;
import eu.mikart.katso.pagination.PaginatedState;
import eu.mikart.katso.pagination.PaginatedView;
import eu.mikart.katso.platform.ScheduledTask;
import eu.mikart.katso.platform.ViewInventory;
import eu.mikart.katso.platform.ViewPlatform;
import eu.mikart.katso.session.SharedContext;
import eu.mikart.katso.session.ViewManager;
import eu.mikart.katso.session.ViewSession;
import eu.mikart.katso.context.ViewContext;
import eu.mikart.katso.view.StatefulView;
import eu.mikart.katso.view.View;
import eu.mikart.katso.view.ViewConfig;
import eu.mikart.katso.view.ViewType;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViewApiCoreTest {

    @Test
    void duplicateSharedContextCreationFails() {
        TestPlatform platform = new TestPlatform();
        ViewManager<TestPlayer, String> manager = new ViewManager<>(platform);

        manager.createSharedContext("team-chest", "state");

        assertThrows(IllegalArgumentException.class,
                () -> manager.createSharedContext("team-chest", "other-state"));
    }

    @Test
    void hotbarSwapRequiresExplicitLayoutOptIn() {
        TestPlatform platform = new TestPlatform();
        ViewManager<TestPlayer, String> manager = new ViewManager<>(platform);
        TestPlayer player = new TestPlayer();

        ViewSession<String, TestPlayer, String> blockedSession = manager.navigator(player)
                .push(new EditableView(false), "state");
        assertFalse(blockedSession.decideTopClick(0, ViewClick.numberKey(2), List.of()).allowInventoryChange());

        ViewSession<String, TestPlayer, String> allowedSession = manager.navigator(new TestPlayer())
                .push(new EditableView(true), "state");
        assertTrue(allowedSession.decideTopClick(0, ViewClick.numberKey(2), List.of()).allowInventoryChange());
    }

    @Test
    void paginatedViewClampsPageBounds() {
        TestPlatform platform = new TestPlatform();
        ViewManager<TestPlayer, String> manager = new ViewManager<>(platform);

        ViewSession<PageState, TestPlayer, String> negativePage = manager.navigator(new TestPlayer())
                .push(new TestPaginatedView(), new PageState(List.of("A", "B", "C"), -4));
        assertEquals("A", negativePage.inventory().getItem(0));
        assertEquals("B", negativePage.inventory().getItem(1));

        ViewSession<PageState, TestPlayer, String> oversizedPage = manager.navigator(new TestPlayer())
                .push(new TestPaginatedView(), new PageState(List.of("A", "B", "C"), 8));
        assertEquals("C", oversizedPage.inventory().getItem(0));
        assertEquals(TestPlatform.AIR, oversizedPage.inventory().getItem(1));
    }

    @Test
    void sharedContextClearDoesNotReinitializeEditableDefaults() {
        TestPlatform platform = new TestPlatform();
        ViewManager<TestPlayer, String> manager = new ViewManager<>(platform);
        SharedContext<String, TestPlayer, String> shared = manager.createSharedContext("shared", "state");
        SeededSharedView view = new SeededSharedView();

        ViewSession<String, TestPlayer, String> firstSession = manager.navigator(new TestPlayer()).pushShared(view, shared);
        assertEquals("seed", firstSession.inventory().getItem(0));

        shared.clearSlotItems();
        assertEquals(TestPlatform.AIR, shared.getSlotItem(0));

        ViewSession<String, TestPlayer, String> secondSession = manager.navigator(new TestPlayer()).pushShared(view, shared);
        assertEquals(TestPlatform.AIR, secondSession.inventory().getItem(0));
    }

    @Test
    void removedAutoUpdateTasksAreCancelledOnLayoutRefresh() {
        TestPlatform platform = new TestPlatform();
        ViewManager<TestPlayer, String> manager = new ViewManager<>(platform);

        ViewSession<Boolean, TestPlayer, String> session = manager.navigator(new TestPlayer())
                .push(new AutoUpdateView(), true);
        assertEquals(1, platform.activeRepeatingTasks());

        session.setState(false);

        assertEquals(0, platform.activeRepeatingTasks());
    }

    private record TestPlayer(UUID id) {
        private TestPlayer() {
            this(UUID.randomUUID());
        }
    }

    private static final class TestPlatform implements ViewPlatform<TestPlayer, String> {
        private static final String AIR = "<air>";

        private final List<TestTask> repeatingTasks = new ArrayList<>();

        @Override
        public UUID playerId(TestPlayer player) {
            return player.id();
        }

        @Override
        public ViewInventory<String> createInventory(TestPlayer player, ViewType type, Component title) {
            return new TestInventory(type.size(), title);
        }

        @Override
        public void openInventory(TestPlayer player, ViewInventory<String> inventory) {
        }

        @Override
        public void closeInventory(TestPlayer player) {
        }

        @Override
        public void sendMessage(TestPlayer player, Component message) {
        }

        @Override
        public String emptyItem() {
            return AIR;
        }

        @Override
        public String copyItem(String item) {
            return item == null ? AIR : item;
        }

        @Override
        public boolean isEmpty(String item) {
            return item == null || AIR.equals(item);
        }

        @Override
        public boolean itemsEqual(String first, String second) {
            if (isEmpty(first) && isEmpty(second)) {
                return true;
            }
            return Objects.equals(first, second);
        }

        @Override
        public ScheduledTask scheduleNextTick(Runnable action) {
            return () -> {
            };
        }

        @Override
        public ScheduledTask scheduleRepeating(Duration interval, Runnable action) {
            TestTask task = new TestTask(action);
            repeatingTasks.add(task);
            return task;
        }

        private int activeRepeatingTasks() {
            return (int) repeatingTasks.stream().filter(task -> !task.cancelled).count();
        }
    }

    private static final class TestTask implements ScheduledTask {
        private final Runnable action;
        private boolean cancelled;

        private TestTask(Runnable action) {
            this.action = action;
        }

        @Override
        public void cancel() {
            cancelled = true;
        }
    }

    private static final class TestInventory implements ViewInventory<String> {
        private final String[] items;
        private Component title;

        private TestInventory(int size, Component title) {
            this.items = new String[size];
            this.title = title;
            for (int slot = 0; slot < size; slot++) {
                items[slot] = TestPlatform.AIR;
            }
        }

        @Override
        public int size() {
            return items.length;
        }

        @Override
        public String getItem(int slot) {
            return items[slot];
        }

        @Override
        public void setItem(int slot, String item) {
            items[slot] = item == null ? TestPlatform.AIR : item;
        }

        @Override
        public void setTitle(Component title) {
            this.title = title;
        }

        @Override
        public Object handle() {
            return this;
        }
    }

    private static final class EditableView implements View<String, TestPlayer, String> {
        private final boolean allowHotkey;

        private EditableView(boolean allowHotkey) {
            this.allowHotkey = allowHotkey;
        }

        @Override
        public ViewConfig<String, TestPlayer, String> config() {
            return ViewConfig.of(ViewType.CHEST_1_ROW, "Editable");
        }

        @Override
        public void render(LayoutBuilder<String, TestPlayer, String> layout, ViewContext<String, TestPlayer, String> context) {
            layout.editable(0);
            layout.allowHotbarSwap(allowHotkey);
        }
    }

    private static final class SeededSharedView implements View<String, TestPlayer, String> {
        @Override
        public ViewConfig<String, TestPlayer, String> config() {
            return ViewConfig.of(ViewType.CHEST_1_ROW, "Shared");
        }

        @Override
        public void render(LayoutBuilder<String, TestPlayer, String> layout, ViewContext<String, TestPlayer, String> context) {
            layout.editable(0, (currentState, currentContext) -> "seed", (slot, oldItem, newItem, current) -> {
            });
        }
    }

    private static final class AutoUpdateView implements StatefulView<Boolean, TestPlayer, String> {
        @Override
        public Boolean initialState() {
            return true;
        }

        @Override
        public ViewConfig<Boolean, TestPlayer, String> config() {
            return ViewConfig.of(ViewType.CHEST_1_ROW, "Auto");
        }

        @Override
        public void render(LayoutBuilder<Boolean, TestPlayer, String> layout, ViewContext<Boolean, TestPlayer, String> context) {
            if (context.state()) {
                layout.autoUpdating(0, (currentState, currentContext) -> "tick", Duration.ofSeconds(1));
            } else {
                layout.slot(0, "static");
            }
        }
    }

    private record PageState(List<String> items, int page) implements PaginatedState<String> {
        @Override
        public PaginatedState<String> withPage(int page) {
            return new PageState(items, page);
        }

        @Override
        public PaginatedState<String> withItems(List<String> items) {
            return new PageState(items, page);
        }
    }

    private static final class TestPaginatedView extends PaginatedView<String, PageState, TestPlayer, String> {
        @Override
        public ViewConfig<PageState, TestPlayer, String> config() {
            return ViewConfig.of(ViewType.CHEST_1_ROW, "Pages");
        }

        @Override
        protected int[] paginatedSlots() {
            return new int[]{0, 1};
        }

        @Override
        protected int previousPageSlot() {
            return -1;
        }

        @Override
        protected int nextPageSlot() {
            return -1;
        }

        @Override
        protected String renderItem(String item, int index, TestPlayer player) {
            return item;
        }

        @Override
        protected void onItemClick(ClickContext<PageState, TestPlayer, String> click, String item, int index) {
        }

        @Override
        protected boolean shouldFilterFromSearch(PageState state, String item) {
            return false;
        }

        @Override
        protected String previousPageItem(int currentPage, int totalPages, ViewContext<PageState, TestPlayer, String> context) {
            return "prev";
        }

        @Override
        protected String nextPageItem(int currentPage, int totalPages, ViewContext<PageState, TestPlayer, String> context) {
            return "next";
        }

        @Override
        protected String emptyItem(ViewContext<PageState, TestPlayer, String> context) {
            return TestPlatform.AIR;
        }
    }
}
