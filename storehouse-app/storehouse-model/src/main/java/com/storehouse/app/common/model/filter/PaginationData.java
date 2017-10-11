package com.storehouse.app.common.model.filter;

/**
 * This class is responsible for receiving all the filter-related pagination.
 * e.g. which page to start and how many records and which filter by ordering etc.
 *
 * @author ejiafzh
 *
 */
public class PaginationData {
    private final int firstResult;
    private final int maxResults;
    private final String orderField;
    private final OrderMode orderMode;

    /**
     * Order mode which could be either ascending or descending.
     *
     * @author ejiafzh
     *
     */
    public enum OrderMode {
        ASCENDING, DESCENDING;
    }

    /**
     * Constructs a PaginationData based on a number of parameters.
     *
     * @param firstResult
     *            the first result index.
     * @param maxResults
     *            maximum results.
     * @param orderField
     *            order field
     * @param orderMode
     *            order mode.
     */
    public PaginationData(final int firstResult, final int maxResults, final String orderField,
            final OrderMode orderMode) {
        this.firstResult = firstResult;
        this.maxResults = maxResults;
        this.orderField = orderField;
        this.orderMode = orderMode;
    }

    /**
     * Gets the first result index.
     * 
     * @return the first result index.
     */
    public int getFirstResult() {
        return firstResult;
    }

    /**
     * Gets the maximum results.
     * 
     * @return the maximum results.
     */
    public int getMaxResults() {
        return maxResults;
    }

    /**
     * Gets the order field.
     * 
     * @return the order field.
     */
    public String getOrderField() {
        return orderField;
    }

    /**
     * Gets the order mode.
     * 
     * @return the order field.
     */
    public OrderMode getOrderMode() {
        return orderMode;
    }

}
