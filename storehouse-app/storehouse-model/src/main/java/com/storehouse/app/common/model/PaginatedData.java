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

    /**
     * Constructs the paginated data object.
     * 
     * @param numberOfRows
     *            number of the rows returned.
     * @param rows
     *            the rows returned.
     */
    public PaginatedData(final int numberOfRows, final List<T> rows) {
        this.numberOfRows = numberOfRows;
        this.rows = rows;
    }

    /**
     * Get the number of rows of this paginated data.
     * 
     * @return the number of rows of this paginated data.
     */
    public int getNumberOfRows() {
        return numberOfRows;
    }

    /**
     * Get the actual rows returned as paginated data.
     * 
     * @return the actual rows returned as paginated data.
     */
    public List<T> getRows() {
        return rows;
    }

    /**
     * Get a particular row data based on the index.
     * 
     * @param index
     *            the index of the row data
     * @return a particular row data based on the index.
     */
    public T getRow(final int index) {
        if (index >= rows.size()) {
            return null;
        }
        return rows.get(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "PaginatedData [numberOfRows=" + numberOfRows + ", rows=" + rows + "]";
    }
}
