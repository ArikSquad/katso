package eu.mikart.katso.pagination;

import java.util.List;

public interface PaginatedState<T> {

    List<T> items();

    int page();

    PaginatedState<T> withPage(int page);

    PaginatedState<T> withItems(List<T> items);
}
