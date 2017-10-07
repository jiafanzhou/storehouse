package com.storehouse.app.user.services.impl;

import static com.storehouse.app.commontests.user.UserForTestsRepository.*;
import static com.storehouse.app.commontests.user.UserMockitoArgumentMatcher.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import com.storehouse.app.common.exception.FieldNotValidException;
import com.storehouse.app.common.exception.UserAlreadyExistingException;
import com.storehouse.app.common.exception.UserNotFoundException;
import com.storehouse.app.common.model.PaginatedData;
import com.storehouse.app.common.model.filter.UserFilter;
import com.storehouse.app.common.utils.PasswordUtils;
import com.storehouse.app.user.model.User;
import com.storehouse.app.user.repository.UserRepository;
import com.storehouse.app.user.services.UserServices;

import java.util.Arrays;
import java.util.List;

import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserServicesUTest {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private UserServices userServices;
    private UserRepository userRepository;

    private Validator validator;

    @Before
    public void initTestCase() {
        userServices = new UserServicesImpl();
        userRepository = mock(UserRepository.class);

        validator = Validation.buildDefaultValidatorFactory().getValidator();
        ((UserServicesImpl) userServices).validator = validator;
        ((UserServicesImpl) userServices).userRepository = userRepository;

    }

    private void addUserWithInvalidField(final User user, final String expectedInvalidField) {
        try {
            userServices.add(user);
            fail("An error should have been thrown");
        } catch (final FieldNotValidException fnvex) {
            assertThat(fnvex.getFieldName(), is(equalTo(expectedInvalidField)));
        }
    }

    @Test
    public void addUserWithNullName() {
        final User user = marySimpson();
        user.setName(null);
        addUserWithInvalidField(user, "name");
    }

    @Test
    public void addUserWithShortName() {
        final User user = marySimpson();
        user.setName("aa");
        addUserWithInvalidField(user, "name");
    }

    @Test
    public void addUserWithLongName() {
        final User user = marySimpson();
        user.setName("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        addUserWithInvalidField(user, "name");
    }

    @Test
    public void addUserWithNullEmail() {
        final User user = marySimpson();
        user.setEmail(null);
        addUserWithInvalidField(user, "email");
    }

    @Test
    public void addUsetWithInvalidEmail() {
        final User user = marySimpson();
        user.setEmail("invalid_email");
        addUserWithInvalidField(user, "email");
    }

    @Test
    public void addUserWithNullPassword() {
        final User user = marySimpson();
        user.setPassword(null);
        addUserWithInvalidField(user, "password");
    }

    @Test(expected = UserAlreadyExistingException.class)
    public void addAlreadyExistingUser() {
        when(userRepository.isUserNameAlreadyExists(marySimpson())).thenReturn(true);
        userServices.add(marySimpson());
    }

    @Test
    public void addValidUser() {
        when(userRepository.isUserNameAlreadyExists(marySimpson())).thenReturn(false);
        when(userRepository.add(userEqual(userWithEncryptedPassword(marySimpson()))))
                .thenReturn(userWithIdAndDate(marySimpson(), 1L));
        final User addedUser = userServices.add(marySimpson());
        logger.info("jiafanz: {}", addedUser);
        assertThat(addedUser, is(notNullValue()));
        assertThat(addedUser.getId(), is(equalTo(1L)));
    }

    private void updateUserWithInvalidName(final String name) {
        when(userRepository.add(marySimpson())).thenReturn(userWithIdAndDate(marySimpson(), 1L));
        try {
            final User addedUser = userServices.add(marySimpson());
            addedUser.setName(name);
            userServices.update(addedUser);
            fail("An error should have been thrown");
        } catch (final FieldNotValidException ex) {
            logger.info(ex.toString());
            assertThat(ex.getFieldName(), is(equalTo("name")));
        }
    }

    @Test
    public void updateUserWithNullName() {
        updateUserWithInvalidName(null);
    }

    @Test
    public void updateUserWithTooShortName() {
        updateUserWithInvalidName("a");
    }

    @Test
    public void updateUserWithTooLongName() {
        updateUserWithInvalidName("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
    }

    @Test(expected = UserNotFoundException.class)
    public void updateUserNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);
        userServices.update(userWithIdAndDate(marySimpson(), 1L));
    }

    @Test
    public void updateValidUser() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(userWithIdAndDate(marySimpson(), 1L));
        userServices.update(userWithIdAndDate(marySimpson(), 1L));

        // verify that this update is invoked
        verify(userRepository).update(userEqual(userWithIdAndDate(marySimpson(), 1L)));
    }

    @Test(expected = UserNotFoundException.class)
    public void findUserByIdWithNull() {
        when(userRepository.findById(1L)).thenReturn(null);
        userServices.findById(1L);
    }

    @Test
    public void findUserById() {
        when(userRepository.findById(1L)).thenReturn(userWithIdAndDate(marySimpson(), 1L));
        final User user = userServices.findById(1L);
        assertThat(user, is(notNullValue()));
        assertThat(user.getId(), is(equalTo(1L)));
        assertThat(user.getName(), is(equalTo(marySimpson().getName())));
    }

    @Test
    public void findAllUsers() {
        when(userRepository.findAll("name")).thenReturn(allUsers());
        final List<User> users = userServices.findAll("name");
        logger.info(users.toString());
        assertThat(users.size(), is(equalTo(3)));
    }

    @Test(expected = UserNotFoundException.class)
    public void updatePasswordUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(null);
        userServices.updatePassword(1L, "new_password_1234");
    }

    @Test
    public void updatePassword() {
        final User user = userWithIdAndDate(marySimpson(), 1L);
        when(userRepository.findById(1L)).thenReturn(user);

        userServices.updatePassword(1L, "new_password_1234");

        final User expectedUser = userWithIdAndDate(marySimpson(), 1L);
        expectedUser.setPassword(PasswordUtils.encryptPassword("new_password_1234"));
        verify(userRepository).update(userEqual(expectedUser));
    }

    @Test(expected = UserNotFoundException.class)
    public void findUserByEmailNotFound() {
        when(userRepository.findByEmail(marySimpson().getEmail())).thenReturn(null);
        userServices.findByEmail(marySimpson().getEmail());
    }

    @Test
    public void findUserByEmail() {
        when(userRepository.findByEmail(marySimpson().getEmail())).thenReturn(userWithIdAndDate(marySimpson(), 1L));
        final User user = userServices.findByEmail(marySimpson().getEmail());
        assertThat(user, is(notNullValue()));
        assertThat(user.getName(), is(equalTo(marySimpson().getName())));
    }

    @Test(expected = UserNotFoundException.class)
    public void findUserByEmailAndPasswordNotFound() {
        final User user = marySimpson();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(null);
        userServices.findByEmailAndPassword(user.getEmail(), user.getPassword());
    }

    @Test(expected = UserNotFoundException.class)
    public void findUserByEmailAndPasswordInvalidPassword() {
        final User user = marySimpson();
        user.setPassword("wrong password");

        User returnedUser = userWithIdAndDate(marySimpson(), 1L);
        returnedUser = userWithEncryptedPassword(returnedUser);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(returnedUser);

        userServices.findByEmailAndPassword(user.getEmail(), user.getPassword());
    }

    @Test
    public void findUserByEmailAndPasswordValid() {
        final User user = marySimpson();
        User returnedUser = userWithIdAndDate(marySimpson(), 1L);
        returnedUser = userWithEncryptedPassword(returnedUser);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(returnedUser);

        final User foundUser = userServices.findByEmailAndPassword(user.getEmail(), user.getPassword());
        assertThat(foundUser, is(notNullValue()));
        assertThat(foundUser.getPassword(), is(equalTo(returnedUser.getPassword())));
    }

    @Test
    public void findUserByFilter() {
        final PaginatedData<User> users = new PaginatedData<User>(1, Arrays.asList(marySimpson()));
        when(userRepository.findByFilter((UserFilter) anyObject())).thenReturn(users);

        final PaginatedData<User> usersRetured = userServices.findByFilter(new UserFilter());
        assertThat(usersRetured.getNumberOfRows(), is(equalTo(1)));
        assertThat(usersRetured.getRows().size(), is(equalTo(1)));
        assertThat(usersRetured.getRow(0), is(equalTo(marySimpson())));
    }
}
