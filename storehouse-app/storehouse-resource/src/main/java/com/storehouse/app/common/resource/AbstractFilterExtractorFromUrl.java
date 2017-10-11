package com.storehouse.app.common.resource;

import com.storehouse.app.common.model.filter.GenericFilter;
import com.storehouse.app.common.model.filter.PaginationData;
import com.storehouse.app.common.model.filter.PaginationData.OrderMode;

import javax.ws.rs.core.UriInfo;

/**
 * Class to extract the parameters from the URL for filter.
 *
 * @author ejiafzh
 *
 */
public abstract class AbstractFilterExtractorFromUrl {
    private UriInfo uriInfo;

    // define default page, and per page
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_PER_PAGE = 10;

    /**
     * Get the default sort field to be used.
     *
     * @return the default sort field to be used.
     */
    protected abstract String getDefaultSortField();

    /**
     * Get the filter to be used.
     *
     * @return the filter to be used.
     */
    protected abstract GenericFilter getFilter();

    /**
     * Constructor to initialise the urlInfo.
     *
     * @param uriInfo
     *            the uriInfo passed in.
     */
    public AbstractFilterExtractorFromUrl(final UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    /**
     * Get the URI info,
     *
     * @return the URI info,
     */
    protected UriInfo getUriInfo() {
        return uriInfo;
    }

    /**
     * Extract the pagination data from the uri info.
     *
     * @return the parsed PaginationData
     */
    protected PaginationData extractPaginationData() {
        final int perPage = getPerPage();
        final int firstResult = getPage() * perPage;

        String orderField;
        OrderMode orderMode;
        final String sortField = getSortField();

        if (sortField.startsWith("+")) {
            orderField = sortField.substring(1);
            orderMode = OrderMode.ASCENDING;
        } else if (sortField.startsWith("-")) {
            orderField = sortField.substring(1);
            orderMode = OrderMode.DESCENDING;
        } else {
            orderField = sortField;
            orderMode = OrderMode.ASCENDING;
        }
        return new PaginationData(firstResult, perPage, orderField, orderMode);
    }

    // we have the current page, per page, name and sort

    /**
     * we have the current page, per page, name and sort
     * if there is no pagination in uri, we defaults to the first page
     *
     * @return the page of from the uri info.
     */
    protected Integer getPage() {
        final String page = uriInfo.getQueryParameters().getFirst("page");
        return page == null ? DEFAULT_PAGE : Integer.parseInt(page);
    }

    /**
     * we have the current page, per page, name and sort
     * if there is no pagination in uri, we defaults to 10 records per page
     *
     * @return the per_page of from the uri info.
     */
    protected Integer getPerPage() {
        final String perPage = uriInfo.getQueryParameters().getFirst("per_page");
        return perPage == null ? DEFAULT_PER_PAGE : Integer.parseInt(perPage);
    }

    /**
     * we have the current page, per page, name and sort
     * if there is no pagination in uri, use default sortField
     *
     * @return the sort field from the uri info.
     */
    private String getSortField() {
        final String sortField = uriInfo.getQueryParameters().getFirst("sort");
        return sortField == null ? getDefaultSortField() : sortField;
    }

}
