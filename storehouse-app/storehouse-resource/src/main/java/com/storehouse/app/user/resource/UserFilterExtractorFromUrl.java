package com.storehouse.app.user.resource;

import com.storehouse.app.common.model.filter.UserFilter;
import com.storehouse.app.common.resource.AbstractFilterExtractorFromUrl;
import com.storehouse.app.user.model.User.UserType;

import javax.ws.rs.core.UriInfo;

/**
 * User filter extractor.
 *
 * @author ejiafzh
 *
 */
public class UserFilterExtractorFromUrl extends AbstractFilterExtractorFromUrl {

    public UserFilterExtractorFromUrl(final UriInfo uriInfo) {
        super(uriInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDefaultSortField() {
        return "name";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserFilter getFilter() {
        final UserFilter userFilter = new UserFilter();
        userFilter.setPaginationData(extractPaginationData());
        userFilter.setName(getUriInfo().getQueryParameters().getFirst("name"));
        final String userType = getUriInfo().getQueryParameters().getFirst("type");
        if (userType != null) {
            userFilter.setUserType(UserType.valueOf(userType));
        }
        return userFilter;
    }

}
