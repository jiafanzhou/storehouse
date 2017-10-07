package com.storehouse.app.common.model.filter;

public class GenericFilter {
    private PaginationData paginationData;

    public GenericFilter() {
    }

    public GenericFilter(final PaginationData paginationData) {
        this.paginationData = paginationData;
    }

    public PaginationData getPaginationData() {
        return paginationData;
    }

    public void setPaginationData(final PaginationData paginationData) {
        this.paginationData = paginationData;
    }

    public boolean hasPaginationData() {
        return getPaginationData() != null;
    }

    public boolean hasOrderField() {
        return hasPaginationData() && getPaginationData().getOrderField() != null;
    }

    @Override
    public String toString() {
        return "GenericFilter [paginationData=" + paginationData + "]";
    }
}
