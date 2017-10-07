package com.storehouse.app.commontests.user;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;

import com.storehouse.app.user.model.User;

import org.mockito.ArgumentMatcher;

/**
 * Extends the basic Mockito matcher.
 *
 * @author ejiafzh
 *
 */
public class UserMockitoArgumentMatcher extends ArgumentMatcher<User> {

    private final User expectedUser;

    public static User userEqual(final User expectedUser) {
        return argThat(new UserMockitoArgumentMatcher(expectedUser));
    }

    public UserMockitoArgumentMatcher(final User expectedUser) {
        this.expectedUser = expectedUser;
    }

    @Override
    public boolean matches(final Object argument) {
        final User actualUser = (User) argument;

        assertThat(actualUser.getId(), is(equalTo(expectedUser.getId())));
        assertThat(actualUser.getName(), is(equalTo(expectedUser.getName())));
        assertThat(actualUser.getEmail(), is(equalTo(expectedUser.getEmail())));
        assertThat(actualUser.getPassword(), is(equalTo(expectedUser.getPassword())));
        return true;
    }
}
