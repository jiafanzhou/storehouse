package com.storehouse.app.common.model;

import java.util.List;

/**
 * This is the paginated data record.
 * e.g. there are 100 records in db and we have 10 pages, so the first page has 10 records.
 *
 * numberOfRows = 100
 * rows.size = 10 (first page 10 records)
 *
 * @author ejiafzh
 *
 * @param <T>
 */
public class PaginatedData<T> {
    private final int numberOfRows; // represents the real count records in the database.
    private final List<T> rows; // presents the returned paginated records

    public PaginatedData(final int numberOfRows, final List<T> rows) {
        this.numberOfRows = numberOfRows;
        this.rows = rows;
    }

    public int getNumberOfRows() {
        return numberOfRows;
    }

    public List<T> getRows() {
        return rows;
    }

    public T getRow(final int index) {
        if (index >= rows.size()) {
            return null;
        }
        return rows.get(index);
    }

    @Override
    public String toString() {
        return "PaginatedData [numberOfRows=" + numberOfRows + ", rows=" + rows + "]";
    }
}
