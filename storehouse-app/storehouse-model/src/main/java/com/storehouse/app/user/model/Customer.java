package com.storehouse.app.user.model;

import java.util.Arrays;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * This is the customer user account used in this storehouse application.
 *
 * @author ejiafzh
 *
 */
@Entity
@DiscriminatorValue("CUSTOMER")
public class Customer extends User {

    private static final long serialVersionUID = -6100894877953675646L;
    public static final Long PREMIUM_ID_MAX = 1000L;

    /**
     * Default constructor with no args.
     */
    public Customer() {
        setUserType(UserType.CUSTOMER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Roles> getDefaultRoles() {
        return Arrays.asList(Roles.CUSTOMER);
    }

    /**
     * For any client ID smaller than 1000 is considered premium customer.
     *
     * @return whether or not a premium customer.
     */
    public boolean isPremiumCustomer() {
        return getId() < PREMIUM_ID_MAX ? true : false;
    }

}
