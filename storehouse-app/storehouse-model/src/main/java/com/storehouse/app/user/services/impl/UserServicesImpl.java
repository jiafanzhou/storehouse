package com.storehouse.app.user.services.impl;

import com.storehouse.app.common.exception.UserAlreadyExistingException;
import com.storehouse.app.common.exception.UserNotFoundException;
import com.storehouse.app.common.model.PaginatedData;
import com.storehouse.app.common.model.filter.UserFilter;
import com.storehouse.app.common.utils.PasswordUtils;
import com.storehouse.app.common.utils.ValidationUtils;
import com.storehouse.app.user.model.User;
import com.storehouse.app.user.repository.UserRepository;
import com.storehouse.app.user.services.UserServices;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.Validator;

@Stateless
public class UserServicesImpl implements UserServices {

    @Inject
    UserRepository userRepository;

    @Inject
    Validator validator;

    @Override
    public User add(final User user) {
        validateUser(user);

        // encrypt the pass before storing in database.
        user.setPassword(PasswordUtils.encryptPassword(user.getPassword()));

        return userRepository.add(user);
    }

    private void validateUser(final User user) {
        ValidationUtils.validateEntityFields(validator, user);

        if (userRepository.isUserNameAlreadyExists(user)) {
            throw new UserAlreadyExistingException();
        }
    }

    /**
     * Password will not be changed as this update user.
     */
    @Override
    public User update(final User user) {
        validateUser(user);

        if (!userRepository.existsById(user.getId())) {
            throw new UserNotFoundException();
        }

        // restore the password from db in case it is changed.
        final User foundUser = userRepository.findById(user.getId());
        user.setPassword(foundUser.getPassword());

        return userRepository.update(user);
    }

    @Override
    public User findById(final Long id) {
        final User user = userRepository.findById(id);
        if (user == null) {
            throw new UserNotFoundException();
        }
        return user;
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public List<User> findAll(final String orderField) {
        return userRepository.findAll(orderField);
    }

    @Override
    public void updatePassword(final Long id, final String password) {
        final User foundUser = userRepository.findById(id);
        if (foundUser == null) {
            throw new UserNotFoundException();
        }
        foundUser.setPassword(PasswordUtils.encryptPassword(password));
        userRepository.update(foundUser);
    }

    @Override
    public User findByEmail(final String email) {
        final User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UserNotFoundException();
        }
        return user;
    }

    @Override
    public User findByEmailAndPassword(final String email, final String password) {
        final User user = findByEmail(email);
        if (!PasswordUtils.encryptPassword(password).equals(user.getPassword())) {
            throw new UserNotFoundException();
        }
        return user;
    }

    @Override
    public PaginatedData<User> findByFilter(final UserFilter userFilter) {
        return userRepository.findByFilter(userFilter);
    }

}
