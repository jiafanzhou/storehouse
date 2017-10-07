package com.storehouse.app.commontests.user;

import com.storehouse.app.common.utils.DateUtils;
import com.storehouse.app.common.utils.PasswordUtils;
import com.storehouse.app.user.model.Customer;
import com.storehouse.app.user.model.Employee;
import com.storehouse.app.user.model.User;
import com.storehouse.app.user.model.User.Roles;

import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;

@Ignore
public class UserForTestsRepository {
    public static User marySimpson() {
        final User user = new Customer();
        user.setName("Mary Simpson");
        user.setEmail("mary.simpson@domain.com");
        user.setPassword("12345678");
        return user;
    }

    public static User johnDoe() {
        final User user = new Customer();
        user.setName("John Doe");
        user.setEmail("john.doe@domain.com");
        user.setPassword("12345678");
        return user;
    }

    public static User admin() {
        final User user = new Employee();
        user.setName("Admin");
        user.setEmail("admin@domain.com");
        user.setPassword("12345678");
        user.setRoles(Arrays.asList(Roles.EMPLOYEE, Roles.ADMIN));
        return user;
    }

    public static List<User> allUsers() {
        return Arrays.asList(johnDoe(), marySimpson(), admin());
    }

    public static List<User> allUsersWithId() {
        return Arrays.asList(userWithIdAndDate(johnDoe(), 1L),
                userWithIdAndDate(marySimpson(), 2L), userWithIdAndDate(admin(), 3L));
    }

    public static User userWithIdAndDate(final User user, final Long id) {
        user.setId(id);
        user.setCreatedAt(DateUtils.getAsDateTime("2017-09-07T08:00:00Z"));
        return user;
    }

    public static User userWithEncryptedPassword(final User user) {
        user.setPassword(PasswordUtils.encryptPassword(user.getPassword()));
        return user;
    }
}
