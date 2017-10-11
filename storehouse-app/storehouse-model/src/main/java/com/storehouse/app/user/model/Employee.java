package com.storehouse.app.user.model;

import java.util.Arrays;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * This class represents the Employee hired by the storehouse application.
 *
 * Joe is probably the first entity of this class.
 *
 * @author ejiafzh
 *
 */
@Entity
@DiscriminatorValue("EMPLOYEE")
public class Employee extends User {

    private static final long serialVersionUID = 8976498066151628068L;

    /**
     * Default constructor with no args.
     */
    public Employee() {
        setUserType(UserType.EMPLOYEE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Roles> getDefaultRoles() {
        return Arrays.asList(Roles.EMPLOYEE);
    }

}
