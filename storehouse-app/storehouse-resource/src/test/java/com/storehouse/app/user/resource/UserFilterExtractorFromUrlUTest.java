package com.storehouse.app.user.resource;

import static com.storehouse.app.commontests.utils.FilterExtractorTestUtils.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import com.storehouse.app.common.model.filter.PaginationData;
import com.storehouse.app.common.model.filter.PaginationData.OrderMode;
import com.storehouse.app.common.model.filter.UserFilter;
import com.storehouse.app.user.model.User.UserType;
import com.storehouse.app.user.resource.UserFilterExtractorFromUrl;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UserFilterExtractorFromUrlUTest {
    @Mock
    private UriInfo uriInfo;

    @Before
    public void initTestCase() {
        MockitoAnnotations.initMocks(this);
    }

    private void setUpUriInfo(final String page, final String perPage,
            final String name, final UserType userType, final String sort) {
        final Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("page", page); // current page
        parameters.put("per_page", perPage); // number of records per page
        parameters.put("name", name); // name of the user to filter
        parameters.put("type", userType != null ? userType.name() : null);
        parameters.put("sort", sort); // use +/- signs to define ASC or DESC order
        setUpUriInfoWithMap(uriInfo, parameters);
    }

    private void assertFieldsOnFilter(final UserFilter userFilter, final String name,
            final UserType userType) {
        assertThat(userFilter.getName(), is(equalTo(name)));
        assertThat(userFilter.getUserType(), is(equalTo(userType)));
    }

    @Test
    public void onlyDefaultValues() {
        setUpUriInfo(null, null, null, null, null);

        final UserFilterExtractorFromUrl extractor = new UserFilterExtractorFromUrl(uriInfo);
        final UserFilter userFilter = extractor.getFilter();

        assertActualPaginationDataWithExpected(userFilter.getPaginationData(),
                new PaginationData(0, 10, "name", OrderMode.ASCENDING));
        assertFieldsOnFilter(userFilter, null, null);
    }

    @Test
    public void withPaginationAndNameAndSortAscending() {
        setUpUriInfo("2", "5", "Mary", UserType.CUSTOMER, "id");

        final UserFilterExtractorFromUrl extractor = new UserFilterExtractorFromUrl(uriInfo);
        final UserFilter userFilter = extractor.getFilter();

        assertActualPaginationDataWithExpected(userFilter.getPaginationData(),
                new PaginationData(10, 5, "id", OrderMode.ASCENDING));
        assertFieldsOnFilter(userFilter, "Mary", UserType.CUSTOMER);
    }

    @Test
    public void withPaginationAndNameAndSortAsencingWithPrefixAsending() {
        setUpUriInfo("2", "5", "Robert", UserType.CUSTOMER, "+id");

        final UserFilterExtractorFromUrl extractor = new UserFilterExtractorFromUrl(uriInfo);
        final UserFilter userFilter = extractor.getFilter();

        assertActualPaginationDataWithExpected(userFilter.getPaginationData(),
                new PaginationData(10, 5, "id", OrderMode.ASCENDING));
        assertFieldsOnFilter(userFilter, "Robert", UserType.CUSTOMER);
    }

    @Test
    public void withPaginationAndNameAndSortAsencingWithPrefixDesending() {
        setUpUriInfo("2", "5", "Robert", UserType.CUSTOMER, "-id");

        final UserFilterExtractorFromUrl extractor = new UserFilterExtractorFromUrl(uriInfo);
        final UserFilter userFilter = extractor.getFilter();

        assertActualPaginationDataWithExpected(userFilter.getPaginationData(),
                new PaginationData(10, 5, "id", OrderMode.DESCENDING));
        assertFieldsOnFilter(userFilter, "Robert", UserType.CUSTOMER);
    }
}
