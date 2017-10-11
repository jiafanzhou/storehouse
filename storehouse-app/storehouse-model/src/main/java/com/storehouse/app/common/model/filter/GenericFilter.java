package com.storehouse.app.common.model.filter;

/**
 * Generic filter class which includes the pagination data we want to construct.
 *
 * @author ejiafzh
 *
 */
public class GenericFilter {
    private PaginationData paginationData;

    /**
     * Constructor with no arguments.
     */
    public GenericFilter() {
    }

    /**
     * Creates a generic filter based on one created already,
     *
     * @param paginationData
     *            the pagination data.
     */
    public GenericFilter(final PaginationData paginationData) {
        this.paginationData = paginationData;
    }

    /**
     * Get the pagination data associated with this generic filter.
     * 
     * @return the pagination data associated with this generic filter.
     */
    public PaginationData getPaginationData() {
        return paginationData;
    }

    /**
     * Sets the pagination data associated with this generic filter.
     * 
     * @param paginationData
     *            the pagination data associated with this generic filter.
     */
    public void setPaginationData(final PaginationData paginationData) {
        this.paginationData = paginationData;
    }

    /**
     * Check whether or not this filter has pagination data.
     * 
     * @return true if it has false otherwise,
     */
    public boolean hasPaginationData() {
        return getPaginationData() != null;
    }

    /**
     * Check whether or not this filter has order field.
     * 
     * @return true if it has false otherwise,
     */
    public boolean hasOrderField() {
        return hasPaginationData() && getPaginationData().getOrderField() != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "GenericFilter [paginationData=" + paginationData + "]";
    }
}
