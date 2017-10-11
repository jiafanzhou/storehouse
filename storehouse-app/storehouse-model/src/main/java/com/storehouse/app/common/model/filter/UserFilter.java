package com.storehouse.app.common.model.filter;

import com.storehouse.app.user.model.User.UserType;

/**
 * User field which includes name and userType.
 *
 * @author ejiafzh
 *
 */
public class UserFilter extends GenericFilter {
    private String name;
    private UserType userType;

    /**
     * Gets the name to be used in this user filter.
     * 
     * @return the name to be used in this user filter.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name to be used in this user filter.
     * 
     * @param name
     *            the name to be used in this user filter.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Get the user type to be used in this user filter.
     * 
     * @return the user type to be used in this user filter.
     */
    public UserType getUserType() {
        return userType;
    }

    /**
     * Set the user type to be used in this user filter.
     * 
     * @param userType
     *            the user type to be used in this user filter.
     */
    public void setUserType(final UserType userType) {
        this.userType = userType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "UserFilter [name=" + name + ", userType=" + userType + "]";
    }

}
