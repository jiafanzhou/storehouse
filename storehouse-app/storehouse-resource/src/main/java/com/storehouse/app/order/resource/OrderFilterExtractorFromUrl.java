package com.storehouse.app.order.resource;

import com.storehouse.app.common.model.filter.OrderFilter;
import com.storehouse.app.common.resource.AbstractFilterExtractorFromUrl;
import com.storehouse.app.common.utils.DateUtils;
import com.storehouse.app.order.model.Order.OrderStatus;

import javax.ws.rs.core.UriInfo;

public class OrderFilterExtractorFromUrl extends AbstractFilterExtractorFromUrl {
    public OrderFilterExtractorFromUrl(final UriInfo uriInfo) {
        super(uriInfo);
    }

    @Override
    public String getDefaultSortField() {
        return "-createdAt";
    }

    @Override
    public OrderFilter getFilter() {
        final OrderFilter orderFilter = new OrderFilter();
        orderFilter.setPaginationData(extractPaginationData());

        final String startDateStr = getUriInfo().getQueryParameters().getFirst("startDate");
        if (startDateStr != null) {
            orderFilter.setStartDate(DateUtils.getAsDateTime(startDateStr));
        }

        final String endDateStr = getUriInfo().getQueryParameters().getFirst("endDate");
        if (endDateStr != null) {
            orderFilter.setEndDate(DateUtils.getAsDateTime(endDateStr));
        }

        final String statusStr = getUriInfo().getQueryParameters().getFirst("status");
        if (statusStr != null) {
            orderFilter.setStatus(OrderStatus.valueOf(statusStr));
        }

        final String custumerIdStr = getUriInfo().getQueryParameters().getFirst("customerId");
        if (custumerIdStr != null) {
            orderFilter.setCustomerId(Long.valueOf(custumerIdStr));
        }
        return orderFilter;
    }
}
