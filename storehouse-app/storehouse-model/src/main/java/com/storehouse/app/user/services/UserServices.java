package com.storehouse.app.user.services;

import com.storehouse.app.common.model.PaginatedData;
import com.storehouse.app.common.model.filter.UserFilter;
import com.storehouse.app.user.model.User;

import java.util.List;

import javax.ejb.Local;

/**
 * User service layer API.
 *
 * @author ejiafzh
 *
 */
@Local
public interface UserServices {
    /**
     * To add a new user.
     *
     * @param user
     *            the user to be added
     * @return an added user.
     */
    User add(User user);

    /**
     * To update an existing user.
     *
     * @param user
     *            the user to be updated
     * @return an updated user.
     */
    User update(User user);

    /**
     * Find the user by its id.
     * 
     * @param id
     *            the database id of the user.
     * @return the found user.
     */
    User findById(Long id);

    /**
     * Find the user by its email address.
     * 
     * @param email
     *            user email address
     * @return the found user.
     */
    User findByEmail(String email);

    /**
     * Find all users.
     * 
     * @return all users.
     */
    List<User> findAll();

    /**
     * Find all users sorted based on the orderField.
     * 
     * @param orderField
     *            the order field to sort
     * @return a sorted list of the users.
     */
    List<User> findAll(String orderField);

    /**
     * Update the password of a user.
     * 
     * @param id
     *            the user database id.
     * @param password
     *            the new password to update
     */
    void updatePassword(Long id, String password);

    /**
     * Find the user by its email and password.
     * This method is essentially used to authenticate a user.
     * 
     * @param email
     *            email to be used as username.
     * @param password
     *            password of the user.
     * @return Found user, null if not found or authentication fails.
     */
    User findByEmailAndPassword(String email, String password);

    /**
     * Find the user based on the user filter.
     * 
     * @param userFilter
     *            the user filter to be used.
     * @return a paginated data of user.
     */
    PaginatedData<User> findByFilter(UserFilter userFilter);

}
