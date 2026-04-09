package eu.mikart.katso.pagination;

import eu.mikart.katso.context.ClickContext;
import eu.mikart.katso.context.ViewContext;
import eu.mikart.katso.layout.LayoutBuilder;
import eu.mikart.katso.layout.Layouts;
import eu.mikart.katso.view.View;

import java.util.List;

public abstract class PaginatedView<T, S extends PaginatedState<T>, P, I> implements View<S, P, I> {

    protected static final int[] DEFAULT_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    @Override
    public void render(LayoutBuilder<S, P, I> layout, ViewContext<S, P, I> context) {
        S state = context.state();
        layoutBackground(layout, context);

        List<T> filteredItems = filteredItems(state);
        int[] slots = paginatedSlots();
        int itemsPerPage = slots.length;
        int totalPages = Math.max(1, (int) Math.ceil((double) filteredItems.size() / itemsPerPage));
        int currentPage = Math.max(0, Math.min(state.page(), totalPages - 1));

        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, filteredItems.size());
        List<T> pageItems = startIndex < filteredItems.size() ? filteredItems.subList(startIndex, endIndex) : List.of();

        for (int index = 0; index < slots.length; index++) {
            int slot = slots[index];
            if (index < pageItems.size()) {
                T item = pageItems.get(index);
                int itemIndex = startIndex + index;
                layout.slot(slot,
                        (currentState, currentContext) -> renderItem(item, itemIndex, currentContext.player()),
                        (click, currentContext) -> onItemClick(click, item, itemIndex));
            } else {
                layout.slot(slot, (currentState, currentContext) -> emptyItem(currentContext));
            }
        }

        layoutNavigation(layout, context, currentPage, totalPages);
        layoutCustom(layout, context);
    }

    protected List<T> filteredItems(S state) {
        return state.items().stream().filter(item -> !shouldFilterFromSearch(state, item)).toList();
    }

    protected void layoutBackground(LayoutBuilder<S, P, I> layout, ViewContext<S, P, I> context) {
    }

    protected void layoutNavigation(LayoutBuilder<S, P, I> layout, ViewContext<S, P, I> context,
                                    int currentPage, int totalPages) {
        int previousPageSlot = previousPageSlot();
        int nextPageSlot = nextPageSlot();

        if (previousPageSlot >= 0) {
            if (currentPage > 0) {
                layout.slot(previousPageSlot,
                        (currentState, currentContext) -> previousPageItem(currentPage, totalPages, currentContext),
                        (click, currentContext) -> currentContext.session()
                                .setStateQuiet(castState(context.state().withPage(currentPage - 1))));
            } else {
                layout.slot(previousPageSlot, (currentState, currentContext) -> emptyItem(currentContext));
            }
        }

        if (nextPageSlot >= 0) {
            if (currentPage < totalPages - 1) {
                layout.slot(nextPageSlot,
                        (currentState, currentContext) -> nextPageItem(currentPage, totalPages, currentContext),
                        (click, currentContext) -> currentContext.session()
                                .setStateQuiet(castState(context.state().withPage(currentPage + 1))));
            } else {
                layout.slot(nextPageSlot, (currentState, currentContext) -> emptyItem(currentContext));
            }
        }
    }

    protected abstract int[] paginatedSlots();

    protected abstract int previousPageSlot();

    protected abstract int nextPageSlot();

    protected abstract I renderItem(T item, int index, P player);

    protected abstract void onItemClick(ClickContext<S, P, I> click, T item, int index);

    protected abstract boolean shouldFilterFromSearch(S state, T item);

    protected abstract I previousPageItem(int currentPage, int totalPages, ViewContext<S, P, I> context);

    protected abstract I nextPageItem(int currentPage, int totalPages, ViewContext<S, P, I> context);

    protected abstract I emptyItem(ViewContext<S, P, I> context);

    protected void layoutCustom(LayoutBuilder<S, P, I> layout, ViewContext<S, P, I> context) {
    }

    public static int[] createGrid(int startSlot, int endSlot) {
        return Layouts.rectangle(startSlot, endSlot).stream().mapToInt(Integer::intValue).toArray();
    }

    @SuppressWarnings("unchecked")
    private S castState(PaginatedState<T> state) {
        return (S) state;
    }
}
