package com.storehouse.app.order.resource;

import static com.storehouse.app.commontests.utils.FilterExtractorTestUtils.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import com.storehouse.app.common.model.filter.OrderFilter;
import com.storehouse.app.common.model.filter.PaginationData;
import com.storehouse.app.common.model.filter.PaginationData.OrderMode;
import com.storehouse.app.common.utils.DateUtils;
import com.storehouse.app.order.model.Order.OrderStatus;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class OrderFilterExtractorFromUrlUTest {
    @Mock
    private UriInfo uriInfo;

    @Before
    public void initTestCase() {
        MockitoAnnotations.initMocks(this);
    }

    private void setUpUriInfo(final String page, final String perPage,
            final String startDate, final String endDate, final String cusomterId,
            final String orderStatus, final String sort) {
        final Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("page", page); // current page
        parameters.put("per_page", perPage); // number of records per page
        parameters.put("sort", sort); // use +/- signs to define ASC or DESC order
        parameters.put("startDate", startDate);
        parameters.put("endDate", endDate);
        parameters.put("status", orderStatus);
        parameters.put("customerId", cusomterId);
        setUpUriInfoWithMap(uriInfo, parameters);
    }

    private void assertFieldsOnFilter(final OrderFilter orderFilter, final Date startDate, final Date endDate,
            final Long customerId, final OrderStatus status) {
        assertThat(orderFilter.getStartDate(), is(equalTo(startDate)));
        assertThat(orderFilter.getEndDate(), is(equalTo(endDate)));
        assertThat(orderFilter.getCustomerId(), is(equalTo(customerId)));
        assertThat(orderFilter.getStatus(), is(equalTo(status)));
    }

    @Test
    public void onlyDefaultValues() {
        setUpUriInfo(null, null, null, null, null, null, null);

        final OrderFilterExtractorFromUrl extractor = new OrderFilterExtractorFromUrl(uriInfo);
        final OrderFilter orderFilter = extractor.getFilter();

        assertActualPaginationDataWithExpected(orderFilter.getPaginationData(),
                new PaginationData(0, 10, "createdAt", OrderMode.DESCENDING));
        assertFieldsOnFilter(orderFilter, null, null, null, null);
    }

    @Test
    public void withPaginationAndStartDateAndEndDateAndCustomerIdAndStatusAndSortAscending() {
        setUpUriInfo("2", "5", "2017-10-04T10:00:00Z", "2017-10-07T10:00:00Z", "10",
                OrderStatus.CANCELLED.name(), "createdAt");

        final OrderFilterExtractorFromUrl extractor = new OrderFilterExtractorFromUrl(uriInfo);
        final OrderFilter orderFilter = extractor.getFilter();

        assertActualPaginationDataWithExpected(orderFilter.getPaginationData(),
                new PaginationData(10, 5, "createdAt", OrderMode.ASCENDING));
        assertFieldsOnFilter(orderFilter, DateUtils.getAsDateTime("2017-10-04T10:00:00Z"),
                DateUtils.getAsDateTime("2017-10-07T10:00:00Z"), 10L, OrderStatus.CANCELLED);
    }

}
