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

    public enum OrderMode {
        ASCENDING, DESCENDING;
    }

    public PaginationData(final int firstResult, final int maxResults, final String orderField,
            final OrderMode orderMode) {
        this.firstResult = firstResult;
        this.maxResults = maxResults;
        this.orderField = orderField;
        this.orderMode = orderMode;
    }

    public int getFirstResult() {
        return firstResult;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public String getOrderField() {
        return orderField;
    }

    public OrderMode getOrderMode() {
        return orderMode;
    }

}
